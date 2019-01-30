package org.fogbowcloud.blowout.core.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.util.AppUtil;
import org.fogbowcloud.blowout.pool.AbstractResource;

public class TaskProcessImpl implements TaskProcess {

	private static final Logger LOGGER = Logger.getLogger(TaskProcessImpl.class);

	public static final String ENV_HOST = "HOST";
	public static final String ENV_SSH_PORT = "SSH_PORT";
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
		this.processId = AppUtil.generateRandomIdentifier();
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
		return new ArrayList<Command>(this.commandList);
	}

	@Override
	public TaskExecutionResult executeTask(AbstractResource resource) {
		this.resource = resource;

		TaskExecutionResult taskExecutionResult = new TaskExecutionResult();

		this.setStatus(TaskState.RUNNING);
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
		if (!this.getStatus().equals(TaskState.FAILED)) {
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
						+ "@" + additionalVariables.get(ENV_HOST) + " -p " + additionalVariables.get(ENV_SSH_PORT) + " "
						+ parseEnvironVariable(commandString, additionalVariables));
		LOGGER.debug("ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i "
				+ additionalVariables.get(ENV_PRIVATE_KEY_FILE) + " " + additionalVariables.get(ENV_SSH_USER) + "@"
				+ additionalVariables.get(ENV_HOST) + " -p " + additionalVariables.get(ENV_SSH_PORT) + " "
				+ parseEnvironVariable(commandString, additionalVariables));
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
		LOGGER.debug("Command: " + builder.command().toString());
		return builder.start();
	}

	private String parseEnvironVariable(String commandString, Map<String, String> additionalVariables) {
		for (String envVar : additionalVariables.keySet()) {
			commandString.replace(envVar, additionalVariables.get(envVar));
		}
		return commandString;
	}

	protected Map<String, String> getAdditionalEnvVariables(AbstractResource resource) {

		Map<String, String> additionalEnvVar = new HashMap<String, String>();
		additionalEnvVar.put(ENV_HOST, resource.getMetadataValue(AbstractResource.METADATA_SSH_PUBLIC_IP));
		LOGGER.debug("Env_host:" + resource.getMetadataValue(AbstractResource.METADATA_SSH_PUBLIC_IP));
		if (this.spec.getUsername() != null && !this.spec.getUsername().isEmpty()) {
			additionalEnvVar.put(ENV_SSH_USER, this.spec.getUsername());
			LOGGER.debug("Env_ssh_user:" + this.spec.getUsername());
		} else if (resource.getMetadataValue(ENV_SSH_USER) != null && !resource.getMetadataValue(ENV_SSH_USER).isEmpty()) {
			additionalEnvVar.put(ENV_SSH_USER, resource.getMetadataValue(ENV_SSH_USER));
			LOGGER.debug("Env_ssh_user:" + resource.getMetadataValue(ENV_SSH_USER));
		}  else {
			additionalEnvVar.put(ENV_SSH_USER, resource.getMetadataValue(AbstractResource.METADATA_SSH_USERNAME_ATT));
			LOGGER.debug("Env_ssh_user:" + resource.getMetadataValue(AbstractResource.METADATA_SSH_USERNAME_ATT));
		}
		additionalEnvVar.put(ENV_PRIVATE_KEY_FILE, spec.getPrivateKeyFilePath());

		additionalEnvVar.put(UserID, this.userId);
		LOGGER.debug("Env_private_key_file:" + spec.getPrivateKeyFilePath());
		// TODO getEnvVariables from task

		return additionalEnvVar;
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
