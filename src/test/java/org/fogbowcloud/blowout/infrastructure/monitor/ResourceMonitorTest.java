package org.fogbowcloud.blowout.infrastructure.monitor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.fogbowcloud.blowout.helpers.Constants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.constants.AppPropertiesConstants;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;
import org.fogbowcloud.blowout.pool.BlowoutPool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ResourceMonitorTest {

	private ResourceMonitor resourceMonitor;
	private InfrastructureProvider infraProvider;
	private BlowoutPool resourcePool;
	private Specification spec;
	
	@Before
	public void setUp() throws Exception {
		
		this.infraProvider = Mockito.mock(InfrastructureProvider.class);
		this.resourcePool = Mockito.mock(BlowoutPool.class);
		Properties properties = new Properties();
		properties.setProperty(AppPropertiesConstants.RESOURCE_MONITOR_SLEEP_PERIOD, "1000");
		properties.setProperty(AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME, "120000");
		properties.setProperty(AppPropertiesConstants.INFRA_RESOURCE_CONNECTION_RETRY, "3");
		this.spec = Mockito.mock(Specification.class);
		this.resourceMonitor = Mockito.spy(new ResourceMonitor(this.infraProvider, this.resourcePool, properties));
	}

	@After
	public void setDown() throws Exception {

	}

	@Test
	public void testProcessPendingResource() throws Exception {
		List<AbstractResource> resources = new ArrayList<>();
		AbstractResource resource = new FogbowResource(FAKE_RESOURCE_ID, FAKE_ORDER_ID, this.spec);
		
		doReturn(resources).when(resourcePool).getAllResources();
		doReturn(resource).when(infraProvider).getResource(FAKE_RESOURCE_ID);
		
		resourceMonitor.addPendingResource(FAKE_RESOURCE_ID, this.spec);
		resourceMonitor.getMonitoringService().monitorProcess();
		
		verify(resourcePool, times(1)).addResource(resource);
		assertTrue(resourceMonitor.getPendingResources().isEmpty());
	}
	
	@Test
	public void testProcessTwoPendingResourceOnReady() throws Exception {

		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		
		String resourceIdA = "resourceA";
		String orderIdA = "orderA";
		
		String resourceIdB = "resourceB";
		
		Specification spec = new Specification("ImageA", "Fogbow", "myKeyA", "path");
		
		AbstractResource resource = new FogbowResource(resourceIdA, orderIdA, spec);
		
		doReturn(resources).when(resourcePool).getAllResources();
		doReturn(resource).when(infraProvider).getResource(resourceIdA);
		doReturn(null).when(infraProvider).getResource(resourceIdB);
		
		resourceMonitor.addPendingResource(resourceIdA, spec);
		resourceMonitor.addPendingResource(resourceIdB, spec);
		resourceMonitor.getMonitoringService().monitorProcess();
		
		verify(resourcePool, times(1)).addResource(resource);
		assertEquals(1, resourceMonitor.getPendingResources().size());

	}
}