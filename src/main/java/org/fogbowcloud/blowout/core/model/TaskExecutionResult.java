package org.fogbowcloud.blowout.core.model;

public class TaskExecutionResult {

	public static final int OK = 0;
	public static final int NOK = 128;
	public static final int TIMEOUT = 124;
	
	private boolean taskFinished = false;
	private int exitValue = -1;
	
	public int getExitValue() {
		return exitValue;
	}

	public boolean isExecutionFinished() {
		return taskFinished;
	}

	public void finish(int exitValue) {
		this.taskFinished = true;
		this.exitValue = exitValue;
	}

	public String toString() {
		return "taskFinished=" + taskFinished + ", exitValue=" + exitValue;
	}
}
