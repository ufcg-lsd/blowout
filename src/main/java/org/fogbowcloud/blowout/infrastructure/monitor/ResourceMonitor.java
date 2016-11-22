package org.fogbowcloud.blowout.infrastructure.monitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.blowout.pool.AbstractResource;
import org.fogbowcloud.blowout.pool.BlowoutPool;
import org.fogbowcloud.manager.occi.order.OrderType;

public class ResourceMonitor {

	private static final Logger LOGGER = Logger.getLogger(ResourceMonitor.class);

	private InfrastructureProvider infraProvider;
	private BlowoutPool resourcePool;
	private Map<String, Long> idleResources = new ConcurrentHashMap<String, Long>();
	private List<AbstractResource> pendingResources = new ArrayList<AbstractResource>();

	private Thread monitoringServiceRunner;
	private MonitoringService monitoringService;
	private long infraMonitoringPeriod;
	private Long noExpirationTime = new Long(0);
	private Long idleLifeTime = new Long(0);
	private int maxConnectionTries;
	private int maxReuse;
	
	public ResourceMonitor(InfrastructureProvider infraProvider, BlowoutPool resourcePool, Properties properties) {
		this.infraProvider = infraProvider;
		this.resourcePool = resourcePool;
		infraMonitoringPeriod = Long
				.parseLong(properties.getProperty(AppPropertiesConstants.INFRA_MONITOR_PERIOD, "0"));
		this.idleLifeTime = Long
				.parseLong(properties.getProperty(AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME, "0"));
		this.maxConnectionTries = Integer
				.parseInt(properties.getProperty(AppPropertiesConstants.INFRA_RESOURCE_CONNECTION_RETRY, "1"));
		this.maxReuse = Integer
				.parseInt(properties.getProperty(AppPropertiesConstants.INFRA_RESOURCE_REUSE_TIMES, "1"));
		
	}

	public void start() {
		monitoringService = new MonitoringService();
		monitoringServiceRunner = new Thread(monitoringService);
		monitoringServiceRunner.start();
	}

	public void addPendingResource(AbstractResource resource){
		pendingResources.add(resource);
		if(monitoringService.isPaused()){
			monitoringService.resume();
		}
	}
	
	protected class MonitoringService implements Runnable {

		private boolean paused = false;
		private boolean active = true;

		@Override
		public void run() {

			while (active) {

				try {

					List<AbstractResource> resources = resourcePool.getAllResources();

					checkIsPaused();

					if (resources.isEmpty() && resources.isEmpty()) {
						pause();
						monitoringServiceRunner.join();
					} else {
						monitoringPendingResources();
						monitoringResources(resources);
						Thread.sleep(infraMonitoringPeriod);
					}
				} catch (InterruptedException e) {
					LOGGER.error("Error while executing MonitoringService");
				}
			}

		}
		
		private void monitoringPendingResources() {

			for (AbstractResource resource : getPendingResources()) {
				resource = infraProvider.getResource(resource.getId());
				if (ResourceState.IDLE.equals(resource.getState())) {
					pendingResources.remove(resource);
					resourcePool.putResource(resource, ResourceState.IDLE);
				}
			}
		}
		
		private void monitoringResources(List<AbstractResource> resources) {

			for (AbstractResource resource : resources) {

				if (ResourceState.IDLE.equals(resource.getState())) {
					resolveIdleResource(resource);
				} else if (ResourceState.BUSY.equals(resource.getState())) {
					idleResources.remove(resource);
				} else if (ResourceState.FAILED.equals(resource.getState())) {
					idleResources.remove(resource);
					boolean isAlive = this.checkResourceConnectivity(resource);
					if(isAlive){
						moveResourceToIdle(resource);
					}
				} else if (ResourceState.TO_REMOVE.equals(resource.getState())) {
					try {
						idleResources.remove(resource);
						infraProvider.deleteResource(resource.getId());
						resourcePool.removeResource(resource);
					} catch (Exception e) {
						LOGGER.error("Error while tring to remove resource "+resource.getId()+" - "+e.getMessage());
					}
					
				}

			}
		}

		private void resolveIdleResource(AbstractResource resource) {

			Long since = idleResources.get(resource);

			// If since == null, resource must go to IDLE list.
			if (since == null) {
				moveResourceToIdle(resource);
			} else {

				String requestType = resource.getMetadataValue(AbstractResource.METADATA_REQUEST_TYPE);
				if (OrderType.ONE_TIME.getValue().equals(requestType)) {

					boolean isAlive = checkResourceConnectivity(resource);
					// Has expiration time?
					if (isAlive && noExpirationTime.compareTo(idleLifeTime) != 0) {
						Date expirationDate = new Date(since.longValue());
						Date currentDate = new Date();
						if (expirationDate.before(currentDate)) {
							resourcePool.putResource(resource, ResourceState.TO_REMOVE);
							idleResources.remove(resource);
						}
					}
				}
			}
		}

		private void moveResourceToIdle(AbstractResource resource) {
			if(resource.getReusedTimes() < maxReuse){
				idleResources.put(resource.getId(), Long.valueOf(new Date().getTime()));
				//TODO this should be called here?
				resourcePool.putResource(resource, ResourceState.IDLE);
			}else{
				resourcePool.putResource(resource, ResourceState.TO_REMOVE);
			}
		}

		private boolean checkResourceConnectivity(AbstractResource resource) {
			if (!resource.checkConnectivity()) {
				if(resource.getConnectionFailTries() >= maxConnectionTries){
					resourcePool.putResource(resource, ResourceState.TO_REMOVE);
				}else{
					resourcePool.putResource(resource, ResourceState.FAILED);
				}
				return false;
			}
			return true;
		}

		public void checkIsPaused() throws InterruptedException {
			synchronized (this) {
				while (paused) {
					wait();
				}
			}
		}
		

		public synchronized void stop() {
			active = false;
		}

		public synchronized void pause() {
			paused = true;
		}

		public synchronized void resume() {
			paused = false;
			notify();
		}

		public boolean isPaused() {
			return paused;
		}
	}
	
	public void stop(){
		if(monitoringService.isPaused()){
			monitoringService.resume();
		}
		monitoringService.stop();
	}

	protected MonitoringService getMonitoringService(){
		return monitoringService;
	}
	
	public List<AbstractResource> getPendingResources() {
		return new ArrayList<AbstractResource>(pendingResources);
	}
	
}
