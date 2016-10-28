package org.fogbowcloud.blowout.core;

import org.fogbowcloud.blowout.core.model.TaskProcess;

public class CommonTaskExecutionChecker extends TaskExecutionChecker {

	public CommonTaskExecutionChecker(TaskProcess tp, Scheduler scheduler) {
		super(tp, scheduler);
	}

	@Override
	public void failure(TaskProcess tp) {
	}

	@Override
	public void completion(TaskProcess tp) {
	}

}
