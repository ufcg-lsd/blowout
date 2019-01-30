package org.fogbowcloud.blowout.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.constants.BlowoutConstants;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskState;
import org.fogbowcloud.blowout.core.monitor.TaskMonitor;
import org.fogbowcloud.blowout.core.constants.AppPropertiesConstants;
import org.fogbowcloud.blowout.infrastructure.manager.InfrastructureManager;
import org.fogbowcloud.blowout.infrastructure.monitor.ResourceMonitor;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.blowout.pool.AbstractResource;
import org.fogbowcloud.blowout.pool.BlowoutPool;

public class BlowoutController {

	private static final Logger LOGGER = Logger.getLogger(BlowoutController.class);

	private SchedulerInterface schedulerInterface;
	private TaskMonitor taskMonitor;
	private Properties properties;

	protected BlowoutPool blowoutPool;
	protected InfrastructureProvider infraProvider;
	protected InfrastructureManager infraManager;
	protected ResourceMonitor resourceMonitor;
	protected boolean started = false;

	public BlowoutController(Properties properties) throws BlowoutException {
		this.properties = properties;
		try {
			if (!checkProperties(properties)) {
				throw new BlowoutException("Error on validate the file ");
			} else {
				LOGGER.info("All properties are set");
			}
		} catch (Exception e) {
			throw new BlowoutException("Error while initialize Blowout Controller.", e);
		}
	}

	public void start(boolean removePreviousResources) throws Exception {
		long timeout = 30000;

		this.started = true;

		this.blowoutPool = createBlowoutInstance();
		this.infraProvider = createInfraProviderInstance(removePreviousResources);

		this.taskMonitor = new TaskMonitor(this.blowoutPool, timeout);
		this.taskMonitor.start();

		this.resourceMonitor = new ResourceMonitor(this.infraProvider, this.blowoutPool, this.properties);
		this.resourceMonitor.start();

		this.schedulerInterface = createSchedulerInstance(this.taskMonitor);
		this.infraManager = createInfraManagerInstance();

		this.blowoutPool.start(this.infraManager, this.schedulerInterface);
	}

	public void stop() throws Exception {
		for (AbstractResource resource : blowoutPool.getAllResources()) {
			infraProvider.deleteResource(resource.getId());
		}

		taskMonitor.stop();
		resourceMonitor.stop();

		started = false;
	}

	public void addTask(Task task) throws BlowoutException {
		if (!started) {
			throw new BlowoutException("Blowout hasn't been started yet");
		}
		blowoutPool.putTask(task);
	}

	public void addTaskList(List<Task> tasks) throws BlowoutException {
		if (!started) {
			throw new BlowoutException("Blowout hasn't been started yet");
		}
		blowoutPool.addTasks(tasks);
	}

	public void cleanTask(Task task) {
		blowoutPool.removeTask(task);
	}

	public TaskState getTaskState(String taskId) {
		Task task = null;
		for (Task t : blowoutPool.getAllTasks()) {
			if (t.getId().equals(taskId)) {
				task = t;
			}
		}
		if (task == null) {
			return TaskState.NOT_CREATED;
		} else {
			return taskMonitor.getTaskState(task);
		}
	}

	public BlowoutPool createBlowoutInstance() throws Exception {
		String providerClassName = this.properties.getProperty(AppPropertiesConstants.IMPLEMENTATION_BLOWOUT_POOL,
				BlowoutConstants.DEFAULT_IMPLEMENTATION_BLOWOUT_POOL);
		Class<?> forName = Class.forName(providerClassName);
		Object clazz = forName.getConstructor().newInstance();
		if (!(clazz instanceof BlowoutPool)) {
			throw new Exception("Blowout Pool Class Name is not a BlowoutPool implementation");
		}
		return (BlowoutPool) clazz;
	}

	public InfrastructureProvider createInfraProviderInstance(boolean removePreviousResouces) throws Exception {
		String providerClassName = this.properties.getProperty(AppPropertiesConstants.IMPLEMENTATION_INFRA_PROVIDER,
				BlowoutConstants.DEFAULT_IMPLEMENTATION_INFRA_PROVIDER);
		Class<?> forName = Class.forName(providerClassName);
		Object clazz = forName.getConstructor(Properties.class, Boolean.TYPE).newInstance(properties, removePreviousResouces);
		if (!(clazz instanceof InfrastructureProvider)) {
			throw new Exception("Provider Class Name is not a InfrastructureProvider implementation");
		}
		return (InfrastructureProvider) clazz;
	}

	public InfrastructureManager createInfraManagerInstance() throws Exception {
		String providerClassName = this.properties.getProperty(AppPropertiesConstants.IMPLEMENTATION_INFRA_MANAGER,
				BlowoutConstants.DEFAULT_IMPLEMENTATION_INFRA_MANAGER);
		Class<?> forName = Class.forName(providerClassName);
		Object clazz = forName.getConstructor(InfrastructureProvider.class, ResourceMonitor.class).newInstance(infraProvider, resourceMonitor);
		if (!(clazz instanceof InfrastructureManager)) {
			throw new Exception("Infrastructure Manager Class Name is not a InfrastructureManager implementation");
		}
		return (InfrastructureManager) clazz;
	}

	protected SchedulerInterface createSchedulerInstance(TaskMonitor taskMonitor) throws Exception {
		String providerClassName = this.properties.getProperty(AppPropertiesConstants.IMPLEMENTATION_SCHEDULER,
				BlowoutConstants.DEFAULT_IMPLEMENTATION_SCHEDULER);
		Class<?> forName = Class.forName(providerClassName);
		Object clazz = forName.getConstructor(TaskMonitor.class).newInstance(taskMonitor);
		if (!(clazz instanceof SchedulerInterface)) {
			throw new Exception("Scheduler Class Name is not a SchedulerInterface implementation");
		}
		return (SchedulerInterface) clazz;
	}

	protected static boolean checkProperties(Properties properties) {
		List<String> propertiesKeys = new ArrayList<>();
		populateWithPropertiesKeys(propertiesKeys);
		return checkAllProperties(properties, propertiesKeys);
	}

	public BlowoutPool getBlowoutPool() {
		return this.blowoutPool;
	}

	public void setBlowoutPool(BlowoutPool blowoutPool) {
		this.blowoutPool = blowoutPool;
	}
	
	public TaskMonitor getTaskMonitor() {
		return taskMonitor;
	}

	public void setTaskMonitor(TaskMonitor taskMonitor) {
		this.taskMonitor = taskMonitor;
	}

	public SchedulerInterface getSchedulerInterface() {
		return schedulerInterface;
	}

	public void setSchedulerInterface(SchedulerInterface schedulerInterface) {
		this.schedulerInterface = schedulerInterface;
	}

	public InfrastructureProvider getInfraProvider() {
		return infraProvider;
	}

	public void setInfraProvider(InfrastructureProvider infraProvider) {
		this.infraProvider = infraProvider;
	}

	public InfrastructureManager getInfraManager() {
		return infraManager;
	}

	public void setInfraManager(InfrastructureManager infraManager) {
		this.infraManager = infraManager;
	}

	public ResourceMonitor getResourceMonitor() {
		return resourceMonitor;
	}

	public void setResourceMonitor(ResourceMonitor resourceMonitor) {
		this.resourceMonitor = resourceMonitor;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public int getTaskRetries(String taskId) {
		Task task = null;
		for (Task t : blowoutPool.getAllTasks()) {
			if (t.getId().equals(taskId)) {
				task = t;
			}
		}
		if (task == null) {
			return 0;
		} else {
			return task.getRetries();
		}
	}

	private static boolean checkAllProperties(Properties properties, List<String> propertiesKeys) {
		boolean passed = true;

		for (String key : propertiesKeys) {
			if (!checkProperty(properties, key)) {
				passed = false;
				break;
			}
		}
		return passed;
	}

	private static boolean checkProperty(Properties properties, String propertyKey) {
		if (!properties.containsKey(propertyKey)) {
			LOGGER.error("Required property " + propertyKey + " was not set");
			return false;
		}
		return true;
	}

	private static void populateWithPropertiesKeys(List<String> propertiesKeys) {
		propertiesKeys.add(AppPropertiesConstants.IMPLEMENTATION_INFRA_PROVIDER);
		propertiesKeys.add(AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME);
		propertiesKeys.add(AppPropertiesConstants.INFRA_RESOURCE_CONNECTION_TIMEOUT);
		propertiesKeys.add(AppPropertiesConstants.INFRA_IS_STATIC);
		propertiesKeys.add(AppPropertiesConstants.INFRA_AUTH_TOKEN_UPDATE_PLUGIN);
	}
}
