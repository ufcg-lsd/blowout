package org.fogbowcloud.blowout.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskProcess;
import org.fogbowcloud.blowout.core.model.TaskProcessImpl;
import org.fogbowcloud.blowout.core.monitor.TaskMonitor;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;
import org.fogbowcloud.blowout.pool.AbstractResource;

public class StandardScheduler implements SchedulerInterface {

	private Map<AbstractResource, Task> runningTasks = new HashMap<AbstractResource, Task>();
	private TaskMonitor taskMon;

	public StandardScheduler(TaskMonitor taskMon) {
		this.taskMon = taskMon;
	}

	@Override
	public void act(List<Task> tasks, List<AbstractResource> resources) {
		for (AbstractResource resource : resources) {
			actOnResource(resource, tasks);
		}
		for (Task runningTask : runningTasks.values()) {
			if (!tasks.contains(runningTask)) {
				stopTask(runningTask);
			}
		}
		for (AbstractResource inUse : runningTasks.keySet()) {
			if (!resources.contains(inUse)) {
				stopTask(runningTasks.get(inUse));
			}
		}
	}

	protected void actOnResource(AbstractResource resource, List<Task> tasks) {
		// if resource idle
		if (resource.getState().equals(ResourceState.IDLE)) {
			Task task = chooseTaskForRunning(resource, tasks);
			if (task != null) {
				runTask(task, resource);
			}
		}
		// if resource is to be removed
		if (resource.getState().equals(ResourceState.TO_REMOVE)) {
			runningTasks.remove(resource);
		}

	}

	protected Task chooseTaskForRunning(AbstractResource resource, List<Task> tasks) {
		for (Task task : tasks) {
			boolean isSameSpecification = resource.getRequestedSpec().equals(task.getSpecification());
			if (!task.isFinished() && !runningTasks.containsValue(task) && isSameSpecification) {
				return task;
			}
		}
		return null;
	}

	@Override
	public void stopTask(Task task) {
		// TODO: Find out how to stop the execution of the process
		for (AbstractResource resource : runningTasks.keySet()) {
			if (runningTasks.get(resource).equals(task)) {
				this.taskMon.stopTask(task);
				runningTasks.remove(resource);
			}
		}
	}

	@Override
	public void runTask(Task task, AbstractResource resource) {

		runningTasks.put(resource, task);
		// submit to task executor
		// resource.setState(ResourceState.NOT_READY);
		submitToMonitor(task, resource);

	}

	public void submitToMonitor(Task task, AbstractResource resource) {
		taskMon.runTask(task, resource);
	}

	protected TaskProcess createProcess(Task task) {
		TaskProcess tp = new TaskProcessImpl(task.getId(), task.getAllCommands(), task.getSpecification());
		return tp;
	}

	@Override
	public List<Task> getRunningTasks() {
		return new ArrayList<Task>(runningTasks.values());
	}
	
	protected void setRunningTasks(Map<AbstractResource, Task> runningTasks) {
		this.runningTasks = runningTasks;
	}
}
