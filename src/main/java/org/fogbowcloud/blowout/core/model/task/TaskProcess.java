package org.fogbowcloud.blowout.core.model.task;

import java.util.List;

import org.fogbowcloud.blowout.core.model.Command;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;

public interface TaskProcess {

	String getProcessId();
	
	String getTaskId();

	List<Command> getCommands();

	TaskExecutionResult executeTask(AbstractResource resource);

	TaskState getTaskState();
	
	Specification getSpecification();
	
	AbstractResource getResource();
	
	void setTaskState(TaskState taskState);
}