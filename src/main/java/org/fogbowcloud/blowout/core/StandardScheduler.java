package org.fogbowcloud.blowout.core;

import java.util.ArrayList;
import java.util.List;

import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskProcess;
import org.fogbowcloud.blowout.core.model.TaskProcessImpl;
import org.fogbowcloud.blowout.core.model.TaskState;
import org.fogbowcloud.blowout.infrastructure.model.AbstractResource;

public class StandardScheduler implements SchedulerInterface {

	List<TaskProcess> processBuffer;
	
	public StandardScheduler() {
		processBuffer = new ArrayList<TaskProcess>();
	}
	
	@Override
	public boolean Act(List<Task> tasks, List<AbstractResource> resources) {
	
		for (AbstractResource resource : resources) {
			
		}
		
		for (Task task : tasks) {
			solveTask(task);
		}
		
		return false;
	}
	
	private void solveTask(Task task) {
		TaskState currentState = task.getState();
		if (currentState.equals(TaskState.NOT_CREATED)) {
			TaskProcess newProcess = createProcess(task);
			processBuffer.add(newProcess);
		}
		
	}
	
	private TaskProcess getProcessOfTask(Task task) {
		for (TaskProcess tp : processBuffer) {
			if (tp.getTaskId().equals(task.getId())) {
				return tp;
			}
		}
		return null;
	}

	public TaskProcess createProcess(Task task) {
		TaskProcess tp = new TaskProcessImpl(task.getId(), task.getAllCommands(), task.getSpecification());
		return tp;
	}

	@Override
	public void run(Task task) {

	}

	@Override
	public void stop(Task task) {
		

	}

}
