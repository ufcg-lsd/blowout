package org.fogbowcloud.blowout.infrastructure.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskState;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;
import org.fogbowcloud.blowout.infrastructure.monitor.ResourceMonitor;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.blowout.pool.AbstractResource;

public class DefaultInfrastructureManager implements InfrastructureManager {

	private InfrastructureProvider infraProvider;
	private ResourceMonitor resourceMonitor;

	// private Map<Task, AbstractResource> allocatedResources = new
	// ConcurrentHashMap<Task, AbstractResource>();
	private Map<Task, AbstractResource> taskResourceMap = new ConcurrentHashMap<Task, AbstractResource>();
	private static final Logger LOGGER = Logger.getLogger(DefaultInfrastructureManager.class);

	public DefaultInfrastructureManager(InfrastructureProvider infraProvider, ResourceMonitor resourceMonitor) {
		this.infraProvider = infraProvider;
		this.resourceMonitor = resourceMonitor;
	}

	@Override
	public synchronized void act(List<AbstractResource> resources, List<Task> tasks) throws Exception {

		LOGGER.debug("Calling DefaultInfrastructureManager act");
		Map<Specification, Integer> specsDemand = new HashMap<Specification, Integer>();
		List<AbstractResource> idleResources = filterResourcesByState(resources, ResourceState.IDLE);
		LOGGER.debug("idleResources=" + idleResources.size());
				
		// Generate demand for tasks
		for (Task task : tasks) {
			if (!task.isFinished()) {
				boolean resourceResolved = false;
				AbstractResource resourceToRemove = null;
				for (AbstractResource resource : idleResources) {
					if (resource.match(task.getSpecification())) {
						resourceResolved = true;
						resourceToRemove = resource;
					}
				}
				
				if (!resourceResolved) {
					incrementDecrementDemand(specsDemand, task.getSpecification(), true);
				} else {
					idleResources.remove(resourceToRemove);
				}				
				LOGGER.debug("resourceResolved=" + resourceResolved + " task=" + task.getId());
			}
		}

		// Reduce demand by pending resources
		for (Specification pendingSpec : resourceMonitor.getPendingSpecification()) {
			incrementDecrementDemand(specsDemand, pendingSpec, false);
		}
		
		LOGGER.debug("specsDemand=" + specsDemand.size());
		LOGGER.debug("pendingSpecification=" + resourceMonitor.getPendingSpecification().size());

		// Request resources according to demand.
		for (Entry<Specification, Integer> entry : specsDemand.entrySet()) {
			Specification spec = entry.getKey();
			Integer qty = entry.getValue();
			for (int count = 0; count < qty.intValue(); count++) {
				String resourceId = infraProvider.requestResource(spec);
				resourceMonitor.addPendingResource(resourceId, spec);
				LOGGER.debug("Adding resource " + resourceId + " to pendingSpecification");
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