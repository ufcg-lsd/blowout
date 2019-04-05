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
		FogbowInfrastructureProvider fogbowInfrastructureProvider = new FogbowInfrastructureProvider(properties, exec, tokenUpdatePluginMock);
		Specification specs = new Specification(Constants.FakeData.CLOUD_NAME,"imageMock", "UserName",
				"publicKeyMock", "privateKeyMock", FILE_PATH_USER_DATA_MOCK, "userDataType");

		try {
			StringEntity bodyJson = fogbowInfrastructureProvider.makeJsonBody(specs);
			String bodyJsonString = EntityUtils.toString(bodyJson);

			assertTrue(bodyJsonString.contains(FogbowConstants.JSON_KEY_RAS_PUBLIC_KEY));
			assertTrue(bodyJsonString.contains(FogbowConstants.JSON_KEY_RAS_IMAGE_ID));
			assertTrue(!bodyJsonString.contains(FogbowConstants.JSON_KEY_RAS_VCPU));
			assertTrue(!bodyJsonString.contains(FogbowConstants.JSON_KEY_RAS_DISK));
			assertTrue(!bodyJsonString.contains(FogbowConstants.JSON_KEY_RAS_MEMORY));
		} catch (JSONException e) {
			Assert.fail();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void requestResourceGetRequestIdTestSuccess(){
		try {

			when(httpWrapperMock.doRequest(eq(HttpGet.METHOD_NAME), eq(Constants.ENDPOINT.getAllImagesEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), any(List.class)))
					.thenReturn(Constants.JSON.Body.IMAGES_RESPONSE);

			when(httpWrapperMock.doRequest(eq(HttpPost.METHOD_NAME), eq(Constants.ENDPOINT.createComputeEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), any(List.class), any(StringEntity.class)))
					.thenReturn(Constants.JSON.Body.COMPUTE_ORDER_ID);

			when(httpWrapperMock.doRequest(eq(HttpPost.METHOD_NAME), eq(Constants.ENDPOINT.createPublicIPEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), any(List.class), any(StringEntity.class)))
					.thenReturn(Constants.JSON.Body.PUBLIC_IP_ORDER_ID);

			when(httpWrapperMock.doRequest(eq(HttpGet.METHOD_NAME), eq(Constants.ENDPOINT.getComputeInstanceEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), anyList()))
					.thenReturn(Constants.JSON.Body.COMPUTE);

			when(httpWrapperMock.doRequest(eq(HttpGet.METHOD_NAME), eq(Constants.ENDPOINT.getPublicIpInstanceEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), any(List.class)))
					.thenReturn(Constants.JSON.Body.PUBLIC_IP_INSTANCE_RESPONSE);

			fogbowInfrastructureProvider.setHttpWrapper(httpWrapperMock);

			String resourceId = fogbowInfrastructureProvider.requestResource(spec);
			assertNotNull(resourceId);

			AbstractResource abstractResource = fogbowInfrastructureProvider.getResource(resourceId);
			assertEquals(resourceId, abstractResource.getId());
			assertEquals(Constants.FakeData.PUBLIC_IP_ORDER_ID, ((FogbowResource) abstractResource).getPublicIpOrderId());

			/*
			String requestIdMock = "request01";

			createDefaultRequestResponse(requestIdMock);

			doReturn(true).when(fogbowResourceDsMock).addFogbowResource(Mockito.any(FogbowResource.class));

			String resourceId = fogbowInfrastructureProvider.requestResource(spec);
			assertNotNull(resourceId);
			*/
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void getResourceTestSuccess() throws Exception{
		when(httpWrapperMock.doRequest(eq(HttpGet.METHOD_NAME), eq(Constants.ENDPOINT.getComputeInstanceEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), anyList()))
				.thenReturn(Constants.JSON.Body.COMPUTE);
		when(httpWrapperMock.doRequest(eq(HttpGet.METHOD_NAME), eq(Constants.ENDPOINT.getPublicIpInstanceEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), any(List.class)))
				.thenReturn(Constants.JSON.Body.PUBLIC_IP_INSTANCE_RESPONSE);
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

		when(httpWrapperMock.doRequest(eq(HttpGet.METHOD_NAME), eq(Constants.ENDPOINT.getComputeInstanceEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), anyList()))
				.thenReturn(Constants.JSON.Body.COMPUTE);
		when(httpWrapperMock.doRequest(eq(HttpGet.METHOD_NAME), eq(Constants.ENDPOINT.getPublicIpInstanceEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), any(List.class)))
				.thenReturn(Constants.JSON.Body.PUBLIC_IP_INSTANCE_RESPONSE);
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
	public void getResourceTestNoInstanceId() throws Exception{

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

	}

	@Test
	public void getResourceTestNotFulfilled() throws Exception{

		//Attributes
		String returnedOrderId = "order02";
		String instanceIdMock = "instance02";
		String ramSizeMock = "1024";
		String diskMock = "4";
		String vCPUMock = "1";
		String hostMock = "10.0.1.10";
		String portMock = "8989";
		String hostNameMock = "fake-hostname";

		//Create Mock behavior for httpWrapperMock
		//Creating response for request for resource.
		createDefaultRequestResponse(returnedOrderId);
//		Creating response for request for Instance ID
//		createDefaulRequestInstanceIdResponseNoId(requestIdMock);
//		Creating response for request for Instance Attributes
		createDefaultInstanceAttributesResponse(returnedOrderId, vCPUMock, ramSizeMock, diskMock, hostNameMock);

		Specification specs = new Specification(Constants.FakeData.CLOUD_NAME,"imageMock", "UserName",
				"publicKeyMock", "privateKeyMock", FILE_PATH_USER_DATA_MOCK, "userDataType");

		fogbowInfrastructureProvider.setHttpWrapper(httpWrapperMock);
		String resourceId = fogbowInfrastructureProvider.requestResource(specs);

		FogbowResource resource = mock(FogbowResource.class);
		doReturn(resourceId).when(resource).getId();
		Map<String, FogbowResource> resourceMap = new HashMap<String, FogbowResource>();
		resourceMap.put(resource.getId(), resource);
		
		fogbowInfrastructureProvider.setResourcesMap(resourceMap);
		
		FogbowResource newResource = fogbowInfrastructureProvider.getFogbowResource(resourceId);

		assertNull(newResource);

	}

	@Test
	public void getResourceTestNoSShInformation() throws Exception{

		//Attributes
		String returnedOrderId = "order03";
		String instanceIdMock = "instance04";
		String ramSizeMock = "1024";
		String diskMock = "1";
		String vCPUMock = "1";
		String memberIdMock = "member01";

//		Create Mock behavior for httpWrapperMock
//		Creating response for request for resource.
		createDefaultRequestResponse(returnedOrderId);
//		Creating response for request for Instance ID
//		createDefaulInstanceIdResponse(returnedOrderId, instanceIdMock, memberIdMock, OrderState.FULFILLED);
//		Creating response for request for Instance Attributes
		createDefaultInstanceAttributesResponseNoShh(returnedOrderId, vCPUMock, ramSizeMock, diskMock);

		Specification specs = new Specification(Constants.FakeData.CLOUD_NAME,"imageMock", "UserName",
				"publicKeyMock", "privateKeyMock", FILE_PATH_USER_DATA_MOCK, "userDataType");

		fogbowInfrastructureProvider.setHttpWrapper(httpWrapperMock);
		fogbowInfrastructureProvider.requestResource(specs);
				
		FogbowResource resource = mock(FogbowResource.class);
		doReturn(returnedOrderId).when(resource).getId();
		
		Map<String, FogbowResource> resourceMap = new HashMap<String, FogbowResource>();
		resourceMap.put(resource.getId(), resource);
		
		fogbowInfrastructureProvider.setResourcesMap(resourceMap);

		FogbowResource newResource = fogbowInfrastructureProvider.getFogbowResource(returnedOrderId);

		assertNull(newResource);

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

	private void createDefaultRequestResponse(String returnedOrderId) throws Exception {

		String urlEndpointNewInstance = properties.getProperty(AppPropertiesConstants.RAS_BASE_URL)
				+ "/" + FogbowConstants.RAS_ENDPOINT_COMPUTE;

		doReturn(returnedOrderId).when(httpWrapperMock).doRequest(Mockito.any(String.class), Mockito.eq(urlEndpointNewInstance),
				Mockito.any(String.class), Mockito.any(List.class), Mockito.any(StringEntity.class));
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

	private void createDefaulRequestInstanceIdResponseNoId(String requestIdMock) 
			throws Exception {

		String urlEndpointRequestInformation = properties.getProperty(AppPropertiesConstants.RAS_BASE_URL)
				+ "/"+ requestIdMock;

		Map<String, String> params = new HashMap<String, String>();
		params.put(FogbowInfrastructureTestUtils.REQUEST_ID_TAG, requestIdMock);
		String fogbowResponse = FogbowInfrastructureTestUtils.createHttpWrapperResponseFromFile(FILE_PATH_RESPONSE_NO_INSTANCE_ID, params);

		doReturn(fogbowResponse).when(httpWrapperMock).doRequest(Mockito.any(String.class), Mockito.eq(urlEndpointRequestInformation),
				Mockito.any(String.class), Mockito.any(List.class));
	}

	private void createDefaultInstanceAttributesResponse(String orderId, String vCPU, String memSize, String disk,
														 String hostName) throws Exception {

		String urlEndpointRequestInformation = properties.getProperty(AppPropertiesConstants.RAS_BASE_URL)
				+ "/" + FogbowConstants.RAS_ENDPOINT_COMPUTE + "/"+ orderId;

		String fogbowResponse = "{"
				+ "\"id\":\"" + orderId + "\", "
				+ "\"vCPU\":\"" +  vCPU + "\", "
				+ "\"memory\":\"" + memSize + "\", "
				+ "\"disk\":\"" + disk + "\", "
				+ "\"hostName\":\"" + hostName + "\"}";

		doReturn(fogbowResponse).when(httpWrapperMock).doRequest(Mockito.any(String.class), Mockito.eq(urlEndpointRequestInformation),
				Mockito.any(String.class), Mockito.any(List.class));
	}

	private void createDefaultInstanceAttributesResponseNoShh(String orderId, String vCPU, String ram, String disk) throws Exception {

		String urlEndpointRequestInformation = properties.getProperty(AppPropertiesConstants.RAS_BASE_URL)
				+ "/" + FogbowConstants.RAS_ENDPOINT_COMPUTE + "/"+ orderId;

		String fogbowResponse = "{"
				+ "\"id\":\"" + orderId + "\", "
				+ "\"vCPU\":\"" +  vCPU + "\", "
				+ "\"ram\":\"" + ram + "\", "
				+ "\"disk\":\"" + disk + "\"}";

		doReturn(fogbowResponse).when(httpWrapperMock).doRequest(Mockito.any(String.class), Mockito.eq(urlEndpointRequestInformation),
				Mockito.any(String.class), Mockito.any(List.class), Mockito.any(StringEntity.class));
	}

	private void generateDefaultProperties(){

		properties = new Properties();

		properties.setProperty(AppPropertiesConstants.INFRA_IS_ELASTIC, "false");
		properties.setProperty(AppPropertiesConstants.INFRA_PROVIDER_PLUGIN,
				"org.fogbowcloud.scheduler.infrastructure.fogbow.FogbowInfrastructureProvider");
		properties.setProperty(AppPropertiesConstants.INFRA_RESOURCE_CONNECTION_TIMEOUT, "10000");
		properties.setProperty(AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME, "300000");
		properties.setProperty(AppPropertiesConstants.RAS_BASE_URL, "100_02_01_01:8098");
		properties.setProperty("fogbow.voms.server", "server");
		properties.setProperty("fogbow.voms.certificate.password", "password");

	}

	private void createDefaultPublicIpResponsePostRequest(String publicOrderId) throws Exception {
		String urlEndpointRequestInformation = properties.getProperty(AppPropertiesConstants.RAS_BASE_URL)
				+ "/" + FogbowConstants.RAS_ENDPOINT_PUBLIC_IP;

		doReturn(publicOrderId).when(httpWrapperMock).doRequest(Mockito.any(String.class), Mockito.eq(urlEndpointRequestInformation),
				Mockito.any(String.class), Mockito.any(List.class), Mockito.any(StringEntity.class));
	}

	private void createDefaultPublicIpResponseGetRequest(String publicOrderId, String ip, String state, String provider) throws Exception {
		String urlEndpointRequestInformations = properties.getProperty(AppPropertiesConstants.RAS_BASE_URL)
				+ "/" + FogbowConstants.RAS_ENDPOINT_PUBLIC_IP + "/"+ publicOrderId;

		String fogbowResponse = "{"
				+ "\"" + FogbowConstants.JSON_KEY_FOGBOW_PUBLIC_IP + "\":\"" + ip + "\", "
				+ "\"" + FogbowConstants.INSTANCE_ATTRIBUTE_STATE + "\":\"" +  state + "\"}";

		doReturn(fogbowResponse).when(httpWrapperMock).doRequest(Mockito.any(String.class), Mockito.eq(urlEndpointRequestInformations),
				Mockito.any(String.class), Mockito.any(List.class));
	}

}
