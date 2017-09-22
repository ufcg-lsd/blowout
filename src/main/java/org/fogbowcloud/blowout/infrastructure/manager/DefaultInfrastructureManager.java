package org.fogbowcloud.blowout.infrastructure.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.infrastructure.exception.RequestResourceException;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;
import org.fogbowcloud.blowout.infrastructure.monitor.ResourceMonitor;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.blowout.pool.AbstractResource;

public class DefaultInfrastructureManager implements InfrastructureManager {

	private InfrastructureProvider infraProvider;
	private ResourceMonitor resourceMonitor;

	public DefaultInfrastructureManager(InfrastructureProvider infraProvider,
			ResourceMonitor resourceMonitor) {
		this.infraProvider = infraProvider;
		this.resourceMonitor = resourceMonitor;
	}

	@Override
	public synchronized void act(List<AbstractResource> resources, List<Task> tasks)
			throws Exception {

		Map<Specification, Integer> specsDemand = this.generateDemandBySpec(tasks, resources);

		this.requestResources(specsDemand);
	}

	private void requestResources(Map<Specification, Integer> specsDemand)
			throws RequestResourceException {

		for (Entry<Specification, Integer> entry : specsDemand.entrySet()) {
			Specification spec = entry.getKey();

			Integer requested = this.resourceMonitor.getPendingRequests().get(spec);
			if (requested == null) {
				requested = new Integer(0);
			}

			int requiredResources = entry.getValue() - requested;
			for (int count = 0; count < requiredResources; count++) {
				String resourceId = this.infraProvider.requestResource(spec);
				this.resourceMonitor.addPendingResource(resourceId, spec);
			}
		}
	}

	private Map<Specification, Integer> generateDemandBySpec(List<Task> tasks,
			List<AbstractResource> resources) {
		Map<Specification, Integer> specsDemand = new HashMap<Specification, Integer>();

		// FIXME: this variable name is incorrect, since the list will not
		List<AbstractResource> currentResources = filterResourcesByState(resources,
				ResourceState.IDLE, ResourceState.BUSY, ResourceState.FAILED);

		for (Task task : tasks) {
			if (!task.isFinished()) {
				boolean resourceResolved = false;

				for (AbstractResource resource : currentResources) {
					if (resource.match(task.getSpecification())) {
						resourceResolved = true;
						currentResources.remove(resource);
						break;
					}
				}
				if (!resourceResolved) {
					this.incrementDemand(specsDemand, task.getSpecification());
				}
			}
		}
		return specsDemand;
	}
	
	private List<AbstractResource> filterResourcesByState(List<AbstractResource> resources,
			ResourceState... resourceStates) {

		List<AbstractResource> filteredResources = new ArrayList<AbstractResource>();
		for (AbstractResource resource : resources) {
			for (ResourceState state : resourceStates) {
				if (state.equals(resource.getState())) {
					filteredResources.add(resource);
				}
			}
		}
		return filteredResources;
	}

	private void incrementDemand(Map<Specification, Integer> specsDemand, Specification spec) {
		Integer demand = specsDemand.get(spec);
		if (demand == null) {
			demand = new Integer(0);
		}
		demand++;
		if (demand.intValue() < 0) {
			specsDemand.put(spec, new Integer(0));
		} else {
			specsDemand.put(spec, demand);
		}
	}
}