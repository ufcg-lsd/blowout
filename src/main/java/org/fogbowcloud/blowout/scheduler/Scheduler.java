package org.fogbowcloud.blowout.scheduler;

import java.util.List;

import org.fogbowcloud.blowout.core.model.task.Task;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;

public interface Scheduler {

	void act(List<Task> tasks, List<AbstractResource> resources);
	
	void runTask(Task task, AbstractResource resource);

	void stopTask(Task task);

	void stopTasks(List<Task> tasks);

	List<Task> getRunningTasks();
}
