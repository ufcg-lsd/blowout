package org.fogbowcloud.blowout.core.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskProcess;
import org.fogbowcloud.blowout.core.model.TaskProcessImpl;
import org.fogbowcloud.blowout.core.model.TaskState;
import org.fogbowcloud.blowout.core.util.ManagerTimer;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;
import org.fogbowcloud.blowout.pool.AbstractResource;
import org.fogbowcloud.blowout.pool.BlowoutPool;

public class TaskMonitor {

	Map<Task, TaskProcess> runningProcesses = new HashMap<Task, TaskProcess>();
	
	private ExecutorService taskExecutor = Executors.newCachedThreadPool();

	private static ManagerTimer executionMonitorTimer = new ManagerTimer(Executors.newScheduledThreadPool(1));
	
	private BlowoutPool pool;
	
	public TaskMonitor(BlowoutPool pool) {
		this.pool = pool;
	}
	
	public void start(){
		executionMonitorTimer.scheduleAtFixedRate(new Runnable() {
			
			@Override
			public void run() {
				procMon();
			}
		}, 0, 30000);
	}

	public void procMon() {
		List<TaskProcess> processes = new ArrayList<TaskProcess>();
		processes.addAll(runningProcesses.values());
		for (TaskProcess tp : processes) {
			if (tp.getStatus().equals(TaskState.FAILED)) {
				runningProcesses.remove(getTaskById(tp.getTaskId()));
				if (tp.getResource()!= null) {
					pool.putResource(tp.getResource(), ResourceState.FAILED);
				}
			}
			if (tp.getStatus().equals(TaskState.FINNISHED)) {
				runningProcesses.remove(getTaskById(tp.getTaskId()));
				if (tp.getResource()!= null) {
					pool.putResource(tp.getResource(), ResourceState.IDLE);
				}
			}
		}
	}
	
	public void runTask(Task task,final AbstractResource resource) {
		final TaskProcess tp = createProcess(task);
		if (runningProcesses.get(task) == null) {
			runningProcesses.put(task, tp);
		}
		taskExecutor.submit(new Runnable() {
			
			@Override
			public void run() {
				tp.executeTask(resource);
			}
		});
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
