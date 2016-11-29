package org.fogbowcloud.blowout.infrastructure.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskState;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;
import org.fogbowcloud.blowout.infrastructure.monitor.ResourceMonitor;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.blowout.pool.AbstractResource;
import org.fogbowcloud.blowout.pool.BlowoutPool;

public class DefaultInfrastructureManager implements InfrastructureManager {

	private InfrastructureProvider infraProvider;
	private ResourceMonitor resourceMonitor;

	// private Map<Task, AbstractResource> allocatedResources = new
	// ConcurrentHashMap<Task, AbstractResource>();

	public DefaultInfrastructureManager(InfrastructureProvider infraProvider, ResourceMonitor resourceMonitor) {
		this.infraProvider = infraProvider;
		this.resourceMonitor = resourceMonitor;
	}

	@Override
	public synchronized void act(List<AbstractResource> resources, List<Task> tasks) throws Exception {

		Map<Specification, Integer> specsDemand = new HashMap<Specification, Integer>();

		List<AbstractResource> idleResources = filterResourcesByState(resources, ResourceState.IDLE, 
				ResourceState.BUSY, ResourceState.FAILED);
		// Generate demand for tasks
		for (Task task : tasks) {

			if (!task.isFinished()) {

				boolean resourceResolved = false;

				for (AbstractResource resource : idleResources) {

					if (resource.match(task.getSpecification())) {
						resourceResolved = true;
					}
				}
				if (!resourceResolved) {
					incrementDecrementDemand(specsDemand, task.getSpecification(), true);
				}
			}
		}

		// Reduce demand by pending resources
		for (Specification pendingSpec : resourceMonitor.getPendingSpecification()) {
			incrementDecrementDemand(specsDemand, pendingSpec, false);
		}

		// Request resources according to the demand.
		for (Entry<Specification, Integer> entry : specsDemand.entrySet()) {

			Specification spec = entry.getKey();
			Integer qty = entry.getValue();
			for (int count = 0; count < qty.intValue(); count++) {

				String resourceId = infraProvider.requestResource(spec);
				resourceMonitor.addPendingResource(resourceId, spec);

			}

		}
	}

	private List<AbstractResource> filterResourcesByState(List<AbstractResource> resources,
			ResourceState... resourceStates) {

		List<AbstractResource> filteredResources = new ArrayList<AbstractResource>();
		for (AbstractResource resource : resources) {
			for(ResourceState state : resourceStates){
				if (state.equals(resource.getState())) {
					filteredResources.add(resource);
				}
			}
		}

		return filteredResources;

	}

	private List<Task> filterTasksByState(List<Task> tasks, TaskState taskState) {

		List<Task> filteredTasks = new ArrayList<Task>();
		for (Task task : tasks) {
			if (taskState.equals(task.getState())) {
				filteredTasks.add(task);
			}
		}

		return filteredTasks;

	}

	private void incrementDecrementDemand(Map<Specification, Integer> specsDemand, Specification spec,
			boolean increment) {
		Integer zero = new Integer(0);
		Integer demand = specsDemand.get(spec);
		if (demand == null) {
			demand = zero;
		}
		demand = new Integer(demand.intValue() + (increment ? 1 : -1));
		specsDemand.put(spec, zero.compareTo(demand) > 0 ? zero : demand);
	}

}