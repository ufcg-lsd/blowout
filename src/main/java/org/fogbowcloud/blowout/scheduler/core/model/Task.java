package org.fogbowcloud.blowout.scheduler.core.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.fogbowcloud.blowout.scheduler.core.model.Command.Type;

public interface Task extends Serializable{

	public Specification getSpecification();

	public Task clone();

	public String getId();

	public void finish();
	
	public void fail();

	public boolean isFinished();
	
	public boolean isFailed();
	
	public boolean checkTimeOuted();

	public void addCommand(Command command);
	
	public List<Command> getCommandsByType(Type commandType);
	
	public List<Command> getAllCommands();
	
	public void startedRunning();

	public void putMetadata(String attributeName, String value);

	public String getMetadata(String attributeName);
	
	public Map<String, String> getAllMetadata();
	
	public boolean mayRetry();

	public int getRetries();

	public void setRetries(int retries);
	
	public int getNumberOfCommands();

	void addProcessId(String procId);
	
	List<String> getProcessId();
}