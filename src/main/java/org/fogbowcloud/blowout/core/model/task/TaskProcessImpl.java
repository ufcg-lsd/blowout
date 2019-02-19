package org.fogbowcloud.blowout.core.model.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.model.Command;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.util.AppUtil;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;

import static java.lang.Thread.sleep;

public class TaskProcessImpl implements TaskProcess {

	private static final Logger LOGGER = Logger.getLogger(TaskProcessImpl.class);

	private static final String ENV_HOST = "HOST";
	private static final String ENV_SSH_USER = "SSH_USER";
	private static final String ENV_PRIVATE_KEY_FILE = "PRIVATE_KEY_FILE";
	private static final String ENV_UUID = "UUID";

	private final String taskId;
	private TaskState taskState;
	private final List<Command> commandList;
	private final Specification specification;
	private String processId;
	private String uuid;
	private AbstractResource resource;

	public TaskProcessImpl(String taskId, List<Command> commandList, Specification specification, String uuid) {
		this.processId = AppUtil.generateIdentifier();
		this.taskId = taskId;
		this.taskState = TaskState.READY;
		this.specification = specification;
		this.commandList = commandList;
		this.uuid = uuid;
	}

	public String getProcessId() {
		return this.processId;
	}

	@Override
	public String getTaskId() {
		return this.taskId;
	}

	@Override
	public List<Command> getCommands() {
		return new ArrayList<>(this.commandList);
	}

	@Override
	public TaskExecutionResult executeTask(AbstractResource resource) {
		this.resource = resource;
		this.setTaskState(TaskState.RUNNING);
		TaskExecutionResult taskExecutionResult = new TaskExecutionResult();

		LOGGER.debug("Task : " + taskId + " is running. ");
		for (Command command : this.getCommands()) {
			LOGGER.debug("Command " + command.getCommand());
			LOGGER.debug("Command Type " + command.getType());
			String commandString = getExecutableCommandString(command);

			taskExecutionResult = executeCommandString(commandString, command.getType(), resource);
			LOGGER.debug("Command result: " + taskExecutionResult.getExitValue());
			if (taskExecutionResult.getExitValue() != TaskExecutionResult.OK) {
				if(taskExecutionResult.getExitValue() == TaskExecutionResult.TIMEOUT) {
					this.setTaskState(TaskState.TIMEDOUT);
					break;
				}
				this.setTaskState(TaskState.FAILED);
				break;
			}
		}
		if (!this.getTaskState().equals(TaskState.FAILED)
				&& !this.getTaskState().equals(TaskState.TIMEDOUT)) {
			this.setTaskState(TaskState.FINISHED);
		}
		return taskExecutionResult;
	}

	@Override
	public void setTaskState(TaskState taskState) {
		this.taskState = taskState;
	}
	
	public void setResource(AbstractResource resource) {
		this.resource = resource;
	}

	private String getExecutableCommandString(Command command) {
			return command.getCommand();
	}

	protected TaskExecutionResult executeCommandString(String commandString, Command.Type type,
			AbstractResource resource) {

		TaskExecutionResult taskExecutionResult = new TaskExecutionResult();
		int returnValue;

		Map<String, String> additionalVariables = getAdditionalEnvVariables(resource);
		try {
			if (type.equals(Command.Type.LOCAL)) {
				Process localProc = startLocalProcess(commandString, additionalVariables);
				returnValue = localProc.waitFor();

			} else {
				Process remoteProc = startRemoteProcess(commandString, additionalVariables);
				returnValue = remoteProc.waitFor();
			}
		} catch (Exception e) {
			LOGGER.error("Failed to execute command in resource of id " + resource.getId());
			e.printStackTrace();
			returnValue = TaskExecutionResult.NOK;
		}

		taskExecutionResult.finish(returnValue);
		return taskExecutionResult;
	}

	private Process startRemoteProcess(String commandString, Map<String, String> additionalVariables)
			throws IOException {
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c",
				"ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i "
						+ additionalVariables.get(ENV_PRIVATE_KEY_FILE) + " " + additionalVariables.get(ENV_SSH_USER)
						+ "@" + additionalVariables.get(ENV_HOST)
						+ " " + parseEnvironVariable(commandString, additionalVariables));
		LOGGER.info("Running: ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i "
				+ additionalVariables.get(ENV_PRIVATE_KEY_FILE) + " " + additionalVariables.get(ENV_SSH_USER) + "@"
				+ additionalVariables.get(ENV_HOST) + " " + parseEnvironVariable(commandString, additionalVariables));
		return builder.start();
	}

	private Process startLocalProcess(String command, Map<String, String> additionalEnvVariables) throws IOException {
		ProcessBuilder builder = new ProcessBuilder("/bin/bash", "-c", command);
		if (additionalEnvVariables == null || additionalEnvVariables.isEmpty()) {
			return builder.start();
		}
		
		for (String envVariable : additionalEnvVariables.keySet()) {
			builder.environment().put(envVariable, additionalEnvVariables.get(envVariable));
		}
		LOGGER.info("Command: " + builder.command().toString());
		return builder.start();
	}

	private String parseEnvironVariable(String commandString, Map<String, String> additionalVariables) {
		for (String envVar : additionalVariables.keySet()) {
			commandString = commandString.replace(envVar, additionalVariables.get(envVar));
		}
		return commandString;
	}

	// TODO getEnvVariables from task
	protected Map<String, String> getAdditionalEnvVariables(AbstractResource resource) {
		Map<String, String> additionalEnvVar = new HashMap<>();

		setPublicIp(resource, additionalEnvVar);
		setUser(resource, additionalEnvVar);
		setPrivateKeyFilePath(additionalEnvVar);
		setUuid(additionalEnvVar);

		return additionalEnvVar;
	}

	private void setPublicIp(AbstractResource resource, Map<String, String> additionalEnvVar) {
		additionalEnvVar.put(ENV_HOST, resource.getMetadataValue(AbstractResource.METADATA_SSH_PUBLIC_IP));
		LOGGER.info("SSH - Host: " + resource.getMetadataValue(AbstractResource.METADATA_SSH_PUBLIC_IP));
	}

	private void setUser(AbstractResource resource, Map<String, String> additionalEnvVar) {
		String specUsername = this.specification.getUsername();
		String metadataUsername = resource.getMetadataValue(AbstractResource.METADATA_SSH_USERNAME_ATT);

		if (validateUsername(specUsername)) {
			additionalEnvVar.put(ENV_SSH_USER, specUsername);
			loggerUser(specUsername);
		} else if (validateUsername(metadataUsername)) {
			additionalEnvVar.put(ENV_SSH_USER, metadataUsername);
			loggerUser(metadataUsername);
		}
	}

	private boolean validateUsername(String username) {
		return (username != null) && !(username.isEmpty());
	}

	private void loggerUser(String username) {
		LOGGER.info("SSH - User: " + username);
	}

	private void setPrivateKeyFilePath(Map<String, String> additionalEnvVar) {
		additionalEnvVar.put(ENV_PRIVATE_KEY_FILE, this.specification.getPrivateKeyFilePath());
		LOGGER.info("SSH - Private key file path: " + this.specification.getPrivateKeyFilePath());
	}

	private void setUuid(Map<String, String> additionalEnvVar) {
		additionalEnvVar.put(ENV_UUID, this.uuid);
		LOGGER.info("SSH - UUID: " + this.uuid);
	}

	@Override
	public TaskState getTaskState() {
		return this.taskState;
	}

	@Override
	public Specification getSpecification() {
		return this.specification;
	}

	@Override
	public AbstractResource getResource() {
		return resource;
	}
	
}
