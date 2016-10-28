package org.fogbowcloud.blowout.infrastructure.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.infrastructure.model.AbstractResource;
import org.fogbowcloud.blowout.infrastructure.model.Requisition;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;

public class DefaultInfrastructureManager implements InfrastructureManager {

	private static final Logger LOGGER = Logger.getLogger(DefaultInfrastructureManager.class);
	private final int MAX_RESOURCE_REUSES = Integer.MAX_VALUE;
	// TODO get from properties????
	private final int MAX_CONNECTION_RETRIES = 5;
	private final long NO_EXPIRATION_DATE = 0;

	private InfrastructureProvider infraProvider;
	//Resources control
	private Map<AbstractResource, Requisition> allocatedResources = new ConcurrentHashMap<AbstractResource, Requisition>();
	private Map<AbstractResource, Long> idleResources = new ConcurrentHashMap<AbstractResource, Long>();
	
	//Requisitions control
	private ConcurrentLinkedQueue<Requisition> openRequisitions = new ConcurrentLinkedQueue<Requisition>();
	private Map<Requisition, String> waitingProvidingOrders = new ConcurrentHashMap<Requisition, String>();

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
			
		}
	}

	
	
	

	
}
