package org.fogbowcloud.blowout.core.model.task;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TaskExecutionResultTest {
	private TaskExecutionResult taskExecutionResult;
	private final int SUCCESS_EXIT_VALUE = 0;
	private final int INITIAL_EXIT_VALUE = -1;

	@Before
	public void setUp() {
		this.taskExecutionResult = new TaskExecutionResult();
	}

	@Test
	public void testGetExitValue() {
		int exitValue = INITIAL_EXIT_VALUE;
		assertEquals(this.taskExecutionResult.getExitValue(), exitValue);

		exitValue = SUCCESS_EXIT_VALUE;
		this.taskExecutionResult.finish(exitValue);

		assertEquals(this.taskExecutionResult.getExitValue(), exitValue);
	}

	@Test
	public void testIsExecutionFinished() {
		assertFalse(this.taskExecutionResult.isExecutionFinished());
		this.taskExecutionResult.finish(SUCCESS_EXIT_VALUE);
		assertTrue(this.taskExecutionResult.isExecutionFinished());
	}

	@Test
	public void testFinish() {
		assertFalse(this.taskExecutionResult.isExecutionFinished());
		assertEquals(this.taskExecutionResult.getExitValue(), INITIAL_EXIT_VALUE);

		this.taskExecutionResult.finish(SUCCESS_EXIT_VALUE);

		assertTrue(this.taskExecutionResult.isExecutionFinished());
		assertEquals(this.taskExecutionResult.getExitValue(), SUCCESS_EXIT_VALUE);

	}
}