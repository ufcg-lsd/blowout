package org.fogbowcloud.blowout.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskImpl;
import org.fogbowcloud.blowout.core.monitor.TaskMonitor;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;
import org.fogbowcloud.blowout.pool.AbstractResource;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class StandardSchedulerTest {

	@Test
	public void testChooseTaskForRunningNotRunningAndSameSpecification() {
		TaskMonitor taskMon = Mockito.mock(TaskMonitor.class);
		StandardScheduler standardScheduler = new StandardScheduler(taskMon);
		
		List<Task> tasks = new ArrayList<Task>();
		Specification specA = new Specification("imageA", "usernameA", "publicKeyA", "privateKeyFilePathA");
		Specification specB = new Specification("imageB", "usernameB", "publicKeyB", "privateKeyFilePathB");
		TaskImpl taskExcepcted = new TaskImpl("taskThree", specB);
		tasks.add(new TaskImpl("taskOne", specA));
		tasks.add(new TaskImpl("taskTwo", specA));
		tasks.add(taskExcepcted);
		tasks.add(new TaskImpl("taskFour", specA));
		
		AbstractResource resourceWithSpecB = new FogbowResource("id", "orderId", specB);
		
		Task choosenTaskForRunning = standardScheduler.chooseTaskForRunning(resourceWithSpecB, tasks);
		Assert.assertEquals(taskExcepcted, choosenTaskForRunning);
	}
	
	@Test
	public void testChooseTaskForRunningWithSameSpecification() {
		TaskMonitor taskMon = Mockito.mock(TaskMonitor.class);
		StandardScheduler standardScheduler = new StandardScheduler(taskMon);
		
		List<Task> tasks = new ArrayList<Task>();
		Specification specA = new Specification("imageA", "usernameA", "publicKeyA", "privateKeyFilePathA");
		Specification specB = new Specification("imageB", "usernameB", "publicKeyB", "privateKeyFilePathB");
		TaskImpl taskExpected = new TaskImpl("taskFour", specB);
		TaskImpl taskTwoRunning = new TaskImpl("taskTwo", specB);
		TaskImpl taskThreeRunning = new TaskImpl("taskThree", specB);
		TaskImpl taskFiveRunning = new TaskImpl("taskFive", specB);
		tasks.add(new TaskImpl("taskOne", specA));
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
		standardScheduler.setRunningTasks(runningTasks);		
		
		Task choosenTaskForRunning = standardScheduler.chooseTaskForRunning(resourceFourSpecB, tasks);		
		Assert.assertEquals(taskExpected, choosenTaskForRunning);
	}	
	
	@Test
	public void testAct() {
		TaskMonitor taskMon = Mockito.mock(TaskMonitor.class);
		Mockito.doNothing().when(taskMon).runTask(Mockito.any(Task.class), Mockito.any(AbstractResource.class));
		StandardScheduler standardScheduler = new StandardScheduler(taskMon);
		
		List<Task> tasks = new ArrayList<Task>();
		Specification specA = new Specification("imageA", "usernameA", "publicKeyA", "privateKeyFilePathA");
		Specification specB = new Specification("imageB", "usernameB", "publicKeyB", "privateKeyFilePathB");
		TaskImpl taskToRunning = new TaskImpl("taskFour", specB);
		TaskImpl taskTwoRunning = new TaskImpl("taskTwo", specB);
		TaskImpl taskThreeRunning = new TaskImpl("taskThree", specB);
		TaskImpl taskFiveRunning = new TaskImpl("taskFive", specB);
		tasks.add(new TaskImpl("taskOne", specA));
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
		
		standardScheduler.runTask(taskTwoRunning, resourceTwoRunning);
		standardScheduler.runTask(taskThreeRunning, resourceThreeRunning);
		standardScheduler.runTask(taskFiveRunning, resourceFiveRunning);
		int countRunningTaskBefore = 3;
		
		Assert.assertEquals(countRunningTaskBefore, standardScheduler.getRunningTasks().size());
		
		standardScheduler.act(tasks, resources);
		
		int addedTaskToRunning = 1;
		Assert.assertEquals(countRunningTaskBefore + addedTaskToRunning, standardScheduler.getRunningTasks().size());
	}
	
}
