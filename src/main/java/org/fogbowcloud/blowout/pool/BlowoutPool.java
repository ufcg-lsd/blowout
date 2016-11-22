package org.fogbowcloud.blowout.pool;

import java.util.List;

import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.infrastructure.manager.ResourceNotifier;

public interface BlowoutPool{
	
	void addResource(AbstractResource resource);
	
	void resourceFailed(AbstractResource resource);
	
	List<AbstractResource> getAllResources();
	
	void allocateResource(AbstractResource resource);
	
	void releaseResource(AbstractResource resource);
	
	void removeResource(AbstractResource resource);
	
	void addTask(Task task);
	
	void taskFaild(Task task);
	
	List<Task> getAllTasks();
	
	void removeTask(Task task);


}
