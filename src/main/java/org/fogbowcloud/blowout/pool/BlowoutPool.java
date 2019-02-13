package org.fogbowcloud.blowout.pool;

import java.util.List;

import org.fogbowcloud.blowout.core.Scheduler;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.infrastructure.manager.InfrastructureManager;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;

public interface BlowoutPool {
	
	void start(InfrastructureManager infraManager, Scheduler scheduler);
	
	void addResource(AbstractResource resource);
	
	void addResourceList(List<AbstractResource> resources);
	
	void updateResource(AbstractResource resource, ResourceState state);
	
	List<AbstractResource> getAllResources();
	
	AbstractResource getResourceById(String resourceId);
	
	void removeResource(AbstractResource resource);
	
	void addTask(Task task);
	
	void addTasks(List<Task> tasks);
	
	List<Task> getAllTasks();
	
	Task getTaskById(String taskId);
	
	void removeTask(Task task);

}