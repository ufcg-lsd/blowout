package org.fogbowcloud.blowout.core.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.util.AppUtil;
import org.fogbowcloud.blowout.pool.AbstractResource;

import static java.lang.Thread.sleep;

public class TaskProcessImpl implements TaskProcess {

	private static final Logger LOGGER = Logger.getLogger(TaskProcessImpl.class);

	public static final String ENV_HOST = "HOST";
	public static final String ENV_SSH_USER = "SSH_USER";
	public static final String ENV_PRIVATE_KEY_FILE = "PRIVATE_KEY_FILE";

	private static final String UserID = "UUID";
	private final String taskId;
	private TaskState status;
	private final List<Command> commandList;
	private final Specification spec;
	private String processId;
	private String userId;
	private AbstractResource resource;
	private String userIdValue;

	public TaskProcessImpl(String taskId, List<Command> commandList, Specification spec, String UserId) {
		this.processId = AppUtil.generateIdentifier();
		this.taskId = taskId;
		this.status = TaskState.READY;
		this.spec = spec;
		this.commandList = commandList;
		this.userId = UserID;
		this.userIdValue = UserId;
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

		TaskExecutionResult taskExecutionResult = new TaskExecutionResult();

		this.setStatus(TaskState.RUNNING);
		LOGGER.debug("Task : " + taskId + " is running. ");
		for (Command command : this.getCommands()) {
			LOGGER.info("Command " + command.getCommand());
			LOGGER.info("Command Type " + command.getType());
			String commandString = getExecutableCommandString(command);

			taskExecutionResult = executeCommandString(commandString, command.getType(), resource);
			LOGGER.info("Command result: " + taskExecutionResult.getExitValue());
			if (taskExecutionResult.getExitValue() != TaskExecutionResult.OK) {
				if(taskExecutionResult.getExitValue() == TaskExecutionResult.TIMEOUT) {
					this.setStatus(TaskState.TIMEDOUT);
					break;
				}
				this.setStatus(TaskState.FAILED);
				break;
			}
		}
		if (!this.getStatus().equals(TaskState.FAILED)
				&& !this.getStatus().equals(TaskState.TIMEDOUT)) {
			this.setStatus(TaskState.FINISHED);
		}
		return taskExecutionResult;
	}

	@Override
	public void setStatus(TaskState status) {
		this.status = status;
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
			commandString.replace(envVar, additionalVariables.get(envVar));
		}
		return commandString;
	}

	// TODO getEnvVariables from task
	protected Map<String, String> getAdditionalEnvVariables(AbstractResource resource) {
		Map<String, String> additionalEnvVar = new HashMap<>();

		setPublicIp(resource, additionalEnvVar);
		setUser(resource, additionalEnvVar);
		setPrivateKeyFilePath(additionalEnvVar);
		setUserId(additionalEnvVar);

		return additionalEnvVar;
	}

	private void setPublicIp(AbstractResource resource, Map<String, String> additionalEnvVar) {
		additionalEnvVar.put(ENV_HOST, resource.getMetadataValue(AbstractResource.METADATA_SSH_PUBLIC_IP));
		LOGGER.info("SSH - Host: " + resource.getMetadataValue(AbstractResource.METADATA_SSH_PUBLIC_IP));
	}

	private void setUser(AbstractResource resource, Map<String, String> additionalEnvVar) {
		String specUsername = this.spec.getUsername();
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
		additionalEnvVar.put(ENV_PRIVATE_KEY_FILE, this.spec.getPrivateKeyFilePath());
		LOGGER.info("SSH - Private key file path: " + this.spec.getPrivateKeyFilePath());
	}

	private void setUserId(Map<String, String> additionalEnvVar) {
		additionalEnvVar.put(UserID, this.userId);
		LOGGER.info("SSH - User ID: " + this.userId);
	}

	@Override
	public TaskState getStatus() {
		return this.status;
	}

	@Override
	public Specification getSpecification() {
		return this.spec;
	}

	@Override
	public AbstractResource getResource() {
		return resource;
	}
	
}
