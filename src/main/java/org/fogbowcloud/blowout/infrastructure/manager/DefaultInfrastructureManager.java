package org.fogbowcloud.blowout.infrastructure.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.infrastructure.model.AbstractResource;
import org.fogbowcloud.blowout.infrastructure.monitor.ResourceMonitor;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;

public class DefaultInfrastructureManager implements InfrastructureManager {

	private InfrastructureProvider infraProvider;
	private ResourceMonitor resourceMonitor;

	//private Map<Task, AbstractResource> allocatedResources = new ConcurrentHashMap<Task, AbstractResource>();

	public DefaultInfrastructureManager(InfrastructureProvider infraProvider, ResourceMonitor resourceMonitor){
		this.infraProvider = infraProvider;
		this.resourceMonitor = resourceMonitor;
	}

	@Override
	public synchronized void act(List<AbstractResource> resources, List<Task> tasks, ResourceNotifier resourceNotifier) throws Exception {

		Map<Specification, Integer> specsDemand = new HashMap<Specification, Integer>();

		//Generate demand for tasks
		for(Task task : tasks){
			boolean resourceResolved = false;

			for(AbstractResource resource : resources){

				if(resource.match(task.getSpecification())){
					resourceResolved = true;
				}

			}
			if(!resourceResolved){
				incrementDecrementDemand(specsDemand, task.getSpecification(), true);
			}
		}

		//Reduce demand by pending resources
		for(AbstractResource pendingResource : resourceMonitor.getPendingResources()){
			incrementDecrementDemand(specsDemand, pendingResource.getRequestedSpec(), true);
		}

		for(Entry<Specification, Integer>  entry : specsDemand.entrySet()){

			Specification spec = entry.getKey();
			Integer qty = entry.getValue();
			for(int count = 0; count < qty.intValue(); count++){

				AbstractResource resource = infraProvider.requestResource(spec);
				resourceMonitor.monitoreResource(resource, resourceNotifier);
				
			}

		}
	}

	private void incrementDecrementDemand(Map<Specification, Integer> specsDemand, Specification spec, boolean increment) {
		Integer zero = new Integer(0); 
		Integer demand = specsDemand.get(spec);
		if(demand == null){
			demand = zero;
		}
		demand = new Integer(demand.intValue()+(increment ? 1:-1) );
		specsDemand.put(spec, zero.compareTo(demand) > 0 ? zero : demand ) ;
	}



}