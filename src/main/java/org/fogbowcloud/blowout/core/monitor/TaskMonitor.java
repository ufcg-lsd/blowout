package org.fogbowcloud.blowout.core.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskProcess;
import org.fogbowcloud.blowout.core.model.TaskProcessImpl;
import org.fogbowcloud.blowout.core.model.TaskState;

public class TaskMonitor {

	Map<Task, TaskProcess> runningProcesses = new HashMap<Task, TaskProcess>();

	public void monitorTasks(List<Task> tasks) {
		for (Task task : tasks) {
			if (runningProcesses.get(task) == null) {
				runningProcesses.put(task, createProcess(task));
			}
		}
	}

	public void procMon() {
		List<TaskProcess> processes = new ArrayList<TaskProcess>();
		processes.addAll(runningProcesses.values());
		for (TaskProcess tp : processes) {
			if (tp.getStatus().equals(TaskState.FAILED)) {
				runningProcesses.remove(getTaskById(tp.getTaskId()));
			}
			if (tp.getStatus().equals(TaskState.READY)) {
				//submit task
			}
		}
	}

	public Task getTaskById(String taskId) {
		for (Task task : runningProcesses.keySet()) {
			if (task.getId().equals(taskId)) {
				return task;
			}
		}
		return null;
	}
	
	protected TaskProcess createProcess(Task task) {
		TaskProcess tp = new TaskProcessImpl(task.getId(), task.getAllCommands(), task.getSpecification());
		return tp;
	}

}
