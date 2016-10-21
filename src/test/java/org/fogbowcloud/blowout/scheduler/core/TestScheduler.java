package org.fogbowcloud.blowout.scheduler.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fogbowcloud.blowout.scheduler.core.model.Command;
import org.fogbowcloud.blowout.scheduler.core.model.Job;
import org.fogbowcloud.blowout.scheduler.core.model.Job.TaskState;
import org.fogbowcloud.blowout.scheduler.core.model.Resource;
import org.fogbowcloud.blowout.scheduler.core.model.Specification;
import org.fogbowcloud.blowout.scheduler.core.model.Task;
import org.fogbowcloud.blowout.scheduler.core.model.TaskImpl;
import org.fogbowcloud.blowout.scheduler.core.model.TaskProcess;
import org.fogbowcloud.blowout.scheduler.core.model.TaskProcessImpl;
import org.fogbowcloud.blowout.scheduler.infrastructure.InfrastructureManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import org.fogbowcloud.manager.occi.model.Token;

public class TestScheduler {

	private static final String JOB_ID3 = "jodId3";

	private static final String JOB_ID2 = "jodId2";

	private static final String JOB_ID1 = "jodId1";

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private Scheduler scheduler;
	private Job jobMock;
	private Job jobMock2;
	private Job jobMock3;
	private InfrastructureManager infraManagerMock;
	private CurrentThreadExecutorService executorService;

	@Before
	public void setUp() throws Exception {

		executorService = new CurrentThreadExecutorService();
		jobMock = mock(Job.class);
		jobMock2 = mock(Job.class);
		jobMock3 = mock(Job.class);
		doReturn("uuid").when(jobMock).getUUID();
		doReturn("uuid").when(jobMock2).getUUID();
		doReturn("uuid").when(jobMock3).getUUID();
		infraManagerMock = mock(InfrastructureManager.class);
		Token token = mock(Token.class);
		doReturn(token).when(infraManagerMock).getToken();
		doReturn(new Token.User("9999", "User")).when(token).getUser();
		scheduler = spy(new Scheduler(infraManagerMock, executorService, jobMock, jobMock2));

	}

	@After
	public void setDown() throws Exception {

		jobMock = null;
		jobMock2 = null;
		infraManagerMock = null;
		scheduler = null;

	}

	@Test
	public void runTest() {

		int qty = 5;

		Specification spec = new Specification("image", "username", "publicKey", "privateKeyFilePath", "userDataFile",
				"userDataType");
		Map<String, Task> tasks = this.generateMockTasks(qty, spec);
		Map<String, Task> tasks2 = this.generateMockTasks(qty, spec);
		doReturn(tasks).when(jobMock).getTasks();
		doReturn(tasks2).when(jobMock2).getTasks();
		scheduler.run();
		verify(infraManagerMock).orderResource(Mockito.eq(spec), Mockito.eq(scheduler), Mockito.anyInt());
	}

	@Test
	public void resourceReadyWithMatchTask() {

		int qty = 3;

		Resource resourceMock = mock(Resource.class);

		Specification specA = new Specification("image", "username", "publicKey", "privateKeyFilePath", "userDataFile",
				"userDataType");
		Specification specB = new Specification("image", "username", "publicKey", "privateKeyFilePath", "userDataFile",
				"userDataType");
		Map<String, Task> tasks = this.generateMockTasks(qty, specA);

		Task tMatch = tasks.get(tasks.keySet().iterator().next());

		doReturn(tasks).when(jobMock).getTasks();
		doReturn(false).when(jobMock).isCreated();
		scheduler.run();
		doReturn(true).when(jobMock).isCreated();
		doReturn(specB).when(tMatch).getSpecification();
		doReturn("resource01").when(resourceMock).getId();
		doReturn(true).when(resourceMock).match(specB);

		scheduler.resourceReady(resourceMock);

		verify(resourceMock, times(1)).match(specB);
		assertEquals(1, scheduler.getRunningTasks().size());
	}

	@Test
	public void resourceReadyWithoutMatchTask() {

		int qty = 3;

		Resource resourceMock = mock(Resource.class);

		Specification specA = new Specification("image", "username", "publicKey", "privateKeyFilePath", "userDataFile",
				"userDataType");
		Specification specB = new Specification("image", "username", "publicKey", "privateKeyFilePath", "userDataFile",
				"userDataType");
		Map<String, Task> tasks = this.generateMockTasks(qty, specA);

		doReturn(tasks).when(jobMock).getTasks();
		doReturn("resource01").when(resourceMock).getId();
		doReturn(false).when(resourceMock).match(specB);

		doReturn(false).when(jobMock).isCreated();
		scheduler.run();
		doReturn(true).when(jobMock).isCreated();

		scheduler.resourceReady(resourceMock);

		verify(resourceMock, times(3)).match(specB);
		assertEquals(0, scheduler.getRunningTasks().size());
		verify(infraManagerMock, times(1)).releaseResource(resourceMock);

	}

	@Test
	public void taskFailed() {

		Task task = mock(Task.class);

		TaskProcess tp = mock(TaskProcess.class);
		doReturn(String.valueOf("task1")).when(tp).getTaskId();

		TaskProcess tClone = mock(TaskProcess.class);
		doReturn(String.valueOf("task1")).when(tClone).getTaskId();

		doReturn(tClone).when(scheduler).createTaskProcess(task, "uuid");

		Resource resourceMock = mock(Resource.class);
		scheduler.getRunningTasks().put(tp.getTaskId(), resourceMock);

		Map<String, Task> tasksOfJob1 = new HashMap<String, Task>();
		tasksOfJob1.put(task.getId(), task);

		doReturn(tasksOfJob1).when(jobMock).getTasks();
		scheduler.taskProcessFailed(tp);

		verify(infraManagerMock, times(1)).releaseResource(resourceMock);

		assertNull(scheduler.getRunningTasks().get(tp.getTaskId()));
	}

	@Test
	public void taskCompleted() {

		Task t = mock(Task.class);
		TaskProcess tp = mock(TaskProcess.class);
		doReturn(String.valueOf("task1")).when(t).getId();
		doReturn(String.valueOf("task1")).when(tp).getTaskId();

		Resource resourceMock = mock(Resource.class);
		scheduler.getRunningTasks().put(t.getId(), resourceMock);

		scheduler.taskCompleted(tp);

		verify(infraManagerMock, times(1)).releaseResource(resourceMock);

		assertNull(scheduler.getRunningTasks().get(t.getId()));
	}

	private Map<String, Task> generateMockTasks(int qty, Specification spec) {

		Map<String, Task> tasks = new HashMap<String, Task>();
		for (int count = 1; count <= qty; count++) {
			List<Command> commands = new ArrayList<Command>();
			Task t = mock(Task.class);
			doReturn("Task_0" + String.valueOf(count)).when(t).getId();
			doReturn(spec).when(t).getSpecification();
			doNothing().when(t).startedRunning();
			doReturn(commands).when(t).getAllCommands();
			tasks.put(t.getId(), t);
		}

		return tasks;

	}

	@Test
	public void testAddJob() {

		int qty = 5;

		Specification spec = new Specification("image", "username", "publicKey", "privateKeyFilePath", "userDataFile",
				"userDataType");
		Map<String, Task> tasks = this.generateMockTasks(qty, spec);
		Map<String, Task> tasks2 = this.generateMockTasks(qty, spec);
		doReturn(tasks).when(jobMock).getTasks();
		doReturn(tasks2).when(jobMock2).getTasks();
		doNothing().when(jobMock).setCreated();
		doNothing().when(jobMock2).setCreated();
		doNothing().when(jobMock3).setCreated();
		doReturn(false).when(jobMock).isCreated();
		doReturn(false).when(jobMock2).isCreated();
		scheduler.run();
		doReturn(true).when(jobMock).isCreated();
		doReturn(true).when(jobMock2).isCreated();
		verify(infraManagerMock).orderResource(Mockito.eq(spec), Mockito.eq(scheduler), Mockito.anyInt());
		doReturn(new HashMap<String, Task>()).when(jobMock3).getTasks();
		doReturn(false).when(jobMock3).isCreated();
		scheduler.addJob(jobMock3);
		doReturn(true).when(jobMock).isCreated();
		scheduler.run();
		verify(jobMock3).getTasks();
		verify(jobMock3).setCreated();
	}

	@Test
	public void testRemoveJob() {

		int qty = 5;

		Specification spec = new Specification("image", "username", "publicKey", "privateKeyFilePath", "userDataFile",
				"userDataType");
		Map<String, Task> tasks = this.generateMockTasks(qty, spec);
		Map<String, Task> tasks2 = this.generateMockTasks(qty, spec);
		doReturn(tasks).when(jobMock).getTasks();
		doReturn(tasks2).when(jobMock2).getTasks();
		doReturn(JOB_ID1).when(jobMock).getId();
		doReturn(JOB_ID2).when(jobMock2).getId();
		doReturn(JOB_ID3).when(jobMock3).getId();
		doNothing().when(jobMock).setCreated();
		doNothing().when(jobMock2).setCreated();
		doNothing().when(jobMock3).setCreated();
		doReturn(false).when(jobMock).isCreated();
		doReturn(false).when(jobMock2).isCreated();
		scheduler.run();
		doReturn(true).when(jobMock).isCreated();
		doReturn(true).when(jobMock2).isCreated();
		verify(infraManagerMock).orderResource(Mockito.eq(spec), Mockito.eq(scheduler), Mockito.anyInt());
		doReturn(new HashMap<String, Task>()).when(jobMock3).getTasks();
		scheduler.addJob(jobMock3);
		doReturn(false).when(jobMock3).isCreated();
		scheduler.run();
		doReturn(true).when(jobMock3).isCreated();
		verify(jobMock3).getTasks();
		scheduler.removeJob(JOB_ID1);
		assertEquals(2, scheduler.getJobs().size());
		assertTrue(scheduler.getJobs().contains(jobMock2));
		assertTrue(scheduler.getJobs().contains(jobMock3));
		assertFalse(scheduler.getJobs().contains(jobMock));

	}

	@Test
	public void testFailTaskWithMultipleJobs() {

		// setup
		int qty = 5;

		Specification spec = new Specification("image", "username", "publicKey", "privateKeyFilePath", "userDataFile",
				"userDataType");

		// We have three jobs. job 1 and 2 are added in the setUp method
		Map<String, Task> tasksOfJob1 = this.generateMockTasks(qty, spec);
		Map<String, Task> tasksOfJob2 = this.generateMockTasks(qty, spec);
		Map<String, Task> tasksOfJob3 = new HashMap<String, Task>();

		// jobs 1 and 2 have 5 failed tasks each
		doReturn(tasksOfJob1).when(jobMock).getTasks();
		doReturn(tasksOfJob2).when(jobMock2).getTasks();

		// job 3 has no ready tasks
		doReturn(new HashMap<String, Task>()).when(jobMock3).getTasks();

		// add the job 3. it is going to have one failed task
		scheduler.addJob(jobMock3);
		doReturn(false).when(jobMock3).isCreated();

		Task task = new TaskImpl("fakeTaskId", spec);
		tasksOfJob3.put("fakeTaskId", task);
		TaskProcess tp = mock(TaskProcess.class);
		doReturn("fakeTaskId").when(tp).getTaskId();
		doReturn(tp).when(scheduler).createTaskProcess(task, "uuid");
		doReturn(tasksOfJob3).when(jobMock3).getTasks();

		Resource resourceMock = mock(Resource.class);
		scheduler.getRunningTasks().put(task.getId(), resourceMock);

		scheduler.run();
		doReturn(true).when(jobMock3).isCreated();
		// test
		scheduler.taskProcessFailed(tp);

		// verify - We have to notify job3 to recover from task failure, and
		// only job3
		verify(infraManagerMock, times(1)).releaseResource(resourceMock);

	}

	@Test
	public void testInferTaskState() {
		TaskProcess fakeTp1 = mock(TaskProcess.class);
		TaskProcess fakeTp2 = mock(TaskProcess.class);
		TaskProcess fakeTp3 = mock(TaskProcess.class);

		doReturn(TaskProcessImpl.State.FAILED).when(fakeTp1).getStatus();
		doReturn(TaskProcessImpl.State.FAILED).when(fakeTp2).getStatus();
		doReturn(TaskProcessImpl.State.READY).when(fakeTp3).getStatus();

		List<TaskProcess> tpList = new ArrayList<TaskProcess>();
		tpList.add(fakeTp1);
		tpList.add(fakeTp2);
		tpList.add(fakeTp3);

		TaskImpl fakeTask = mock(TaskImpl.class);
		
		doReturn(tpList).when(scheduler).getProcessFromTask(fakeTask);
		
		TaskState state = scheduler.inferTaskState(fakeTask);
		
		assertEquals(TaskState.READY, state);
	}
	
	@Test
	public void testInferTaskState2() {
		TaskProcess fakeTp1 = mock(TaskProcess.class);
		TaskProcess fakeTp2 = mock(TaskProcess.class);
		TaskProcess fakeTp3 = mock(TaskProcess.class);

		doReturn(TaskProcessImpl.State.FAILED).when(fakeTp1).getStatus();
		doReturn(TaskProcessImpl.State.READY).when(fakeTp2).getStatus();
		doReturn(TaskProcessImpl.State.FAILED).when(fakeTp3).getStatus();

		List<TaskProcess> tpList = new ArrayList<TaskProcess>();
		tpList.add(fakeTp1);
		tpList.add(fakeTp2);
		tpList.add(fakeTp3);

		TaskImpl fakeTask = mock(TaskImpl.class);
		
		doReturn(tpList).when(scheduler).getProcessFromTask(fakeTask);
		
		TaskState state = scheduler.inferTaskState(fakeTask);
		
		assertEquals(TaskState.READY, state);
	}
	
	@Test
	public void testInferTaskState3() {
		TaskProcess fakeTp1 = mock(TaskProcess.class);
		TaskProcess fakeTp2 = mock(TaskProcess.class);
		TaskProcess fakeTp3 = mock(TaskProcess.class);

		doReturn(TaskProcessImpl.State.FAILED).when(fakeTp1).getStatus();
		doReturn(TaskProcessImpl.State.FINNISHED).when(fakeTp2).getStatus();
		doReturn(TaskProcessImpl.State.READY).when(fakeTp3).getStatus();

		List<TaskProcess> tpList = new ArrayList<TaskProcess>();
		tpList.add(fakeTp1);
		tpList.add(fakeTp2);
		tpList.add(fakeTp3);

		TaskImpl fakeTask = mock(TaskImpl.class);
		
		doReturn(tpList).when(scheduler).getProcessFromTask(fakeTask);
		
		TaskState state = scheduler.inferTaskState(fakeTask);
		
		assertEquals(TaskState.COMPLETED, state);
	}
	
	@Test
	public void testInferTaskState4() {

		List<TaskProcess> tpList = new ArrayList<TaskProcess>();

		TaskImpl fakeTask = mock(TaskImpl.class);
		
		doReturn(tpList).when(scheduler).getProcessFromTask(fakeTask);
		
		TaskState state = scheduler.inferTaskState(fakeTask);
		
		assertEquals(TaskState.NOT_CREATED, state);
	}
}
