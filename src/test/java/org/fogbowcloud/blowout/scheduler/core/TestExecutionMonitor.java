package org.fogbowcloud.blowout.scheduler.core;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.fogbowcloud.blowout.core.core.model.Job;
import org.fogbowcloud.blowout.core.core.model.Resource;
import org.fogbowcloud.blowout.core.core.model.Task;
import org.fogbowcloud.blowout.core.core.model.TaskImpl;
import org.fogbowcloud.blowout.core.core.model.TaskProcess;
import org.fogbowcloud.blowout.core.core.model.TaskProcessImpl;
import org.fogbowcloud.blowout.core.infrastructure.InfrastructureManager;
import org.fogbowcloud.blowout.core.infrastructure.exceptions.InfrastructureException;
import org.junit.Before;
import org.junit.Test;

public class TestExecutionMonitor {

	public Task task;
	public Scheduler scheduler;
	public Job job;
	public InfrastructureManager IM;
	public FogbowResource resource;
	public String FAKE_TASK_ID = "FAKE_TASK_ID";
	private CurrentThreadExecutorService executorService;

	@Before
	public void setUp() {
		task = spy(new TaskImpl(FAKE_TASK_ID, null));
		IM = mock(InfrastructureManager.class);
		resource = mock(FogbowResource.class);
		job = mock(Job.class);
		executorService = new CurrentThreadExecutorService();
		scheduler = spy(new Scheduler(IM, job));
	}

	@Test
	public void testExecutionMonitor() throws InfrastructureException, InterruptedException {
		ExecutionMonitor executionMonitor = new ExecutionMonitor(scheduler, executorService, job);
		TaskProcess tp = mock(TaskProcess.class);
		List<TaskProcess> processes = new ArrayList<TaskProcess>();
		processes.add(tp);
		doReturn(processes).when(scheduler).getRunningProcs();
		doReturn(TaskProcessImpl.State.FINNISHED).when(tp).getStatus();
		doNothing().when(scheduler).taskCompleted(tp);
		executionMonitor.run();
		Thread.sleep(500);
		verify(tp, times(2)).getStatus();
	}

	@Test
	public void testExecutionMonitorTaskFails() throws InterruptedException {
		ExecutionMonitor executionMonitor = new ExecutionMonitor(scheduler, executorService, job);
		TaskProcess tp = mock(TaskProcess.class);
		List<TaskProcess> processes = new ArrayList<TaskProcess>();
		processes.add(tp);
		doReturn(processes).when(scheduler).getRunningProcs();
		doReturn(TaskProcessImpl.State.FAILED).when(tp).getStatus();
		doNothing().when(scheduler).taskCompleted(tp);
		doNothing().when(job).finish(task);
		executionMonitor.run();
		Thread.sleep(500);
		verify(tp).getStatus();
	}

	@Test
	public void testExecutionIsNotOver() throws InfrastructureException, InterruptedException {
		ExecutionMonitor executionMonitor = new ExecutionMonitor(scheduler, executorService, job);
		TaskProcess tp = mock(TaskProcess.class);
		doReturn(TaskProcessImpl.State.RUNNING).when(tp).getStatus();
		List<TaskProcess> processes = new ArrayList<TaskProcess>();
		processes.add(tp);
		doReturn(processes).when(scheduler).getRunningProcs();
		executionMonitor.run();
		verify(tp, times(2)).getStatus();
		verify(scheduler, never()).taskCompleted(tp);
	}
}
