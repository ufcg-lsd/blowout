package org.fogbowcloud.blowout.infrastructure.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskState;
import org.fogbowcloud.blowout.infrastructure.exception.RequestResourceException;
import org.fogbowcloud.blowout.infrastructure.model.ResourceState;
import org.fogbowcloud.blowout.infrastructure.monitor.ResourceMonitor;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.blowout.pool.AbstractResource;

public class DefaultInfrastructureManager implements InfrastructureManager {
    private static final Logger LOGGER = Logger.getLogger(DefaultInfrastructureManager.class);

    private final InfrastructureProvider infraProvider;
    private final ResourceMonitor resourceMonitor;

    public DefaultInfrastructureManager(InfrastructureProvider infraProvider, ResourceMonitor resourceMonitor) {
        this.infraProvider = infraProvider;
        this.resourceMonitor = resourceMonitor;
    }

    @Override
    public synchronized void act(List<AbstractResource> resources, List<Task> tasks) throws Exception {
        LOGGER.debug("Calling Infrastructure act to resources " + resources.toString() + " and tasks " + tasks.toString());
        Map<Specification, Integer> specsDemand = generateDemandBySpec(tasks, resources);
        requestResources(specsDemand);
    }

    private void requestResources(Map<Specification, Integer> specsDemand) throws RequestResourceException {

        for (Entry<Specification, Integer> entry : specsDemand.entrySet()) {

            Specification spec = entry.getKey();

            Integer requested = this.resourceMonitor.getPendingRequests().get(
                    spec);
            if (requested == null)
                requested = 0;
            int requiredResources = entry.getValue() - requested;

            for (int count = 0; count < requiredResources; count++) {

                String resourceId = infraProvider.requestResource(spec);
                resourceMonitor.addPendingResource(resourceId, spec);
            }
        }
    }

    private Map<Specification, Integer> generateDemandBySpec(List<Task> tasks, List<AbstractResource> resources) {

        LOGGER.debug("Generating Fogbow specification");
        Map<Specification, Integer> specsDemand = new HashMap<>();

        List<AbstractResource> currentResources = filterResourcesByState(resources,
                ResourceState.IDLE,
                ResourceState.BUSY,
                ResourceState.FAILED
        );

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
                    incrementDecrementDemand(specsDemand,
                            task.getSpecification(), true);
                }
            }
        }
        return specsDemand;
    }

    private void incrementDecrementDemand(
            Map<Specification, Integer> specsDemand, Specification spec,
            boolean increment) {
        Integer zero = new Integer(0);
        Integer demand = specsDemand.get(spec);
        if (demand == null) {
            demand = zero;
        }
        demand = new Integer(demand.intValue() + (increment ? 1 : -1));
        specsDemand.put(spec, zero.compareTo(demand) > 0 ? zero : demand);
    }

    private List<AbstractResource> filterResourcesByState(
            List<AbstractResource> resources, ResourceState... resourceStates) {

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

    private List<Task> filterTasksByState(List<Task> tasks, TaskState taskState) {

        List<Task> filteredTasks = new ArrayList<Task>();
        for (Task task : tasks) {
            if (taskState.equals(task.getState())) {
                filteredTasks.add(task);
            }
        }
        return filteredTasks;
    }
}