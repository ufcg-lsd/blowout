package org.fogbowcloud.blowout.infrastructure.manager;

import java.util.List;

import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.pool.AbstractResource;

public interface InfrastructureManager {
	
	public void act(List<AbstractResource> resources, List<Task> tasks) throws Exception ;

}
