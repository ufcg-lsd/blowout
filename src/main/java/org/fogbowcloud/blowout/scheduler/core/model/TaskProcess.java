package org.fogbowcloud.blowout.scheduler.core.model;

import java.util.List;

import org.fogbowcloud.blowout.scheduler.core.model.TaskProcessImpl.State;

public interface TaskProcess {

	String getProcessId();
	
	String getTaskId();

	List<Command> getCommands();

	void executeTask(Resource resource);

	State getStatus();
	
	Specification getSpecification();

}