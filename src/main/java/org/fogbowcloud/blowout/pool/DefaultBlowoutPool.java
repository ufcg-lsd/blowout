package org.fogbowcloud.blowout.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.scheduler.Scheduler;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.infrastructure.manager.InfrastructureManager;
import org.fogbowcloud.blowout.infrastructure.model.AbstractResource;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;

public class DefaultBlowoutPool implements BlowoutPool {
	
	private static final Logger LOGGER = Logger.getLogger(DefaultBlowoutPool.class);

	private Map<String, AbstractResource> resourcePool;
	private List<Task> taskPool;
	private InfrastructureManager infraManager;
	private Scheduler scheduler;

	@Override
	public void start(InfrastructureManager infraManager, Scheduler scheduler) {
		this.resourcePool = new ConcurrentHashMap<>();
		this.taskPool = new CopyOnWriteArrayList<>();
		this.infraManager = infraManager;
		this.scheduler = scheduler;
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
			LOGGER.debug("Calling act from the Thread " + Thread.currentThread().getId() +
					" of entity: " + Thread.currentThread().getName());
			infraManager.act(getAllResources(), getAllTasks());
			scheduler.act(getAllTasks(), getAllResources());
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
		taskPool.addAll(tasks);
		LOGGER.info("The tasks that references the job " + Thread.currentThread().getName() +
				" was added to the Pool.");
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

	protected Scheduler getScheduler() {
		return scheduler;
	}

	protected void setScheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
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
