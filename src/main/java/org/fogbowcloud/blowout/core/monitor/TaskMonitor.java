package org.fogbowcloud.blowout.core.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.model.task.Task;
import org.fogbowcloud.blowout.core.model.task.TaskProcess;
import org.fogbowcloud.blowout.core.model.task.TaskProcessImpl;
import org.fogbowcloud.blowout.core.model.task.TaskState;
import org.fogbowcloud.blowout.core.model.resource.ResourceState;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;
import org.fogbowcloud.blowout.pool.BlowoutPool;

public class TaskMonitor implements Runnable {

	private static final Logger LOGGER = Logger.getLogger(TaskMonitor.class);

	private Map<Task, TaskProcess> runningTasks;
	private ThreadPoolExecutor taskExecutor;
	private Thread monitoringServiceRunner;
	private BlowoutPool blowoutPool;
	private long timeout;
	private boolean isActive;

	public TaskMonitor(BlowoutPool blowoutPool, long timeout) {
		this.blowoutPool = blowoutPool;
		this.timeout = timeout;
		this.runningTasks = new HashMap<>();
		this.taskExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        this.isActive = false;
	}

	public void start() {
	    LOGGER.info("Starting Task Monitor");
		isActive = true;
		monitoringServiceRunner = new Thread(this, "task-monitor");
		monitoringServiceRunner.start();
	}

	public void stop() {
	    LOGGER.info("Stopping Task Monitor");
		isActive = false;
		this.taskExecutor.shutdown();
		monitoringServiceRunner.interrupt();
	}

	@Override
	public void run() {
	    LOGGER.info("Running Task Monitor");
		while(isActive){
			processMonitor();
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
                LOGGER.error("Task monitor was interrupted");
			}
		}
	}

	public void processMonitor() {
	    LOGGER.debug("Task Monitor process");
	    LOGGER.debug(listTaskProcess());
		for (TaskProcess taskProcess : getRunningProcesses()) {
		    TaskState taskProcessState = taskProcess.getTaskState();
            AbstractResource taskProcessResource = taskProcess.getResource();
			if (taskProcessState.equals(TaskState.FAILED)) {
			    removeRunningTask(getTaskById(taskProcess.getTaskId()));

				if (taskProcessResource != null) {
					blowoutPool.updateResource(taskProcessResource, ResourceState.FAILED);
				}
			}
			if (taskProcessState.equals(TaskState.FINISHED)) {
				Task task = getTaskById(taskProcess.getTaskId());
				task.finish();
                removeRunningTask(task);

				if (taskProcessResource != null) {
					blowoutPool.updateResource(taskProcessResource, ResourceState.IDLE);
				}
			}
		}
	}

	private String listTaskProcess(){
		String output = "List Task Process -> ";
		for(TaskProcess tp : this.getRunningProcesses()){
			output += " Id Process: " + tp.getProcessId();
		}
		return output;
	}

    private void removeRunningTask(Task task) {
        this.runningTasks.remove(task);
    }

	public Map<Task, TaskProcess> getRunningTasks(){
		return this.runningTasks;
	}

	public void setRunningTasks(Map<Task, TaskProcess> runningTasks){
		this.runningTasks = runningTasks;
	}

	public List<TaskProcess> getRunningProcesses(){
		List<TaskProcess> processes = new ArrayList<TaskProcess>();
		processes.addAll(this.runningTasks.values());
		return processes;
	}

	public void runTask(Task task, final AbstractResource resource) {
        if (!runningTaskContains(task)) {

            final TaskProcess taskProcess = createProcess(task);
            putTaskToRunningTasks(task, taskProcess);
            LOGGER.debug("Starting to run task of id " + task.getId() + " on resource " + resource.getId());
            task.startedRunning();

            LOGGER.debug("Setting state of resource [id: " + resource.getId() + "] to busy.");
            blowoutPool.updateResource(resource, ResourceState.BUSY);

            getExecutorService().submit(() -> {
                taskProcess.executeTask(resource);
            });
        }
	}

	private boolean runningTaskContains(Task task) {
	    return this.runningTasks.containsKey(task);
    }

    private void putTaskToRunningTasks(Task task, TaskProcess taskProcess) {
        this.runningTasks.put(task, taskProcess);
    }

	public TaskState getTaskState(Task task){

		TaskProcess taskProcess = runningTasks.get(task);
		if(taskProcess == null){
			if(task.isFinished()){
				return TaskState.COMPLETED;
			}
			return TaskState.READY;
		}
		return taskProcess.getTaskState();
	}

	public ExecutorService getExecutorService() {
		return this.taskExecutor;
	}

	public Task getTaskById(String taskId) {
		for (Task task : runningTasks.keySet()) {
			if (task.getId().equals(taskId)) {
				return task;
			}
		}
		return null;
	}

	protected TaskProcess createProcess(Task task) {
		return new TaskProcessImpl(task.getId(), task.getAllCommands(), task.getSpecification(), task.getUUID());
	}

	public void stopTasks(List<Task> tasks){
	    for(Task task : tasks){
	        this.stopTask(task);
        }
    }

	public void stopTask(Task task) {
		TaskProcess processToHalt = getRunningTasks().get(task);
		LOGGER.debug("Removing task " + task.getId());
		removeRunningTask(task);

		if (processToHalt != null) {
			LOGGER.debug("TaskProcess of Task " + task.getId() + " was found.");
			LOGGER.info("Removing TaskProcess of ExecutorService.");
			this.taskExecutor.remove(() -> {
				processToHalt.executeTask(processToHalt.getResource());
			});
			if (processToHalt.getResource() != null) {
				blowoutPool.updateResource(processToHalt.getResource(), ResourceState.IDLE);
				LOGGER.debug("Resource " + processToHalt.getResource().getId() + " was stopped.");
			}
		} else {
			LOGGER.debug("Process To Halt not found.");
		}
	}

	public BlowoutPool getBlowoutPool() {
		return this.blowoutPool;
	}
}
