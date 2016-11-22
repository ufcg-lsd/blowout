package org.fogbowcloud.blowout.pool;

import java.util.ArrayList;
import java.util.List;

import org.fogbowcloud.blowout.core.SchedulerInterface;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.infrastructure.manager.InfrastructureManager;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;

public class DefauBlowoutlPool implements BlowoutPool{

	private List<AbstractResource> resourcePool = new ArrayList<AbstractResource>();
	private List<Task> taskPool = new ArrayList<Task>();
	private InfrastructureManager infraManager;
	private SchedulerInterface schedulerInterface;
	
	public DefauBlowoutlPool(SchedulerInterface schedulerInterface, InfrastructureManager infraManager){
		this.schedulerInterface = schedulerInterface;
		this.infraManager = infraManager;
		
	}
	
	@Override
	public synchronized void addResource(AbstractResource resource) {
		resource.setState(ResourceState.IDLE);
		resourcePool.add(resource);
		//TODO send all resources and tasks or filter by values?
		try {
			infraManager.act(getAllResources(), getAllTasks());
			schedulerInterface.act(getAllTasks(), getAllResources());
		} catch (Exception e) {
			// TODO Do what when it fails?
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void resourceFaild(AbstractResource resource) {
		resource.setState(ResourceState.FAILED);
		
	}


	@Override
	public void releaseResource(AbstractResource resource) {
		resource.setState(ResourceState.IDLE);
		try {
			//FIXME Is there needing to call infraManager.act ???
			infraManager.act(getAllResources(), getAllTasks());
			schedulerInterface.act(getAllTasks(), getAllResources());
		} catch (Exception e) {
			// TODO Do what when it fails?
			e.printStackTrace();
		}
	}
	
	@Override
	public List<AbstractResource> getAllResources() {
		return new ArrayList<AbstractResource>(resourcePool);
	}

	@Override
	public synchronized void allocateResource(AbstractResource resource) {
		resource.setState(ResourceState.BUSY);
		
	}

	@Override
	public synchronized void removeResource(AbstractResource resource) {
		
		boolean isFreeToRemove = true;
		
		if(ResourceState.BUSY.equals(resource.getState())){
			isFreeToRemove = false;
		}
		
		//TODO verify on scheduler if this resource isn't being used.
		
		if(isFreeToRemove){
			resource.setState(ResourceState.TO_REMOVE);
		}
		
	}

	@Override
	public synchronized void addTask(Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public synchronized void taskFaild(Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Task> getAllTasks() {
		return new ArrayList<Task>(taskPool);
	}

	@Override
	public void removeTask(Task task) {
		// TODO Auto-generated method stub
		
	}

	

}
