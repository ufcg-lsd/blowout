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

public class TaskMonitor implements Runnable{

	Map<Task, TaskProcess> runningTasks = new HashMap<Task, TaskProcess>();
	
	private ExecutorService taskExecutor = Executors.newCachedThreadPool();

	private static ManagerTimer executionMonitorTimer = new ManagerTimer(Executors.newScheduledThreadPool(1));
	private Thread monitoringServiceRunner;
	
	private BlowoutPool pool;
	
	private long timeout;
	
	private boolean active = false;
	
	public TaskMonitor(BlowoutPool pool, long timeout) {
		this.pool = pool;
		this.timeout = timeout;
	}
	
	public void start() {
		active = true;
		monitoringServiceRunner = new Thread(this);
		monitoringServiceRunner.start();
	}
	
	public void stop(){
		active = false;
		monitoringServiceRunner.interrupt();
	}
	
	@Override
	public void run() {
		while(active){
			procMon();
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
			}
		}
	}
		

	public void procMon() {
		for (TaskProcess tp : getRunningProcesses()) {
			if (tp.getStatus().equals(TaskState.FAILED)) {
				getRunningTasks().remove(getTaskById(tp.getTaskId()));
				if (tp.getResource()!= null) {
					pool.updateResource(tp.getResource(), ResourceState.FAILED);
				}
			}
			if (tp.getStatus().equals(TaskState.FINNISHED)) {
				Task task = getTaskById(tp.getTaskId());
				task.finish();
				getRunningTasks().remove(task);
				if (tp.getResource()!= null) {
					pool.updateResource(tp.getResource(), ResourceState.IDLE);
				}
			}
		}
	}
	
	protected Map<Task, TaskProcess> getRunningTasks(){
		return this.runningTasks;
	}
	
	protected List<TaskProcess> getRunningProcesses(){
		List<TaskProcess> processes = new ArrayList<TaskProcess>();
		processes.addAll(runningTasks.values());
		return processes;
	}
	
	public void runTask(Task task,final AbstractResource resource) {
		final TaskProcess tp = createProcess(task);
		if (getRunningTasks().get(task) == null) {
			getRunningTasks().put(task, tp);
			pool.updateResource(resource, ResourceState.BUSY);
		}
		getExecutorService().submit(new Runnable() {
			
			@Override
			public void run() {
				tp.executeTask(resource);
			}
		});
	}
	
	public TaskState getTaskState(Task task){
		
		TaskProcess tp = runningTasks.get(task);
		if(tp == null){
			if(task.isFinished()){
				return TaskState.COMPLETED;
			}
			return TaskState.READY;
		}
		return tp.getStatus();
	}
	
	protected ExecutorService getExecutorService() {
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
		TaskProcess tp = new TaskProcessImpl(task.getId(), task.getAllCommands(), task.getSpecification());
		return tp;
	}

	public void stopTask(Task task) {
		TaskProcess processToHalt = getRunningTasks().get(task);
		if (processToHalt != null) {
			if (processToHalt.getResource() != null) {
				pool.updateResource(processToHalt.getResource(), ResourceState.IDLE);
			}
		}
		
	}
	
	public BlowoutPool getBlowoutPool() {
		return this.pool;
	}
}
