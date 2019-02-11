package org.fogbowcloud.blowout.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.SchedulerInterface;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.infrastructure.manager.InfrastructureManager;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;

public class DefaultBlowoutPool implements BlowoutPool {
	
	private static final Logger LOGGER = Logger.getLogger(DefaultBlowoutPool.class);

	private Map<String, AbstractResource> resourcePool;
	private List<Task> taskPool;
	private InfrastructureManager infraManager;
	private SchedulerInterface schedulerInterface;

	@Override
	public void start(InfrastructureManager infraManager, SchedulerInterface schedulerInterface) {
		this.resourcePool = new ConcurrentHashMap<>();
		this.taskPool = new CopyOnWriteArrayList<>();
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
		AbstractResource currentResource = resourcePool.get(resource.getId());
		if (currentResource != null) {
			currentResource.setState(state);
			resourcePool.put(resource.getId(), currentResource);
			callAct();
		}
	}

	protected synchronized void callAct() {
		try {
			LOGGER.debug("Calling act to the job " + Thread.currentThread().getName());
			infraManager.act(getAllResources(), getAllTasks());
			schedulerInterface.act(getAllTasks(), getAllResources());
		} catch (Exception e) {
			LOGGER.error("Error while calling act", e);
		}
	}

	@Override
	public List<AbstractResource> getAllResources() {
		return new ArrayList<>(resourcePool.values());
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
	public void addTask(Task task) {

		taskPool.add(task);
		callAct();
	}

	@Override
	public void addTasks(List<Task> tasks) {
		LOGGER.info("The tasks that references the job " + Thread.currentThread().getName() +
				" was added to the Pool.");
		taskPool.addAll(tasks);
		callAct();
	}

	@Override
	public List<Task> getAllTasks() {
		return new ArrayList<>(taskPool);
	}

	@Override
	public Task getTaskById(String taskId) {
		for(int i = 0; i <= taskPool.size(); i++) {
			if(taskPool.get(i).getId().equals(taskId)) {
				return taskPool.get(i);
			}
		}
		return null;
	}

	@Override
	public void removeTask(Task task) {
		taskPool.remove(task);
		callAct();
	}

	protected InfrastructureManager getInfraManager() {
		return infraManager;
	}

	protected void setInfraManager(InfrastructureManager infraManager) {
		this.infraManager = infraManager;
	}

	protected SchedulerInterface getSchedulerInterface() {
		return schedulerInterface;
	}

	protected void setSchedulerInterface(SchedulerInterface schedulerInterface) {
		this.schedulerInterface = schedulerInterface;
	}

	protected Map<String, AbstractResource> getResourcePool() {
		return resourcePool;
	}

	protected void setResourcePool(Map<String, AbstractResource> resourcePool) {
		this.resourcePool = resourcePool;
	}

	protected List<Task> getTaskPool() {
		return taskPool;
	}

	protected void setTaskPool(List<Task> taskPool) {
		this.taskPool = taskPool;
	}
}
