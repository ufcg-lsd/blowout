package org.fogbowcloud.blowout.infrastructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.fogbowcloud.blowout.core.Scheduler;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.fogbowcloud.blowout.core.util.DateUtils;
import org.fogbowcloud.blowout.database.ResourceIdDatastore;
import org.fogbowcloud.blowout.infrastructure.manager.DefaultInfrastructureManager;
import org.fogbowcloud.blowout.infrastructure.manager.InfrastructureManager;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.infrastructure.model.TestResourceHelper;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.manager.occi.order.OrderType;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.google.gson.Gson;

public class TestDefaultInfrastructureManager {

	private static final Long NO_EXPIRATION_TIME = new Long(0);
	private String DATASTORE_FULL_PATH = "jdbc:h2:mem:" + new File("src/test/resources/persistance/").getAbsolutePath() + "orders";
	private Scheduler schedulerMock;
	private InfrastructureProvider infrastructureProviderMock;
	private DefaultInfrastructureManager defaultInfrastructureManager;
	private Properties properties;
	private ResourceIdDatastore dsMock;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Before
	public void setUp() throws Exception {

		// Initiating properties file.
		
		File file = new File(DATASTORE_FULL_PATH);
		
		generateDefaulProperties();

		dsMock = mock(ResourceIdDatastore.class);
		schedulerMock = mock(Scheduler.class);
		infrastructureProviderMock = mock(InfrastructureProvider.class);

		defaultInfrastructureManager = spy(new DefaultInfrastructureManager(new ArrayList<Specification>(), true,
				infrastructureProviderMock, properties));
	}

	@After
	public void setDown() throws Exception {

		defaultInfrastructureManager.stop(true);
		properties = null;
		schedulerMock = null;
		infrastructureProviderMock = null;
	}

	@Test
	public void propertiesEmptyTest() throws Exception {

		exception.expect(Exception.class);s

		properties = new Properties();
		defaultInfrastructureManager = new DefaultInfrastructureManager(new ArrayList<Specification>(), true,
				infrastructureProviderMock, properties);

	}

	@Test
	public void propertiesWrongConnTimeoutTest() throws Exception {

		exception.expect(Exception.class);
		properties.put(AppPropertiesConstants.INFRA_RESOURCE_CONNECTION_TIMEOUT, "AB");
		infrastructureManager = new InfrastructureManager(new ArrayList<Specification>(), true,
				infrastructureProviderMock, properties);

	}

	@Test
	public void propertiesWrongIdleLifetimeTest() throws Exception {

		exception.expect(Exception.class);
		properties.put(AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME, "AB");
		infrastructureManager = new InfrastructureManager(new ArrayList<Specification>(), true,
				infrastructureProviderMock, properties);

	}

	@Test
	public void propertiesWrongOrderServiceTimeTest() throws Exception {

		exception.expect(Exception.class);
		properties.put(AppPropertiesConstants.INFRA_ORDER_SERVICE_TIME, "AB");
		infrastructureManager = new InfrastructureManager(new ArrayList<Specification>(), true,
				infrastructureProviderMock, properties);

	}

	@Test
	public void propertiesWrongResourceServiceTimeTest() throws Exception {

		exception.expect(Exception.class);
		properties.put(AppPropertiesConstants.INFRA_RESOURCE_SERVICE_TIME, "AB");
		infrastructureManager = new InfrastructureManager(new ArrayList<Specification>(), true,
				infrastructureProviderMock, properties);
	}

	@Test
	public void createWithNoInitialSpecsAndNonElastic() throws Exception {

		exception.expect(Exception.class);
		infrastructureManager = new InfrastructureManager(new ArrayList<Specification>(), false,
				infrastructureProviderMock, properties);
		
		infrastructureManager.
	}

	@Test
	public void startsInfraManagerWithInitialSpec() throws Exception {

		String initialSpecFile = "src/test/resources/Specs_Json";
		String requestIdFake1 = "FakeResourceResourceRequestID1";
		String requestIdFake2 = "FakeResourceResourceRequestID2";

		BufferedReader br = new BufferedReader(new FileReader(initialSpecFile));
		Gson gson = new Gson();
		List<Specification> specifications = Arrays.asList(gson.fromJson(br, Specification[].class));

		// This test demands two initial specifications.
		if (specifications.size() < 2) {
			fail();
		}

		Map<String, String> resourceMetadataA = new HashMap<String, String>();
		FogbowResource fakeResourceA = TestResourceHelper.generateMockResource(requestIdFake1, resourceMetadataA, true);

		Map<String, String> resourceMetadataB = new HashMap<String, String>();
		FogbowResource fakeResourceB = TestResourceHelper.generateMockResource(requestIdFake2, resourceMetadataB, true);

		doReturn(requestIdFake1).when(infrastructureProviderMock).requestResource(Mockito.eq(specifications.get(0)));
		doReturn(requestIdFake2).when(infrastructureProviderMock).requestResource(Mockito.eq(specifications.get(1)));

		doReturn(fakeResourceA).when(infrastructureProviderMock).getResource(Mockito.eq(requestIdFake1));
		doReturn(fakeResourceB).when(infrastructureProviderMock).getResource(Mockito.eq(requestIdFake2));

		infrastructureManager = new InfrastructureManager(specifications, false, infrastructureProviderMock,
				properties);
		infrastructureManager.setDataStore(dsMock);
		infrastructureManager.start(true, true);
		infrastructureManager.cancelOrderTimer();
		infrastructureManager.cancelResourceTimer();
		assertEquals(specifications.size(), infrastructureManager.getIdleResources().size());

		infrastructureManager.stop(true);
		br.close();
	}

	@Test
	public void removePreviousResources() throws Exception {

		String requestIdFake1 = "FakeResourceResourceRequestID1";
		String requestIdFake2 = "FakeResourceResourceRequestID2";

		List<String> previousResources = new ArrayList<String>();
		previousResources.add(requestIdFake1);
		previousResources.add(requestIdFake2);

		doReturn(previousResources).when(dsMock).getRequesId();

		infrastructureManager.start(true, true);
		infrastructureManager.cancelOrderTimer();
		infrastructureManager.cancelResourceTimer();
		
		verify(infrastructureProviderMock).deleteResource(requestIdFake1);
		verify(infrastructureProviderMock).deleteResource(requestIdFake2);
		
		infrastructureManager.stop(true);

	}

	@Test
	public void orderResourceSingle() throws Exception {

		String fakeResourceResourceRequestId = "requestId";

		Specification specs = new Specification("imageMock", "UserName",
				"publicKeyMock", "privateKeyMock", "userDataMock", "userDataType");

		// Creating mocks behaviors
		doReturn(fakeResourceResourceRequestId).when(infrastructureProviderMock).requestResource(Mockito.eq(specs));
		infrastructureManager.setInfraProvider(infrastructureProviderMock);

		validateOrderResourceResourceRequested(specs, 1);
		assertEquals(1, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).size());
		assertEquals(ResourceResourceRequestState.ORDERED, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).get(0).getState());

	}

	@Test
	public void orderResourceMultiple() throws Exception {

		String fakeResourceResourceRequestId = "requestId";

		Specification specs = new Specification("imageMock", "UserName",
				"publicKeyMock", "privateKeyMock", "userDataMock", "userDataType");

		// Creating mocks behaviors
		doReturn(fakeResourceResourceRequestId).when(infrastructureProviderMock).requestResource(Mockito.eq(specs));
		infrastructureManager.setInfraProvider(infrastructureProviderMock);

		validateOrderResourceResourceRequested(specs, 3);
		assertEquals(3, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).size());
		assertEquals(ResourceResourceRequestState.ORDERED, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).get(0).getState());
		assertEquals(ResourceResourceRequestState.ORDERED, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).get(1).getState());
		assertEquals(ResourceResourceRequestState.ORDERED, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).get(2).getState());
		verify(infrastructureProviderMock, times(3)).requestResource(specs);

	}

	@Test
	public void orderResourceMultipleHavingEqualOpenOrOrdered() throws Exception {

		String fakeResourceResourceRequestId = "requestId";
		Specification specs = mock(Specification.class);

		String orderIdA = "request01";
		String orderIdB = "request02";
		String orderIdC = "request03";

		ResourceResourceRequest orderA = new ResourceResourceRequest(schedulerMock, specs);
		orderA.setResourceResourceRequestId(orderIdA);
		orderA.setState(ResourceResourceRequestState.OPEN);

		ResourceResourceRequest orderB = new ResourceResourceRequest(schedulerMock, specs);
		orderB.setResourceResourceRequestId(orderIdB);
		orderB.setState(ResourceResourceRequestState.ORDERED);

		ResourceResourceRequest orderC = new ResourceResourceRequest(schedulerMock, specs);
		orderC.setResourceResourceRequestId(orderIdC);
		orderC.setState(ResourceResourceRequestState.ORDERED);

		infrastructureManager.getOrders().add(orderA);
		infrastructureManager.getOrders().add(orderB);
		infrastructureManager.getOrders().add(orderC);

		// Creating mocks behaviors
		doReturn(fakeResourceResourceRequestId).when(infrastructureProviderMock).requestResource(Mockito.eq(specs));
		infrastructureManager.setInfraProvider(infrastructureProviderMock);

		assertEquals(3, infrastructureManager.getOrders().size());
		infrastructureManager.orderResource(specs, schedulerMock, 3);
		assertEquals(3, infrastructureManager.getOrders().size());
		verify(infrastructureProviderMock, times(0)).requestResource(specs);
		verify(infrastructureProviderMock, times(0)).deleteResource(fakeResourceResourceRequestId);

	}

	@Test
	public void orderResourceMultipleHavingMoreOpenOrOrdered() throws Exception {

		String fakeResourceResourceRequestId = "requestId";
		Specification specs = mock(Specification.class);

		String orderIdA = "request01";
		String orderIdB = "request02";
		String orderIdC = "request03";

		 requestA = new (schedulerMock, specs);
		requestA.setResourceResourceRequestId(orderIdA);
		requestA.setState(ResourceResourceRequestState.OPEN);

		 requestB = new (schedulerMock, specs);
		requestB.setResourceResourceRequestId(orderIdB);
		requestB.setState(ResourceResourceRequestState.ORDERED);

		 requestC = new (schedulerMock, specs);
		requestC.setResourceResourceRequestId(orderIdC);
		requestC.setState(ResourceResourceRequestState.ORDERED);

		infrastructureManager.getOrders().add(requestA);
		infrastructureManager.getOrders().add(requestB);
		infrastructureManager.getOrders().add(requestC);

		// Creating mocks behaviors
		doReturn(fakeResourceResourceRequestId).when(infrastructureProviderMock).requestResource(Mockito.eq(specs));
		infrastructureManager.setInfraProvider(infrastructureProviderMock);

		assertEquals(3, infrastructureManager.getOrders().size());
		infrastructureManager.orderResource(specs, schedulerMock, 2);
		assertEquals(2, infrastructureManager.getOrders().size());
		assertEquals(0, infrastructureManager.getOrdersByState(ResourceResourceRequestState.OPEN).size());
		verify(infrastructureProviderMock, times(0)).requestResource(specs);
		verify(infrastructureProviderMock, times(1)).deleteResource(orderIdA);

	}

	@Test
	public void orderResourceMultipleHavingLessOpenOrOrdered() throws Exception {

		String fakeResourceResourceRequestId = "requestId";
		Specification specs = mock(Specification.class);

		String orderIdA = "request01";
		String orderIdB = "request02";

		ResourceResourceRequest orderA = new ResourceResourceRequest(schedulerMock, specs);
		orderA.setResourceResourceRequestId(orderIdA);
		orderA.setState(ResourceResourceRequestState.OPEN);

		ResourceResourceRequest orderB = new ResourceResourceRequest(schedulerMock, specs);
		orderB.setResourceResourceRequestId(orderIdB);
		orderB.setState(ResourceResourceRequestState.ORDERED);

		infrastructureManager.getOrders().add(orderA);
		infrastructureManager.getOrders().add(orderB);

		// Creating mocks behaviors
		doReturn(fakeResourceResourceRequestId).when(infrastructureProviderMock).requestResource(Mockito.eq(specs));
		infrastructureManager.setInfraProvider(infrastructureProviderMock);

		assertEquals(2, infrastructureManager.getOrders().size());
		infrastructureManager.orderResource(specs, schedulerMock, 3);
		assertEquals(3, infrastructureManager.getOrders().size());
		assertEquals(1, infrastructureManager.getOrdersByState(ResourceResourceRequestState.OPEN).size());
		assertEquals(2, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).size());
		verify(infrastructureProviderMock, times(1)).requestResource(specs);
		verify(infrastructureProviderMock, times(0)).deleteResource(orderIdA);

	}

	@Test
	public void orderResourceMultipleHavingIdle() throws Exception {

		String fakeResourceResourceRequestId01 = "requestId01";
		String fakeResourceResourceRequestId02 = "requestId02";

		Specification specs = new Specification("imageMock", "UserName",
				"publicKeyMock", "privateKeyMock", "userDataMock", "userDataType");
		FogbowResource fakeResource = mock(FogbowResource.class);
		doReturn(true).when(fakeResource).checkConnectivity();
		doReturn(fakeResourceResourceRequestId02).when(fakeResource).getId();
		doReturn(true).when(fakeResource).match(specs);

		// Creating mocks behaviors
		doReturn(fakeResourceResourceRequestId01).when(infrastructureProviderMock).requestResource(Mockito.eq(specs));
		infrastructureManager.setInfraProvider(infrastructureProviderMock);
		infrastructureManager.getIdleResourcesMap().put(fakeResource, NO_EXPIRATION_TIME);

		infrastructureManager.orderResource(specs, schedulerMock, 3);
		assertEquals(2, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).size());
		assertEquals(ResourceResourceRequestState.ORDERED, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).get(0).getState());
		assertEquals(ResourceResourceRequestState.ORDERED, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).get(1).getState());
		assertEquals(1, infrastructureManager.getOrdersByState(ResourceResourceRequestState.FULFILLED).size());
		assertEquals(ResourceResourceRequestState.FULFILLED,
				infrastructureManager.getOrdersByState(ResourceResourceRequestState.FULFILLED).get(0).getState());
		assertEquals(fakeResourceResourceRequestId02,
				infrastructureManager.getOrdersByState(ResourceResourceRequestState.FULFILLED).get(0).getResourceResourceRequestId());
		verify(infrastructureProviderMock, times(2)).requestResource(specs);

	}

	@Test
	public void orderResourceMultipleHavingIdleNoConnection() throws Exception {

		String fakeResourceResourceRequestId01 = "requestId01";
		String fakeResourceResourceRequestId02 = "requestId02";

		Specification specs = new Specification("imageMock", "UserName",
				"publicKeyMock", "privateKeyMock", "userDataMock", "userDataType");
		FogbowResource fakeResource = mock(FogbowResource.class);
		doReturn(false).when(fakeResource).checkConnectivity();
		doReturn(fakeResourceResourceRequestId02).when(fakeResource).getId();
		doReturn(true).when(fakeResource).match(specs);

		// Creating mocks behaviors
		doReturn(fakeResourceResourceRequestId01).when(infrastructureProviderMock).requestResource(Mockito.eq(specs));
		infrastructureManager.setInfraProvider(infrastructureProviderMock);
		infrastructureManager.getIdleResourcesMap().put(fakeResource, NO_EXPIRATION_TIME);

		infrastructureManager.orderResource(specs, schedulerMock, 3);
		assertEquals(3, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).size());
		assertEquals(ResourceResourceRequestState.ORDERED, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).get(0).getState());
		assertEquals(ResourceResourceRequestState.ORDERED, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).get(1).getState());
		assertEquals(ResourceResourceRequestState.ORDERED, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).get(2).getState());
		verify(infrastructureProviderMock, times(3)).requestResource(specs);

	}

	@Test
	public void releaseResource() {

		String fakeResourceResourceRequestId = "requestId";
		Specification specs = mock(Specification.class);

		String orderIdA = "request01";

		ResourceResourceRequest orderA = new ResourceResourceRequest(schedulerMock, specs);
		orderA.setResourceResourceRequestId(orderIdA);
		orderA.setState(ResourceResourceRequestState.FULFILLED);

		FogbowResource fakeResource = mock(FogbowResource.class);
		doReturn(OrderType.ONE_TIME.getValue()).when(fakeResource).getMetadataValue(FogbowResource.METADATA_REQUEST_TYPE);
		doReturn(true).when(fakeResource).checkConnectivity();
		doReturn(fakeResourceResourceRequestId).when(fakeResource).getId();
		doReturn(true).when(fakeResource).match(specs);

		DateUtils dateUtilsMock = mock(DateUtils.class);

		Long dateMock = System.currentTimeMillis();
		Long lifetime = Long.valueOf(properties.getProperty(AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME));
		doReturn(dateMock).when(dateUtilsMock).currentTimeMillis();

		infrastructureManager.getAllocatedResourcesMap().put(fakeResource, orderA);
		infrastructureManager.getOrders().add(orderA);
		infrastructureManager.setDateUtils(dateUtilsMock);
		infrastructureManager.releaseResource(fakeResource);

		assertEquals(0, infrastructureManager.getAllocatedResources().size());
		assertEquals(1, infrastructureManager.getIdleResources().size());
		assertEquals(0, infrastructureManager.getOrders().size());
		Long expirationTime = infrastructureManager.getIdleResourcesMap().get(fakeResource);
		assertNotNull(expirationTime);
		assertEquals(Long.valueOf(dateMock + lifetime), expirationTime);

	}
	
	@Test
	public void releaseResourceReusedTooManyTimes() {

		String fakeResourceResourceRequestId = "requestId";
		Specification specs = mock(Specification.class);

		String orderIdA = "request01";

		ResourceResourceRequest orderA = new ResourceResourceRequest(schedulerMock, specs);
		orderA.setResourceResourceRequestId(orderIdA);
		orderA.setState(ResourceResourceRequestState.FULFILLED);

		FogbowResource fakeResource = mock(FogbowResource.class);
		doReturn(OrderType.ONE_TIME.getValue()).when(fakeResource).getMetadataValue(FogbowResource.METADATA_REQUEST_TYPE);
		doReturn(true).when(fakeResource).checkConnectivity();
		doReturn(fakeResourceResourceRequestId).when(fakeResource).getId();
		doReturn(true).when(fakeResource).match(specs);
		doReturn(1).when(fakeResource).getReusedTimes();

		DateUtils dateUtilsMock = mock(DateUtils.class);

		Long dateMock = System.currentTimeMillis();
		Long lifetime = Long.valueOf(properties.getProperty(AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME));
		doReturn(dateMock).when(dateUtilsMock).currentTimeMillis();
		doReturn(0).when(infrastructureManager).getMaxResourceReuses();

		infrastructureManager.getAllocatedResourcesMap().put(fakeResource, orderA);
		infrastructureManager.getOrders().add(orderA);
		infrastructureManager.setDateUtils(dateUtilsMock);
		infrastructureManager.releaseResource(fakeResource);

		
		assertEquals(0, infrastructureManager.getAllocatedResources().size());
		assertEquals(0, infrastructureManager.getIdleResources().size());
		assertEquals(0, infrastructureManager.getOrders().size());

	}

	@Test
	public void releaseResourceReuseOpen() {

		String fakeResourceResourceRequestId = "requestId";
		Specification specs = mock(Specification.class);

		String orderIdA = "request01";
		String orderIdB = "request01";

		ResourceResourceRequest orderA = new ResourceResourceRequest(schedulerMock, specs);
		orderA.setResourceResourceRequestId(orderIdA);
		orderA.setState(ResourceResourceRequestState.FULFILLED);

		ResourceResourceRequest orderB = new ResourceResourceRequest(schedulerMock, specs);
		orderB.setResourceResourceRequestId(orderIdB);
		orderB.setState(ResourceResourceRequestState.OPEN);

		FogbowResource fakeResource = mock(FogbowResource.class);
		doReturn(OrderType.ONE_TIME.getValue()).when(fakeResource).getMetadataValue(FogbowResource.METADATA_REQUEST_TYPE);
		doReturn(true).when(fakeResource).checkConnectivity();
		doReturn(fakeResourceResourceRequestId).when(fakeResource).getId();
		doReturn(true).when(fakeResource).match(specs);

		DateUtils dateUtilsMock = mock(DateUtils.class);

		Long dateMock = System.currentTimeMillis();
		doReturn(dateMock).when(dateUtilsMock).currentTimeMillis();

		infrastructureManager.getAllocatedResourcesMap().put(fakeResource, orderA);
		infrastructureManager.getOrders().add(orderA);
		infrastructureManager.getOrders().add(orderB);
		infrastructureManager.setDateUtils(dateUtilsMock);
		infrastructureManager.releaseResource(fakeResource);

		assertEquals(1, infrastructureManager.getAllocatedResources().size());
		assertEquals(0, infrastructureManager.getIdleResources().size());
		assertEquals(1, infrastructureManager.getOrders().size());
		assertNull(infrastructureManager.getIdleResourcesMap().get(fakeResource));

	}

	@Test
	public void releaseResourceReuseOrdered() throws Exception {

		String fakeResourceResourceRequestId = "requestId";
		Specification specs = mock(Specification.class);

		String orderIdA = "requestA";
		String orderIdB = "requestB";

		ResourceResourceRequest orderA = new ResourceResourceRequest(schedulerMock, specs);
		orderA.setResourceResourceRequestId(orderIdA);
		orderA.setState(ResourceResourceRequestState.FULFILLED);

		ResourceResourceRequest orderB = new ResourceResourceRequest(schedulerMock, specs);
		orderB.setResourceResourceRequestId(orderIdB);
		orderB.setState(ResourceResourceRequestState.ORDERED);

		FogbowResource fakeResource = mock(FogbowResource.class);
		doReturn(OrderType.ONE_TIME.getValue()).when(fakeResource).getMetadataValue(FogbowResource.METADATA_REQUEST_TYPE);
		doReturn(true).when(fakeResource).checkConnectivity();
		doReturn(fakeResourceResourceRequestId).when(fakeResource).getId();
		doReturn(true).when(fakeResource).match(specs);

		DateUtils dateUtilsMock = mock(DateUtils.class);

		Long dateMock = System.currentTimeMillis();
		doReturn(dateMock).when(dateUtilsMock).currentTimeMillis();

		infrastructureManager.getAllocatedResourcesMap().put(fakeResource, orderA);
		infrastructureManager.getOrders().add(orderA);
		infrastructureManager.getOrders().add(orderB);
		infrastructureManager.setDateUtils(dateUtilsMock);
		infrastructureManager.releaseResource(fakeResource);

		assertEquals(1, infrastructureManager.getAllocatedResources().size());
		assertEquals(0, infrastructureManager.getIdleResources().size());
		assertEquals(1, infrastructureManager.getOrders().size());
		assertNull(infrastructureManager.getIdleResourcesMap().get(fakeResource));
		assertEquals(ResourceResourceRequestState.FULFILLED, orderB.getState());
		assertEquals(fakeResourceResourceRequestId, orderB.getResourceResourceRequestId());
		verify(infrastructureProviderMock).deleteResource(orderIdB);

	}

	@Test
	public void movePersistentResourceToIdle() throws Exception {

		DateUtils dateUtilsMock = mock(DateUtils.class);

		Long dateMock = System.currentTimeMillis();

		FogbowResource fakeResource = mock(FogbowResource.class);
		Map<String, String> resourceAMetadata = new HashMap<String, String>();
		resourceAMetadata.put(FogbowResource.METADATA_REQUEST_TYPE, OrderType.PERSISTENT.getValue());
		doReturn(resourceAMetadata).when(fakeResource).getAllMetadata();
		doReturn(resourceAMetadata.get(FogbowResource.METADATA_REQUEST_TYPE)).when(fakeResource)
				.getMetadataValue(Mockito.eq(FogbowResource.METADATA_REQUEST_TYPE));

		// Creating mocks behaviors
		doReturn(dateMock).when(dateUtilsMock).currentTimeMillis();

		infrastructureManager.setInfraProvider(infrastructureProviderMock);
		infrastructureManager.setDateUtils(dateUtilsMock);

		infrastructureManager.moveResourceToIdle(fakeResource);

		Long expirationTime = infrastructureManager.getIdleResourcesMap().get(fakeResource);
		assertNotNull(expirationTime);
		assertEquals(NO_EXPIRATION_TIME, expirationTime);

	}

	@Test
	public void resolveOpenOrder() throws Exception {

		String resourceId = "resourceID";

		Specification specA = mock(Specification.class);
		Specification specB = mock(Specification.class);
		Specification specC = mock(Specification.class);

		String orderIdA = "requestA";
		String orderIdB = "requestB";

		ResourceResourceRequest orderA = new ResourceResourceRequest(schedulerMock, specA);
		ResourceResourceRequest orderB = new ResourceResourceRequest(schedulerMock, specB);
		ResourceResourceRequest orderC = new ResourceResourceRequest(schedulerMock, specC);

		FogbowResource fakeResource = mock(FogbowResource.class);
		doReturn(OrderType.ONE_TIME.getValue()).when(fakeResource).getMetadataValue(FogbowResource.METADATA_REQUEST_TYPE);
		doReturn(true).when(fakeResource).checkConnectivity();
		doReturn(resourceId).when(fakeResource).getId();
		doReturn(true).when(fakeResource).match(specA);

		doReturn(orderIdA).when(infrastructureProviderMock).requestResource(specA);
		doReturn(orderIdB).when(infrastructureProviderMock).requestResource(specB);
		doThrow(new ResourceResourceRequestResourceException("Error while requesting resource")).when(infrastructureProviderMock)
			.requestResource(specC);

		infrastructureManager.getOrders().add(orderA);
		infrastructureManager.getOrders().add(orderB);
		infrastructureManager.getOrders().add(orderC);
		infrastructureManager.getIdleResourcesMap().put(fakeResource, NO_EXPIRATION_TIME);

		infrastructureManager.resolveOpenOrder(orderA);
		infrastructureManager.resolveOpenOrder(orderB);
		infrastructureManager.resolveOpenOrder(orderC);

		assertEquals(1, infrastructureManager.getAllocatedResources().size());
		assertEquals(0, infrastructureManager.getIdleResources().size());
		assertEquals(1, infrastructureManager.getOrdersByState(ResourceResourceRequestState.OPEN).size());
		assertEquals(1, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).size());
		assertEquals(1, infrastructureManager.getOrdersByState(ResourceResourceRequestState.FULFILLED).size());
		assertEquals(3, infrastructureManager.getOrders().size());
		assertEquals(resourceId, infrastructureManager.getOrdersByState(ResourceResourceRequestState.FULFILLED).get(0).getResourceResourceRequestId());
		verify(infrastructureProviderMock, times(0)).requestResource(specA);
		verify(infrastructureProviderMock, times(1)).requestResource(specB);

	}
	
	@Test
	public void resolveOpenOrderIdleNotLive() throws Exception {

		String resourceId = "resourceID";

		Specification specA = mock(Specification.class);

		String orderIdA = "requestA";

		ResourceResourceRequest orderA = new ResourceResourceRequest(schedulerMock, specA);

		FogbowResource fakeResource = mock(FogbowResource.class);
		doReturn(OrderType.ONE_TIME.getValue()).when(fakeResource).getMetadataValue(FogbowResource.METADATA_REQUEST_TYPE);
		doReturn(false).when(fakeResource).checkConnectivity();
		doReturn(resourceId).when(fakeResource).getId();
		doReturn(true).when(fakeResource).match(specA);

		doReturn(orderIdA).when(infrastructureProviderMock).requestResource(specA);

		infrastructureManager.getOrders().add(orderA);
		infrastructureManager.getIdleResourcesMap().put(fakeResource, NO_EXPIRATION_TIME);

		infrastructureManager.resolveOpenOrder(orderA);

		assertEquals(0, infrastructureManager.getAllocatedResources().size());
		assertEquals(1, infrastructureManager.getIdleResources().size());
		assertEquals(0, infrastructureManager.getOrdersByState(ResourceResourceRequestState.OPEN).size());
		assertEquals(1, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).size());
		assertEquals(0, infrastructureManager.getOrdersByState(ResourceResourceRequestState.FULFILLED).size());
		assertEquals(1, infrastructureManager.getOrders().size());
		assertEquals(orderIdA, infrastructureManager.getOrdersByState(ResourceResourceRequestState.ORDERED).get(0).getResourceResourceRequestId());
		verify(infrastructureProviderMock, times(1)).requestResource(specA);

	}
	
	@Test
	public void resolveOrderedOrder() throws Exception {

		String requestId = "request01";

		Specification specMockA = mock(Specification.class);
		Specification specMockB = mock(Specification.class);
		Scheduler schedulerMock = mock(Scheduler.class);
		
		ResourceResourceRequest orderA = new ResourceResourceRequest(schedulerMock, specMockA);
		orderA.setResourceResourceRequestId(requestId);
		orderA.setState(ResourceResourceRequestState.ORDERED);
		
		FogbowResource resourceMock = mock(FogbowResource.class);
		doReturn(requestId).when(resourceMock).getId();
		doReturn(true).when(resourceMock).match(specMockA);
		doReturn(true).when(resourceMock).checkConnectivity();
		
		FogbowResource resourceMockB = mock(FogbowResource.class);
		doReturn(requestId).when(resourceMockB).getId();
		doReturn(true).when(resourceMockB).match(specMockB);
		doReturn(true).when(resourceMockB).checkConnectivity();
		
		doReturn(resourceMock).when(infrastructureProviderMock).getResource(requestId);
		
		infrastructureManager.getOrders().add(orderA);
		infrastructureManager.getIdleResourcesMap().put(resourceMockB, new Long(0));
		
		infrastructureManager.setInfraProvider(infrastructureProviderMock);

		infrastructureManager.resolveOrderedOrder(orderA);
		assertEquals(ResourceResourceRequestState.FULFILLED, orderA.getState());
		assertEquals(requestId, orderA.getResourceResourceRequestId());
		assertEquals(1, infrastructureManager.getIdleResources().size());
		verify(infrastructureProviderMock).getResource(requestId);

	}
	
	@Test
	public void resolveOrderedOrderReuse() throws Exception {

		String resourceId = "request01";
		String orderId = "request02";

		Specification specMock = mock(Specification.class);
		Scheduler schedulerMock = mock(Scheduler.class);
		
		ResourceResourceRequest orderA = new ResourceResourceRequest(schedulerMock, specMock);
		orderA.setResourceResourceRequestId(orderId);
		orderA.setState(ResourceResourceRequestState.ORDERED);
		
		FogbowResource resourceMock = mock(FogbowResource.class);
		doReturn(resourceId).when(resourceMock).getId();
		doReturn(true).when(resourceMock).match(specMock);
		doReturn(true).when(resourceMock).checkConnectivity();
		
		infrastructureManager.getOrders().add(orderA);
		infrastructureManager.getIdleResourcesMap().put(resourceMock, new Long(0));
		
		infrastructureManager.setInfraProvider(infrastructureProviderMock);

		infrastructureManager.resolveOrderedOrder(orderA);
		assertEquals(ResourceResourceRequestState.FULFILLED, orderA.getState());
		assertEquals(resourceId, orderA.getResourceResourceRequestId());
		assertEquals(0, infrastructureManager.getIdleResources().size());
		verify(infrastructureProviderMock).deleteResource(orderId);

	}
	
	@Test
	public void relateResourceToOrderPersistentIdle(){
		
		String resourceIdA = "resourceIDA";

		Specification specA = mock(Specification.class);

		ResourceResourceRequest orderA = new ResourceResourceRequest(schedulerMock, specA);
		orderA.setResourceResourceRequestId(resourceIdA);
		orderA.setState(ResourceResourceRequestState.ORDERED);

		FogbowResource fakeResourceA = mock(FogbowResource.class);
		doReturn(OrderType.PERSISTENT.getValue()).when(fakeResourceA).getMetadataValue(FogbowResource.METADATA_REQUEST_TYPE);
		doReturn(false).when(fakeResourceA).checkConnectivity();
		doReturn(resourceIdA).when(fakeResourceA).getId();
		doReturn(true).when(fakeResourceA).match(specA);
		
		FogbowResource fakeResourceB = mock(FogbowResource.class);
		doReturn(OrderType.PERSISTENT.getValue()).when(fakeResourceB).getMetadataValue(FogbowResource.METADATA_REQUEST_TYPE);
		doReturn(true).when(fakeResourceB).checkConnectivity();
		doReturn(resourceIdA).when(fakeResourceB).getId();
		doReturn(true).when(fakeResourceB).match(specA);

		doReturn(fakeResourceB).when(infrastructureProviderMock).getResource(resourceIdA);
		
		infrastructureManager.getIdleResourcesMap().put(fakeResourceA, NO_EXPIRATION_TIME);
		
		assertEquals(1, infrastructureManager.getIdleResources().size());
		
		infrastructureManager.relateResourceToOrder(fakeResourceA, orderA, true);
		
		verify(infrastructureProviderMock).getResource(fakeResourceA.getId());
		verify(fakeResourceA).copyInformations(fakeResourceB);
		assertEquals(ResourceResourceRequestState.FULFILLED, orderA.getState());
		assertEquals(0, infrastructureManager.getIdleResources().size());
	}
	
	@Test
	public void retryPersistentResourceFailed() throws Exception {

		DateUtils dateUtilsMock = mock(DateUtils.class);

		String fakeResourceResourceRequestId = "requestId";

		String hostA = "100.10.1.1";
		String hostB = "100.10.1.10";
		String port = "9898";
		String userName = "user";
		String extraPorts = "";
		String cpuSize = "1";
		String menSize = "1024";
		String diskSize = "";
		String location = "";

		Specification specs = new Specification("imageMock", "UserName",
				"publicKeyMock", "privateKeyMock", "userDataMock", "userDataType");

		Map<String, String> resourceAMetadata = TestResourceHelper.generateResourceMetadata(hostA, port, userName,
				extraPorts, OrderType.PERSISTENT, specs.getImage(), specs.getPublicKey(), cpuSize, menSize, diskSize,
				location);
		FogbowResource fakeResourceA = TestResourceHelper.generateMockResource(fakeResourceResourceRequestId, resourceAMetadata, false);

		Map<String, String> resourceBMetadata = TestResourceHelper.generateResourceMetadata(hostB, port, userName,
				extraPorts, OrderType.PERSISTENT, specs.getImage(), specs.getPublicKey(), cpuSize, menSize, diskSize,
				location);

		FogbowResource fakeResourceB = TestResourceHelper.generateMockResource(fakeResourceResourceRequestId, resourceBMetadata, true);

		doNothing().when(fakeResourceA).copyInformations(fakeResourceB);
		doReturn(fakeResourceResourceRequestId).when(infrastructureProviderMock).requestResource(Mockito.any(Specification.class));
		doReturn(fakeResourceB).when(infrastructureProviderMock).getResource(Mockito.eq(fakeResourceResourceRequestId));

		infrastructureManager.setInfraProvider(infrastructureProviderMock);
		infrastructureManager.setDateUtils(dateUtilsMock);

		infrastructureManager.getIdleResourcesMap().put(fakeResourceA, NO_EXPIRATION_TIME);

		infrastructureManager.getInfraIntegrityService().run();

		verify(infrastructureProviderMock).getResource(fakeResourceA.getId());
		assertEquals(0, infrastructureManager.getAllocatedResources().size());
		assertEquals(1, infrastructureManager.getIdleResources().size());
		verify(fakeResourceA).copyInformations(fakeResourceB);
	}

	@Test
	public void deleteResourceDueExpirationTime() throws Exception {

		DateUtils dateUtilsMock = mock(DateUtils.class);

		Long dateMock = System.currentTimeMillis();

		String resourceId ="resource01";
		FogbowResource fakeResource = mock(FogbowResource.class);
		doReturn(OrderType.ONE_TIME.getValue()).when(fakeResource).getMetadataValue(FogbowResource.METADATA_REQUEST_TYPE);
		doReturn(true).when(fakeResource).checkConnectivity();
		doReturn(resourceId).when(fakeResource).getId();

		doReturn(dateMock).when(dateUtilsMock).currentTimeMillis();

		infrastructureManager.setInfraProvider(infrastructureProviderMock);
		infrastructureManager.setDateUtils(dateUtilsMock);
		infrastructureManager.getIdleResourcesMap().put(fakeResource, dateMock);

		// "advancing time to simulate future monitor of the idle resource.
		doReturn(dateMock + 1).when(dateUtilsMock).currentTimeMillis();
		infrastructureManager.getInfraIntegrityService().run();

		assertEquals(0, infrastructureManager.getAllocatedResources().size());
		assertEquals(0, infrastructureManager.getIdleResources().size());
		verify(infrastructureProviderMock).deleteResource(resourceId);

	}

	@Test
	public void deleteResourceDueConectionFailed() throws Exception {

		DateUtils dateUtilsMock = mock(DateUtils.class);

		String resourceId ="resource01";
		FogbowResource fakeResource = mock(FogbowResource.class);
		doReturn(OrderType.ONE_TIME.getValue()).when(fakeResource).getMetadataValue(FogbowResource.METADATA_REQUEST_TYPE);
		doReturn(false).when(fakeResource).checkConnectivity();
		doReturn(resourceId).when(fakeResource).getId();

		infrastructureManager.setInfraProvider(infrastructureProviderMock);
		infrastructureManager.setDateUtils(dateUtilsMock);
		infrastructureManager.getIdleResourcesMap().put(fakeResource, Long.valueOf(System.currentTimeMillis()));

		infrastructureManager.getInfraIntegrityService().run();

		assertEquals(0, infrastructureManager.getAllocatedResources().size());
		assertEquals(0, infrastructureManager.getIdleResources().size());
		verify(infrastructureProviderMock).deleteResource(resourceId);

	}

	private void validateResourceRequested(Specification specs, int qty) {
		assertEquals(0, infrastructureManager.getOrders().size());
		assertEquals(0, infrastructureManager.getAllocatedResources().size());
		assertEquals(0, infrastructureManager.getIdleResources().size());
		infrastructureManager.orderResource(specs, schedulerMock, qty);
		assertEquals(qty, infrastructureManager.getOrders().size());
	}


	private void generateDefaulProperties() {

		properties = new Properties();

		properties.setProperty(AppPropertiesConstants.INFRA_IS_STATIC, "false");
		properties.setProperty(AppPropertiesConstants.INFRA_PROVIDER_CLASS_NAME,
				"org.fogbowcloud.scheduler.infrastructure.fogbow.FogbowInfrastructureProvider");
		properties.setProperty(AppPropertiesConstants.INFRA_ORDER_SERVICE_TIME, "2000");
		properties.setProperty(AppPropertiesConstants.INFRA_RESOURCE_SERVICE_TIME, "2000");
		properties.setProperty(AppPropertiesConstants.INFRA_RESOURCE_CONNECTION_TIMEOUT, "10000");
		properties.setProperty(AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME, "300000");
		properties.setProperty(AppPropertiesConstants.INFRA_INITIAL_SPECS_FILE_PATH, "src/test/resources/Specs_Json");
		properties.setProperty(AppPropertiesConstants.INFRA_SPECS_BLOCK_CREATING, "false");
		properties.setProperty(AppPropertiesConstants.INFRA_FOGBOW_MANAGER_BASE_URL, "100_02_01_01:8098");
		properties.setProperty(AppPropertiesConstants.INFRA_FOGBOW_TOKEN_PUBLIC_KEY_FILEPATH,
				"src/test/resources/publickey_file");
		properties.put("accounting_datastore_url", DATASTORE_FULL_PATH);

	}

}
