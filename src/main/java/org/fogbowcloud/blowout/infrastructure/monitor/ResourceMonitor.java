package org.fogbowcloud.blowout.infrastructure.monitor;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.constants.AppPropertiesConstants;
import org.fogbowcloud.blowout.core.model.resource.ResourceState;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;
import org.fogbowcloud.blowout.pool.BlowoutPool;

public class ResourceMonitor {

	private static final Logger LOGGER = Logger.getLogger(ResourceMonitor.class);

	private InfrastructureProvider infraProvider;
	private BlowoutPool blowoutPool;
	private Map<String, Long> idleResources;
	private Map<String, Specification> pendingResources;

	private Thread monitoringServiceRunner;
	private MonitoringService monitoringService;
	private long sleepPeriod;
	private Long idleLifeTime;
	private int maxConnectionTries;
	private int maxReuse;
	
	public ResourceMonitor(InfrastructureProvider infraProvider, BlowoutPool blowoutPool, Properties properties) {
		this.idleResources = new ConcurrentHashMap<>();
		this.pendingResources = new ConcurrentHashMap<>();
		this.infraProvider = infraProvider;
		this.blowoutPool = blowoutPool;

		final String defaultInfraMonitorPeriod = "30000";
		final String defaultIdleLifeTime = "120000";
		final String defaultMaxConnectTries = "1";
		final String defaultMaxReuse = "1";

		this.sleepPeriod = Long.parseLong(properties.getProperty(
				AppPropertiesConstants.RESOURCE_MONITOR_SLEEP_PERIOD, defaultInfraMonitorPeriod));
		this.idleLifeTime = Long.parseLong(properties.getProperty(
				AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME, defaultIdleLifeTime));
		this.maxConnectionTries = Integer.parseInt(properties.getProperty(
				AppPropertiesConstants.INFRA_RESOURCE_CONNECTION_RETRY, defaultMaxConnectTries));
		this.maxReuse = Integer.parseInt(properties
				.getProperty(AppPropertiesConstants.INFRA_RESOURCE_REUSE_TIMES, defaultMaxReuse));

		this.monitoringService = new MonitoringService();
		this.monitoringServiceRunner = new Thread(this.monitoringService, this.monitoringService.toString());
		List<AbstractResource> previousResources = infraProvider.getAllResources();
		if (previousResources != null && !previousResources.isEmpty()) {
			this.blowoutPool.addResourceList(previousResources);
		}
	}

	public void start() {
		monitoringServiceRunner.start();
		LOGGER.warn("Resource Monitor started.");
	}

	public void addPendingResource(String resourceId, Specification spec){
		pendingResources.put(resourceId, spec);
		if(monitoringService.isPaused()){
			monitoringService.resume();
		}
	}

	protected class MonitoringService implements Runnable {

		private boolean isPaused;
		private boolean isActive;

		public MonitoringService() {
			this.isPaused = false;
			this.isActive = true;
		}

		@Override
		public void run() {
			while (isActive) {
				try {
					LOGGER.info("Resource monitor is waiting.");
					Thread.sleep(sleepPeriod);
					monitorProcess();
				} catch (InterruptedException e) {
					LOGGER.error("Error while executing MonitoringService.");
				}
			}
		}

		protected void monitorProcess() throws InterruptedException {

			List<AbstractResource> resources = blowoutPool.getAllResources();
			monitoringPendingResources();
			monitoringResources(resources);
		}

		private void monitoringPendingResources() {
			LOGGER.info("Monitoring pending resources.");

			for (String resourceId : pendingResources.keySet()) {
				AbstractResource resource = infraProvider.getResource(resourceId);
				if (resource != null) {
					LOGGER.info("Monitoring resource with id " + resource.getId() + " and state " + resource.getState() + ".");
					pendingResources.remove(resourceId);
					blowoutPool.addResource(resource);
				}
			}
		}

		private void monitoringResources(List<AbstractResource> resources) {

			LOGGER.info("Monitoring resources.");

			for (AbstractResource resource : resources) {

                LOGGER.info("Monitoring resource with id " + resource.getId() + " and state " + resource.getState() + ".");

				if (ResourceState.BUSY.equals(resource.getState())) {
					idleResources.remove(resource.getId());
				} else if (ResourceState.FAILED.equals(resource.getState())) {
					idleResources.remove(resource.getId());
					boolean isAlive = this.checkResourceConnectivity(resource);
					if(isAlive){
						if(moveResourceToIdle(resource)){
							blowoutPool.updateResource(resource, ResourceState.IDLE);
						}
					}
				} else if (ResourceState.TO_REMOVE.equals(resource.getState())) {
					try {
						idleResources.remove(resource.getId());
						infraProvider.deleteResource(resource.getId());
						blowoutPool.removeResource(resource);
					} catch (Exception e) {
						LOGGER.error("Error while tring to remove resource "+resource.getId()+" - "+e.getMessage() + ".");
					}
				}
			}
		}

		private boolean moveResourceToIdle(AbstractResource resource) {
			// TODO: Check the following options for maxReuse problem
			//       1. See if it's viable to only mark resource as TO_REMOVE
			//          if there's no task processes READY or RUNNING
			//       2. Make maxReuse indefinite by default and not one
			//       3. Always reuse instance
			if (resource.getReusedTimes() < maxReuse) {
				Calendar c = Calendar.getInstance();
				c.setTime(new Date());
				c.add(Calendar.MILLISECOND, idleLifeTime.intValue());
				Long expirationDate = c.getTimeInMillis();
				idleResources.put(resource.getId(), expirationDate);
				return true;
			} else {
				blowoutPool.updateResource(resource, ResourceState.TO_REMOVE);
				return false;
			}
		}

		private boolean checkResourceConnectivity(AbstractResource resource) {
			if (!resource.checkConnectivity()) {
				if(resource.getConnectionFailTries() >= maxConnectionTries){
					blowoutPool.updateResource(resource, ResourceState.TO_REMOVE);
				}else{
					blowoutPool.updateResource(resource, ResourceState.FAILED);
				}
				return false;
			}
			return true;
		}

		public void checkIsPaused() throws InterruptedException {
			synchronized (this) {
				while (isPaused) {
					wait();
				}
			}
		}

		public synchronized void stop() {
			if(isPaused){ resume(); }
			this.isActive = false;
		}

		public synchronized void pause() {
			isPaused = true;
		}

		public synchronized void resume() {
			this.isPaused = false;
			notify();
		}

		public boolean isPaused() {
			return isPaused;
		}
	}

	public void stop(){
		if(monitoringService.isPaused()){
			monitoringService.resume();
		}
		monitoringService.stop();
	}

	protected void setMonitoringService(MonitoringService monitoringService){
		this.monitoringService = monitoringService;
	}

	protected MonitoringService getMonitoringService(){
		return monitoringService;
	}

	public List<String> getPendingResources() {
		return new ArrayList<>(pendingResources.keySet());
	}

	public List<Specification> getPendingSpecification() {
		return new ArrayList<>(pendingResources.values());
	}

	public Map<Specification, Integer> getPendingRequests() {
		Map<Specification, Integer> specCount = new HashMap<>();
		for (Entry<String, Specification> e : this.pendingResources.entrySet()) {
			if (specCount.containsKey(e.getValue())) {
				specCount.put(e.getValue(), specCount.get(e.getValue()) +1);
			} else {
				specCount.put(e.getValue(), 1);
			}
		}
		return specCount;
	}
}