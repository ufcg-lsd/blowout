package org.fogbowcloud.blowout.core.model.task;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.fogbowcloud.blowout.helpers.Constants.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fogbowcloud.blowout.core.model.Command;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.helpers.Constants;
import org.junit.Before;
import org.junit.Test;

public class TaskImplTest {

	private String taskId;
	private Specification spec;
	private Task task;
	
	@Before
	public void setUp(){
		this.spec = mock(Specification.class);
		this.taskId = Constants.FakeData.TASK_ID;
		this.task = spy(new TaskImpl(taskId, spec, Constants.FakeData.UUID));
	}
	
	@Test
	public void testCheckTimeOutedNotTimeOuted(){
		this.task.startedRunning();
		
		doReturn(TIME_OUT_VALUE_BIG).when(task).getMetadata(TaskImpl.METADATA_TASK_TIMEOUT);
		assertFalse(task.checkTimeOuted());
	}
	
	@Test
	public void testCheckTimeOutedTimeOuted() throws InterruptedException{
		task.startedRunning();
		Thread.sleep(5);
		doReturn(TIME_OUT_VALUE_SMALL).when(task).getMetadata(TaskImpl.METADATA_TASK_TIMEOUT);
		assertTrue(task.checkTimeOuted());
	}
	
	@Test
	public void testCheckTimeOutedBadlyFormated() throws InterruptedException{
		task.startedRunning();
		Thread.sleep(5);
		doReturn(TIME_OUT_VALUE_GIBBERISH).when(task).getMetadata(TaskImpl.METADATA_TASK_TIMEOUT);
		assertFalse(task.checkTimeOuted());
	}
	
	@Test
	public void testCheckTimeOutedNullTimeOut() throws InterruptedException{
		task.startedRunning();
		Thread.sleep(5);
		doReturn(null).when(task).getMetadata(TaskImpl.METADATA_TASK_TIMEOUT);
		assertFalse(task.checkTimeOuted());
	}
	
	@Test
	public void testCheckTimeOutedEmptyTimeOut() throws InterruptedException{
		task.startedRunning();
		Thread.sleep(5);
		doReturn(TIME_OUT_VALUE_EMPTY).when(task).getMetadata(TaskImpl.METADATA_TASK_TIMEOUT);
		assertFalse(task.checkTimeOuted());
	}

	@Test
	public void testClone(){
		Map<String, String> metadata = new HashMap<String, String>();
		metadata.put(Constants.FakeData.METADATA, Constants.FakeData.METADATA_VALUE);
		doReturn(metadata).when(task).getAllMetadata();
		List<Command> commands = new ArrayList<Command>();
		Command command = new Command(Constants.FakeData.COMMAND, Command.Type.REMOTE);
		commands.add(command);
		doReturn(commands).when(task).getAllCommands();
		
		Task clonedTask = task.clone();
		
		assertEquals(commands, clonedTask.getAllCommands());
		assertEquals(metadata, clonedTask.getAllMetadata());
		assert(clonedTask.getId().contains("_clonedFrom_" + taskId));
	}
	
	@Test
	public void testGetCommandsByType(){
		List<Command> commands = new ArrayList<Command>();
		Command remoteCommand = new Command("fakeremotecomand", Command.Type.REMOTE);
		Command prologueCommand = new Command("fakeprologueCommand", Command.Type.LOCAL);
		Command epilogueCommand = new Command("fakeepilogueCommand", Command.Type.EPILOGUE);
		Command epilogueCommand2 = new Command("fakeepilogueCommand2", Command.Type.EPILOGUE);
		commands.add(remoteCommand);
		commands.add(epilogueCommand);
		commands.add(epilogueCommand2);
		commands.add(prologueCommand);
		doReturn(commands).when(task).getAllCommands();
		
		List<Command> remoteCommands = task.getCommandsByType(Command.Type.REMOTE);
		assertEquals(1, remoteCommands.size());
		assert(remoteCommands.contains(remoteCommand));
		
		List<Command> epilogueCommands = task.getCommandsByType(Command.Type.EPILOGUE);
		assertEquals(2, epilogueCommands.size());
		assert(epilogueCommands.contains(epilogueCommand));
		assert(epilogueCommands.contains(epilogueCommand2));
	}
}
