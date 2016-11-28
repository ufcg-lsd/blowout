package org.fogbowcloud.blowout.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskState;
import org.fogbowcloud.blowout.core.monitor.TaskMonitor;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.fogbowcloud.blowout.infrastructure.manager.InfrastructureManager;
import org.fogbowcloud.blowout.infrastructure.monitor.ResourceMonitor;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.blowout.pool.AbstractResource;
import org.fogbowcloud.blowout.pool.BlowoutPool;

public class BlowoutController {

	public static final Logger LOGGER = Logger.getLogger(BlowoutController.class);
	
	private String DEFAULT_IMPLEMENTATION_BLOWOUT_POOL = "org.fogbowcloud.blowout.pool.DefauBlowoutlPool";
	private String DEFAULT_IMPLEMENTATION_SCHEDULER = "org.fogbowcloud.blowout.core.StandardScheduler";
	private String DEFAULT_IMPLEMENTATION_INFRA_MANAGER = "org.fogbowcloud.blowout.infrastructure.manager.DefaultInfrastructureManager";
	private String DEFAULT_IMPLEMENTATION_INFRA_PROVIDER = "org.fogbowcloud.blowout.infrastructure.provider.fogbow.FogbowInfrastructureProvider";

	private BlowoutPool blowoutPool;

	// Scheduler elements
	private SchedulerInterface schedulerInterface;
	private TaskMonitor taskMonitor;

	// Infrastructure elements.
	private InfrastructureProvider infraProvider;
	private InfrastructureManager infraManager;
	private ResourceMonitor resourceMonitor;

	private boolean started = false;
	private Properties properties;

	public BlowoutController() {

		try {

			String configFile = System.getProperty(AppPropertiesConstants.BLOWOUT_CONFIG_FILE);

			properties = new Properties();
			properties.load(new FileInputStream(configFile));
			
			if(!this.checkProperties(properties)){
				throw new Exception("Error on validate the file "+configFile);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Create a new exception for blowout and throws
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void start(boolean removePreviousResouces) throws Exception {
		
		started = true;

		blowoutPool = createBlowoutInstance();
		infraProvider = createInfraProviderInstance();

		taskMonitor = new TaskMonitor(blowoutPool, 30000);
		taskMonitor.start();
		resourceMonitor = new ResourceMonitor(infraProvider, blowoutPool, properties);
		resourceMonitor.start();

		schedulerInterface = createSchedulerInstance();
		infraManager = createInfraManagerInstance();

		blowoutPool.start(infraManager, schedulerInterface);
	}

	public void stop() throws Exception {

		for (AbstractResource resource : blowoutPool.getAllResources()) {
			infraProvider.deleteResource(resource.getId());
		}

		taskMonitor.stop();
		resourceMonitor.stop();
		
		started = false;
	}

	public void addTask(Task task) {
		if(!started){
			//TODO Throw new Blowout exception
		}
		blowoutPool.putTask(task);
	}

	public void addTaskList(List<Task> tasks) {
		if(!started){
			//TODO Throw new Blowout exception
		}
		blowoutPool.addTasks(tasks);
	}
	
	public void cleanTask(Task task){
		//TODO remove task from the pool. 
		blowoutPool.removeTask(task);
	}
	
	public TaskState getTaskState(String taskId){
		Task task = null;
		for (Task t : blowoutPool.getAllTasks()) {
			if(t.getId().equals(taskId)){
				task = t;
			}
		}
		if(task == null){
			//TODO throw blowout exception
			return null;
		}else{
			return taskMonitor.getTaskState(task);
		}
	}

	private BlowoutPool createBlowoutInstance() throws Exception {
		String providerClassName = this.properties.getProperty(AppPropertiesConstants.IMPLEMENTATION_BLOWOUT_POOL,
				DEFAULT_IMPLEMENTATION_BLOWOUT_POOL);
		Class<?> forName = Class.forName(providerClassName);
		Object clazz = forName.getConstructor(Properties.class).newInstance();
		if (!(clazz instanceof BlowoutPool)) {
			throw new Exception("Blowout Pool Class Name is not a BlowoutPool implementation");
		}
		return (BlowoutPool) clazz;
	}

	private InfrastructureProvider createInfraProviderInstance() throws Exception {
		String providerClassName = this.properties.getProperty(AppPropertiesConstants.INFRA_PROVIDER_CLASS_NAME,
				DEFAULT_IMPLEMENTATION_INFRA_PROVIDER);
		Class<?> forName = Class.forName(providerClassName);
		Object clazz = forName.getConstructor(Properties.class).newInstance(properties);
		if (!(clazz instanceof InfrastructureProvider)) {
			throw new Exception("Provider Class Name is not a InfrastructureProvider implementation");
		}
		return (InfrastructureProvider) clazz;
	}

	private InfrastructureManager createInfraManagerInstance() throws Exception {
		String providerClassName = this.properties.getProperty(AppPropertiesConstants.IMPLEMENTATION_INFRA_MANAGER,
				DEFAULT_IMPLEMENTATION_INFRA_MANAGER);
		Class<?> forName = Class.forName(providerClassName);
		Object clazz = forName.getConstructor(Properties.class).newInstance(infraProvider, resourceMonitor);
		if (!(clazz instanceof InfrastructureManager)) {
			throw new Exception("Infrastructure Manager Class Name is not a InfrastructureManager implementation");
		}
		return (InfrastructureManager) clazz;
	}

	private SchedulerInterface createSchedulerInstance() throws Exception {
		String providerClassName = this.properties.getProperty(AppPropertiesConstants.IMPLEMENTATION_SCHEDULER,
				DEFAULT_IMPLEMENTATION_SCHEDULER);
		Class<?> forName = Class.forName(providerClassName);
		Object clazz = forName.getConstructor(Properties.class).newInstance(taskMonitor);
		if (!(clazz instanceof SchedulerInterface)) {
			throw new Exception("Scheduler Class Name is not a SchedulerInterface implementation");
		}
		return (SchedulerInterface) clazz;
	}

	private static boolean checkProperties(Properties properties) {
		if (!properties.containsKey(AppPropertiesConstants.INFRA_PROVIDER_CLASS_NAME)) {
			LOGGER.error("Required property " + AppPropertiesConstants.INFRA_PROVIDER_CLASS_NAME + " was not set");
			return false;
		}
		if (!properties.containsKey(AppPropertiesConstants.INFRA_RESOURCE_SERVICE_TIME)) {
			LOGGER.error("Required property " + AppPropertiesConstants.INFRA_RESOURCE_SERVICE_TIME + " was not set");
			return false;
		}
		if (!properties.containsKey(AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME)) {
			LOGGER.error("Required property " + AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME + " was not set");
			return false;
		}
		if (!properties.containsKey(AppPropertiesConstants.INFRA_RESOURCE_CONNECTION_TIMEOUT)) {
			LOGGER.error(
					"Required property " + AppPropertiesConstants.INFRA_RESOURCE_CONNECTION_TIMEOUT + " was not set");
			return false;
		}
		if (!properties.containsKey(AppPropertiesConstants.REST_SERVER_PORT)) {
			LOGGER.error("Required property " + AppPropertiesConstants.REST_SERVER_PORT + " was not set");
			return false;
		}
		if (!properties.containsKey(AppPropertiesConstants.EXECUTION_MONITOR_PERIOD)) {
			LOGGER.error("Required property " + AppPropertiesConstants.EXECUTION_MONITOR_PERIOD + " was not set");
			return false;
		}
		if (!properties.containsKey(AppPropertiesConstants.INFRA_IS_STATIC)) {
			LOGGER.error("Required property " + AppPropertiesConstants.INFRA_IS_STATIC + " was not set");
			return false;
		}
		if (!properties.containsKey(AppPropertiesConstants.INFRA_FOGBOW_USERNAME)) {
			LOGGER.error("Required property " + AppPropertiesConstants.INFRA_FOGBOW_USERNAME + " was not set");
			return false;
		}

		if (properties.containsKey(
				AppPropertiesConstants.INFRA_FOGBOW_TOKEN_UPDATE_PLUGIN)) {

			String tokenUpdatePluginClass = properties.getProperty(
					AppPropertiesConstants.INFRA_FOGBOW_TOKEN_UPDATE_PLUGIN);

			// Checking for required properties of Keystone Token Update
			// Plugin
			if (tokenUpdatePluginClass
					.equals("org.fogbowcloud.blowout.infrastructure.plugin.KeystoneTokenUpdatePlugin")) {
				if (!properties.containsKey("fogbow.keystone.username")) {
					LOGGER.error("Required property " + "fogbow.keystone.username" + " was not set");
					return false;
				}
			}

						
			// Checking for required properties of NAF Token Update Plugin
			if (tokenUpdatePluginClass.equals("org.fogbowcloud.blowout.infrastructure.plugin.NAFTokenUpdatePlugin")) {
				if (!properties.containsKey(AppPropertiesConstants.NAF_IDENTITY_PRIVATE_KEY)) {
					LOGGER.error("Required property " + AppPropertiesConstants.NAF_IDENTITY_PRIVATE_KEY + " was not set");
					return false;
				}

				if (!properties.containsKey(AppPropertiesConstants.NAF_IDENTITY_PUBLIC_KEY)) {
					LOGGER.error("Required property " + AppPropertiesConstants.NAF_IDENTITY_PUBLIC_KEY + " was not set");
					return false;
				}

				if (!properties.containsKey(AppPropertiesConstants.NAF_IDENTITY_TOKEN_GENERATOR_URL)) {
					LOGGER.error("Required property " + AppPropertiesConstants.NAF_IDENTITY_TOKEN_GENERATOR_URL + " was not set");
					return false;
				}

				if (!properties.containsKey(AppPropertiesConstants.NAF_IDENTITY_TOKEN_USERNAME)) {
					LOGGER.error("Required property " + AppPropertiesConstants.NAF_IDENTITY_TOKEN_USERNAME + " was not set");
					return false;
				}

				if (!properties.containsKey(AppPropertiesConstants.NAF_IDENTITY_TOKEN_PASSWORD)) {
					LOGGER.error("Required property " + AppPropertiesConstants.NAF_IDENTITY_TOKEN_PASSWORD + " was not set");
					return false;
				}
			}

		} else {
			LOGGER.error("Required property "
					+ AppPropertiesConstants.INFRA_FOGBOW_TOKEN_UPDATE_PLUGIN
					+ " was not set");
			return false;
		}
		LOGGER.debug("All properties are set");
		return true;
	}
}
