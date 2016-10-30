package org.fogbowcloud.blowout.infrastructure.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.fogbowcloud.blowout.core.util.DateUtils;
import org.fogbowcloud.blowout.infrastructure.exception.RequestResourceException;
import org.fogbowcloud.blowout.infrastructure.model.AbstractResource;
import org.fogbowcloud.blowout.infrastructure.model.Request;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.manager.occi.order.OrderType;

public class DefaultInfrastructureManager implements InfrastructureManager {

	private static final Logger LOGGER = Logger.getLogger(DefaultInfrastructureManager.class);
	private final int MAX_RESOURCE_REUSES = Integer.MAX_VALUE;
	// TODO get from properties????
	private final int MAX_CONNECTION_RETRIES = 5;
	private final Long NO_EXPIRATION_DATE = new Long(0);

	private InfrastructureProvider infraProvider;
	private Properties properties;
	private DateUtils dateUtils = new DateUtils();
	
	// Resources control
	private List<AbstractResource> allocatedResources = new ArrayList<AbstractResource>();
	private Map<AbstractResource, Long> idleResources = new ConcurrentHashMap<AbstractResource, Long>();

	// Requisitions control
	private List<Request> openRequests = new ArrayList<Request>();
	private Map<String, Specification> penddingOrder = new ConcurrentHashMap<String, Specification>();

	@Override
	public void start(boolean blockWhileInitializing, boolean removePrevious) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop(boolean deleteResource) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void request(Specification specification, ResourceNotifier resourceNotifier, int resourceNumber) {
		// TODO Auto-generated method stub

	}

	@Override
	public void release(AbstractResource resource) {
		// TODO Auto-generated method stub

	}

	@Override
	public void releaseAll() {
		// TODO Auto-generated method stub

	}

	protected class InfrastructureService implements Runnable {
		@Override
		public void run() {

			LOGGER.debug("Executing Infrastructure Manager periodic checks.");

			Map<Specification, Integer> especificationsDemand = new ConcurrentHashMap<Specification, Integer>();

			/*
			 * Step 1 - Check if there ir any resource available for each open
			 * request.
			 **/
			resolveOpenRequests(especificationsDemand);

			/*
			 * Step 2 - Verify if any pending order is ready and if is, put the
			 * new resource on idle pool and remove this order from pending
			 * list.
			 */
			checkPendingOrders(especificationsDemand);

			/*
			 * Step 3 - Order new resources on Infrastructure Provider
			 * accordingly the demand.
			 */
			orderResourcesByDemand(especificationsDemand);

		}

	}

	private void resolveOpenRequests(Map<Specification, Integer> especificationsDemand) {

		for (Request request : getOpenRequests()) {
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

	private AbstractResource resourceMatch(Request request, Map<AbstractResource, Long> idleResources2) {
		// TODO Auto-generated method stub
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

	protected boolean relateResourceToRequest(AbstractResource resource, Request request) {

		try {

			LOGGER.debug("Resource " + resource.getId() + " related to Request " + request.getRequestId()
					+ " with specs:" + request.getSpecification().toString());

			// Moving resource from idle pool to allocated list.
			idleResources.remove(resource);
			allocatedResources.add(resource);
			request.getResourceNotifier().resourceReady(resource);

			return true;

		} catch (Exception e) {
			
			LOGGER.error("An error occurred while relating resource " + resource.getId() + " to Request " + request.getRequestId()
			+ " with specs:" + request.getSpecification().toString());
			
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
		//updateInfrastuctureState();
		LOGGER.debug("Resource [" + resource.getId() + "] moved to Idle - Expiration Date: ["
				+ DateUtils.getStringDateFromMiliFormat(expirationDate, DateUtils.DATE_FORMAT_YYYY_MM_DD_HOUR) + "]");

	}

	protected List<Request> getOpenRequests() {
		return Collections.synchronizedList(openRequests);
	}

	protected List<AbstractResource> getAllocatedResources() {
		return Collections.synchronizedList(allocatedResources);
	}

	protected Map<AbstractResource, Long> getIdleResources() {
		return idleResources;
	}

	protected <T, E> Set<Entry<T, E>> getEntriesFromMap(Map<T, E> map) {
		return new HashSet<Entry<T, E>>(map.entrySet());
	}
}
