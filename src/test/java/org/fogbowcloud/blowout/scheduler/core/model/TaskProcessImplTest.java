package org.fogbowcloud.blowout.scheduler.core.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.fogbowcloud.manager.occi.model.Token;
import org.junit.Test;

public class TaskProcessImplTest {

	private static final String FAKE_TASK_ID = "fakeTaskId";
	private static final String FAKE_COMMAND = "fakeCommand";
	private static final String FAKE_COMMAND2 = "fakeCommand2";
	private static final String FAKE_COMMAND3 = "fakeCommand3";

	@Test
	public void testExecOneCommand() {
		String taskId = FAKE_TASK_ID;
		Specification spec = mock(Specification.class);
		Properties properties = new Properties();
		List<Command> commandList = new ArrayList<Command>();
		commandList.add(new Command(FAKE_COMMAND, Command.Type.LOCAL));
		Resource resource = mock(Resource.class);

		TaskProcessImpl tp = spy(new TaskProcessImpl(taskId, commandList, spec, "execution", new Token.User("9999", "User")));

		doReturn(0).when(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		//
		tp.executeTask(resource);

		verify(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		assertEquals(tp.getStatus(), TaskProcessImpl.State.FINNISHED);
	}

	@Test
	public void testExecOneCommandItFails() {
		String taskId = FAKE_TASK_ID;
		Specification spec = mock(Specification.class);
		List<Command> commandList = new ArrayList<Command>();
		commandList.add(new Command(FAKE_COMMAND, Command.Type.LOCAL));
		Resource resource = mock(Resource.class);

		TaskProcessImpl tp = spy(new TaskProcessImpl(taskId, commandList, spec, "execution", new Token.User("9999", "User")));

		doReturn(1).when(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);

		tp.executeTask(resource);

		assertEquals(tp.getStatus(), TaskProcessImpl.State.FAILED);

	}

	@Test
	public void testExecThreeCommands() {
		String taskId = FAKE_TASK_ID;
		Specification spec = mock(Specification.class);
		List<Command> commandList = new ArrayList<Command>();
		commandList.add(new Command(FAKE_COMMAND, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND2, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND3, Command.Type.LOCAL));
		Resource resource = mock(Resource.class);

		TaskProcessImpl tp = spy(new TaskProcessImpl(taskId, commandList, spec, "execution", new Token.User("9999", "User")));

		doReturn(0).when(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		doReturn(0).when(tp).executeCommandString(FAKE_COMMAND2, Command.Type.LOCAL, resource);
		doReturn(0).when(tp).executeCommandString(FAKE_COMMAND3, Command.Type.LOCAL, resource);

		tp.executeTask(resource);

		verify(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		verify(tp).executeCommandString(FAKE_COMMAND2, Command.Type.LOCAL, resource);
		verify(tp).executeCommandString(FAKE_COMMAND3, Command.Type.LOCAL, resource);
		assertEquals(tp.getStatus(), TaskProcessImpl.State.FINNISHED);
	}

	@Test
	public void testExecThreeCommandsSecondFails() {
		String taskId = FAKE_TASK_ID;
		Specification spec = mock(Specification.class);
		List<Command> commandList = new ArrayList<Command>();
		commandList.add(new Command(FAKE_COMMAND, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND2, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND3, Command.Type.LOCAL));
		Resource resource = mock(Resource.class);

		TaskProcessImpl tp = spy(new TaskProcessImpl(taskId, commandList, spec, "execution", new Token.User("9999", "User")));

		doReturn(0).when(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		doReturn(1).when(tp).executeCommandString(FAKE_COMMAND2, Command.Type.LOCAL, resource);

		tp.executeTask(resource);

		verify(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		verify(tp).executeCommandString(FAKE_COMMAND2, Command.Type.LOCAL, resource);
		verify(tp, never()).executeCommandString(FAKE_COMMAND3, Command.Type.LOCAL, resource);
		assertEquals(tp.getStatus(), TaskProcessImpl.State.FAILED);
	}

	@Test
	public void testExecThreeCommandsFirstFails() {
		String taskId = FAKE_TASK_ID;
		Specification spec = mock(Specification.class);
		List<Command> commandList = new ArrayList<Command>();
		commandList.add(new Command(FAKE_COMMAND, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND2, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND3, Command.Type.LOCAL));
		Resource resource = mock(Resource.class);

		TaskProcessImpl tp = spy(new TaskProcessImpl(taskId, commandList, spec, "execution", new Token.User("9999", "User")));

		doReturn(1).when(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);

		tp.executeTask(resource);

		verify(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		verify(tp, never()).executeCommandString(FAKE_COMMAND2, Command.Type.LOCAL, resource);
		verify(tp, never()).executeCommandString(FAKE_COMMAND3, Command.Type.LOCAL, resource);
		assertEquals(tp.getStatus(), TaskProcessImpl.State.FAILED);
	}

//	@Test
//	public void testExecuteTask(){
//
//		List<Command> commandsPrologue = new ArrayList<Command>();
//		List<Command> commandsRemote = new ArrayList<Command>();
//		List<Command> commandsEpilogue = new ArrayList<Command>();
//		Map<String, String> envVariables = new HashMap<String, String>();
//
//		String image = "image";
//		String userName = "userName";
//		String publicKey = "publicKey";
//		String privateKey = "privateKey";
//		String host = "10.100.0.1";
//		String port = "1091";
//		String userDataFile = "scripts/lvl-user-data.sh";
//		String userDataType = "text/x-shellscript";
//
//		Task task = prepareMockCommandsToExecute(commandsPrologue, commandsRemote, commandsEpilogue, envVariables,
//				image, userName, publicKey, privateKey, host, port, userDataFile, userDataType);
//
//		doReturn(TaskExecutionResult.OK).when(executionCommandHelperMock).execLocalCommands(Mockito.eq(commandsPrologue),
//				Mockito.eq(envVariables));
//		doReturn(TaskExecutionResult.OK).when(executionCommandHelperMock).execRemoteCommands(Mockito.eq(host),
//				Mockito.eq(Integer.parseInt(port)), Mockito.eq(userName), Mockito.eq(privateKey), Mockito.eq(commandsRemote));
//		doReturn(TaskExecutionResult.OK).when(executionCommandHelperMock).execLocalCommands(Mockito.eq(commandsEpilogue),
//				Mockito.eq(envVariables));
//
//		resource.executeTask(task);
//
//		verify(executionCommandHelperMock, times(1)).execLocalCommands(Mockito.eq(commandsPrologue), Mockito.eq(envVariables));
//		verify(executionCommandHelperMock, times(1)).execRemoteCommands(Mockito.eq(host),
//				Mockito.eq(Integer.parseInt(port)), Mockito.eq(userName), Mockito.eq(privateKey), Mockito.eq(commandsRemote));
//		verify(executionCommandHelperMock, times(1)).execLocalCommands(Mockito.eq(commandsEpilogue), Mockito.eq(envVariables));
//		assertEquals(TaskExecutionResult.OK, resource.getTaskExecutionResult().getExitValue());
//
//		commandsPrologue.clear();
//		commandsPrologue = null;
//		commandsRemote.clear();
//		commandsRemote = null;
//		commandsEpilogue.clear();
//		commandsEpilogue = null;
//		envVariables.clear();
//		envVariables = null;
//	}


//
//
//	@Test
//	public void testExecuteTaskFail(){
//
//		List<Command> commandsPrologue = new ArrayList<Command>();
//		List<Command> commandsRemote = new ArrayList<Command>();
//		List<Command> commandsEpilogue = new ArrayList<Command>();
//		Map<String, String> envVariables = new HashMap<String, String>();
//
//		String image = "image";
//		String userName = "userName";
//		String publicKey = "publicKey";
//		String privateKey = "privateKey";
//		String host = "10.100.0.1";
//		String port = "1091";
//		String userDataFile = "scripts/lvl-user-data.sh";
//		String userDataType = "text/x-shellscript";
//
//		Task task = prepareMockCommandsToExecute(commandsPrologue, commandsRemote, commandsEpilogue, envVariables,
//				image, userName, publicKey, privateKey, host, port, userDataFile, userDataType);
//
//		doReturn(TaskExecutionResult.OK).when(executionCommandHelperMock).execLocalCommands(Mockito.eq(commandsPrologue),
//				Mockito.eq(envVariables));
//		doReturn(TaskExecutionResult.NOK).when(executionCommandHelperMock).execRemoteCommands(Mockito.eq(host),
//				Mockito.eq(Integer.parseInt(port)), Mockito.eq(userName), Mockito.eq(privateKey), Mockito.eq(commandsRemote));
//		doReturn(TaskExecutionResult.OK).when(executionCommandHelperMock).execLocalCommands(Mockito.eq(commandsEpilogue),
//				Mockito.eq(envVariables));
//
//		resource.executeTask(task);
//
//		verify(executionCommandHelperMock, times(1)).execLocalCommands(Mockito.eq(commandsPrologue), Mockito.eq(envVariables));
//		verify(executionCommandHelperMock, times(1)).execRemoteCommands(Mockito.eq(host),
//				Mockito.eq(Integer.parseInt(port)), Mockito.eq(userName), Mockito.eq(privateKey), Mockito.eq(commandsRemote));
//		assertEquals(TaskExecutionResult.NOK, resource.getTaskExecutionResult().getExitValue());
//
//		commandsPrologue.clear();
//		commandsPrologue = null;
//		commandsRemote.clear();
//		commandsRemote = null;
//		commandsEpilogue.clear();
//		commandsEpilogue = null;
//		envVariables.clear();
//		envVariables = null;
//
//	}



//	private Task prepareMockCommandsToExecute(List<Command> commandsPrologue, List<Command> commandsRemote,
//			List<Command> commandsEpilogue, Map<String, String> envVariables, String image, String userName,
//			String publicKey, String privateKey, String host, String port, String userDataFile, String userDataType) {
//
//		Specification spec = new Specification(image, userName, publicKey, privateKey, userDataFile, userDataType);
//
//		resource.putMetadata(Resource.METADATA_IMAGE, "image");
//		resource.putMetadata(Resource.METADATA_PUBLIC_KEY, "publicKey");
//		resource.putMetadata(Resource.METADATA_SSH_HOST, host);
//		resource.putMetadata(Resource.METADATA_SSH_PORT, port);
//
//		Command c1 = new Command("command_01", Command.Type.LOCAL);
//		Command c2 = new Command("command_02", Command.Type.REMOTE);
//		Command c3 = new Command("command_03", Command.Type.EPILOGUE);
//
//		commandsPrologue.add(c1);
//		commandsRemote.add(c2);
//		commandsEpilogue.add(c3);
//
//		envVariables.put(Resource.ENV_HOST, host);
//		envVariables.put(Resource.ENV_SSH_PORT, port);
//		envVariables.put(Resource.ENV_SSH_USER, userName);
//		envVariables.put(Resource.ENV_PRIVATE_KEY_FILE, privateKey);
//
//		Task task = mock(Task.class);
//		doReturn("Task_01").when(task).getId();
//		doReturn(spec).when(task).getSpecification();
//
//		doReturn(commandsPrologue).when(task).getCommandsByType(Command.Type.LOCAL);
//		doReturn(commandsRemote).when(task).getCommandsByType(Command.Type.REMOTE);
//		doReturn(commandsEpilogue).when(task).getCommandsByType(Command.Type.EPILOGUE);
//		return task;
//	}
//
//
//	private void generateDefaulProperties(){
//
//		properties = new Properties();
//
//		properties.setProperty(AppPropertiesConstants.INFRA_IS_STATIC, "false");
//		properties.setProperty(AppPropertiesConstants.INFRA_PROVIDER_CLASS_NAME,
//				"org.fogbowcloud.scheduler.infrastructure.fogbow.FogbowInfrastructureProvider");
//		properties.setProperty(AppPropertiesConstants.INFRA_ORDER_SERVICE_TIME, "2000");
//		properties.setProperty(AppPropertiesConstants.INFRA_RESOURCE_SERVICE_TIME, "3000");
//		properties.setProperty(AppPropertiesConstants.INFRA_RESOURCE_CONNECTION_TIMEOUT, "10000");
//		properties.setProperty(AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME, "300000");
//		properties.setProperty(AppPropertiesConstants.INFRA_INITIAL_SPECS_FILE_PATH, "src/test/resources/Specs_Json");
//		properties.setProperty(AppPropertiesConstants.INFRA_SPECS_BLOCK_CREATING, "false");
//		properties.setProperty(AppPropertiesConstants.INFRA_FOGBOW_MANAGER_BASE_URL, "100_02_01_01:8098");
//		properties.setProperty(AppPropertiesConstants.INFRA_FOGBOW_TOKEN_PUBLIC_KEY_FILEPATH,
//				"src/test/resources/publickey_file");
//
//	}
}
