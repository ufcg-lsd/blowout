package org.fogbowcloud.blowout.pool;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fogbowcloud.blowout.core.StandardScheduler;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskImpl;
import org.fogbowcloud.blowout.core.monitor.TaskMonitor;
import org.fogbowcloud.blowout.infrastructure.manager.DefaultInfrastructureManager;
import org.fogbowcloud.blowout.infrastructure.manager.InfrastructureManager;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;
import org.fogbowcloud.blowout.infrastructure.monitor.ResourceMonitor;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestDefaultBlowoutPool {
	
	private DefaultBlowoutPool defaultBlowoutPool;
	private InfrastructureManager infraManager;
	private StandardScheduler standardScheduler;
	private Specification spec;
	
	@Before
	public void setUp() {
		defaultBlowoutPool = spy(new DefaultBlowoutPool());
		spec = mock(Specification.class);
	}
	
	@Test
	public void testCallAct() {
		// set up
		FogbowResource resourceOne = new FogbowResource("resource-one-id", "order-one-id", spec);
		resourceOne.setState(ResourceState.IDLE);
		FogbowResource resourceTwo = new FogbowResource("resource-two-id", "order-two-id", spec);
		resourceTwo.setState(ResourceState.IDLE);
		
		Map<String, AbstractResource> resourcePool = new ConcurrentHashMap<String, AbstractResource>();
		resourcePool.put(resourceOne.getId(), resourceOne);
		resourcePool.put(resourceTwo.getId(), resourceTwo);
		
		TaskImpl taskOne = new TaskImpl("task-one-id", spec);
		TaskImpl taskTwo = new TaskImpl("task-two-id", spec);
		
		List<Task> taskList = new ArrayList<Task>();
		taskList.add(taskOne);
		taskList.add(taskTwo);
		
		defaultBlowoutPool.setResourcePool(resourcePool);
		defaultBlowoutPool.setTaskPool(taskList);
		
		InfrastructureProvider fogbowInfraProvider = mock(InfrastructureProvider.class);
		ResourceMonitor resourceMonitor = mock(ResourceMonitor.class);
		
		TaskMonitor taskMon = new TaskMonitor(defaultBlowoutPool, 3000);
		
		infraManager = new DefaultInfrastructureManager(fogbowInfraProvider, resourceMonitor);
		standardScheduler = new StandardScheduler(taskMon);
		
		defaultBlowoutPool.start(infraManager, standardScheduler);
		
		// exercise
		defaultBlowoutPool.callAct();
		
		// expect
		Assert.assertEquals(ResourceState.BUSY, resourceOne.getState());
		Assert.assertEquals(ResourceState.BUSY, resourceTwo.getState());
	}
	
	@Test
	public void testUpdateResource() {
		// set up
		FogbowResource resource = spy(new FogbowResource("resource-id", "order-id", spec));
		resource.setState(ResourceState.BUSY);
		
		Map<String, AbstractResource> resourcePool = new ConcurrentHashMap<String, AbstractResource>();
		resourcePool.put(resource.getId(), resource);
		
		defaultBlowoutPool.setResourcePool(resourcePool);
		
		// exercise
		defaultBlowoutPool.updateResource(resource, ResourceState.IDLE);
		
		// expect
		Assert.assertEquals(ResourceState.IDLE, resource.getState());
	}
	
	@Test
	public void testAddResourceToList() {
		
	}
}
