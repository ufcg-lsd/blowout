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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import static org.fogbowcloud.blowout.constants.TestConstants.*;

public class TestDefaultInfrastructureManager {
	private ResourceMonitor resourceMonitor;
	private InfrastructureProvider infraProvider;
	private InfrastructureManager defaultInfrastructureManager;
	
	@Before
	public void setUp() throws Exception {
		infraProvider = Mockito.mock(InfrastructureProvider.class);
		resourceMonitor = Mockito.mock(ResourceMonitor.class);
		defaultInfrastructureManager = Mockito.spy(new DefaultInfrastructureManager(infraProvider, resourceMonitor));
	}

	@After
	public void setDown() throws Exception {}

	@Test
	public void testActOneReadyTaskNoResource() throws Exception {
		Specification spec = new Specification(FAKE_CLOUD_NAME,
				FAKE_IMAGE_FLAVOR_NAME, FAKE_FOGBOW_USER_NAME, FAKE_PUBLIC_KEY, FAKE_PRIVATE_KEY_FILE_PATH);

		Task task = new TaskImpl(FAKE_TASK_ID, spec, FAKE_UUID);
		
		List<Task> tasks = new ArrayList<>();
		tasks.add(task);
		List<AbstractResource> resources = new ArrayList<>();
		
		doReturn(FAKE_RESOURCE_ID).when(infraProvider).requestResource(spec);
		doReturn(new ArrayList<AbstractResource>()).when(resourceMonitor).getPendingResources();
		
		defaultInfrastructureManager.act(resources, tasks);
		verify(infraProvider, times(1)).requestResource(spec);
		verify(resourceMonitor, times(1)).addPendingResource(FAKE_RESOURCE_ID, spec);
		
	}
	
	@Test
	public void testActThreeReadyTaskNoResource() throws Exception {
		Specification spec = new Specification(FAKE_CLOUD_NAME,
				FAKE_IMAGE_FLAVOR_NAME, FAKE_FOGBOW_USER_NAME, FAKE_PUBLIC_KEY, FAKE_PRIVATE_KEY_FILE_PATH);

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
				FAKE_IMAGE_FLAVOR_NAME, FAKE_FOGBOW_USER_NAME,FAKE_PUBLIC_KEY,FAKE_PRIVATE_KEY_FILE_PATH);

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
				FAKE_IMAGE_FLAVOR_NAME,FAKE_FOGBOW_USER_NAME,FAKE_PUBLIC_KEY,FAKE_PRIVATE_KEY_FILE_PATH);

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
				FAKE_IMAGE_FLAVOR_NAME,FAKE_FOGBOW_USER_NAME, FAKE_PUBLIC_KEY, FAKE_PRIVATE_KEY_FILE_PATH);
		Specification specB = new Specification(FAKE_CLOUD_NAME+POSTFIX_B,
				FAKE_IMAGE_FLAVOR_NAME +POSTFIX_B, FAKE_FOGBOW_USER_NAME+POSTFIX_B,
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
		final String image = "image";
		final String userName = "userName";
		final String publicKey = "publicKey";
		final String privateKey = "privateKey";
		final String fogbowRequirement = "Glue2vCPU >= 1 && Glue2RAM >= 1024 ";
		final String userDataFile = "scripts/lvl-user-data.sh";
		final String userDataType = "text/x-shellscript";
		
		final String coreSize = "1";
		final String menSize = "1024";
		final String diskSize = "20";
		final String location = "edu.ufcg.lsd.cloud_1s";

		Specification specA =
				new Specification(FAKE_CLOUD_NAME, image, userName, publicKey, privateKey, userDataFile, userDataType);
		specA.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, fogbowRequirement);
		
		AbstractResource idleResource = new FogbowResource(FAKE_RESOURCE_ID, FAKE_ORDER_ID, specA);
		idleResource.putMetadata(AbstractResource.METADATA_IMAGE, FAKE_IMAGE_FLAVOR_NAME);
		idleResource.putMetadata(AbstractResource.ENV_PRIVATE_KEY_FILE, FAKE_PRIVATE_KEY_FILE_PATH);
		ResourceStateHelper.changeResourceToState(idleResource, ResourceState.IDLE);
		
		idleResource.putMetadata(FogbowResource.METADATA_IMAGE, image);
		idleResource.putMetadata(FogbowResource.METADATA_PUBLIC_KEY, publicKey);
		idleResource.putMetadata(FogbowResource.METADATA_VCPU, coreSize);
		idleResource.putMetadata(FogbowResource.METADATA_MEM_SIZE, menSize);
		idleResource.putMetadata(FogbowResource.METADATA_DISK_SIZE, diskSize);
		idleResource.putMetadata(FogbowResource.METADATA_LOCATION, location);
		
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
		
		Specification specA = new Specification(FAKE_CLOUD_NAME, FAKE_IMAGE_FLAVOR_NAME,
				FAKE_FOGBOW_USER_NAME, FAKE_PUBLIC_KEY, FAKE_PRIVATE_KEY_FILE_PATH);
		Specification specB = new Specification(FAKE_CLOUD_NAME+POSTFIX_B,
				FAKE_IMAGE_FLAVOR_NAME +POSTFIX_B, FAKE_FOGBOW_USER_NAME+POSTFIX_B,
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
