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
			actOnResource(resource);
		}

		for (Task task : tasks) {
			solveTask(task);
		}
		
		for (Task task : runningTasks.keySet()) {
			if (!tasks.contains(task)) {
				stopTask(task);
			}
		}

		return false;
	}

	private void actOnResource(AbstractResource resource) {
		AbstractResource.ResourceState state = resource.getState();
		
	}

	private void stopTask(Task task) {
				//TODO: Find out how to stop the execution of the process
	}

	private void solveTask(Task task) {
		TaskState currentState = task.getState();
	}

	private void runTask(Task task, AbstractResource resource) {
		TaskProcess tp = createProcess(task);
		runningTasks.put(task, tp);
		tp.executeTask(resource);
	}

	public TaskProcess createProcess(Task task) {
		TaskProcess tp = new TaskProcessImpl(task.getId(), task.getAllCommands(), task.getSpecification());
		return tp;
	}
	
	@SuppressWarnings("unchecked")
	public List<Task> getRunningTasks() {
		return (List<Task>) runningTasks.keySet();
	}

	@Override
	public void run(Task task) {

	}

	@Override
	public void stop(Task task) {

	}

}
