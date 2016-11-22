package org.fogbowcloud.blowout.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fogbowcloud.blowout.core.SchedulerInterface;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.infrastructure.manager.InfrastructureManager;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;

public class DefauBlowoutlPool implements BlowoutPool{

	private Map<String, AbstractResource> resourcePool = new ConcurrentHashMap<String, AbstractResource>();
	private List<Task> taskPool = new ArrayList<Task>();
	private InfrastructureManager infraManager;
	private SchedulerInterface schedulerInterface;
	
	public DefauBlowoutlPool(SchedulerInterface schedulerInterface, InfrastructureManager infraManager){
		this.schedulerInterface = schedulerInterface;
		this.infraManager = infraManager;
		
	}
	
	@Override
	public void putResource(AbstractResource resource, ResourceState state) {
		resource.setState(state);
		resourcePool.put(resource.getId(), resource);
		try {
			infraManager.act(getAllResources(), getAllTasks());
			schedulerInterface.act(getAllTasks(), getAllResources());
		} catch (Exception e) {
			// TODO TODO Do what when it fails?
			e.printStackTrace();
		}
		
	}

	@Override
	public List<AbstractResource> getAllResources() {
		return new ArrayList<AbstractResource>(resourcePool.values());
	}
	
	@Override
	public AbstractResource getResourceById(String resourceId) {
		return resourcePool.get(resourceId);
	}

	@Override
	public synchronized void removeResource(AbstractResource resource) {
		resourcePool.remove(resource);
	}

	@Override
	public void putTask(Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Task> getAllTasks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Task getTaskById(String taskId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeTask(Task task) {
		// TODO Auto-generated method stub
		
	}

	

}
