package org.fogbowcloud.blowout.scheduler;

import java.util.List;

import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.infrastructure.model.AbstractResource;

public interface Scheduler {

	void act(List<Task> tasks, List<AbstractResource> resources);
	
	void runTask(Task task, AbstractResource resource);

	void stopTask(Task task);

	List<Task> getRunningTasks();
}
