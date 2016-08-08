package org.fogbowcloud.blowout.scheduler.core.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

public abstract class Job implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6111900503095749695L;

	private Map<String, Task> taskList = new HashMap<String, Task>();
	
	public static enum TaskState{
		READY,RUNNING,COMPLETED,FAILED
	}
	
	public static final Logger LOGGER = Logger.getLogger(Job.class);
	
	protected ReentrantReadWriteLock taskReadyLock = new ReentrantReadWriteLock();
	protected ReentrantReadWriteLock taskCompletedLock = new ReentrantReadWriteLock();

	private boolean isCreated = false;
	
	public void addTask(Task task) {
		LOGGER.debug("Adding task " + task.getId());
		taskReadyLock.writeLock().lock();
		try {
			taskList.put(task.getId(), task);
		} finally {
			taskReadyLock.writeLock().unlock();
		}
	}
	
	public void addFakeTask(Task task) {
		LOGGER.debug("Adding fake completed task " + task.getId());
		taskCompletedLock.writeLock().lock();
		try {
		} finally {
			taskCompletedLock.writeLock().unlock();
		}
	}

	public Map<String, Task> getTasks(){
		return this.taskList;
	}
	
	public abstract void finish(Task task);

	public abstract void fail(Task task);
	
	public String getId(){
		return null;
	}

	public boolean isCreated() {
		return this.isCreated;
	}
	
	public void setCreated() {
		this.isCreated = true;
	}
}
