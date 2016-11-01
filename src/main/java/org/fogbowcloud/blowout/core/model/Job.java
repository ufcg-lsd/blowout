package org.fogbowcloud.blowout.core.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

public abstract class Job implements Serializable {

	private static final long serialVersionUID = -6111900503095749695L;

	protected Map<String, Task> taskList = new HashMap<String, Task>();
	
	public static final Logger LOGGER = Logger.getLogger(Job.class);
	
	private String UUID = "";

	public Job(List<Task> tasks) {
		for(Task task : tasks){
			addTask(task);
		}
	}
	
	public void addTask(Task task) {
		LOGGER.debug("Adding task " + task.getId());
		getTaskList().put(task.getId(), task);
	}

	public Map<String, Task> getTasks(){
		return this.getTaskList();
	}
	
	public abstract void finish(Task task);

	public abstract void fail(Task task);

	public abstract String getId();

	public Map<String, Task> getTaskList() {
		return taskList;
	}
	
	public void setUUID(String UUID) {
		this.UUID = UUID;
	}
	
	public String getUUID() {
		return this.UUID;
	}
}
