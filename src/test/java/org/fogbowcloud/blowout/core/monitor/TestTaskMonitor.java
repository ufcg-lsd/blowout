package org.fogbowcloud.blowout.core.monitor;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskProcess;
import org.fogbowcloud.blowout.core.model.TaskState;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;
import org.fogbowcloud.blowout.pool.AbstractResource;
import org.fogbowcloud.blowout.pool.BlowoutPool;
import org.junit.Before;
import org.junit.Test;


public class TestTaskMonitor {

	private static final String FAKE_ID = "fakeId";
	public TaskMonitor taskMon;
	public BlowoutPool pool;
	
	@Before
	public void setUp(){
		pool = mock(BlowoutPool.class);
		this.taskMon = spy(new TaskMonitor(pool, 0));
	}
	
	@Test
	public void testProcMonNothingHappens() {
		Task fakeTask = mock(Task.class);
		TaskProcess fakeProcess = mock(TaskProcess.class);
		doReturn(TaskState.RUNNING).when(fakeProcess).getStatus();
		AbstractResource fakeResource = mock(AbstractResource.class);
		List<TaskProcess> runningPrc = new ArrayList<TaskProcess>();
		runningPrc.add(fakeProcess);
		doReturn(runningPrc).when(this.taskMon).getRunningProcesses();
		
		
		this.taskMon.procMon();
		verify(pool, never()).putResource(fakeResource, ResourceState.FAILED);
		verify(pool, never()).putResource(fakeResource, ResourceState.IDLE);
	}
	
	@Test
	public void testProcMonProcFails() {
		Task fakeTask = mock(Task.class);
		TaskProcess fakeProcess = mock(TaskProcess.class);
		doReturn(TaskState.FAILED).when(fakeProcess).getStatus();
		AbstractResource fakeResource = mock(AbstractResource.class);
		doReturn(FAKE_ID).when(fakeTask).getId();
		doReturn(FAKE_ID).when(fakeProcess).getTaskId();
		doReturn(fakeTask).when(this.taskMon).getTaskById(FAKE_ID);
		doReturn(fakeResource).when(fakeProcess).getResource();
		List<TaskProcess> runningPrc = new ArrayList<TaskProcess>();
		runningPrc.add(fakeProcess);
		doReturn(runningPrc).when(this.taskMon).getRunningProcesses();
		Map<Task, TaskProcess> runningTasks = new HashMap<Task, TaskProcess>();
		runningTasks.put(fakeTask, fakeProcess);
		doReturn(runningTasks).when(this.taskMon).getRunningTasks();
		
		this.taskMon.procMon();
		
		verify(pool).putResource(fakeResource, ResourceState.FAILED);
		verify(pool, never()).putResource(fakeResource, ResourceState.IDLE);
	}
	
	@Test
	public void testProcMonProcFinnished() {
		Task fakeTask = mock(Task.class);
		TaskProcess fakeProcess = mock(TaskProcess.class);
		doReturn(TaskState.FINNISHED).when(fakeProcess).getStatus();
		AbstractResource fakeResource = mock(AbstractResource.class);
		doReturn(FAKE_ID).when(fakeTask).getId();
		doReturn(FAKE_ID).when(fakeProcess).getTaskId();
		doReturn(fakeTask).when(this.taskMon).getTaskById(FAKE_ID);
		doReturn(fakeResource).when(fakeProcess).getResource();
		List<TaskProcess> runningPrc = new ArrayList<TaskProcess>();
		runningPrc.add(fakeProcess);
		doReturn(runningPrc).when(this.taskMon).getRunningProcesses();
		Map<Task, TaskProcess> runningTasks = new HashMap<Task, TaskProcess>();
		runningTasks.put(fakeTask, fakeProcess);
		doReturn(runningTasks).when(this.taskMon).getRunningTasks();
		
		this.taskMon.procMon();
		
		verify(pool, never()).putResource(fakeResource, ResourceState.FAILED);
		verify(pool ).putResource(fakeResource, ResourceState.IDLE);
	}
	
	@Test
	public void testRunTask() {
		Task fakeTask = mock(Task.class);
		TaskProcess fakeProcess = mock(TaskProcess.class);
		AbstractResource fakeResource = mock(AbstractResource.class);
		doReturn(FAKE_ID).when(fakeTask).getId();
		doReturn(FAKE_ID).when(fakeProcess).getTaskId();
		List<TaskProcess> runningPrc = new ArrayList<TaskProcess>();
		runningPrc.add(fakeProcess);
		doReturn(runningPrc).when(this.taskMon).getRunningProcesses();
		doNothing().when(pool).putResource(fakeResource, ResourceState.BUSY);
		ExecutorService execServ = mock(ExecutorService.class);
		doReturn(execServ).when(this.taskMon).getExecutorService();
		doReturn(mock(Future.class)).when(execServ).submit(any(Runnable.class));
		Map<Task, TaskProcess> runningTasks = new HashMap<Task, TaskProcess>();
		runningTasks.put(fakeTask, fakeProcess);
		doReturn(runningTasks).when(this.taskMon).getRunningTasks();
		
		this.taskMon.runTask(fakeTask, fakeResource);
		
	}
	
}
