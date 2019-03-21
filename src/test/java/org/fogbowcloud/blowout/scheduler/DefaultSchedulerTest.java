package org.fogbowcloud.blowout.scheduler;

import static org.fogbowcloud.blowout.helpers.Constants.*;
import org.fogbowcloud.blowout.helpers.Constants.*;

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
	private TaskImpl taskA;
	private TaskImpl taskB;
	private TaskImpl taskC;
	private TaskImpl taskD;

	@Before
	public void setUp() {
		this.specA = new Specification(FakeData.CLOUD_NAME, FakeData.COMPUTE_IMAGE_FLAVOR_NAME, FakeData.FOGBOW_USER_NAME,
				FakeData.PUBLIC_KEY, FakeData.PRIVATE_KEY_FILE_PATH);
		this.specB = new Specification(FakeData.CLOUD_NAME+POSTFIX_B,
				FakeData.COMPUTE_IMAGE_FLAVOR_NAME +POSTFIX_B,
				FakeData.FOGBOW_USER_NAME+POSTFIX_B, FakeData.PUBLIC_KEY+POSTFIX_B,
				FakeData.PRIVATE_KEY_FILE_PATH+POSTFIX_B);
		this.taskA = new TaskImpl(FakeData.UUID, specA, FakeData.UUID);
		this.taskB = new TaskImpl(FakeData.UUID+POSTFIX_B, specA, FakeData.UUID+POSTFIX_B);
		this.taskC = new TaskImpl(FakeData.UUID+POSTFIX_C, specB, FakeData.UUID+POSTFIX_C);
		this.taskD = new TaskImpl(FakeData.UUID+POSTFIX_D, specA, FakeData.UUID+POSTFIX_D);
	}

	@Test
	public void testChooseTaskForRunningNotRunningAndSameSpecification() {
		TaskMonitor taskMon = Mockito.mock(TaskMonitor.class);
		DefaultScheduler defaultScheduler = new DefaultScheduler(taskMon);
		
		List<Task> tasks = new ArrayList<>();

		TaskImpl taskExpected = this.taskC;

		tasks.add(this.taskA);
		tasks.add(this.taskB);
		tasks.add(taskExpected);
		tasks.add(this.taskD);
		
		AbstractResource resourceWithSpecB = new FogbowResource(FakeData.RESOURCE_ID, FakeData.ORDER_ID, specB);
		
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
		TaskImpl taskExpected = new TaskImpl("taskFour", specB, FakeData.UUID);
		TaskImpl taskTwoRunning = new TaskImpl("taskTwo", specB, FakeData.UUID);
		TaskImpl taskThreeRunning = new TaskImpl("taskThree", specB, FakeData.UUID);
		TaskImpl taskFiveRunning = new TaskImpl("taskFive", specB, FakeData.UUID);
		tasks.add(new TaskImpl("taskOne", specA, FakeData.UUID));
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
		Specification specA = new Specification(FakeData.CLOUD_NAME, FakeData.COMPUTE_IMAGE_FLAVOR_NAME,
				FakeData.FOGBOW_USER_NAME, FakeData.PUBLIC_KEY, FakeData.PRIVATE_KEY_FILE_PATH);
		Specification specB = new Specification("imageB", "usernameB", "publicKeyB", "privateKeyFilePathB");
		TaskImpl taskToRunning = new TaskImpl("taskFour", specB, FakeData.UUID);
		TaskImpl taskTwoRunning = new TaskImpl("taskTwo", specB, FakeData.UUID);
		TaskImpl taskThreeRunning = new TaskImpl("taskThree", specB, FakeData.UUID);
		TaskImpl taskFiveRunning = new TaskImpl("taskFive", specB, FakeData.UUID);
		tasks.add(new TaskImpl("taskOne", specA, FakeData.UUID));
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
		TaskImpl taskToRunning = new TaskImpl("taskFour", specB, FakeData.UUID);
		TaskImpl taskTwoRunning = new TaskImpl("taskTwo", specB, FakeData.UUID);
		TaskImpl taskThreeRunning = new TaskImpl("taskThree", specB, FakeData.UUID);
		TaskImpl taskFiveRunning = new TaskImpl("taskFive", specB, FakeData.UUID);
		taskToRunning.setRetries(2);
		tasks.add(new TaskImpl("taskOne", specA, FakeData.UUID));
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
