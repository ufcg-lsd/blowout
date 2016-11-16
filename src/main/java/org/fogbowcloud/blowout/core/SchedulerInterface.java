package org.fogbowcloud.blowout.core;

import java.util.List;

import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.infrastructure.model.AbstractResource;

public interface SchedulerInterface {

	public boolean act(List<Task> tasks, List<AbstractResource> resources);
	
	public void run(Task task);
	
	public void stop(Task task);
}
