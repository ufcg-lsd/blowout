package org.fogbowcloud.blowout.core.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public abstract class Job implements Serializable {

	private static final long serialVersionUID = -6111900503095749695L;

	protected Map<String, Task> taskList = new HashMap<String, Task>();
	
	public static final Logger LOGGER = Logger.getLogger(Job.class);
	
	private String UUID = "";

	private boolean isCreated = false;

	public Job(List<Task> tasks) {
		for(Task task : tasks){
			LOGGER.debug("Adding task " + task.getId());
			getTaskList().put(task.getId(), task);
		}
	}

	public Map<String, Task> getTasks(){
		return this.getTaskList();
	}
	
	public abstract void finish(Task task);

	public abstract void fail(Task task);

	public abstract String getId();

	//TODO: it seems this *created* and restart methods help the Scheduler class to its job. I'm not sure
	//if we should keep them.
	public boolean isCreated() {
		return this.isCreated;
	}
	
	public void setCreated() {
		this.isCreated = true;
	}

	public void restart() {
		this.isCreated = false;
		
	}

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
