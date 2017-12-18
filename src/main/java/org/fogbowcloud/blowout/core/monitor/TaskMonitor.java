package org.fogbowcloud.blowout.core.monitor;

import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskProcess;
import org.fogbowcloud.blowout.core.model.TaskProcessImpl;
import org.fogbowcloud.blowout.core.model.TaskState;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;
import org.fogbowcloud.blowout.pool.AbstractResource;
import org.fogbowcloud.blowout.pool.BlowoutPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskMonitor implements Runnable {

	private ExecutorService taskExecutor;
	private Thread monitoringServiceRunner;
	private BlowoutPool pool;
	private long timeout;
	private boolean active;

	private Map<Task, TaskProcess> runningTasks = new HashMap<>();

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
		while (active) {
			procMon();
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				// do nothing
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
        if (!this.runningTaskContains(task)) {

            final TaskProcess taskProcess = this.createProcess(task);

            this.putTaskToRunningTasks(task, taskProcess);
            task.startedRunning();

            this.updateResource(resource, ResourceState.BUSY);

            this.taskExecutor.submit(
                    new Runnable() {
                        @Override
                        public void run() {
                            taskProcess.executeTask(resource);
                        }
                    }
            );
        }
    }

	private TaskProcess createProcess(Task task) {
        return new TaskProcessImpl(task.getId(), task.getAllCommands(), task.getSpecification(), task.getUUID());
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
        return new ArrayList<>(this.runningTasks.values());
	}

	private boolean runningTaskContains(Task task) {
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
