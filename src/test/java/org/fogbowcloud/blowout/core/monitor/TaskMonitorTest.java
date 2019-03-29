package org.fogbowcloud.blowout.core.monitor;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.fogbowcloud.blowout.helpers.Constants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.fogbowcloud.blowout.helpers.Constants;
import org.fogbowcloud.blowout.scheduler.DefaultScheduler;
import org.fogbowcloud.blowout.core.model.Command;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.model.task.Task;
import org.fogbowcloud.blowout.core.model.task.TaskImpl;
import org.fogbowcloud.blowout.core.model.task.TaskProcess;
import org.fogbowcloud.blowout.core.model.task.TaskProcessImpl;
import org.fogbowcloud.blowout.core.model.task.TaskState;
import org.fogbowcloud.blowout.infrastructure.manager.DefaultInfrastructureManager;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.core.model.resource.ResourceState;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;
import org.fogbowcloud.blowout.pool.BlowoutPool;
import org.fogbowcloud.blowout.pool.DefaultBlowoutPool;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TaskMonitorTest {

	private TaskMonitor taskMon;
	private BlowoutPool pool;
	private Specification spec;
	
	@Before
	public void setUp(){
		this.pool = mock(BlowoutPool.class);
		this.taskMon = spy(new TaskMonitor(pool, 0));
		this.spec = mock(Specification.class);
	}
	
	@Test
	public void testGetTaskStateCorrect() {
		// set up
		TaskImpl taskImpl = new TaskImpl(Constants.FakeData.TASK_ID, this.spec, Constants.FakeData.UUID);
		taskImpl.setState(TaskState.RUNNING);
		
		List<Command> commandListMock = mock(ArrayList.class);
		
		TaskProcessImpl taskProcessImpl = new TaskProcessImpl(taskImpl.getId(), commandListMock, this.spec,Constants.FakeData.UUID);
		taskProcessImpl.setTaskState(TaskState.RUNNING);
		
		Map<Task, TaskProcess> runningTasks = mock(Map.class);
		
		TaskMonitor taskMon = new TaskMonitor(this.pool, 3000);
		taskMon.setRunningTasks(runningTasks);
		
		doReturn(taskProcessImpl).when(runningTasks).get(taskImpl);
		
		// exercise
		TaskState state = taskMon.getTaskState(taskImpl);
		
		// expect
		Assert.assertEquals(TaskState.RUNNING, state);
	}
	
	@Test
	public void testGetTaskStateCompleted() {
		// set up
		TaskImpl taskImpl = new TaskImpl(Constants.FakeData.TASK_ID, this.spec, Constants.FakeData.UUID);
		taskImpl.setState(TaskState.FINISHED);
		taskImpl.finish();
		
		Map<Task, TaskProcess> runningTasks = mock(Map.class);
		
		TaskMonitor taskMon = new TaskMonitor(pool, 3000);
		taskMon.setRunningTasks(runningTasks);
		
		doReturn(null).when(runningTasks).get(taskImpl);
		
		// exercise
		TaskState state = taskMon.getTaskState(taskImpl);
		
		// expect
		Assert.assertEquals(TaskState.COMPLETED, state);
	}
	
	@Test
	public void testGetTaskStateReady() {
		// set up
		TaskImpl taskImpl = new TaskImpl(Constants.FakeData.TASK_ID, this.spec, Constants.FakeData.UUID);
		taskImpl.setState(TaskState.READY);
		
		Map<Task, TaskProcess> runningTasks = mock(Map.class);
		
		TaskMonitor taskMon = new TaskMonitor(this.pool, 3000);
		taskMon.setRunningTasks(runningTasks);
		
		doReturn(null).when(runningTasks).get(taskImpl);
		
		// exercise
		TaskState state = taskMon.getTaskState(taskImpl);
		
		// expect
		Assert.assertEquals(TaskState.READY, state);
	}
	
	@Test
	public void testTaskFinished() {
		// set up
		DefaultInfrastructureManager infraManager = mock(DefaultInfrastructureManager.class);
		DefaultScheduler defaultScheduler = mock(DefaultScheduler.class);
		
		DefaultBlowoutPool blowoutPool = new DefaultBlowoutPool();
		blowoutPool.start(infraManager, defaultScheduler);

		TaskImpl taskOne = new TaskImpl(Constants.FakeData.TASK_ID, this.spec, Constants.FakeData.UUID);
		
		List<Task> taskList = new ArrayList<Task>();
		taskList.add(taskOne);
		
		blowoutPool.addTasks(taskList);
		
		AbstractResource resourceOne = new FogbowResource(Constants.FakeData.RESOURCE_ID,
				Constants.FakeData.ORDER_ID, this.spec);
		
		blowoutPool.addResource(resourceOne);
		
		List<Command> commandListMock = mock(ArrayList.class);
		
		TaskProcessImpl taskProcessOne = new TaskProcessImpl(taskOne.getId(), commandListMock, this.spec,
				Constants.FakeData.UUID);
		
		taskProcessOne.setResource(resourceOne);
		
		taskProcessOne.setTaskState(TaskState.FINISHED);
		
		Map<Task, TaskProcess> runningTasks = new HashMap<>();
		runningTasks.put(taskOne, taskProcessOne);
		
		TaskMonitor taskMon = new TaskMonitor(blowoutPool, 3000);
		taskMon.setRunningTasks(runningTasks);
		
		// exercise
		taskMon.processMonitor();
		
		// expect
		Assert.assertEquals(ResourceState.IDLE, resourceOne.getState());
		Assert.assertTrue(taskOne.isFinished());
	}
	
	@Test
	public void testProcMonNothingHappens() {
		TaskProcess fakeProcess = mock(TaskProcess.class);
		doReturn(TaskState.RUNNING).when(fakeProcess).getTaskState();
		AbstractResource fakeResource = mock(AbstractResource.class);
		List<TaskProcess> runningPrc = new ArrayList<>();
		runningPrc.add(fakeProcess);
		doReturn(runningPrc).when(this.taskMon).getRunningProcesses();
		
		
		this.taskMon.processMonitor();
		verify(this.pool, never()).updateResource(fakeResource, ResourceState.FAILED);
		verify(this.pool, never()).updateResource(fakeResource, ResourceState.IDLE);
	}
	
	@Test
	public void testProcMonProcFails() {
		Task fakeTask = mock(Task.class);
		TaskProcess fakeProcess = mock(TaskProcess.class);
		doReturn(TaskState.FAILED).when(fakeProcess).getTaskState();
		AbstractResource fakeResource = mock(AbstractResource.class);
		doReturn(Constants.FakeData.TASK_ID).when(fakeTask).getId();
		doReturn(Constants.FakeData.TASK_ID).when(fakeProcess).getTaskId();
		doReturn(fakeTask).when(this.taskMon).getTaskById(Constants.FakeData.TASK_ID);
		doReturn(fakeResource).when(fakeProcess).getResource();
		List<TaskProcess> runningPrc = new ArrayList<>();
		runningPrc.add(fakeProcess);
		doReturn(runningPrc).when(this.taskMon).getRunningProcesses();
		Map<Task, TaskProcess> runningTasks = new HashMap<>();
		runningTasks.put(fakeTask, fakeProcess);
		doReturn(runningTasks).when(this.taskMon).getRunningTasks();
		
		this.taskMon.processMonitor();
		
		verify(this.pool).updateResource(fakeResource, ResourceState.FAILED);
		verify(this.pool, never()).updateResource(fakeResource, ResourceState.IDLE);
	}
	
	@Test
	public void testProcMonProcFinished() {
		Task fakeTask = mock(Task.class);
		TaskProcess fakeProcess = mock(TaskProcess.class);
		doReturn(TaskState.FINISHED).when(fakeProcess).getTaskState();
		AbstractResource fakeResource = mock(AbstractResource.class);
		doReturn(Constants.FakeData.TASK_ID).when(fakeTask).getId();
		doReturn(Constants.FakeData.TASK_ID).when(fakeProcess).getTaskId();
		doReturn(fakeTask).when(this.taskMon).getTaskById(Constants.FakeData.TASK_ID);
		doReturn(fakeResource).when(fakeProcess).getResource();
		List<TaskProcess> runningPrc = new ArrayList<>();
		runningPrc.add(fakeProcess);
		doReturn(runningPrc).when(this.taskMon).getRunningProcesses();
		Map<Task, TaskProcess> runningTasks = new HashMap<>();
		runningTasks.put(fakeTask, fakeProcess);
		doReturn(runningTasks).when(this.taskMon).getRunningTasks();
		
		this.taskMon.processMonitor();
		
		verify(pool, never()).updateResource(fakeResource, ResourceState.FAILED);
		verify(pool ).updateResource(fakeResource, ResourceState.IDLE);
	}
	
	@Test
	public void testRunTask() {
		Task fakeTask = mock(Task.class);
		TaskProcess fakeProcess = mock(TaskProcess.class);
		AbstractResource fakeResource = mock(AbstractResource.class);
		doReturn(Constants.FakeData.TASK_ID).when(fakeTask).getId();
		doReturn(Constants.FakeData.TASK_ID).when(fakeProcess).getTaskId();
		List<TaskProcess> runningPrc = new ArrayList<TaskProcess>();
		runningPrc.add(fakeProcess);
		doReturn(runningPrc).when(this.taskMon).getRunningProcesses();
		doNothing().when(pool).updateResource(fakeResource, ResourceState.BUSY);
		ExecutorService execServ = mock(ExecutorService.class);
		doReturn(execServ).when(this.taskMon).getExecutorService();
		doReturn(mock(Future.class)).when(execServ).submit(any(Runnable.class));
		Map<Task, TaskProcess> runningTasks = new HashMap<Task, TaskProcess>();
		runningTasks.put(fakeTask, fakeProcess);
		doReturn(runningTasks).when(this.taskMon).getRunningTasks();
		
		this.taskMon.runTask(fakeTask, fakeResource);
		
	}
	
}
