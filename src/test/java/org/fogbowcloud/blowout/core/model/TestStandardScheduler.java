package org.fogbowcloud.blowout.core.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.ArrayList;
import java.util.List;

import org.fogbowcloud.blowout.core.SchedulerInterface;
import org.fogbowcloud.blowout.core.StandardScheduler;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;
import org.fogbowcloud.blowout.pool.AbstractResource;
import org.fogbowcloud.blowout.pool.BlowoutPool;
import org.fogbowcloud.blowout.pool.ResourceStateHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


public class TestStandardScheduler {

	SchedulerInterface sched;
	BlowoutPool blowoutPool;
	
	@Before
	public void setUp() {
		this.blowoutPool = Mockito.mock(BlowoutPool.class);
		this.sched = spy(new StandardScheduler(blowoutPool));
	}
	
	@Test
	public void testActOnEmptyLists() {
		List<Task> tasks = new ArrayList<Task>();
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		sched.act(tasks, resources);
		
		
		List<Task> emptyTaskList = new ArrayList<Task>();
		assertEquals(sched.getRunningTasks(), emptyTaskList);
	}

	@Test
	public void testActOnEmptyResourceList() {
		List<Task> tasks = new ArrayList<Task>();
		Task task = new TaskImpl("fakeId", mock(Specification.class));
		tasks.add(task);
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		sched.act(tasks, resources);
		
		
		List<Task> emptyTaskList = new ArrayList<Task>();
		assertEquals(sched.getRunningTasks(), emptyTaskList);
	}
	
	@Test
	public void testActOnEmptyTaskList() {
		List<Task> tasks = new ArrayList<Task>();
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		
		AbstractResource resource = new FogbowResource("resourceId", "fakeOrderId", mock(Specification.class));
		
		resources.add(resource);
		
		sched.act(tasks, resources);
		
		
		List<Task> emptyTaskList = new ArrayList<Task>();
		assertEquals(sched.getRunningTasks(), emptyTaskList);
	}
	
	@Test
	public void testActGoldePath() {
		List<Task> tasks = new ArrayList<Task>();
		Task task = new TaskImpl("fakeId", mock(Specification.class));
		tasks.add(task);
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		AbstractResource resource = new FogbowResource("resourceId", "fakeOrderId", mock(Specification.class));
		ResourceStateHelper.changeResourceToState(resource, ResourceState.IDLE);
		resources.add(resource);
		
		sched.act(tasks, resources);
		
		
		List<Task> runningTaskList = new ArrayList<Task>();
		runningTaskList.add(task);
		assertEquals(sched.getRunningTasks(), runningTaskList);
	}
	
	@Test
	public void testActOnFailedResource() {
		List<Task> tasks = new ArrayList<Task>();
		Task task = new TaskImpl("fakeId", mock(Specification.class));
		tasks.add(task);
		
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		sched.act(tasks, resources);
		
		List<Task> emptyTaskList = new ArrayList<Task>();
		assertEquals(sched.getRunningTasks(), emptyTaskList);
	}
	
	@Test
	public void testActOnRemovedTask() {
		List<Task> tasks = new ArrayList<Task>();
		Task task = new TaskImpl("fakeId", mock(Specification.class));
		tasks.add(task);
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		sched.act(tasks, resources);
		
		
		List<Task> emptyTaskList = new ArrayList<Task>();
		assertEquals(sched.getRunningTasks(), emptyTaskList);
	}
	
	@Test
	public void testActOnRemovedResource() {
		List<Task> tasks = new ArrayList<Task>();
		Task task = new TaskImpl("fakeId", mock(Specification.class));
		tasks.add(task);
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		sched.act(tasks, resources);
		
		
		List<Task> emptyTaskList = new ArrayList<Task>();
		assertEquals(sched.getRunningTasks(), emptyTaskList);
	}
	

	@Test
	public void testChooseTaskForRunning() {
		
	}
	
	@Test
	public void testStopTask(){
		
	}
	
	@Test
	public void testRunTask(){
		
	}
	
	@Test
	public void testSubmitToMonitor(){
		
	}
	
	@Test
	public void testCreateProcess(){
		
	}
	
}
