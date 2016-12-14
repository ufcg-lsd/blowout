package org.fogbowcloud.blowout.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.fogbowcloud.blowout.core.SchedulerInterface;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.database.ResourceIdDatastore;
import org.fogbowcloud.blowout.infrastructure.manager.InfrastructureManager;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;

public class DefaultBlowoutPool implements BlowoutPool {

	private Map<String, AbstractResource> resourcePool = new ConcurrentHashMap<String, AbstractResource>();
	private List<Task> taskPool = new ArrayList<Task>();
	private InfrastructureManager infraManager;
	private SchedulerInterface schedulerInterface;

	@Override
	public void start(InfrastructureManager infraManager, SchedulerInterface schedulerInterface) {
		this.infraManager = infraManager;
		this.schedulerInterface = schedulerInterface;
	}

	@Override
	public void addResource(AbstractResource resource) {
		resource.setState(ResourceState.IDLE);
		resourcePool.put(resource.getId(), resource);
		callAct();
	}
	
	@Override
	public void addResourceList(List<AbstractResource> resources) {
		for (AbstractResource resource : resources) {
			resource.setState(ResourceState.IDLE);
			resourcePool.put(resource.getId(), resource);
		}
		callAct();
	}

	@Override
	public void updateResource(AbstractResource resource, ResourceState state) {

		AbstractResource oldResource = resourcePool.get(resource.getId());
		if (oldResource != null) {
			oldResource.setState(state);
			resourcePool.put(resource.getId(), oldResource);
			callAct();
		}
	}

	private void callAct() {
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
		resourcePool.remove(resource.getId());
	}

	@Override
	public void putTask(Task task) {

		taskPool.add(task);
		callAct();
	}

	@Override
	public void addTasks(List<Task> tasks) {

		taskPool.addAll(tasks);
		callAct();
	}

	@Override
	public List<Task> getAllTasks() {
		// TODO Auto-generated method stub
		return new ArrayList<Task>(taskPool);
	}

	@Override
	public Task getTaskById(String taskId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeTask(Task task) {
		taskPool.remove(task);
		callAct();
	}

	public InfrastructureManager getInfraManager() {
		return infraManager;
	}

	public void setInfraManager(InfrastructureManager infraManager) {
		this.infraManager = infraManager;
	}

	public SchedulerInterface getSchedulerInterface() {
		return schedulerInterface;
	}

	public void setSchedulerInterface(SchedulerInterface schedulerInterface) {
		this.schedulerInterface = schedulerInterface;
	}

}
