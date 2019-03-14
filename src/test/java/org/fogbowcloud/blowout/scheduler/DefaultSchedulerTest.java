package org.fogbowcloud.blowout.scheduler;

import static org.fogbowcloud.blowout.constants.TestConstants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.model.task.Task;
import org.fogbowcloud.blowout.core.model.task.TaskImpl;
import org.fogbowcloud.blowout.core.monitor.TaskMonitor;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.core.model.resource.ResourceState;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DefaultSchedulerTest {
	private Specification specA;
	private Specification specB;
	private Task taskA;
	private Task taskB;
	private Task taskC;
	private Task taskD;

	@Before
	public void setUp() {
		this.specA = new Specification(FAKE_CLOUD_NAME, FAKE_IMAGE_ID,
				FAKE_FOGBOW_USER_NAME, FAKE_PUBLIC_KEY, FAKE_PRIVATE_KEY_FILE_PATH);
		this.specB = new Specification(FAKE_CLOUD_NAME+POSTFIX_B,FAKE_IMAGE_ID+POSTFIX_B,
				FAKE_FOGBOW_USER_NAME+POSTFIX_B, FAKE_PUBLIC_KEY+POSTFIX_B, FAKE_PRIVATE_KEY_FILE_PATH+POSTFIX_B);
		this.taskA = new TaskImpl(FAKE_UUID, specA, FAKE_UUID);
		this.taskB = new TaskImpl(FAKE_UUID+POSTFIX_B, specA, FAKE_UUID+POSTFIX_B);
		this.taskC = new TaskImpl(FAKE_UUID+POSTFIX_C, specA, FAKE_UUID+POSTFIX_C);
		this.taskD = new TaskImpl(FAKE_UUID+POSTFIX_D, specA, FAKE_UUID+POSTFIX_D);
	}

	@Test
	public void testChooseTaskForRunningNotRunningAndSameSpecification() {
		TaskMonitor taskMon = Mockito.mock(TaskMonitor.class);
		DefaultScheduler defaultScheduler = new DefaultScheduler(taskMon);
		
		List<Task> tasks = new ArrayList<>();

		TaskImpl taskExpected = (TaskImpl) this.taskC;
		tasks.add(this.taskA);
		tasks.add(this.taskB);
		tasks.add(taskExpected);
		tasks.add(this.taskD);
		
		AbstractResource resourceWithSpecB = new FogbowResource(FAKE_RESOURCE_ID, FAKE_ORDER_ID, specB);
		
		Task chooseTaskForRunning = defaultScheduler.chooseTaskForRunning(resourceWithSpecB, tasks);
		Assert.assertEquals(taskExpected, chooseTaskForRunning);
	}
	
	@Test
	public void testChooseTaskForRunningWithSameSpecification() {
		TaskMonitor taskMon = Mockito.mock(TaskMonitor.class);
		DefaultScheduler defaultScheduler = new DefaultScheduler(taskMon);
		
		List<Task> tasks = new ArrayList<>();
		Specification specA = this.specA;
		Specification specB = this.specB;
		TaskImpl taskExpected = new TaskImpl("taskFour", specB, FAKE_UUID);
		TaskImpl taskTwoRunning = new TaskImpl("taskTwo", specB, FAKE_UUID);
		TaskImpl taskThreeRunning = new TaskImpl("taskThree", specB, FAKE_UUID);
		TaskImpl taskFiveRunning = new TaskImpl("taskFive", specB, FAKE_UUID);
		tasks.add(new TaskImpl("taskOne", specA, FAKE_UUID));
		tasks.add(taskTwoRunning);
		tasks.add(taskThreeRunning);
		tasks.add(taskExpected);
		tasks.add(taskFiveRunning);
		
		AbstractResource resourceTwoSpecB = new FogbowResource("idTwo", "orderIdTwo", specB);
		AbstractResource resourceThreeSpecB = new FogbowResource("idThree", "orderIdThree", specB);
		AbstractResource resourceFourSpecB = new FogbowResource("idFour", "orderIdFour", specB);
		AbstractResource resourceFiveSpecB = new FogbowResource("idFive", "orderIdFIve", specB);
		Map<AbstractResource, Task> runningTasks = new HashMap<AbstractResource, Task>();
		runningTasks.put(resourceTwoSpecB, taskTwoRunning);
		runningTasks.put(resourceThreeSpecB, taskThreeRunning);
		runningTasks.put(resourceFiveSpecB, taskFiveRunning);
		defaultScheduler.setRunningTasks(runningTasks);
		
		Task choosenTaskForRunning = defaultScheduler.chooseTaskForRunning(resourceFourSpecB, tasks);
		Assert.assertEquals(taskExpected, choosenTaskForRunning);
	}	
	
	@Test
	public void testAct() {
		TaskMonitor taskMon = Mockito.mock(TaskMonitor.class);
		Mockito.doNothing().when(taskMon).runTask(Mockito.any(Task.class), Mockito.any(AbstractResource.class));
		DefaultScheduler defaultScheduler = new DefaultScheduler(taskMon);
		
		List<Task> tasks = new ArrayList<Task>();
		Specification specA = new Specification(FAKE_CLOUD_NAME, FAKE_IMAGE_ID,FAKE_FOGBOW_USER_NAME,
				FAKE_PUBLIC_KEY, FAKE_PRIVATE_KEY_FILE_PATH);
		Specification specB = new Specification("imageB", "usernameB", "publicKeyB", "privateKeyFilePathB");
		TaskImpl taskToRunning = new TaskImpl("taskFour", specB, FAKE_UUID);
		TaskImpl taskTwoRunning = new TaskImpl("taskTwo", specB, FAKE_UUID);
		TaskImpl taskThreeRunning = new TaskImpl("taskThree", specB, FAKE_UUID);
		TaskImpl taskFiveRunning = new TaskImpl("taskFive", specB, FAKE_UUID);
		tasks.add(new TaskImpl("taskOne", specA, FAKE_UUID));
		tasks.add(taskTwoRunning);
		tasks.add(taskThreeRunning);
		tasks.add(taskToRunning);
		tasks.add(taskFiveRunning);
		
		AbstractResource resourceTwoRunning = new FogbowResource("idTwo", "orderIdTwo", specB);
		resourceTwoRunning.setState(ResourceState.BUSY);
		AbstractResource resourceThreeRunning = new FogbowResource("idThree", "orderIdThree", specB);
		resourceThreeRunning.setState(ResourceState.BUSY);
		AbstractResource resourceFourIdle = new FogbowResource("idFour", "orderIdFour", specB);
		resourceFourIdle.setState(ResourceState.IDLE);
		AbstractResource resourceFiveRunning = new FogbowResource("idFive", "orderIdFIve", specB);
		resourceFiveRunning.setState(ResourceState.BUSY);
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		resources.add(resourceTwoRunning);
		resources.add(resourceThreeRunning);
		resources.add(resourceFourIdle);
		resources.add(resourceFiveRunning);
		
		defaultScheduler.runTask(taskTwoRunning, resourceTwoRunning);
		defaultScheduler.runTask(taskThreeRunning, resourceThreeRunning);
		defaultScheduler.runTask(taskFiveRunning, resourceFiveRunning);
		int countRunningTaskBefore = 3;
		
		Assert.assertEquals(countRunningTaskBefore, defaultScheduler.getRunningTasks().size());
		
		defaultScheduler.act(tasks, resources);
		
		int addedTaskToRunning = 1;
		Assert.assertEquals(countRunningTaskBefore + addedTaskToRunning, defaultScheduler.getRunningTasks().size());
	}

	@Test
	public void testActRetryTask(){
		TaskMonitor taskMon = Mockito.mock(TaskMonitor.class);
		Mockito.doNothing().when(taskMon).runTask(Mockito.any(Task.class), Mockito.any(AbstractResource.class));
		DefaultScheduler defaultScheduler = new DefaultScheduler(taskMon);
		
		List<Task> tasks = new ArrayList<Task>();
		Specification specA = new Specification("imageA", "usernameA", "publicKeyA", "privateKeyFilePathA");
		Specification specB = new Specification("imageB", "usernameB", "publicKeyB", "privateKeyFilePathB");
		TaskImpl taskToRunning = new TaskImpl("taskFour", specB, FAKE_UUID);
		TaskImpl taskTwoRunning = new TaskImpl("taskTwo", specB, FAKE_UUID);
		TaskImpl taskThreeRunning = new TaskImpl("taskThree", specB, FAKE_UUID);
		TaskImpl taskFiveRunning = new TaskImpl("taskFive", specB, FAKE_UUID);
		taskToRunning.setRetries(2);
		tasks.add(new TaskImpl("taskOne", specA, FAKE_UUID));
		tasks.add(taskTwoRunning);
		tasks.add(taskThreeRunning);
		tasks.add(taskToRunning);
		tasks.add(taskFiveRunning);
		
		AbstractResource resourceTwoRunning = new FogbowResource("idTwo", "orderIdTwo", specB);
		resourceTwoRunning.setState(ResourceState.BUSY);
		AbstractResource resourceThreeRunning = new FogbowResource("idThree", "orderIdThree", specB);
		resourceThreeRunning.setState(ResourceState.BUSY);
		AbstractResource resourceFourIdle = new FogbowResource("idFour", "orderIdFour", specB);
		resourceFourIdle.setState(ResourceState.IDLE);
		AbstractResource resourceFiveRunning = new FogbowResource("idFive", "orderIdFIve", specB);
		resourceFiveRunning.setState(ResourceState.BUSY);
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		resources.add(resourceTwoRunning);
		resources.add(resourceThreeRunning);
		resources.add(resourceFourIdle);
		resources.add(resourceFiveRunning);
		
		defaultScheduler.runTask(taskTwoRunning, resourceTwoRunning);
		defaultScheduler.runTask(taskThreeRunning, resourceThreeRunning);
		defaultScheduler.runTask(taskFiveRunning, resourceFiveRunning);
		int countRunningTaskBefore = 3;
		
		Assert.assertEquals(countRunningTaskBefore, defaultScheduler.getRunningTasks().size());
		
		defaultScheduler.act(tasks, resources);
		
		int addedTaskToRunning = 1;
		Assert.assertEquals(countRunningTaskBefore + addedTaskToRunning, defaultScheduler.getRunningTasks().size());
		Assert.assertEquals(3, taskToRunning.getRetries());
		Assert.assertEquals(0, taskTwoRunning.getRetries());
	}
	
}
