package org.fogbowcloud.blowout.pool;

import java.util.List;

import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.infrastructure.manager.ResourceNotifier;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;

public interface BlowoutPool{
	
	void putResource(AbstractResource resource, ResourceState state);
	
	List<AbstractResource> getAllResources();
	
	AbstractResource getResourceById(String resourceId);
	
	void removeResource(AbstractResource resource);
	
	void putTask(Task task);
	
	List<Task> getAllTasks();
	
	Task getTaskById(String taskId);
	
	void removeTask(Task task);


}