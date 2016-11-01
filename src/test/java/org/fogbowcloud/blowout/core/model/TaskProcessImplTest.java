package org.fogbowcloud.blowout.core.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
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
		FogbowResource resource = mock(FogbowResource.class);

		TaskProcessImpl tp = spy(new TaskProcessImpl(taskId, commandList, spec));

		doReturn(0).when(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		//
		tp.executeTask(resource);

		verify(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		assertEquals(tp.getStatus(), TaskState.FINNISHED);
	}

	@Test
	public void testExecOneCommandItFails() {
		String taskId = FAKE_TASK_ID;
		Specification spec = mock(Specification.class);
		List<Command> commandList = new ArrayList<Command>();
		commandList.add(new Command(FAKE_COMMAND, Command.Type.LOCAL));
		FogbowResource resource = mock(FogbowResource.class);

		TaskProcessImpl tp = spy(new TaskProcessImpl(taskId, commandList, spec));

		doReturn(1).when(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);

		tp.executeTask(resource);

		assertEquals(tp.getStatus(), TaskState.FAILED);

	}

	@Test
	public void testExecThreeCommands() {
		String taskId = FAKE_TASK_ID;
		Specification spec = mock(Specification.class);
		List<Command> commandList = new ArrayList<Command>();
		commandList.add(new Command(FAKE_COMMAND, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND2, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND3, Command.Type.LOCAL));
		FogbowResource resource = mock(FogbowResource.class);

		TaskProcessImpl tp = spy(new TaskProcessImpl(taskId, commandList, spec));

		doReturn(0).when(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		doReturn(0).when(tp).executeCommandString(FAKE_COMMAND2, Command.Type.LOCAL, resource);
		doReturn(0).when(tp).executeCommandString(FAKE_COMMAND3, Command.Type.LOCAL, resource);

		tp.executeTask(resource);

		verify(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		verify(tp).executeCommandString(FAKE_COMMAND2, Command.Type.LOCAL, resource);
		verify(tp).executeCommandString(FAKE_COMMAND3, Command.Type.LOCAL, resource);
		assertEquals(tp.getStatus(), TaskState.FINNISHED);
	}

	@Test
	public void testExecThreeCommandsSecondFails() {
		String taskId = FAKE_TASK_ID;
		Specification spec = mock(Specification.class);
		List<Command> commandList = new ArrayList<Command>();
		commandList.add(new Command(FAKE_COMMAND, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND2, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND3, Command.Type.LOCAL));
		FogbowResource resource = mock(FogbowResource.class);

		TaskProcessImpl tp = spy(new TaskProcessImpl(taskId, commandList, spec));

		doReturn(0).when(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		doReturn(1).when(tp).executeCommandString(FAKE_COMMAND2, Command.Type.LOCAL, resource);

		tp.executeTask(resource);

		verify(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		verify(tp).executeCommandString(FAKE_COMMAND2, Command.Type.LOCAL, resource);
		verify(tp, never()).executeCommandString(FAKE_COMMAND3, Command.Type.LOCAL, resource);
		assertEquals(tp.getStatus(), TaskState.FAILED);
	}

	@Test
	public void testExecThreeCommandsFirstFails() {
		String taskId = FAKE_TASK_ID;
		Specification spec = mock(Specification.class);
		List<Command> commandList = new ArrayList<Command>();
		commandList.add(new Command(FAKE_COMMAND, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND2, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND3, Command.Type.LOCAL));
		FogbowResource resource = mock(FogbowResource.class);

		TaskProcessImpl tp = spy(new TaskProcessImpl(taskId, commandList, spec));

		doReturn(1).when(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);

		tp.executeTask(resource);

		verify(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		verify(tp, never()).executeCommandString(FAKE_COMMAND2, Command.Type.LOCAL, resource);
		verify(tp, never()).executeCommandString(FAKE_COMMAND3, Command.Type.LOCAL, resource);
		assertEquals(tp.getStatus(), TaskState.FAILED);
	}


}
