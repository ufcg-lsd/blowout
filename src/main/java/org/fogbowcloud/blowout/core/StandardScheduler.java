package org.fogbowcloud.blowout.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskProcess;
import org.fogbowcloud.blowout.core.model.TaskProcessImpl;
import org.fogbowcloud.blowout.core.model.TaskState;
import org.fogbowcloud.blowout.infrastructure.model.AbstractResource;

public class StandardScheduler implements SchedulerInterface {

	Map<Task, TaskProcess> runningTasks = new HashMap<Task, TaskProcess>();

	public StandardScheduler() {
	}

	@Override
	public boolean act(List<Task> tasks, List<AbstractResource> resources) {

		for (AbstractResource resource : resources) {
			actOnResource(resource, tasks);
		}

		for (Task task : tasks) {
			actOnTask(task, resources);
		}

		for (Task runningTask : runningTasks.keySet()) {
			if (!tasks.contains(runningTask)) {
				stopTask(runningTask);
			}
		}

		return false;
	}

	protected void actOnResource(AbstractResource resource, List<Task> tasks) {
		AbstractResource.ResourceState state = resource.getState();
		// if resource idle
		if (true) {
			Task task = chooseTaskForRunning(tasks);
			if (task != null) {
				runTask(task, resource);
			}
		}

	}

	protected Task getTaskRunningOnResource(AbstractResource resource) {
		for (TaskProcess tp : runningTasks.values()) {
			if (tp.getResource() != null && tp.getResource().equals(resource)) {
				for (Task task : runningTasks.keySet()) {
					if (runningTasks.get(task).equals(tp)) {
						return task;
					}
				}
			}
		}
		return null;
	}

	protected Task chooseTaskForRunning(List<Task> tasks) {
		for (Task task : tasks) {
			if (task.getState().equals(TaskState.READY)) {
				return task;
			}
		}
		return null;
	}

	@Override
	public void stopTask(Task task) {
		// TODO: Find out how to stop the execution of the process
		runningTasks.remove(task);
	}

	protected void actOnTask(Task task, List<AbstractResource> resources) {
		TaskState currentState = task.getState();
	}

	@Override
	public void runTask(Task task, AbstractResource resource) {
		TaskProcess tp = createProcess(task);
		runningTasks.put(task, tp);
		// submit to task executor
		submitToMonitor(tp, resource);
		task.setState(TaskState.RUNNING);

	}

	public void submitToMonitor(TaskProcess tp, AbstractResource resource) {

	}

	protected TaskProcess createProcess(Task task) {
		TaskProcess tp = new TaskProcessImpl(task.getId(), task.getAllCommands(), task.getSpecification());
		return tp;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Task> getRunningTasks() {
		return (List<Task>) runningTasks.keySet();
	}
}
