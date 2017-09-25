package org.fogbowcloud.blowout.core;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskState;
import org.fogbowcloud.blowout.core.monitor.TaskMonitor;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.fogbowcloud.blowout.infrastructure.manager.InfrastructureManager;
import org.fogbowcloud.blowout.infrastructure.monitor.ResourceMonitor;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.blowout.pool.BlowoutPool;

public class BlowoutController {

	public static final Logger LOGGER = Logger.getLogger(BlowoutController.class);

	private String DEFAULT_IMPLEMENTATION_BLOWOUT_POOL = "org.fogbowcloud.blowout.pool.DefaultBlowoutPool";
	private String DEFAULT_IMPLEMENTATION_SCHEDULER = "org.fogbowcloud.blowout.core.StandardScheduler";
	private String DEFAULT_IMPLEMENTATION_INFRA_MANAGER = "org.fogbowcloud.blowout.infrastructure.manager.DefaultInfrastructureManager";
	private String DEFAULT_IMPLEMENTATION_INFRA_PROVIDER = "org.fogbowcloud.blowout.infrastructure.provider.fogbow.FogbowInfrastructureProvider";
	private String DEFAULT_TASK_MONITOR_PERIOD = "30000";

	protected BlowoutPool blowoutPool;

	private SchedulerInterface schedulerInterface;
	private TaskMonitor taskMonitor;

	protected InfrastructureProvider infraProvider;
	protected InfrastructureManager infraManager;
	protected ResourceMonitor resourceMonitor;

	private Properties properties;
	protected boolean started;

	public BlowoutController(Properties properties) throws BlowoutException {
		this.started = false;

		try {
			if (!BlowoutController.checkProperties(properties)) {
				throw new BlowoutException("Error on validate the file ");
			}
		} catch (Exception e) {
			throw new BlowoutException("Error while initialize Blowout Controller.", e);
		}

		this.properties = properties;
	}

	public void start(boolean removePreviousResouces) throws Exception {
		this.blowoutPool = createBlowoutInstance();
		this.infraProvider = createInfraProviderInstance(removePreviousResouces);

		long taskMonitorPeriod = Long.parseLong(this.properties.getProperty(
				AppPropertiesConstants.TASK_MONITOR_PERIOD, this.DEFAULT_TASK_MONITOR_PERIOD));

		this.taskMonitor = new TaskMonitor(this.blowoutPool, taskMonitorPeriod);
		this.taskMonitor.start();

		this.resourceMonitor = new ResourceMonitor(this.infraProvider, this.blowoutPool,
				this.properties);
		this.resourceMonitor.start();

		this.schedulerInterface = createSchedulerInstance(this.taskMonitor);
		this.infraManager = createInfraManagerInstance();

		this.blowoutPool.start(this.infraManager, this.schedulerInterface);

		this.started = true;
	}

	public void stop() {
		this.taskMonitor.stop();
		this.resourceMonitor.stop();

		this.started = false;
	}

	public void addTask(Task task) throws BlowoutException {
		if (!this.started) {
			throw new BlowoutException("Blowout hasn't been started yet");
		}
		this.blowoutPool.putTask(task);
	}

	public void addTaskList(List<Task> tasks) throws BlowoutException {
		if (!started) {
			throw new BlowoutException("Blowout hasn't been started yet");
		}
		this.blowoutPool.addTasks(tasks);
	}

	public void cleanTask(Task task) {
		this.blowoutPool.removeTask(task);
	}

	public TaskState getTaskState(String taskId) {
		Task task = null;
		for (Task taskInPool : this.blowoutPool.getAllTasks()) {
			if (taskInPool.getId().equals(taskId)) {
				task = taskInPool;
			}
		}
		if (task == null) {
			return TaskState.NOT_CREATED;
		} else {
			return this.taskMonitor.getTaskState(task);
		}
	}

	public BlowoutPool createBlowoutInstance() throws Exception {
		String blowoutPoolClassName = this.properties.getProperty(
				AppPropertiesConstants.IMPLEMENTATION_BLOWOUT_POOL,
				this.DEFAULT_IMPLEMENTATION_BLOWOUT_POOL);

		Class<?> blowoutPoolClass = Class.forName(blowoutPoolClassName);

		Object blowoutPool = blowoutPoolClass.getConstructor().newInstance();

		if (!(blowoutPool instanceof BlowoutPool)) {
			throw new Exception("Blowout Pool Class Name is not a BlowoutPool implementation");
		}
		return (BlowoutPool) blowoutPool;
	}

	public InfrastructureProvider createInfraProviderInstance(boolean removePreviousResouces)
			throws Exception {
		String infraProviderClassName = this.properties.getProperty(
				AppPropertiesConstants.IMPLEMENTATION_INFRA_PROVIDER,
				this.DEFAULT_IMPLEMENTATION_INFRA_PROVIDER);

		Class<?> infraProviderClass = Class.forName(infraProviderClassName);

		Object infraProvider = infraProviderClass.getConstructor(Properties.class, Boolean.TYPE)
				.newInstance(this.properties, removePreviousResouces);

		if (!(infraProvider instanceof InfrastructureProvider)) {
			throw new Exception(
					"Infrastructure Provider Class Name is not a InfrastructureProvider implementation");
		}
		return (InfrastructureProvider) infraProvider;
	}

	public InfrastructureManager createInfraManagerInstance() throws Exception {
		String infraManagerClassName = this.properties.getProperty(
				AppPropertiesConstants.IMPLEMENTATION_INFRA_MANAGER,
				this.DEFAULT_IMPLEMENTATION_INFRA_MANAGER);

		Class<?> infraManagerClass = Class.forName(infraManagerClassName);

		Object infraManager = infraManagerClass
				.getConstructor(InfrastructureProvider.class, ResourceMonitor.class)
				.newInstance(this.infraProvider, this.resourceMonitor);

		if (!(infraManager instanceof InfrastructureManager)) {
			throw new Exception(
					"Infrastructure Manager Class Name is not a InfrastructureManager implementation");
		}
		return (InfrastructureManager) infraManager;
	}

	protected SchedulerInterface createSchedulerInstance(TaskMonitor taskMonitor) throws Exception {
		String schedulerClassName = this.properties.getProperty(
				AppPropertiesConstants.IMPLEMENTATION_SCHEDULER,
				this.DEFAULT_IMPLEMENTATION_SCHEDULER);

		Class<?> schedulerClass = Class.forName(schedulerClassName);

		Object scheduler = schedulerClass.getConstructor(TaskMonitor.class)
				.newInstance(taskMonitor);

		if (!(scheduler instanceof SchedulerInterface)) {
			throw new Exception("Scheduler Class Name is not a SchedulerInterface implementation");
		}
		return (SchedulerInterface) scheduler;
	}

	protected static boolean checkProperties(Properties properties) {
		return propertiesContainsAll(properties,
				AppPropertiesConstants.IMPLEMENTATION_INFRA_PROVIDER,
				AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME,
				AppPropertiesConstants.INFRA_IS_STATIC,
				AppPropertiesConstants.INFRA_AUTH_TOKEN_UPDATE_PLUGIN);
	}

	private static boolean propertiesContainsAll(Properties properties,
			String... wantedProperties) {
		for (String property : wantedProperties) {
			if (!properties.containsKey(property)) {
				LOGGER.error("Required property " + property + " was not set");
				return false;
			}
		}
		LOGGER.debug("All properties are set");
		return true;
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
}
