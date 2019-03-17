package org.fogbowcloud.blowout.core.model.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.fogbowcloud.blowout.helpers.Constants.*;

import java.util.ArrayList;
import java.util.List;

import org.fogbowcloud.blowout.core.model.*;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.junit.Assert;
import org.junit.Test;

public class TaskProcessImplTest {

	private static final String FAKE_COMMAND_B = FAKE_COMMAND + POSTFIX_B;
	private static final String FAKE_COMMAND_C = FAKE_COMMAND + POSTFIX_C;

	@Test
	public void testExecOneCommand() {
		String taskId = FAKE_TASK_ID;
		Specification spec = mock(Specification.class);
		List<Command> commandList = new ArrayList<Command>();
		commandList.add(new Command(FAKE_COMMAND, Command.Type.LOCAL));
		FogbowResource resource = mock(FogbowResource.class);

		TaskProcessImpl tp = spy(new TaskProcessImpl(taskId, commandList, spec, FAKE_UUID));

		TaskExecutionResult terSuccess = new TaskExecutionResult();
		terSuccess.finish(0);
		
		doReturn(terSuccess).when(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		//
		tp.executeTask(resource);

		verify(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		Assert.assertEquals(tp.getTaskState(), TaskState.FINISHED);
	}

	@Test
	public void testExecOneCommandItFails() {
		String taskId = FAKE_TASK_ID;
		Specification spec = mock(Specification.class);
		List<Command> commandList = new ArrayList<Command>();
		commandList.add(new Command(FAKE_COMMAND, Command.Type.LOCAL));
		FogbowResource resource = mock(FogbowResource.class);

		TaskProcessImpl tp = spy(new TaskProcessImpl(taskId, commandList, spec, FAKE_UUID));

		TaskExecutionResult terFail = new TaskExecutionResult();
		terFail.finish(1);
		
		doReturn(terFail).when(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);

		tp.executeTask(resource);

		assertEquals(tp.getTaskState(), TaskState.FAILED);

	}

	@Test
	public void testExecThreeCommands() {
		String taskId = FAKE_TASK_ID;
		Specification spec = mock(Specification.class);
		List<Command> commandList = new ArrayList<Command>();
		commandList.add(new Command(FAKE_COMMAND, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND_B, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND_C, Command.Type.LOCAL));
		FogbowResource resource = mock(FogbowResource.class);

		TaskProcessImpl tp = spy(new TaskProcessImpl(taskId, commandList, spec, FAKE_UUID));

		TaskExecutionResult terSuccess = new TaskExecutionResult();
		terSuccess.finish(0);
		
		doReturn(terSuccess).when(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		doReturn(terSuccess).when(tp).executeCommandString(FAKE_COMMAND_B, Command.Type.LOCAL, resource);
		doReturn(terSuccess).when(tp).executeCommandString(FAKE_COMMAND_C, Command.Type.LOCAL, resource);

		tp.executeTask(resource);

		verify(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		verify(tp).executeCommandString(FAKE_COMMAND_B, Command.Type.LOCAL, resource);
		verify(tp).executeCommandString(FAKE_COMMAND_C, Command.Type.LOCAL, resource);
		assertEquals(tp.getTaskState(), TaskState.FINISHED);
	}

	@Test
	public void testExecThreeCommandsSecondFails() {
		String taskId = FAKE_TASK_ID;
		Specification spec = mock(Specification.class);
		List<Command> commandList = new ArrayList<Command>();
		commandList.add(new Command(FAKE_COMMAND, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND_B, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND_C, Command.Type.LOCAL));
		FogbowResource resource = mock(FogbowResource.class);

		TaskProcessImpl tp = spy(new TaskProcessImpl(taskId, commandList, spec, FAKE_UUID));
		
		TaskExecutionResult terSuccess = new TaskExecutionResult();
		terSuccess.finish(0);
		TaskExecutionResult terFail = new TaskExecutionResult();
		terFail.finish(1);

		doReturn(terSuccess).when(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		doReturn(terFail).when(tp).executeCommandString(FAKE_COMMAND_B, Command.Type.LOCAL, resource);

		tp.executeTask(resource);

		verify(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		verify(tp).executeCommandString(FAKE_COMMAND_B, Command.Type.LOCAL, resource);
		verify(tp, never()).executeCommandString(FAKE_COMMAND_C, Command.Type.LOCAL, resource);
		assertEquals(tp.getTaskState(), TaskState.FAILED);
	}

	@Test
	public void testExecThreeCommandsFirstFails() {
		
		TaskExecutionResult ter = new TaskExecutionResult();
		ter.finish(1);
		
		String taskId = FAKE_TASK_ID;
		Specification spec = mock(Specification.class);
		List<Command> commandList = new ArrayList<Command>();
		commandList.add(new Command(FAKE_COMMAND, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND_B, Command.Type.LOCAL));
		commandList.add(new Command(FAKE_COMMAND_C, Command.Type.LOCAL));
		FogbowResource resource = mock(FogbowResource.class);

		TaskProcessImpl tp = spy(new TaskProcessImpl(taskId, commandList, spec, FAKE_UUID));

		doReturn(ter).when(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);

		tp.executeTask(resource);

		verify(tp).executeCommandString(FAKE_COMMAND, Command.Type.LOCAL, resource);
		verify(tp, never()).executeCommandString(FAKE_COMMAND_B, Command.Type.LOCAL, resource);
		verify(tp, never()).executeCommandString(FAKE_COMMAND_C, Command.Type.LOCAL, resource);
		assertEquals(tp.getTaskState(), TaskState.FAILED);
	}


}
