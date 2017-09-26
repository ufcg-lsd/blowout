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
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;
import org.fogbowcloud.blowout.pool.AbstractResource;
import org.fogbowcloud.blowout.pool.BlowoutPool;

public class TaskMonitor implements Runnable {

	private ExecutorService taskExecutor;
	private Thread monitoringServiceRunner;
	private BlowoutPool pool;
	private long timeout;
	private boolean active;

	Map<Task, TaskProcess> runningTasks = new HashMap<Task, TaskProcess>();

	public TaskMonitor(BlowoutPool pool, long timeout) {
		this.pool = pool;
		this.timeout = timeout;
		this.taskExecutor = Executors.newCachedThreadPool();
		this.active = false;
	}

	public void start() {
		this.active = true;
		this.monitoringServiceRunner = new Thread(this);
		this.monitoringServiceRunner.start();
	}

	public void stop() {
		this.active = false;
		this.taskExecutor.shutdownNow();
		this.monitoringServiceRunner.interrupt();
	}

	@Override
	public void run() {
		while (this.active) {
			this.procMon();
			try {
				Thread.sleep(this.timeout);
			} catch (InterruptedException e) {
				
			}
		}
	}

	public void procMon() {
		for (TaskProcess taskProcess : this.getRunningProcesses()) {
			TaskState taskProcessState = taskProcess.getStatus();

			if (taskProcessState.equals(TaskState.FAILED)) {
				this.removeRunningTask(this.getTaskById(taskProcess.getTaskId()));

				AbstractResource resource = taskProcess.getResource();
				if (resource != null) {
					this.updateResource(resource, ResourceState.FAILED);
				}
			}
			if (taskProcessState.equals(TaskState.FINNISHED)) {
				Task task = this.getTaskById(taskProcess.getTaskId());
				task.finish();
				this.removeRunningTask(task);

				AbstractResource resource = taskProcess.getResource();
				if (resource != null) {
					this.updateResource(resource, ResourceState.IDLE);
				}
			}
		}
	}

	// FIXME: observation.
	public void runTask(Task task, final AbstractResource resource) {
		if (this.runningTaskContains(task) == false) {

			final TaskProcess taskProcess = this.createProcess(task);

			this.putTaskToRunningTasks(task, taskProcess);
			task.startedRunning();

			this.updateResource(resource, ResourceState.BUSY);

			this.taskExecutor.submit(new Runnable() {
				@Override
				public void run() {
					taskProcess.executeTask(resource);
				}
			});
		}
	}

	protected TaskProcess createProcess(Task task) {
		TaskProcess taskProcess = new TaskProcessImpl(task.getId(), task.getAllCommands(),
				task.getSpecification(), task.getUUID());
		return taskProcess;
	}

	public void stopTask(Task task) {
		TaskProcess processToHalt = getRunningTasks().get(task);
		if (processToHalt != null) {
			if (processToHalt.getResource() != null) {
				this.updateResource(processToHalt.getResource(), ResourceState.IDLE);
			}
		}
	}

	public TaskState getTaskState(Task task) {
		TaskProcess taskProcess = this.getTaskProcess(task);
		if (taskProcess == null) {
			if (task.isFinished()) {
				return TaskState.COMPLETED;
			}
			return TaskState.READY;
		}
		return taskProcess.getStatus();
	}

	public Task getTaskById(String taskId) {
		for (Task task : this.runningTasks.keySet()) {
			if (task.getId().equals(taskId)) {
				return task;
			}
		}
		return null;
	}

	public List<TaskProcess> getRunningProcesses() {
		List<TaskProcess> processes = new ArrayList<TaskProcess>();
		processes.addAll(this.runningTasks.values());
		return processes;
	}

	public boolean runningTaskContains(Task task) {
		return this.runningTasks.containsKey(task);
	}

	public Map<Task, TaskProcess> getRunningTasks() {
		return this.runningTasks;
	}

	public ExecutorService getExecutorService() {
		return this.taskExecutor;
	}

	protected void setRunningTasks(Map<Task, TaskProcess> runningTasks) {
		this.runningTasks = runningTasks;
	}

	private void removeRunningTask(Task task) {
		this.runningTasks.remove(task);
	}

	private void putTaskToRunningTasks(Task task, TaskProcess taskProcess) {
		this.runningTasks.put(task, taskProcess);
	}

	private void updateResource(AbstractResource resource, ResourceState resourceState) {
		this.pool.updateResource(resource, resourceState);
	}

	private TaskProcess getTaskProcess(Task task) {
		return this.runningTasks.get(task);
	}
}
