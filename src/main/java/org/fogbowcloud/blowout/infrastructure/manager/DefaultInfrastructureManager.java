package org.fogbowcloud.blowout.infrastructure.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.fogbowcloud.blowout.core.util.DateUtils;
import org.fogbowcloud.blowout.database.ResourceIdDatastore;
import org.fogbowcloud.blowout.infrastructure.exception.InfrastructureException;
import org.fogbowcloud.blowout.infrastructure.exception.RequestResourceException;
import org.fogbowcloud.blowout.infrastructure.model.AbstractResource;
import org.fogbowcloud.blowout.infrastructure.model.ResourceRequest;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.blowout.infrastructure.provider.fogbow.FogbowRequirementsHelper;
import org.fogbowcloud.manager.occi.order.OrderType;

public class DefaultInfrastructureManager implements InfrastructureManager {

	private static final Logger LOGGER = Logger.getLogger(DefaultInfrastructureManager.class);
	private final int MAX_RESOURCE_REUSES = Integer.MAX_VALUE;
	// TODO get from properties????
	private final int MAX_CONNECTION_RETRIES = 5;
	private final Long NO_EXPIRATION_DATE = new Long(0);
	
	private Thread infrastructureServiceRunner;
	private InfrastructureService infrastructureService;
	private InfrastructureProvider infraProvider;
	private Properties properties;
	private DateUtils dateUtils = new DateUtils();

	// Resources control
	private List<AbstractResource> allocatedResources = new ArrayList<AbstractResource>();
	private Map<AbstractResource, Long> idleResources = new ConcurrentHashMap<AbstractResource, Long>();
	//This map holds the Resource ID and the specification used to request this one.
	private Map<String, Specification> pendingResources = new ConcurrentHashMap<String, Specification>();

	// Requisitions control
	private List<ResourceRequest> openRequests = new ArrayList<ResourceRequest>();

	private boolean isElastic;
	private int maxResourceReuses;
	private ResourceIdDatastore ds;
	private List<Specification> initialSpec;
	
	protected DefaultInfrastructureManager(List<Specification> initialSpec, boolean isElastic,
			InfrastructureProvider infraProvider, Properties properties, ResourceIdDatastore ds) throws InfrastructureException{
		this(initialSpec, isElastic, infraProvider, properties);
		
		this.ds = ds;
		
	}

	public DefaultInfrastructureManager(List<Specification> initialSpec, boolean isElastic,
			InfrastructureProvider infraProvider, Properties properties)
			throws InfrastructureException {

		this.properties = properties;
		this.initialSpec = initialSpec;
		this.infraProvider = infraProvider;
		this.isElastic = true;

		String resourceReuseTimesStr = this.properties.getProperty(AppPropertiesConstants.INFRA_RESOURCE_REUSE_TIMES,
				String.valueOf(MAX_RESOURCE_REUSES));
		this.maxResourceReuses = Integer.parseInt(resourceReuseTimesStr);

		this.validateProperties();

		if (!this.isElastic && (initialSpec == null || initialSpec.isEmpty())) {
			throw new IllegalArgumentException(
					"No resource may be created with isElastic=" + this.isElastic + " and initialSpec=" + initialSpec + ".");
		}

		ds = new ResourceIdDatastore(properties);
	}

	@Override
	public void start(boolean blockWhileInitializing, boolean removePrevious) throws Exception {
		LOGGER.info("Starting Infrastructure Manager");

		if (removePrevious) {
			for(String resourceId : ds.getResourceIds()){
				try{
					infraProvider.deleteResource(resourceId);
				}catch(Exception e){
					LOGGER.warn("Was not possible to delete resource "+resourceId+" due: "+e.getMessage());
				}
			}
			ds.deleteAll();
		}

		this.createInitialInfrastructure();
		
		//Starting the periodic management.
		infrastructureService = new InfrastructureService();
		infrastructureServiceRunner = new Thread(infrastructureService);
		infrastructureServiceRunner.start();
		
		LOGGER.info("Block while waiting initial resources? " + blockWhileInitializing);
		if (blockWhileInitializing && initialSpec != null) {
			while (idleResources.size() != initialSpec.size()) {
				Thread.sleep(2000);
			}
		}
		LOGGER.info("Infrastructure manager started");

	}

	@Override
	public void stop(boolean deleteResource) throws Exception {
		LOGGER.info("Stoping Infrastructure Manager");

		//Stopping the periodic management.
		infrastructureService.terminate();
		infrastructureServiceRunner.join();

		for (String resourceId : pendingResources.keySet()) {
			infraProvider.deleteResource(resourceId);
		}
		pendingResources.clear();

		if (deleteResource) {
			for (AbstractResource resource : this.getAllResources()) {
				infraProvider.deleteResource(resource.getId());
				ds.deleteResourceId(resource.getId());
			}
			allocatedResources.clear();
			idleResources.clear();
		}
		
		// ds.dispose();
		LOGGER.info("Stoping Infrastructure Manager finished");
	}

	@Override
	public void request(Specification specification, ResourceNotifier resourceNotifier, int resourceNumber) {
			
		String requestId = UUID.randomUUID().toString();
		for(int count = 0; count < resourceNumber; count++){
			ResourceRequest resourceRequest = new ResourceRequest(requestId, resourceNotifier, specification);
			openRequests.add(resourceRequest);
		}

	}

	@Override
	public void release(AbstractResource resource) {
		
		resource.incrementReuse();
		LOGGER.debug("Releasing Resource [" + resource.getId() + "]");
		allocatedResources.remove(resource);
		
		resource.checkConnectivity();
		boolean excededMaxRetries = resource.getConnectionFailTries() < MAX_CONNECTION_RETRIES ? false : true;

		if (resource.getReusedTimes() < maxResourceReuses && !excededMaxRetries) {
			moveResourceToIdle(resource);
		} else {
			try {
				infraProvider.deleteResource(resource.getId());
				ds.deleteResourceId(resource.getId());
			} catch (Exception e) {
				LOGGER.error("Error when disposing of resource for excessive reuse", e);
			}
		}

	}

	// -------- INFRASTRUCTURE SERVICE METHODS --------------//
	
	private void createInitialInfrastructure() {
		if (initialSpec != null) {
			LOGGER.info("Creating orders to initial specs \n" + initialSpec);

			for (Specification spec : initialSpec) {
				// Must initial spec be Persistent ?
				spec.addRequirement(FogbowRequirementsHelper.METADATA_FOGBOW_REQUEST_TYPE,
						OrderType.PERSISTENT.getValue());
				this.request(spec, null, 1);
			}
		}
	}
	
	private class InfrastructureService implements Runnable {
		
		private boolean stop = false;
		
		@Override
		public void run() {
			
			int infraManagementPeriod = Integer.parseInt(properties.getProperty(AppPropertiesConstants.INFRA_MANAGEMENT_SERVICE_TIME));
			
			while(!stop){

				LOGGER.debug("Executing Infrastructure Manager periodic checks.");

				Map<Specification, Integer> especificationsDemand = new ConcurrentHashMap<Specification, Integer>();
				
				checkInfrastructureIntegrity();
				
				/* Step 1 - Verify if any pending order is ready and if is, put the
				 * new resource on idle pool and remove this order from pending list.
				 */
				checkPendingResources(especificationsDemand);

				/* Step 2 - Check if there is any resource available for each open request. */
				resolveOpenRequests(especificationsDemand);

				/* Step 3 - Order new resources on Infrastructure Provider accordingly the demand. */
				requestResourcesByDemand(especificationsDemand);

				try {
					Thread.sleep(infraManagementPeriod);
				} catch (InterruptedException e) {
					LOGGER.error("Error on execution of InfrastructureService");
				}

			}
		}
		
		public void terminate(){
			stop = true;
		}
		

	}

	private void checkInfrastructureIntegrity(){
		
		List<AbstractResource> resourcesToRemove = new ArrayList<AbstractResource>();

		for (Entry<AbstractResource, Long> entry : idleResources.entrySet()) {
			if (entry != null) {
				AbstractResource r = entry.getKey();
				String requestType = r.getMetadataValue(AbstractResource.METADATA_REQUEST_TYPE);
				// Persistent resource can not be removed.
				if (OrderType.ONE_TIME.getValue().equals(requestType)) {

					if (isElastic && NO_EXPIRATION_DATE.compareTo(entry.getValue()) != 0) {
						Date expirationDate = new Date(entry.getValue().longValue());
						Date currentDate = new Date(dateUtils.currentTimeMillis());

						if (expirationDate.before(currentDate)) {
							resourcesToRemove.add(r);
							LOGGER.info("Resource: [" + r.getId() + "] to be disposed due lifetime's expiration");
							continue;
						}
					}
				} 
			}
		}

		for (AbstractResource resource : resourcesToRemove) {
			if (resource != null) {
				try {
					infraProvider.deleteResource(resource.getId());
					idleResources.remove(resource);
				} catch (Exception e) {
					LOGGER.error("Error while disposing resource: [" + resource.getId() + "]", e);
				}
			}
		}
		
	}
	
	private void resolveOpenRequests(Map<Specification, Integer> especificationsDemand) {

		for (ResourceRequest request : getOpenRequests()) {
			AbstractResource resource = this.resourceMatch(request, getIdleResources());
			if (resource != null) {
				this.relateResourceToRequest(resource, request);
			} else {
				Integer demand = especificationsDemand.get(request.getSpecification());
				if (demand == null) {
					demand = new Integer(0);
				}
				demand = new Integer(demand.intValue() + 1);
				especificationsDemand.put(request.getSpecification(), demand);
			}
		}
	}

	private AbstractResource resourceMatch(ResourceRequest request, Map<AbstractResource, Long> idleResources) {
		for (AbstractResource abstractResource : idleResources.keySet()) {
			abstractResource.match(request.getSpecification());
			return abstractResource;
		}
		return null;
	}

	private void checkPendingResources(Map<Specification, Integer> especificationsDemand) {

		for (Entry<String, Specification> entry : getEntriesFromMap(pendingResources)) {

			String resourceId = entry.getKey();
			Specification spec = entry.getValue();

			/*
			 * Each request for a type of spec must reduce by one the demand for
			 * this spec. For example, if we have a demand of 2 resources for an
			 * spec A, and we have 1 pendding order for spec A, we dont need to
			 * order 2 new resources for spec A, we need to ask only one more.
			 */
			Integer demand = especificationsDemand.get(spec);
			if (demand == null) {
				demand = new Integer(0);
			}
			demand = new Integer(demand.intValue() - 1);
			especificationsDemand.put(spec, demand);

			AbstractResource newResource = infraProvider.getResource(resourceId);
			if (newResource != null) {
				LOGGER.info("New resource " + newResource.getId() + " is being put into Idle Pool");
				moveResourceToIdle(newResource);
				pendingResources.remove(resourceId);
				// updateInfrastuctureState();
			}

		}
	}

	private void requestResourcesByDemand(Map<Specification, Integer> especificationsDemand) {

		List<String> requestedResourceIds = new ArrayList<String>();
		
		for (Entry<Specification, Integer> entry : getEntriesFromMap(especificationsDemand)) {

			Specification spec = entry.getKey();
			Integer qty = entry.getValue();
			if (qty > 0) {
				for (int count = 0; count < qty; count++) {
					String resourceId;
					try {
						resourceId = infraProvider.requestResource(spec);
						pendingResources.put(resourceId, spec);
						requestedResourceIds.add(resourceId);
					} catch (RequestResourceException e) {
						LOGGER.error("Erro while ordering resource for spec: " + spec, e);
					}
				}
			}
		}
		
		ds.addResourceIds(requestedResourceIds);
	}

	protected boolean relateResourceToRequest(AbstractResource resource, ResourceRequest request) {

		try {

			LOGGER.debug("Resource " + resource.getId() + " related to Request " + request.getRequestId()
					+ " with specs:" + request.getSpecification().toString());

			// Moving resource from idle pool to allocated list.
			idleResources.remove(resource);
			allocatedResources.add(resource);
			openRequests.remove(request);
			request.getResourceNotifier().resourceReady(resource);

			return true;

		} catch (Exception e) {

			LOGGER.error("An error occurred while relating resource " + resource.getId() + " to Request "
					+ request.getRequestId() + " with specs:" + request.getSpecification().toString());

			moveResourceToIdle(resource);
			allocatedResources.remove(resource);

			return false;
		}

	}

	protected void moveResourceToIdle(AbstractResource resource) {

		LOGGER.debug("Moving resource " + resource.getId() + " to idle");
		Long expirationDate = NO_EXPIRATION_DATE;

		if (OrderType.ONE_TIME.getValue().equals(resource.getMetadataValue(AbstractResource.METADATA_REQUEST_TYPE))) {
			int idleLifeTime = Integer
					.parseInt(properties.getProperty(AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME));

			expirationDate = Long.valueOf(+idleLifeTime);
			Calendar c = Calendar.getInstance();
			c.setTime(new Date(dateUtils.currentTimeMillis()));
			c.add(Calendar.MILLISECOND, idleLifeTime);
			expirationDate = c.getTimeInMillis();
		}
		idleResources.put(resource, expirationDate);
		// updateInfrastuctureState();
		LOGGER.debug("Resource [" + resource.getId() + "] moved to Idle - Expiration Date: ["
				+ DateUtils.getStringDateFromMiliFormat(expirationDate, DateUtils.DATE_FORMAT_YYYY_MM_DD_HOUR) + "]");

	}
	
	protected List<ResourceRequest> getOpenRequests() {
		return new ArrayList<ResourceRequest>(openRequests);
	}

	protected List<AbstractResource> getAllocatedResources() {
		return new ArrayList<AbstractResource>(allocatedResources);
	}

	protected Map<AbstractResource, Long> getIdleResources() {
		return idleResources;
	}

	protected <T, E> Set<Entry<T, E>> getEntriesFromMap(Map<T, E> map) {
		return new HashSet<Entry<T, E>>(map.entrySet());
	}
	
	protected List<AbstractResource> getAllResources(){
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		resources.addAll(allocatedResources);
		resources.addAll(idleResources.keySet());
		return resources;
	}
	
	protected InfrastructureService getInfrastructureService(){
		return infrastructureService;
	}
	
	private void validateProperties() throws InfrastructureException {

		try {
			Integer.parseInt(properties.getProperty(AppPropertiesConstants.INFRA_RESOURCE_CONNECTION_TIMEOUT));
		} catch (Exception e) {
			LOGGER.debug("App Properties are not correctly configured: ["
					+ AppPropertiesConstants.INFRA_RESOURCE_CONNECTION_TIMEOUT + "]", e);
			throw new InfrastructureException("App Properties are not correctly configured: ["
					+ AppPropertiesConstants.INFRA_RESOURCE_CONNECTION_TIMEOUT + "]", e);
		}

		try {
			Integer.parseInt(properties.getProperty(AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME));
		} catch (Exception e) {
			LOGGER.debug("App Properties are not correctly configured: ["
					+ AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME + "]", e);
			throw new InfrastructureException("App Properties are not correctly configured: ["
					+ AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME + "]", e);
		}

		try {
			Integer.parseInt(properties.getProperty(AppPropertiesConstants.INFRA_MANAGEMENT_SERVICE_TIME));
		} catch (Exception e) {
			LOGGER.debug("App Properties are not correctly configured: ["
					+ AppPropertiesConstants.INFRA_MANAGEMENT_SERVICE_TIME + "]", e);
			throw new InfrastructureException("App Properties are not correctly configured: ["
					+ AppPropertiesConstants.INFRA_MANAGEMENT_SERVICE_TIME + "]", e);
		}
		try {
			Integer.parseInt(properties.getProperty(AppPropertiesConstants.INFRA_RESOURCE_SERVICE_TIME));
		} catch (Exception e) {
			LOGGER.debug("App Properties are not correctly configured: ["
					+ AppPropertiesConstants.INFRA_RESOURCE_SERVICE_TIME + "]", e);
			throw new InfrastructureException("App Properties are not correctly configured: ["
					+ AppPropertiesConstants.INFRA_RESOURCE_SERVICE_TIME + "]", e);
		}

	}
}