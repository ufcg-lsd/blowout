package org.fogbowcloud.blowout.infrastructure.monitor;

import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.blowout.pool.AbstractResource;
import org.fogbowcloud.blowout.pool.BlowoutPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TestResourceMonitor {

	private ResourceMonitor resourceMonitor;
	private InfrastructureProvider infraProvider;
	private BlowoutPool resourcePool;
	
	@Before
	public void setUp() throws Exception {
		
		infraProvider = Mockito.mock(InfrastructureProvider.class);
		resourcePool = Mockito.mock(BlowoutPool.class);
		Properties properties = new Properties();
		properties.setProperty(AppPropertiesConstants.INFRA_MONITOR_PERIOD, "120000");
		properties.setProperty(AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME, "120000");
		properties.setProperty(AppPropertiesConstants.INFRA_RESOURCE_CONNECTION_RETRY, "3");
		
		resourceMonitor = Mockito.spy(new ResourceMonitor(infraProvider, resourcePool, properties));
		
	}

	@After
	public void setDown() throws Exception {

	}

	@Test
	public void propertiesEmptyTest() throws Exception {

		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		
		doReturn(resources).when(resourcePool).getAllResources();

	}
	
	
//	private boolean paused = false;
//	private boolean active = true;
//
//	@Override
//	public void run() {
//
//		while (active) {
//
//			try {
//
//				List<AbstractResource> resources = resourcePool.getAllResources();
//
//				checkIsPaused();
//
//				if (resources.isEmpty() && resources.isEmpty()) {
//					pause();
//					monitoringServiceRunner.join();
//				} else {
//					monitoringPendingResources();
//					monitoringResources(resources);
//					Thread.sleep(infraMonitoringPeriod);
//				}
//			} catch (InterruptedException e) {
//				LOGGER.error("Error while executing MonitoringService");
//			}
//		}
//
//	}
//	
//	private void monitoringPendingResources() {
//
//		for (AbstractResource resource : getPendingResources()) {
//			resource = infraProvider.getResource(resource.getId());
//			if (ResourceState.READY.equals(resource.getState())) {
//				resource.setState(ResourceState.READY);
//				pendingResources.remove(resource);
//				resourcePool.addResource(resource);
//			}
//		}
//	}
//	
//	private void monitoringResources(List<AbstractResource> resources) {
//
//		for (AbstractResource resource : resources) {
//
//			if (ResourceState.READY.equals(resource.getState())) {
//				this.checkResourceConnectivity(resource);
//			} else if (ResourceState.IDLE.equals(resource.getState())) {
//				resolveIdleResource(resource);
//			} else if (ResourceState.BUSY.equals(resource.getState())) {
//				idleResources.remove(resource);
//			} else if (ResourceState.FAILED.equals(resource.getState())) {
//				boolean isAlive = this.checkResourceConnectivity(resource);
//				if(isAlive){
//					moveResourceToIdle(resource);
//				}
//			} else if (ResourceState.TO_REMOVE.equals(resource.getState())) {
//				try {
//					infraProvider.deleteResource(resource.getId());
//					resourcePool.removeResource(resource);
//					idleResources.remove(resource);
//				} catch (Exception e) {
//					LOGGER.error("Error while tring to remove resource "+resource.getId()+" - "+e.getMessage());
//				}
//				
//			}
//
//		}
//	}
//
//	private void resolveIdleResource(AbstractResource resource) {
//
//		Long since = idleResources.get(resource);
//
//		// If since == null, resource must go to IDLE list.
//		if (since == null) {
//			moveResourceToIdle(resource);
//		} else {
//
//			String requestType = resource.getMetadataValue(AbstractResource.METADATA_REQUEST_TYPE);
//			if (OrderType.ONE_TIME.getValue().equals(requestType)) {
//
//				boolean isAlive = checkResourceConnectivity(resource);
//				// Has expiration time?
//				if (isAlive && noExpirationTime.compareTo(idleLifeTime) != 0) {
//					Date expirationDate = new Date(since.longValue());
//					Date currentDate = new Date();
//					if (expirationDate.before(currentDate)) {
//						resource.setState(ResourceState.TO_REMOVE);
//						idleResources.remove(resource);
//					}
//				}
//			}
//		}
//	}
//
//	private void moveResourceToIdle(AbstractResource resource) {
//		resource.setState(ResourceState.IDLE);
//		idleResources.put(resource, Long.valueOf(new Date().getTime()));
//		//TODO this should be called here?
//		resourcePool.releaseResource(resource);
//	}
//
//	private boolean checkResourceConnectivity(AbstractResource resource) {
//		if (!resource.checkConnectivity()) {
//			if(resource.getConnectionFailTries() >= maxConnectionTries){
//				resource.setState(ResourceState.TO_REMOVE);
//			}else{
//				resource.setState(ResourceState.FAILED);
//			}
//			idleResources.remove(resource);
//			return false;
//		}
//		return true;
//	}
//
//	public void checkIsPaused() throws InterruptedException {
//		synchronized (this) {
//			while (paused) {
//				wait();
//			}
//		}
//	}
//	
//
//	public synchronized void stop() {
//		active = false;
//	}
//
//	public synchronized void pause() {
//		paused = true;
//	}
//
//	public synchronized void resume() {
//		paused = false;
//		notify();
//	}
//
//	public boolean isPaused() {
//		return paused;
//	}
	
}
