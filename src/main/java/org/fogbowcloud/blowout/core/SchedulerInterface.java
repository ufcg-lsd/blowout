package org.fogbowcloud.blowout.core;

import java.util.List;

import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.pool.AbstractResource;

public interface SchedulerInterface {

	boolean act(List<Task> tasks, List<AbstractResource> resources);
	
	void runTask(Task task, AbstractResource resource);

	void stopTask(Task task);

	List<Task> getRunningTasks();
}
