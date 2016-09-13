package org.fogbowcloud.blowout.scheduler.core;

import org.fogbowcloud.blowout.scheduler.core.model.TaskProcess;

public class CommonTaskExecutionChecker extends TaskExecutionChecker {

	public CommonTaskExecutionChecker(TaskProcess tp, Scheduler scheduler) {
		super(tp, scheduler);
	}

	@Override
	public void Failure(TaskProcess tp) {
	}

	@Override
	public void Completion(TaskProcess tp) {
	}

}
