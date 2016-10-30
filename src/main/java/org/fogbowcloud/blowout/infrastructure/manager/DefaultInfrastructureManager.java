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
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.fogbowcloud.blowout.core.util.DateUtils;
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
	
	private ManagerTimer executionTimer = new ManagerTimer(Executors.newScheduledThreadPool(1));
	private InfrastructureProvider infraProvider;
	private Properties properties;
	private DateUtils dateUtils = new DateUtils();

	// Resources control
	private List<AbstractResource> allocatedResources = new ArrayList<AbstractResource>();
	private Map<AbstractResource, Long> idleResources = new ConcurrentHashMap<AbstractResource, Long>();

	// Requisitions control
	private List<ResourceRequest> openRequests = new ArrayList<ResourceRequest>();
	private Map<String, Specification> penddingOrder = new ConcurrentHashMap<String, Specification>();

	private boolean isElastic;
	private int maxResourceReuses;
	// private DataStore ds;
	private List<Specification> initialSpec;

	public DefaultInfrastructureManager(List<Specification> initialSpec, boolean isElastic,
			InfrastructureProvider infraProvider, Properties properties)
			throws InfrastructureException {

		this.properties = properties;
		this.initialSpec = initialSpec;
		this.infraProvider = infraProvider;

		String resourceReuseTimesStr = this.properties.getProperty(AppPropertiesConstants.INFRA_RESOURCE_REUSE_TIMES,
				String.valueOf(MAX_RESOURCE_REUSES));
		this.maxResourceReuses = Integer.parseInt(resourceReuseTimesStr);

		this.validateProperties();

		if (!isElastic && (initialSpec == null || initialSpec.isEmpty())) {
			throw new IllegalArgumentException(
					"No resource may be created with isElastic=" + isElastic + " and initialSpec=" + initialSpec + ".");
		}

		// ds = new DataStore(properties);
		this.isElastic = isElastic;
		// this.resourceComputeId = new String();
	}

	@Override
	public void start(boolean blockWhileInitializing, boolean removePrevious) throws Exception {
		LOGGER.info("Starting Infrastructure Manager");

		if (removePrevious) {
			//TODO get resources from datastore and remove from infra provider
		}

		this.createInitialInfrastructure();
		
		//Starting the periodic management.
		int infraManagementPeriod = Integer.parseInt(properties.getProperty(AppPropertiesConstants.INFRA_MANAGEMENT_SERVICE_TIME));
		executionTimer.scheduleAtFixedRate(new InfrastructureService(), 0, infraManagementPeriod);

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
		executionTimer.cancel();

		for (String orderId : penddingOrder.keySet()) {
			infraProvider.cancelOrder(orderId);
		}
		penddingOrder.clear();

		if (deleteResource) {
			for (AbstractResource resource : this.getAllResources()) {
				infraProvider.deleteResource(resource);
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
		
		ResourceRequest resourceRequest = new ResourceRequest(requestId, resourceNotifier, specification);
		openRequests.add(resourceRequest);

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
				infraProvider.deleteResource(resource);
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
	
	protected class InfrastructureService implements Runnable {
		@Override
		public void run() {

			LOGGER.debug("Executing Infrastructure Manager periodic checks.");

			Map<Specification, Integer> especificationsDemand = new ConcurrentHashMap<Specification, Integer>();

			/* Step 1 - Check if there is any resource available for each open request. */
			resolveOpenRequests(especificationsDemand);

			/* Step 2 - Verify if any pending order is ready and if is, put the
			 * new resource on idle pool and remove this order from pending list.
			 */
			checkPendingOrders(especificationsDemand);

			/* Step 3 - Order new resources on Infrastructure Provider accordingly the demand. */
			orderResourcesByDemand(especificationsDemand);

		}

	}

	private void resolveOpenRequests(Map<Specification, Integer> especificationsDemand) {

		for (ResourceRequest request : getOpenRequests()) {
			AbstractResource resource = this.resourceMatch(request, getIdleResources());
			if (resource != null) {
				// TODO this method should remove resource from idle, put on
				// allocated resources (is needed this other list?)
				// remove the request from the openRequestsList and call the
				// "resourceReady" of the notifier.
				this.relateResourceToRequest(resource, request);
			} else {
				Integer demand = especificationsDemand.get(request.getSpecification());
				if (demand == null) {
					demand = new Integer(0);
				}
				demand = new Integer(demand.intValue() + 1);
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

	private void checkPendingOrders(Map<Specification, Integer> especificationsDemand) {

		for (Entry<String, Specification> entry : getEntriesFromMap(penddingOrder)) {

			String orderId = entry.getKey();
			Specification spec = entry.getValue();

			/*
			 * Each request for a type of spec must reduce by one the demand for
			 * this spec. For example, if we have a demand of 2 resources for an
			 * spec A, and we have 1 pendding order for spec A, we dont need to
			 * order 2 new resources for spec A, we need to ask only one more.
			 */
			Integer demand = especificationsDemand.get(spec);
			if (demand != null) {
				demand = new Integer(demand.intValue() - 1);
			}

			AbstractResource newResource = infraProvider.getResource(orderId);
			if (newResource != null) {
				LOGGER.info("New resource " + newResource.getId() + " is being put into Idle Pool");
				moveResourceToIdle(newResource);
				penddingOrder.remove(orderId);
				// updateInfrastuctureState();
			}

		}
	}

	private void orderResourcesByDemand(Map<Specification, Integer> especificationsDemand) {

		for (Entry<Specification, Integer> entry : getEntriesFromMap(especificationsDemand)) {

			Specification spec = entry.getKey();
			Integer qty = entry.getValue();
			if (qty > 0) {
				for (int count = 0; count < qty; count++) {
					String orderId;
					try {
						orderId = infraProvider.requestResource(spec);
						penddingOrder.put(orderId, spec);
					} catch (RequestResourceException e) {
						LOGGER.error("Erro while ordering resource for spec: " + spec, e);
					}
				}
			}

		}
	}

	protected boolean relateResourceToRequest(AbstractResource resource, ResourceRequest request) {

		try {

			LOGGER.debug("Resource " + resource.getId() + " related to Request " + request.getRequestId()
					+ " with specs:" + request.getSpecification().toString());

			// Moving resource from idle pool to allocated list.
			idleResources.remove(resource);
			allocatedResources.add(resource);
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