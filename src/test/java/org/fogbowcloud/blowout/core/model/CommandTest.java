package org.fogbowcloud.blowout.core.model;
import org.fogbowcloud.blowout.helpers.TestsUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.junit.Assert.*;
import static org.fogbowcloud.blowout.helpers.Constants.*;

public class CommandTest {

	private Command command;

	@Before
	public void setUp() {
		this.command = new Command(FAKE_COMMAND, COMMAND_TYPE_DEFAULT);
	}

	@Test
	public void testClone() {
		assertTrue(TestsUtils.isJSONValid(JSON_BODY_COMMAND));
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
		assertTrue(TestsUtils.isJSONValid(JSON_BODY_COMMAND));
		assertTrue(TestsUtils.isJSONValid(JSON_BODY_COMMAND_RUNNING));
		JSONObject actualForm = this.command.toJSON();

		JSONAssert.assertEquals(JSON_BODY_COMMAND, actualForm, true);
		JSONAssert.assertEquals(JSON_BODY_COMMAND, actualForm, true);

		this.command.setState(Command.State.RUNNING);
		actualForm = this.command.toJSON();

		JSONAssert.assertEquals(JSON_BODY_COMMAND_RUNNING, actualForm, true);
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