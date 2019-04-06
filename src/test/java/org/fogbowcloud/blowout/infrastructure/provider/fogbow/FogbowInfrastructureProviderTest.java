package org.fogbowcloud.blowout.infrastructure.provider.fogbow;

import static org.junit.Assert.*;
import static org.fogbowcloud.blowout.helpers.Constants.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.fogbowcloud.blowout.core.constants.AppMessagesConstants;
import org.fogbowcloud.blowout.core.constants.BlowoutConstants;
import org.fogbowcloud.blowout.core.constants.FogbowConstants;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;
import org.fogbowcloud.blowout.helpers.Constants;
import org.fogbowcloud.blowout.helpers.FogbowInfrastructureTestUtils;
import org.fogbowcloud.blowout.infrastructure.token.KeystoneTokenUpdatePlugin;
import org.json.JSONException;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.constants.AppPropertiesConstants;
import org.fogbowcloud.blowout.database.FogbowResourceDatastore;
import org.fogbowcloud.blowout.infrastructure.exception.InfrastructureException;
import org.fogbowcloud.blowout.infrastructure.http.HttpWrapper;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.infrastructure.token.AbstractTokenUpdatePlugin;
import org.fogbowcloud.blowout.infrastructure.model.Token;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

public class FogbowInfrastructureProviderTest {
	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private FogbowInfrastructureProvider fogbowInfrastructureProvider;
	private HttpWrapper httpWrapperMock;
	private static Properties properties;
	private ScheduledCurrentThreadExecutorService exec;
	private static AbstractTokenUpdatePlugin tokenUpdatePluginMock;
	private FogbowResourceDatastore fogbowResourceDsMock;
	private static Specification spec;

	@BeforeClass
	public static void init() throws Exception {
		properties = new Properties();
		properties.load(new FileInputStream(Constants.FILE_PATH_TESTS_CONFIG));

		Token token = Mockito.mock(Token.class);
		when(token.getAccessId()).thenReturn(Constants.FakeData.FAKE_ACCESS_ID);

		tokenUpdatePluginMock =  mock(KeystoneTokenUpdatePlugin.class);
		when(tokenUpdatePluginMock.generateToken()).thenReturn(token);
		when(tokenUpdatePluginMock.getUpdateTime()).thenReturn(6);
		when(tokenUpdatePluginMock.getUpdateTimeUnits()).thenReturn(TimeUnit.HOURS);

		spec = new Specification(Constants.FakeData.CLOUD_NAME, Constants.FakeData.COMPUTE_IMAGE_FLAVOR_NAME,
				Constants.FakeData.FOGBOW_USER_NAME, Constants.FakeData.PUBLIC_KEY, Constants.FakeData.PRIVATE_KEY_FILE_PATH);

	}

	@Before
	public void setUp() {
		httpWrapperMock = mock(HttpWrapper.class);
		fogbowResourceDsMock = mock(FogbowResourceDatastore.class);
		exec = new ScheduledCurrentThreadExecutorService();
		fogbowInfrastructureProvider = spy(new FogbowInfrastructureProvider(properties, exec, tokenUpdatePluginMock));
		fogbowInfrastructureProvider.setFrDatastore(fogbowResourceDsMock);
	}

	@After
	public void setDown() throws Exception {
		httpWrapperMock = null;
		fogbowInfrastructureProvider = null;
	}


	@Test
	public void testHandleTokenUpdate(){
		fogbowInfrastructureProvider.handleTokenUpdate(exec);
		verify(fogbowInfrastructureProvider).handleTokenUpdate(exec);
	}

	@Test
	public void testMakeBodyJsonToComputeRequest() throws Exception {
		createResponse(Constants.ENDPOINT.getAllImagesEndpoint, Constants.JSON.Body.IMAGES_RESPONSE);
		fogbowInfrastructureProvider.setHttpWrapper(httpWrapperMock);
		StringEntity stringEntity = fogbowInfrastructureProvider.makeJsonBody(spec);

		verify(fogbowInfrastructureProvider, times(1)).makeJsonBody(spec);

		assertEquals(Constants.JSON.Body.SPECIFICATION, EntityUtils.toString(stringEntity));
	}

	@Test
	public void requestResourceGetRequestIdTestSuccess(){
		try {
			createResponse(Constants.ENDPOINT.getAllImagesEndpoint, Constants.JSON.Body.IMAGES_RESPONSE);
			createResponse(Constants.ENDPOINT.createComputeEndpoint, Constants.JSON.Body.COMPUTE_ORDER_ID);
			createResponse(Constants.ENDPOINT.createPublicIPEndpoint, Constants.JSON.Body.PUBLIC_IP_ORDER_ID);
			createResponse(Constants.ENDPOINT.getComputeInstanceEndpoint, Constants.JSON.Body.COMPUTE);
			createResponse(Constants.ENDPOINT.getPublicIpInstanceEndpoint, Constants.JSON.Body.PUBLIC_IP_INSTANCE_RESPONSE);

			fogbowInfrastructureProvider.setHttpWrapper(httpWrapperMock);

			String resourceId = fogbowInfrastructureProvider.requestResource(spec);
			assertNotNull(resourceId);

			AbstractResource abstractResource = fogbowInfrastructureProvider.getResource(resourceId);
			assertEquals(resourceId, abstractResource.getId());
			assertEquals(Constants.FakeData.PUBLIC_IP_ORDER_ID, ((FogbowResource) abstractResource).getPublicIpOrderId());

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void getResourceTestSuccess() throws Exception{

		createResponse(Constants.ENDPOINT.getComputeInstanceEndpoint, Constants.JSON.Body.COMPUTE);
		createResponse(Constants.ENDPOINT.getPublicIpInstanceEndpoint, Constants.JSON.Body.PUBLIC_IP_INSTANCE_RESPONSE);
		fogbowInfrastructureProvider.setHttpWrapper(httpWrapperMock);

		FogbowResource fogbowResource = new FogbowResource(Constants.FakeData.RESOURCE_ID,
				Constants.FakeData.COMPUTE_ORDER_ID, spec, Constants.FakeData.PUBLIC_IP_ORDER_ID);
		Map<String, FogbowResource> resourcesMap = new ConcurrentHashMap<>();
		resourcesMap.put(Constants.FakeData.RESOURCE_ID, fogbowResource);
		fogbowInfrastructureProvider.setResourcesMap(resourcesMap);

		AbstractResource abstractResource = fogbowInfrastructureProvider.getResource(Constants.FakeData.RESOURCE_ID);

		assertEquals(Constants.FakeData.RESOURCE_ID, abstractResource.getId());
		assertEquals(Constants.FakeData.PUBLIC_IP_FAKE, abstractResource.getMetadataValue(BlowoutConstants.METADATA_PUBLIC_IP));

	}

	@Test
	public void getFogbowResourceTestSuccess() throws Exception{

		createResponse(Constants.ENDPOINT.getComputeInstanceEndpoint, Constants.JSON.Body.COMPUTE);
		createResponse(Constants.ENDPOINT.getPublicIpInstanceEndpoint, Constants.JSON.Body.PUBLIC_IP_INSTANCE_RESPONSE);

		fogbowInfrastructureProvider.setHttpWrapper(httpWrapperMock);

		FogbowResource fogbowResource = new FogbowResource(Constants.FakeData.RESOURCE_ID,
				Constants.FakeData.COMPUTE_ORDER_ID, spec, Constants.FakeData.PUBLIC_IP_ORDER_ID);
		Map<String, FogbowResource> resourcesMap = new ConcurrentHashMap<>();
		resourcesMap.put(Constants.FakeData.RESOURCE_ID, fogbowResource);
		fogbowInfrastructureProvider.setResourcesMap(resourcesMap);

		FogbowResource fogbowResourceReturn = fogbowInfrastructureProvider.getFogbowResource(Constants.FakeData.RESOURCE_ID);

		assertEquals(Constants.FakeData.RESOURCE_ID, fogbowResourceReturn.getId());
		assertEquals(Constants.FakeData.PUBLIC_IP_ORDER_ID, fogbowResourceReturn.getPublicIpOrderId());
		assertEquals(Constants.FakeData.COMPUTE_ORDER_ID, fogbowResourceReturn.getComputeOrderId());
		assertEquals(Constants.FakeData.PUBLIC_IP_FAKE, fogbowResourceReturn.getMetadataValue(BlowoutConstants.METADATA_PUBLIC_IP));


	}

	@Test
	public void getResourceTestInvalid(){
		String resourceId = Constants.FakeData.RESOURCE_ID;
		AbstractResource abstractResource = fogbowInfrastructureProvider.getResource(resourceId);
		assertNull(abstractResource);
	}

	@Test
	public void getFogbowResourceTestInvalid(){
		String resourceId = Constants.FakeData.RESOURCE_ID;
		String expectedExceptionMessage = AppMessagesConstants.RESOURCE_NOT_VALID;

		try {
			fogbowInfrastructureProvider.getFogbowResource(resourceId);
		} catch (InfrastructureException ie){
			assertEquals(expectedExceptionMessage, ie.getMessage());
		}
	}

	@Test
	public void getFogbowResourceFailRequests(){
		FogbowResource fogbowResource = new FogbowResource(Constants.FakeData.RESOURCE_ID,
				Constants.FakeData.COMPUTE_ORDER_ID, spec, Constants.FakeData.PUBLIC_IP_ORDER_ID);
		Map<String, FogbowResource> resourceMap = new ConcurrentHashMap<>();
		resourceMap.put(fogbowResource.getId(), fogbowResource);
		fogbowInfrastructureProvider.setResourcesMap(resourceMap);
		try {
			FogbowResource fr = fogbowInfrastructureProvider.getFogbowResource(fogbowResource.getId());
			assertNull(fr);
		} catch (InfrastructureException e) {
			e.printStackTrace();
			fail();
		}

	}

	@Test
	public void getResourceTestNoInstanceId() throws Exception{


		/*
		//Attributes
		String returnedOrderId = "order03";
		String instanceIdMock = "instance03";
		String ramSizeMock = "1024";
		String diskMock = "2";
		String vCPUMock = "1";
		String memberIdMock = "member01";

//		Create Mock behavior for httpWrapperMock
//		Creating response for request for resource.
		createDefaultRequestResponse(returnedOrderId);
//		Creating response for request for Instance ID
//		createDefaultInstanceIdResponse(returnedOrderId, instanceIdMock, memberIdMock, OrderState.FAILED);

		createDefaultInstanceAttributesResponseNoShh(returnedOrderId, vCPUMock, ramSizeMock, diskMock);

		fogbowInfrastructureProvider.setHttpWrapper(httpWrapperMock);

		FogbowResource resource = mock(FogbowResource.class);
		doReturn(returnedOrderId).when(resource).getId();

		Map<String, FogbowResource> resourceMap = new HashMap<String, FogbowResource>();
		resourceMap.put(resource.getId(), resource);
		
		fogbowInfrastructureProvider.setResourcesMap(resourceMap);
		
		FogbowResource newResource = fogbowInfrastructureProvider.getFogbowResource(returnedOrderId);

		assertNull(newResource);
		*/

	}

	@Test
	public void getResourceTestNotFulfilled() throws Exception{

		createResponse(Constants.ENDPOINT.getPublicIpInstanceEndpoint, Constants.JSON.Body.PUBLIC_IP_INSTANCE_RESPONSE);
		createResponse(Constants.ENDPOINT.getComputeInstanceEndpoint, Constants.JSON.Body.COMPUTE_NOT_READY);

		fogbowInfrastructureProvider.setHttpWrapper(httpWrapperMock);

		FogbowResource fogbowResource = new FogbowResource(Constants.FakeData.RESOURCE_ID,
				Constants.FakeData.COMPUTE_ORDER_ID, spec, Constants.FakeData.PUBLIC_IP_ORDER_ID);
		Map<String, FogbowResource> resourceMap = new ConcurrentHashMap<>();
		resourceMap.put(fogbowResource.getId(), fogbowResource);
		fogbowInfrastructureProvider.setResourcesMap(resourceMap);

		FogbowResource fogbowResourceReturned = fogbowInfrastructureProvider.getFogbowResource(Constants.FakeData.RESOURCE_ID);
		assertNull(fogbowResourceReturned);

	}

	@Test
	public void deleteResourceTestSuccess() throws Exception{

		String requestIdMock = "requestId";
		String instanceIdMock = "instance01";
		String memberIdMock = "member01";
		String urlEndpointInstanceDelete = properties.getProperty(AppPropertiesConstants.RAS_BASE_URL)
				+ "/compute/" + instanceIdMock;

		FogbowResource resource = mock(FogbowResource.class);
		doReturn(requestIdMock).when(resource).getId();
		createDefaultInstanceIdResponse(requestIdMock, instanceIdMock, memberIdMock);

		doReturn("OK").when(httpWrapperMock).doRequest(Mockito.eq("delete"), Mockito.eq(urlEndpointInstanceDelete), 
				Mockito.any(String.class), Mockito.any(List.class));
		doReturn(true).when(fogbowResourceDsMock).deleteFogbowResourceById(resource);

		Map<String, FogbowResource> resourceMap = new HashMap<String, FogbowResource>();
		resourceMap.put(resource.getId(), resource);
		
		fogbowInfrastructureProvider.setHttpWrapper(httpWrapperMock);
		fogbowInfrastructureProvider.setResourcesMap(resourceMap);
		fogbowInfrastructureProvider.deleteResource(resource.getId());

	}

	@Test
	public void deleteResourceTestFail() throws Exception {

		exception.expect(InfrastructureException.class);

		String requestIdMock = "requestId";
		String instanceIdMock = "instance01";
		String memberIdMock = "member01";
		String urlEndpointInstanceDelete = properties.getProperty(AppPropertiesConstants.RAS_BASE_URL)
				+ "/compute/" + instanceIdMock;

		FogbowResource resource = mock(FogbowResource.class);
		doReturn(requestIdMock).when(resource).getId();
		createDefaultInstanceIdResponse(requestIdMock, instanceIdMock, memberIdMock);

		doThrow(new Exception("Error on request.")).when(httpWrapperMock).doRequest(Mockito.eq("delete"), Mockito.eq(urlEndpointInstanceDelete),
				Mockito.any(String.class), Mockito.any(List.class));

		fogbowInfrastructureProvider.setHttpWrapper(httpWrapperMock);
		fogbowInfrastructureProvider.deleteResource(resource.getId());
	}

	// ---- HELPER METHODS ---- //

	private void createResponse(String endpoint, String bodyResponse) throws Exception {
		switch (endpoint){
			case Constants.ENDPOINT.getComputeInstanceEndpoint:
				createDefaultHttpWrapperResponses(HttpGet.METHOD_NAME, Constants.ENDPOINT.getComputeInstanceEndpoint, bodyResponse);
				break;
			case Constants.ENDPOINT.getPublicIpInstanceEndpoint:
				createDefaultHttpWrapperResponses(HttpGet.METHOD_NAME, Constants.ENDPOINT.getPublicIpInstanceEndpoint, bodyResponse);
				break;
			case Constants.ENDPOINT.getAllImagesEndpoint:
				createDefaultHttpWrapperResponses(HttpGet.METHOD_NAME, Constants.ENDPOINT.getAllImagesEndpoint, bodyResponse);
				break;
			case Constants.ENDPOINT.createPublicIPEndpoint:
				createDefaultHttpWrapperResponses(HttpPost.METHOD_NAME, Constants.ENDPOINT.createPublicIPEndpoint, bodyResponse);
				break;
			case Constants.ENDPOINT.createComputeEndpoint:
				createDefaultHttpWrapperResponses(HttpPost.METHOD_NAME, Constants.ENDPOINT.createComputeEndpoint, bodyResponse);
				break;
		}
	}

	private void createDefaultHttpWrapperResponses(String method, String endpoint, String bodyResponse) throws Exception {
		if(method.equals(HttpPost.METHOD_NAME)){
			when(httpWrapperMock.doRequest(eq(method), eq(endpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), anyList(), any(new StringEntity("null").getClass())))
					.thenReturn(bodyResponse);
		} else {
			when(httpWrapperMock.doRequest(eq(method), eq(endpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), anyList()))
					.thenReturn(bodyResponse);
		}

	}

	private void createDefaultInstanceIdResponse(String requestIdMock, String instanceIdMock, String location)
			throws Exception {

		String urlEndpointRequestInformation = properties.getProperty(AppPropertiesConstants.RAS_BASE_URL)
				+ "/"+ requestIdMock;

		Map<String, String> params = new HashMap<String, String>();
		params.put(FogbowInfrastructureTestUtils.REQUEST_ID_TAG, requestIdMock);
		params.put(FogbowInfrastructureTestUtils.INSTANCE_TAG, instanceIdMock);
		params.put(FogbowInfrastructureTestUtils.PROVIDER_MEMBER_TAG, location);
		String fogbowResponse = FogbowInfrastructureTestUtils.createHttpWrapperResponseFromFile(FILE_PATH_RESPONSE_INSTANCE_ID, params);

		doReturn(fogbowResponse).when(httpWrapperMock).doRequest(Mockito.any(String.class), Mockito.eq(urlEndpointRequestInformation),
				Mockito.any(String.class), Mockito.any(List.class));
	}

}
