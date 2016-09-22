package org.fogbowcloud.blowout.scheduler.core;

import org.fogbowcloud.blowout.scheduler.core.model.Job;
import org.fogbowcloud.blowout.scheduler.core.model.TaskProcess;
import org.fogbowcloud.blowout.scheduler.core.model.TaskProcessImpl;

public abstract class TaskExecutionChecker implements Runnable {

	protected TaskProcess tp;
	protected Scheduler scheduler;
	protected Job job;

	public TaskExecutionChecker(TaskProcess tp, Scheduler scheduler) {
		this.tp = tp;
		this.scheduler = scheduler;
	}

	@Override
	public void run() {

		if (tp.getStatus().equals(TaskProcessImpl.State.FAILED)) {
			scheduler.taskFailed(tp);
			Failure(tp);
			return;
		}

		if (tp.getStatus().equals(TaskProcessImpl.State.FINNISHED)) {
			scheduler.taskCompleted(tp);
			Completion(tp);
			return;
		}
	}
	
	public abstract void Failure(TaskProcess tp);
	
	public abstract void Completion(TaskProcess tp);
}

