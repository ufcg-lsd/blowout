package org.fogbowcloud.blowout.infrastructure.manager;

import java.util.List;

import org.fogbowcloud.blowout.core.model.task.Task;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;

public interface InfrastructureManager {
	
	void act(List<AbstractResource> resources, List<Task> tasks) throws Exception ;

}
