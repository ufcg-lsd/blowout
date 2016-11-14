package org.fogbowcloud.blowout.infrastructure.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.fogbowcloud.blowout.infrastructure.manager.ResourceNotifier;
import org.fogbowcloud.blowout.infrastructure.model.AbstractResource;
import org.fogbowcloud.blowout.infrastructure.model.AbstractResource.ResourceStatus;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;

public class ResourceMonitor {

	private static final Logger LOGGER = Logger.getLogger(ResourceMonitor.class);

	private InfrastructureProvider infraProvider;
	private Map<AbstractResource, ResourceNotifier> monitoredResources = new ConcurrentHashMap<AbstractResource, ResourceNotifier>();

	private Thread monitoringServiceRunner;
	private MonitoringService monitoringService;
	private long infraMonitoringPeriod;

	public ResourceMonitor(InfrastructureProvider infraProvider, Properties properties) {
		this.infraProvider = infraProvider;
		infraMonitoringPeriod = Long
				.parseLong(properties.getProperty(AppPropertiesConstants.INFRA_MONITOR_PERIOD, "0"));

	}

	public void monitoreResource(AbstractResource resource, ResourceNotifier notifier) {
		monitoredResources.put(resource, notifier);
		if (!monitoringService.isPaused()) {
			monitoringService.resume();
		}
	}

	private class MonitoringService implements Runnable {

		private boolean paused = false;

		@Override
		public void run() {

			while (true) {

				try {

					checkIsPaused();

					if (monitoredResources.isEmpty()) {
						pause();
						monitoringServiceRunner.join();
					} else {
						for (Entry<AbstractResource, ResourceNotifier> entry : getMonitoredResources().entrySet()) {

							AbstractResource resource = entry.getKey();
							ResourceNotifier resourceNotifier = entry.getValue();

							resource = infraProvider.getResource(resource.getId());
							if (ResourceStatus.READY.equals(resource.getState())) {
								resourceNotifier.resourceReady(resource);
								monitoredResources.remove(resource);
							}
						}
						Thread.sleep(infraMonitoringPeriod);
					}
				} catch (InterruptedException e) {
					LOGGER.error("Error while executing MonitoringService");
				}
			}

		}
		
		public Map<AbstractResource, ResourceNotifier> getMonitoredResources() {
			return new ConcurrentHashMap<AbstractResource, ResourceNotifier>(monitoredResources);
		}

		public void checkIsPaused() throws InterruptedException {
			synchronized (this) {
				while (paused) {
					wait();
				}
			}
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
	
	
	public List<AbstractResource> getPendingResources(){
		return new ArrayList<AbstractResource>(monitoredResources.keySet());
	}
}
