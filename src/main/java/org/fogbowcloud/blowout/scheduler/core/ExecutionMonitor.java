package org.fogbowcloud.blowout.scheduler.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.scheduler.core.model.Job;
import org.fogbowcloud.blowout.scheduler.core.model.Resource;
import org.fogbowcloud.blowout.scheduler.core.model.Task;
import org.fogbowcloud.blowout.scheduler.core.model.TaskProcess;
import org.fogbowcloud.blowout.scheduler.core.model.TaskProcessImpl;
import org.fogbowcloud.blowout.scheduler.infrastructure.exceptions.InfrastructureException;

public class ExecutionMonitor implements Runnable {

	private Job[] job;
	private Scheduler scheduler;
	private static final Logger LOGGER = Logger.getLogger(ExecutionMonitor.class);
	private ExecutorService service;

	public ExecutionMonitor(Scheduler scheduler, Job... job) {
		this(scheduler, Executors.newFixedThreadPool(3), job);
	}

	public ExecutionMonitor(Scheduler scheduler, ExecutorService service, Job... job) {
		this.job = job;
		this.scheduler = scheduler;
		if (service == null) {
			this.service = Executors.newFixedThreadPool(3);
		} else {
			this.service = service;
		}
	}

	@Override
	public void run() {
		LOGGER.debug("Submitting monitoring tasks");
		for (TaskProcess tp : scheduler.getRunningProcs()) {
			service.submit(new CommonTaskExecutionChecker(tp, this.scheduler));
		}
	}
}

