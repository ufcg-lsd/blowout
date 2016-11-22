package org.fogbowcloud.blowout.infrastructure.manager;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.model.Task;
import org.fogbowcloud.blowout.core.model.TaskImpl;
import org.fogbowcloud.blowout.core.model.TaskState;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.infrastructure.monitor.ResourceMonitor;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.blowout.pool.AbstractResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

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
	public void setDown() throws Exception {

	}

	@Test
	public void testActOneReadyTaskNoResource() throws Exception {
		
		String resourceId = "Rsource01";
		String orderId = "order01";
		String taskId = "Task01";
		Specification spec = new Specification("Image", "Fogbow", "myKey", "path");

		Task task = new TaskImpl(taskId, spec);
		AbstractResource newResource = new FogbowResource(resourceId, orderId, spec);
		
		List<Task> tasks = new ArrayList<Task>();
		tasks.add(task);
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		
		doReturn(newResource).when(infraProvider).requestResource(spec);
		doReturn(new ArrayList<AbstractResource>()).when(resourceMonitor).getPendingResources();
		
		defaultInfrastructureManager.act(resources, tasks);
		verify(infraProvider, times(1)).requestResource(spec);
		verify(resourceMonitor, times(1)).addPendingResource(newResource);
		
	}
	
	@Test
	public void testActThreeReadyTaskNoResource() throws Exception {
		
		String resourceIdA = "Rsource01";
		String resourceIdB = "Rsource02";
		String resourceIdC = "Rsource03";
		
		String orderIdA = "order01";
		String orderIdB = "order02";
		String orderIdC = "order03";
		
		String taskIdA = "Task01";
		String taskIdB = "Task02";
		String taskIdC = "Task03";
		
		Specification spec = new Specification("Image", "Fogbow", "myKey", "path");

		Task taskA = new TaskImpl(taskIdA, spec);
		Task taskB = new TaskImpl(taskIdB, spec);
		Task taskC = new TaskImpl(taskIdC, spec);
		
		//These are the resources returned when the InfrastructureManager ask for new resources.
		AbstractResource newResourceA = new FogbowResource(resourceIdA, orderIdA, spec);
		AbstractResource newResourceB = new FogbowResource(resourceIdB, orderIdB, spec);
		AbstractResource newResourceC = new FogbowResource(resourceIdC, orderIdC, spec);
		
		final Queue<AbstractResource> resourcesToReturn = new LinkedList<AbstractResource>();
		resourcesToReturn.add(newResourceA);
		resourcesToReturn.add(newResourceB);
		resourcesToReturn.add(newResourceC);
		
		List<Task> tasks = new ArrayList<Task>();
		tasks.add(taskA);
		tasks.add(taskB);
		tasks.add(taskC);
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		
		Answer<AbstractResource> requestResourceAnswer = new Answer<AbstractResource>() {
			
			@Override
			public AbstractResource answer(InvocationOnMock invocation) throws Throwable {
				return resourcesToReturn.poll();
			}
		};
		
		doAnswer(requestResourceAnswer).when(infraProvider).requestResource(spec);
		doReturn(new ArrayList<AbstractResource>()).when(resourceMonitor).getPendingResources();
		
		defaultInfrastructureManager.act(resources, tasks);
		verify(infraProvider, times(3)).requestResource(spec);
		verify(resourceMonitor, times(1)).addPendingResource(newResourceA);
		verify(resourceMonitor, times(1)).addPendingResource(newResourceB);
		verify(resourceMonitor, times(1)).addPendingResource(newResourceC);
		
	}
	
	@Test
	public void testActIgnoreNonReadyTasks() throws Exception {
		
		String resourceIdA = "Rsource01";
		String resourceIdB = "Rsource02";
		String resourceIdC = "Rsource03";
		
		String orderIdA = "order01";
		String orderIdB = "order02";
		String orderIdC = "order03";
		
		String taskIdA = "Task01";
		String taskIdB = "Task02";
		String taskIdC = "Task03";
		String taskIdD = "Task04";
		String taskIdE = "Task05";
		String taskIdF = "Task06";
		
		Specification spec = new Specification("Image", "Fogbow", "myKey", "path");

		Task taskA = new TaskImpl(taskIdA, spec);
		taskA.setState(TaskState.COMPLETED);
		Task taskB = new TaskImpl(taskIdB, spec);
		taskB.setState(TaskState.READY); //Only this one will be considered to request new resource.
		Task taskC = new TaskImpl(taskIdC, spec);
		taskC.setState(TaskState.RUNNING);
		Task taskD = new TaskImpl(taskIdD, spec);
		taskD.setState(TaskState.FINNISHED);
		Task taskE = new TaskImpl(taskIdE, spec);
		taskE.setState(TaskState.NOT_CREATED);
		Task taskF = new TaskImpl(taskIdF, spec);
		taskF.setState(TaskState.FAILED);
		
		//These are the resources returned when the InfrastructureManager ask for new resources.
		AbstractResource newResourceA = new FogbowResource(resourceIdA, orderIdA, spec);
		AbstractResource newResourceB = new FogbowResource(resourceIdB, orderIdB, spec);
		AbstractResource newResourceC = new FogbowResource(resourceIdC, orderIdC, spec);
		
		final Queue<AbstractResource> resourcesToReturn = new LinkedList<AbstractResource>();
		resourcesToReturn.add(newResourceA);
		resourcesToReturn.add(newResourceB);
		resourcesToReturn.add(newResourceC);
		
		List<Task> tasks = new ArrayList<Task>();
		tasks.add(taskA);
		tasks.add(taskB);
		tasks.add(taskC);
		tasks.add(taskD);
		tasks.add(taskE);
		tasks.add(taskF);
		
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		
		Answer<AbstractResource> requestResourceAnswer = new Answer<AbstractResource>() {
			
			@Override
			public AbstractResource answer(InvocationOnMock invocation) throws Throwable {
				return resourcesToReturn.poll();
			}
		};
		
		doAnswer(requestResourceAnswer).when(infraProvider).requestResource(spec);
		doReturn(new ArrayList<AbstractResource>()).when(resourceMonitor).getPendingResources();
		
		defaultInfrastructureManager.act(resources, tasks);
		verify(infraProvider, times(1)).requestResource(spec);
		verify(resourceMonitor, times(1)).addPendingResource(newResourceA);
		
	}
	
	@Test
	public void testActOneReadyTaskOnePendingResource() throws Exception {
		
		String resourceId = "Rsource01";
		String orderId = "order01";
		String taskId = "Task01";
		Specification spec = new Specification("Image", "Fogbow", "myKey", "path");

		Task task = new TaskImpl(taskId, spec);
		AbstractResource pendingResource = new FogbowResource(resourceId, orderId, spec);
		
		List<Task> tasks = new ArrayList<Task>();
		tasks.add(task);
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		List<AbstractResource> pendingResources = new ArrayList<AbstractResource>();
		pendingResources.add(pendingResource);
		
		doReturn(pendingResources).when(resourceMonitor).getPendingResources();
		
		defaultInfrastructureManager.act(resources, tasks);
		verify(infraProvider, times(0)).requestResource(spec);
		verify(resourceMonitor, times(0)).addPendingResource(Mockito.any(AbstractResource.class));
		
	}
	
	@Test
	public void testActTwoReadyTasksOnePendingResource() throws Exception {
		
		String resourceId = "Rsource01";
		String orderId = "order01";
		String taskIdA = "Task01";
		String taskIdB = "Task02";
		Specification spec = new Specification("Image", "Fogbow", "myKey", "path");

		Task taskA = new TaskImpl(taskIdA, spec);
		Task taskB = new TaskImpl(taskIdB, spec);
		AbstractResource pendingResource = new FogbowResource(resourceId, orderId, spec);
		
		List<Task> tasks = new ArrayList<Task>();
		tasks.add(taskA);
		tasks.add(taskB);
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		List<AbstractResource> pendingResources = new ArrayList<AbstractResource>();
		pendingResources.add(pendingResource);
		
		doReturn(pendingResources).when(resourceMonitor).getPendingResources();
		
		defaultInfrastructureManager.act(resources, tasks);
		verify(infraProvider, times(1)).requestResource(spec);
		verify(resourceMonitor, times(1)).addPendingResource(Mockito.any(AbstractResource.class));
		
	}
	
	@Test
	public void testActOnReadyTasksOnePendingResourceDiffSpec() throws Exception {
		
		String resourceId = "Rsource01";
		String orderId = "order01";
		String taskIdA = "Task01";
		
		Specification specA = new Specification("ImageA", "Fogbow", "myKeyA", "path");
		Specification specB = new Specification("ImageB", "Fogbow", "myKeyB", "path");

		Task taskA = new TaskImpl(taskIdA, specA);
		AbstractResource pendingResource = new FogbowResource(resourceId, orderId, specB);
		
		List<Task> tasks = new ArrayList<Task>();
		tasks.add(taskA);
		List<AbstractResource> resources = new ArrayList<AbstractResource>();
		List<AbstractResource> pendingResources = new ArrayList<AbstractResource>();
		pendingResources.add(pendingResource);
		
		doReturn(pendingResources).when(resourceMonitor).getPendingResources();
		
		defaultInfrastructureManager.act(resources, tasks);
		verify(infraProvider, times(1)).requestResource(specA);
		verify(resourceMonitor, times(1)).addPendingResource(Mockito.any(AbstractResource.class));
		
	}
	
}
