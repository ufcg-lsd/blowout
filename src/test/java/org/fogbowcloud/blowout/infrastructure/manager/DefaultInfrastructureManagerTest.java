package org.fogbowcloud.blowout.infrastructure.manager;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.fogbowcloud.blowout.core.constants.BlowoutConstants;
import org.fogbowcloud.blowout.core.constants.FogbowConstants;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.model.task.Task;
import org.fogbowcloud.blowout.core.model.task.TaskImpl;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.core.model.resource.ResourceState;
import org.fogbowcloud.blowout.infrastructure.monitor.ResourceMonitor;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;
import org.fogbowcloud.blowout.pool.ResourceStateHelper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import static org.fogbowcloud.blowout.helpers.Constants.*;

public class DefaultInfrastructureManagerTest {
	private ResourceMonitor resourceMonitor;
	private InfrastructureProvider infraProvider;
	private InfrastructureManager defaultInfrastructureManager;
	private Specification spec;
	
	@Before
	public void setUp() throws Exception {
		this.infraProvider = Mockito.mock(InfrastructureProvider.class);
		this.resourceMonitor = Mockito.mock(ResourceMonitor.class);
		this.defaultInfrastructureManager = Mockito.spy(new DefaultInfrastructureManager(infraProvider, resourceMonitor));
		this.spec = new Specification(FAKE_CLOUD_NAME, FAKE_COMPUTE_IMAGE_FLAVOR_NAME, FAKE_FOGBOW_USER_NAME,
				FAKE_PUBLIC_KEY, FAKE_PRIVATE_KEY_FILE_PATH);
	}

	@Test
	public void testActOneReadyTaskNoResource() throws Exception {
		Task task = new TaskImpl(FAKE_TASK_ID, this.spec, FAKE_UUID);
		
		List<Task> tasks = new ArrayList<>();
		tasks.add(task);
		List<AbstractResource> resources = new ArrayList<>();
		
		doReturn(FAKE_RESOURCE_ID).when(this.infraProvider).requestResource(this.spec);
		doReturn(new ArrayList<AbstractResource>()).when(resourceMonitor).getPendingResources();
		
		defaultInfrastructureManager.act(resources, tasks);
		verify(infraProvider, times(1)).requestResource(spec);
		verify(resourceMonitor, times(1)).addPendingResource(FAKE_RESOURCE_ID, spec);
		
	}
	
	@Test
	public void testActThreeReadyTaskNoResource() throws Exception {
		Specification spec = new Specification(FAKE_CLOUD_NAME,
                FAKE_COMPUTE_IMAGE_FLAVOR_NAME, FAKE_FOGBOW_USER_NAME, FAKE_PUBLIC_KEY, FAKE_PRIVATE_KEY_FILE_PATH);

		Task taskA = new TaskImpl(FAKE_TASK_ID, spec, FAKE_UUID);
		Task taskB = new TaskImpl(FAKE_TASK_ID+ POSTFIX_B, spec, FAKE_UUID);
		Task taskC = new TaskImpl(FAKE_TASK_ID+POSTFIX_C, spec, FAKE_UUID);
		
		final Queue<String> resourcesToReturn = new LinkedList<>();
		resourcesToReturn.add(FAKE_RESOURCE_ID);
		resourcesToReturn.add(FAKE_RESOURCE_ID+POSTFIX_B);
		resourcesToReturn.add(FAKE_RESOURCE_ID+POSTFIX_C);
		
		List<Task> tasks = new ArrayList<>();
		tasks.add(taskA);
		tasks.add(taskB);
		tasks.add(taskC);
		List<AbstractResource> resources = new ArrayList<>();
		
		Answer<String> requestResourceAnswer = invocation -> resourcesToReturn.poll();
		
		doAnswer(requestResourceAnswer).when(infraProvider).requestResource(spec);
		doReturn(new ArrayList<AbstractResource>()).when(resourceMonitor).getPendingResources();
		
		defaultInfrastructureManager.act(resources, tasks);
		verify(infraProvider, times(3)).requestResource(spec);
		verify(resourceMonitor, times(1)).addPendingResource(FAKE_RESOURCE_ID, spec);
		verify(resourceMonitor, times(1))
				.addPendingResource(FAKE_RESOURCE_ID+POSTFIX_B, spec);
		verify(resourceMonitor, times(1))
				.addPendingResource(FAKE_RESOURCE_ID+POSTFIX_C, spec);
	}
	
	@Test
	public void testActOneReadyTaskOnePendingResource() throws Exception {
		Specification spec = new Specification(FAKE_CLOUD_NAME,
                FAKE_COMPUTE_IMAGE_FLAVOR_NAME, FAKE_FOGBOW_USER_NAME,FAKE_PUBLIC_KEY,FAKE_PRIVATE_KEY_FILE_PATH);

		Task task = new TaskImpl(FAKE_TASK_ID, spec, FAKE_UUID);
		
		List<Task> tasks = new ArrayList<>();
		tasks.add(task);
		List<AbstractResource> resources = new ArrayList<>();
		List<String> pendingResources = new ArrayList<>();
		pendingResources.add(FAKE_RESOURCE_ID);
		List<Specification> pendingSpecs = new ArrayList<>();
		pendingSpecs.add(spec);
		Map<Specification, Integer> pendingRequests = new HashMap<>();
		pendingRequests.put(spec, 1);
		
		doReturn(pendingResources).when(resourceMonitor).getPendingResources();
		doReturn(pendingSpecs).when(resourceMonitor).getPendingSpecification();
		doReturn(pendingRequests).when(resourceMonitor).getPendingRequests();
		
		defaultInfrastructureManager.act(resources, tasks);
		verify(infraProvider, times(0)).requestResource(spec);
		verify(resourceMonitor, times(0))
				.addPendingResource(Mockito.any(String.class), Mockito.any(Specification.class));
	}
	
	@Test
	public void testActTwoReadyTasksOnePendingResource() throws Exception {
		Specification spec = new Specification(FAKE_CLOUD_NAME,
                FAKE_COMPUTE_IMAGE_FLAVOR_NAME,FAKE_FOGBOW_USER_NAME,FAKE_PUBLIC_KEY,FAKE_PRIVATE_KEY_FILE_PATH);

		Task taskA = new TaskImpl(FAKE_TASK_ID, spec, FAKE_UUID);
		Task taskB = new TaskImpl(FAKE_TASK_ID+POSTFIX_B, spec, FAKE_UUID+POSTFIX_B);
		
		List<Task> tasks = new ArrayList<>();
		tasks.add(taskA);
		tasks.add(taskB);
		List<AbstractResource> resources = new ArrayList<>();
		List<String> pendingResources = new ArrayList<>();
		pendingResources.add(FAKE_RESOURCE_ID);
		List<Specification> pendingSpecs = new ArrayList<>();
		pendingSpecs.add(spec);
		Map<Specification, Integer> pendingRequests = new HashMap<>();
		pendingRequests.put(spec, 1);
		
		doReturn(pendingResources).when(resourceMonitor).getPendingResources();
		doReturn(pendingSpecs).when(resourceMonitor).getPendingSpecification();
		doReturn(pendingRequests).when(resourceMonitor).getPendingRequests();
		
		defaultInfrastructureManager.act(resources, tasks);
		verify(infraProvider, times(1)).requestResource(spec);
		verify(resourceMonitor, times(1))
				.addPendingResource(Mockito.any(String.class), Mockito.any(Specification.class));
	}
	
	@Test
	public void testActOnReadyTasksOnePendingResourceDiffSpec() throws Exception {
		Specification specA = new Specification(FAKE_CLOUD_NAME,
                FAKE_COMPUTE_IMAGE_FLAVOR_NAME,FAKE_FOGBOW_USER_NAME, FAKE_PUBLIC_KEY, FAKE_PRIVATE_KEY_FILE_PATH);
		Specification specB = new Specification(FAKE_CLOUD_NAME+POSTFIX_B,
				FAKE_COMPUTE_IMAGE_FLAVOR_NAME +POSTFIX_B, FAKE_FOGBOW_USER_NAME+POSTFIX_B,
				FAKE_PUBLIC_KEY+POSTFIX_B, FAKE_PRIVATE_KEY_FILE_PATH+POSTFIX_B);

		Task taskA = new TaskImpl(FAKE_TASK_ID, specA, FAKE_UUID);
		AbstractResource pendingResource = new FogbowResource(FAKE_RESOURCE_ID, FAKE_ORDER_ID, specB);
		
		List<Task> tasks = new ArrayList<>();
		tasks.add(taskA);
		List<AbstractResource> resources = new ArrayList<>();
		List<AbstractResource> pendingResources = new ArrayList<>();
		pendingResources.add(pendingResource);
		Map<Specification, Integer> pendingRequests = new HashMap<>();
		pendingRequests.put(specB, 1);
		
		doReturn(pendingResources).when(resourceMonitor).getPendingResources();
		doReturn(pendingRequests).when(resourceMonitor).getPendingRequests();
		
		defaultInfrastructureManager.act(resources, tasks);
		verify(infraProvider, times(1)).requestResource(specA);
		verify(resourceMonitor, times(1))
				.addPendingResource(Mockito.any(String.class), Mockito.any(Specification.class));
	}
	
	@Test
	public void testActOnReadyTasksOneIdleResource() throws Exception {
		Specification specA =
				new Specification(FAKE_CLOUD_NAME, FAKE_COMPUTE_IMAGE_FLAVOR_NAME, FAKE_FOGBOW_USER_NAME,
						FAKE_PUBLIC_KEY, FAKE_PRIVATE_KEY_FILE_PATH, USER_DATA_FILE, USER_DATA_TYPE);

		specA.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_A);
		
		AbstractResource idleResource = new FogbowResource(FAKE_RESOURCE_ID, FAKE_ORDER_ID, specA);
		idleResource.putMetadata(BlowoutConstants.METADATA_IMAGE_NAME, FAKE_COMPUTE_IMAGE_FLAVOR_NAME);
		idleResource.putMetadata(BlowoutConstants.ENV_PRIVATE_KEY_FILE, FAKE_PRIVATE_KEY_FILE_PATH);
		ResourceStateHelper.changeResourceToState(idleResource, ResourceState.IDLE);
		
		idleResource.putMetadata(BlowoutConstants.METADATA_IMAGE_NAME, FAKE_COMPUTE_IMAGE_FLAVOR_NAME);
		idleResource.putMetadata(BlowoutConstants.METADATA_PUBLIC_KEY, FAKE_PUBLIC_KEY);
		idleResource.putMetadata(BlowoutConstants.METADATA_VCPU, EXAMPLE_CORE_SIZE);
		idleResource.putMetadata(BlowoutConstants.METADATA_MEM_SIZE, EXAMPLE_MEM_SIZE);
		idleResource.putMetadata(BlowoutConstants.METADATA_DISK_SIZE, EXAMPLE_DISK_SIZE);
		idleResource.putMetadata(BlowoutConstants.METADATA_LOCATION, EXAMPLE_LOCATION);
		
		Task taskA = new TaskImpl(FAKE_TASK_ID, specA, FAKE_UUID);
		List<Task> tasks = new ArrayList<>();
		tasks.add(taskA);
		List<AbstractResource> resources = new ArrayList<>();
		resources.add(idleResource);
		List<AbstractResource> pendingResources = new ArrayList<>();
		
		doReturn(pendingResources).when(resourceMonitor).getPendingResources();
		
		defaultInfrastructureManager.act(resources, tasks);
		verify(infraProvider, times(0)).requestResource(specA);
		verify(resourceMonitor, times(0))
				.addPendingResource(Mockito.any(String.class), Mockito.any(Specification.class));
	}
	
	@Test
	public void testActOnReadyTasksOneIdleResourceDiffSpec() throws Exception {
		
		Specification specA = new Specification(FAKE_CLOUD_NAME, FAKE_COMPUTE_IMAGE_FLAVOR_NAME,
				FAKE_FOGBOW_USER_NAME, FAKE_PUBLIC_KEY, FAKE_PRIVATE_KEY_FILE_PATH);
		Specification specB = new Specification(FAKE_CLOUD_NAME+POSTFIX_B,
				FAKE_COMPUTE_IMAGE_FLAVOR_NAME +POSTFIX_B, FAKE_FOGBOW_USER_NAME+POSTFIX_B,
				FAKE_PUBLIC_KEY+POSTFIX_B, FAKE_PRIVATE_KEY_FILE_PATH+POSTFIX_B);

		Task taskA = new TaskImpl(FAKE_TASK_ID, specA, FAKE_UUID);
		AbstractResource idleResource = new FogbowResource(FAKE_RESOURCE_ID, FAKE_ORDER_ID, specB);
		ResourceStateHelper.changeResourceToState(idleResource, ResourceState.IDLE);
		
		List<Task> tasks = new ArrayList<>();
		tasks.add(taskA);
		List<AbstractResource> resources = new ArrayList<>();
		resources.add(idleResource);
		List<AbstractResource> pendingResources = new ArrayList<>();
		
		doReturn(pendingResources).when(resourceMonitor).getPendingResources();
		
		defaultInfrastructureManager.act(resources, tasks);
		verify(infraProvider, times(1)).requestResource(specA);
		verify(resourceMonitor, times(1))
				.addPendingResource(Mockito.any(String.class), Mockito.any(Specification.class));
	}
}
