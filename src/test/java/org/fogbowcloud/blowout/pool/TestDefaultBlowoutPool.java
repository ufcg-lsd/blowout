package org.fogbowcloud.blowout.pool;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doReturn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.fogbowcloud.blowout.scheduler.DefaultScheduler;
import org.fogbowcloud.blowout.core.constants.FogbowConstants;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.model.task.Task;
import org.fogbowcloud.blowout.core.model.task.TaskImpl;
import org.fogbowcloud.blowout.core.monitor.TaskMonitor;
import org.fogbowcloud.blowout.infrastructure.manager.DefaultInfrastructureManager;
import org.fogbowcloud.blowout.infrastructure.manager.InfrastructureManager;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.core.model.resource.ResourceState;
import org.fogbowcloud.blowout.infrastructure.monitor.ResourceMonitor;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestDefaultBlowoutPool {
	
	private static final String FAKE_UUID = "1234";
	private DefaultBlowoutPool defaultBlowoutPool;
	private InfrastructureManager infraManager;
	private DefaultScheduler defaultScheduler;
	private Specification specification;
	private ResourceMonitor resourceMonitor;
	private InfrastructureProvider fogbowInfraProvider;
	private TaskMonitor taskMonitor;
	
	@Before
	public void setUp() {
		defaultBlowoutPool = spy(new DefaultBlowoutPool());
		specification = new Specification("fakeimage", "fakeusername", "fakepublickey", "fakekeypath");
		specification.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, "fakeRequirements");

		fogbowInfraProvider = mock(InfrastructureProvider.class);
		resourceMonitor = mock(ResourceMonitor.class);

		taskMonitor = new TaskMonitor(defaultBlowoutPool, 3000);

		infraManager = new DefaultInfrastructureManager(fogbowInfraProvider, resourceMonitor);
		defaultScheduler = new DefaultScheduler(taskMonitor);

		defaultBlowoutPool.start(infraManager, defaultScheduler);
	}
	
	@Test
	public void testCallAct() {
		// set up
		FogbowResource resourceOne = spy(new FogbowResource("resource-one-id", "order-one-id", specification, "fake-public-ip-one"));
		resourceOne.setState(ResourceState.IDLE);
		doReturn(true).when(resourceOne).match(specification);
		
		FogbowResource resourceTwo = spy( new FogbowResource("resource-two-id", "order-two-id", specification, "fake-public-ip-two"));
		resourceTwo.setState(ResourceState.IDLE);
		doReturn(true).when(resourceTwo).match(specification);

		defaultBlowoutPool.addResource(resourceOne);
		defaultBlowoutPool.addResource(resourceTwo);
		
		TaskImpl taskOne = new TaskImpl("task-one-id", specification, FAKE_UUID);
		TaskImpl taskTwo = new TaskImpl("task-two-id", specification, FAKE_UUID);
		
		defaultBlowoutPool.addTask(taskOne);
		defaultBlowoutPool.addTask(taskTwo);
		
		// exercise
		defaultBlowoutPool.callAct();

		ResourceState resourceStateOne = defaultBlowoutPool.getResourceById(resourceOne.getId()).getState();
		ResourceState resourceStateTwo = defaultBlowoutPool.getResourceById(resourceTwo.getId()).getState();

		// expect
		Assert.assertEquals(ResourceState.BUSY, resourceStateOne);
		Assert.assertEquals(ResourceState.BUSY, resourceStateTwo);
	}
	
	@Test
	public void testAddTask(){
		// set up
				
				Map<String, AbstractResource> resourcePool = new ConcurrentHashMap<String, AbstractResource>();
				
				
				List<Task> taskList = new ArrayList<Task>();
				
				defaultBlowoutPool.setResourcePool(resourcePool);
				defaultBlowoutPool.setTaskPool(taskList);
				
				// exercise
				defaultBlowoutPool.callAct();
				TaskImpl task = new TaskImpl("task-two-id", specification, FAKE_UUID);

				
				defaultBlowoutPool.addTask(task);
				verify(resourceMonitor).addPendingResource(any(String.class), any (Specification.class));
				// expect
				
	}
	
	@Test
	public void testAddTaskWithFreeResourceJobs(){
		// set up
		FogbowResource resourceOne = spy(new FogbowResource("resource-one-id", "order-one-id", specification));
		doReturn(true).when(resourceOne).match(specification);
		resourceOne.setState(ResourceState.IDLE);
		
		Map<String, AbstractResource> resourcePool = new ConcurrentHashMap<String, AbstractResource>();
		
		resourcePool.put(resourceOne.getId(), resourceOne);
		
		List<Task> taskList = new ArrayList<Task>();
		
		defaultBlowoutPool.setResourcePool(resourcePool);
		defaultBlowoutPool.setTaskPool(taskList);
		
		// exercise
		defaultBlowoutPool.callAct();
		TaskImpl task = new TaskImpl("task-two-id", specification, FAKE_UUID);

		defaultBlowoutPool.addTask(task);
		
//		TaskImpl task2 = new TaskImpl("task-two-id2", specification, FAKE_UUID);
//
//		defaultBlowoutPool.addTask(task2);
		
		verify(resourceMonitor, never()).addPendingResource(any(String.class), any (Specification.class));
		// expect
		
	}
	
	@Test
	public void testAddTaskWithRunningTask(){
		// set up
		FogbowResource resourceOne = spy(new FogbowResource("resource-one-id", "order-one-id", specification));
		resourceOne.setState(ResourceState.BUSY);
		doReturn(true).when(resourceOne).match(specification);
		
		Map<String, AbstractResource> resourcePool = new ConcurrentHashMap<String, AbstractResource>();
		
		resourcePool.put(resourceOne.getId(), resourceOne);
		
		List<Task> taskList = new ArrayList<Task>();
		
		TaskImpl task = new TaskImpl("task-two-id2", specification, FAKE_UUID);
		taskList.add(task);
		defaultBlowoutPool.setResourcePool(resourcePool);
		defaultBlowoutPool.setTaskPool(taskList);
		
		// exercise
		TaskImpl task2 = new TaskImpl("task-two-id", specification, FAKE_UUID);

		defaultBlowoutPool.addTask(task2);
		
//		TaskImpl task2 = new TaskImpl("task-two-id2", specification, FAKE_UUID);
//
//		defaultBlowoutPool.addTask(task2);
		verify(defaultBlowoutPool).callAct();
		verify(resourceMonitor).addPendingResource(any(String.class), any (Specification.class));
		// expect
		
	}
	
	@Test
	public void testStopTask(){
		// set up
				
				Map<String, AbstractResource> resourcePool = new ConcurrentHashMap<String, AbstractResource>();
				
				
				List<Task> taskList = new ArrayList<Task>();
				TaskImpl task = new TaskImpl("task-two-id", specification, FAKE_UUID);
				taskList.add(task);
				defaultBlowoutPool.setResourcePool(resourcePool);
				defaultBlowoutPool.setTaskPool(taskList);
				
				// exercise
				defaultBlowoutPool.callAct();

				//TODO: Create pending resource remover on monitor
				defaultBlowoutPool.removeTask(task);
				verify(resourceMonitor).addPendingResource(any(String.class), any (Specification.class));
				// expect
				
	}
	
	
	@Test
	public void testUpdateResource() {
		// set up
		FogbowResource resource = spy(new FogbowResource("resource-id", "order-id", specification));
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
