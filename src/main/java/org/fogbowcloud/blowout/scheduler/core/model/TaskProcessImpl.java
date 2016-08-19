package org.fogbowcloud.blowout.scheduler.core.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.scheduler.core.Scheduler;

public class TaskProcessImpl implements TaskProcess {
	public enum State {
		READY, RUNNING, FINNISHED, FAILED
	}

	private static final Logger LOGGER = Logger.getLogger(TaskProcessImpl.class);
	
	int COMMAND_EXECUTION_OK = 0;
	
	public static final String ENV_HOST = "HOST";
	public static final String ENV_SSH_PORT = "SSH_PORT";
	public static final String ENV_SSH_USER = "SSH_USER";
	public static final String ENV_PRIVATE_KEY_FILE = "PRIVATE_KEY_FILE";
	
	public static final String METADATA_SSH_HOST = "metadataSSHHost";
	public static final String METADATA_SSH_PORT = "metadataSSHPort";
	public static final String METADATA_SSH_USERNAME_ATT = "metadateSshUsername";
	public static final String METADATA_EXTRA_PORTS_ATT = "metadateExtraPorts";

	private final String taskId;

	
	//TODO choose between setResource + exec or exec(resource)
	private Resource resource;

	private State status;

	private final List<Command> commandList;

	private final Specification spec;

	private final String localCommandInterpreter;
	
	private String processId;

	public TaskProcessImpl(String taskId, List<Command> commandList, Specification spec, String interpreter) {
		//check parameters?
		this.processId = UUID.randomUUID().toString();
		this.taskId = taskId;
		this.status = State.READY;
		this.spec = spec;
		this.commandList = commandList;
		//extract string to constants
		localCommandInterpreter = interpreter;
		
	}

	public String getProcessId() {
		return this.processId;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fogbowcloud.blowout.scheduler.core.model.TaskProcess#getTaskId()
	 */
	@Override
	public String getTaskId() {
		return this.taskId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.fogbowcloud.blowout.scheduler.core.model.TaskProcess#getCommands()
	 */
	@Override
	public List<Command> getCommands() {
		//Retorna uma copia
		return this.commandList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.fogbowcloud.blowout.scheduler.core.model.TaskProcess#executeTask()
	 */
	@Override
	public void executeTask(Resource resource) {
		this.setStatus(State.RUNNING);
		for (Command command : this.getCommands()) {
			LOGGER.debug("Command " + command.getCommand());
			LOGGER.debug("Command Type " + command.getType());
			String commandString = getExecutableCommandString(command);
			
			int executionResult = executeCommandString(commandString, command.getType(), resource);
			LOGGER.debug("Command result: " + executionResult);
			if (executionResult != COMMAND_EXECUTION_OK) {
				this.setStatus(State.FAILED);
				break;
			}
		}
		if (!this.getStatus().equals(State.FAILED)) {
			this.setStatus(State.FINNISHED);
		}
	}

	private void setStatus(State status) {
		this.status = status;
	}

	private String getExecutableCommandString(Command command) {
		if (command.getType().equals(Command.Type.LOCAL)) {
			return command.getCommand();
		} else {
			return command.getCommand();
		}
	}

	protected int executeCommandString(String commandString, Command.Type type, Resource resource) {
		Map<String, String> additionalVariables = getAdditionalEnvVariables(resource);
		if (type.equals(Command.Type.LOCAL)) {
			//remove duplications
			try {
				Process localProc = startLocalProcess(commandString, additionalVariables);
				int returnValue = localProc.waitFor();
				return returnValue;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				Process remoteProc = startRemoteProcess(commandString, additionalVariables);
				int returnValue = remoteProc.waitFor();
				return returnValue;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;
	}

	private Process startRemoteProcess(String commandString, Map<String, String> additionalVariables) throws IOException {
		ProcessBuilder builder = new ProcessBuilder(localCommandInterpreter, "-c",
				"ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i " + additionalVariables.get(ENV_PRIVATE_KEY_FILE) + " "
						+ additionalVariables.get(ENV_SSH_USER) + "@" + additionalVariables.get(ENV_HOST) + " -p " + additionalVariables.get(ENV_SSH_PORT) + " " + parseEnvironVariable(commandString, additionalVariables));
		LOGGER.debug("ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i " + additionalVariables.get(ENV_PRIVATE_KEY_FILE) + " "
				+ additionalVariables.get(ENV_SSH_USER) + "@" + additionalVariables.get(ENV_HOST) + " -p " + additionalVariables.get(ENV_SSH_PORT) + " " + parseEnvironVariable(commandString, additionalVariables));
		return builder.start();

	}
	

	private Process startLocalProcess(String command, Map<String, String> additionalEnvVariables)
			throws IOException {
		ProcessBuilder builder = new ProcessBuilder(localCommandInterpreter, "-c",
				command);
		if (additionalEnvVariables == null || additionalEnvVariables.isEmpty()) {
			return builder.start();	
		}
		
		// adding additional environment variables related to resource and/or task
		for (String envVariable : additionalEnvVariables.keySet()) {
			builder.environment().put(envVariable, additionalEnvVariables.get(envVariable));
		}
		return builder.start();
	}
	
	private String parseEnvironVariable(String commandString, Map<String, String> additionalVariables) {
		for (String envVar : additionalVariables.keySet()) {
			commandString.replace(envVar, additionalVariables.get(envVar));
		}
		return commandString;
	}
	
	protected Map<String, String> getAdditionalEnvVariables(Resource resource) {
		Map<String, String> additionalEnvVar = new HashMap<String, String>();
		additionalEnvVar.put(ENV_HOST, resource.getMetadataValue(METADATA_SSH_HOST));
		LOGGER.debug("Env_host:" + resource.getMetadataValue(METADATA_SSH_HOST));
		additionalEnvVar.put(ENV_SSH_PORT, resource.getMetadataValue(METADATA_SSH_PORT));
		LOGGER.debug("Env_ssh_port:" + resource.getMetadataValue(METADATA_SSH_PORT));
		if (this.spec.getUsername() != null && !this.spec.getUsername().isEmpty()) {
			additionalEnvVar.put(ENV_SSH_USER, this.spec.getUsername());
			LOGGER.debug("Env_ssh_user:" + this.spec.getUsername());
		} else {
			additionalEnvVar.put(ENV_SSH_USER, resource.getMetadataValue(ENV_SSH_USER));
			LOGGER.debug("Env_ssh_user:" + resource.getMetadataValue(ENV_SSH_USER));
		}
		additionalEnvVar.put(ENV_PRIVATE_KEY_FILE, spec.getPrivateKeyFilePath());
		LOGGER.debug("Env_private_key_file:" + spec.getPrivateKeyFilePath());
		// TODO getEnvVariables from task

		return additionalEnvVar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.fogbowcloud.blowout.scheduler.core.model.TaskProcess#getStatus()
	 */
	@Override
	public State getStatus() {
		return this.status;
	}

	@Override
	public Specification getSpecification() {
		return this.spec;
	}

}
