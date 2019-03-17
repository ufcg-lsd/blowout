package org.fogbowcloud.blowout.core.model;
import org.fogbowcloud.blowout.helpers.TestsUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.Assert.*;

public class CommandTest {

	private static final String FAKE_COMMAND = "echo fake-echo";
	private static final Command.Type COMMAND_TYPE_DEFAULT = Command.Type.REMOTE;
	private static final String COMMAND_JSON = "{" +
			"\"command\": \"echo fake-echo\", " +
			"\"state\": \"QUEUED\", " +
			"\"type\": \"REMOTE\"" +
			"}";
	private static final String COMMAND_JSON_RUNNING = "{" +
			"\"command\": \"echo fake-echo\", " +
			"\"state\": \"RUNNING\", " +
			"\"type\": \"REMOTE\"" +
			"}";

	private Command command;

	@Before
	public void setUp() {
		this.command = new Command(FAKE_COMMAND, COMMAND_TYPE_DEFAULT);
	}

	@Test
	public void testClone() {
		assertTrue(TestsUtils.isJSONValid(COMMAND_JSON));
		assertEquals(this.command.getState(), Command.State.QUEUED);


		final Command commandB = this.command.clone();
		assertNotEquals(null, commandB);
		assertEquals(this.command.getCommand(), commandB.getCommand());
		assertEquals(this.command.getType(), commandB.getType());
		assertEquals(this.command.getState(), commandB.getState());

		assertEquals(this.command, commandB);

	}

	@Test
	public void testToJSON() {
		assertTrue(TestsUtils.isJSONValid(COMMAND_JSON));
		assertTrue(TestsUtils.isJSONValid(COMMAND_JSON_RUNNING));
		JSONObject actualForm = this.command.toJSON();

		JSONAssert.assertEquals(COMMAND_JSON, actualForm, true);
		JSONAssert.assertEquals(COMMAND_JSON, actualForm, true);

		this.command.setState(Command.State.RUNNING);
		actualForm = this.command.toJSON();

		JSONAssert.assertEquals(COMMAND_JSON_RUNNING, actualForm, true);
	}

	@Test
	public void testFromJSON() {
		Command expectedCommand = Command.fromJSON(this.command.toJSON());

		assertEquals(this.command, expectedCommand);

		this.command.setState(Command.State.RUNNING);
		expectedCommand = Command.fromJSON(this.command.toJSON());

		assertEquals(expectedCommand, this.command);
	}
}