package org.fogbowcloud.blowout.infrastructure.provider.fogbow;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.util.AppUtil;
import org.fogbowcloud.blowout.helpers.Constants;
import org.fogbowcloud.blowout.infrastructure.exception.RequestResourceException;
import org.fogbowcloud.blowout.infrastructure.http.HttpWrapper;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.infrastructure.model.Token;
import org.fogbowcloud.blowout.infrastructure.token.KeystoneTokenUpdatePlugin;
import org.fogbowcloud.blowout.infrastructure.token.AbstractTokenUpdatePlugin;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class RASRequestsHelperTest {
	private RASRequestsHelper rasRequestsHelperSpy;
	private static Specification spec;
	private static AbstractTokenUpdatePlugin tokenUpdatePluginMock;
	private static Properties properties;
	public HttpWrapper httpWrapperMock;

	@BeforeClass
	public static void init() throws IOException {
		properties = new Properties();
		properties.load(new FileInputStream(Constants.FILE_PATH_TESTS_CONFIG));

		Token token = Mockito.mock(Token.class);
		when(token.getAccessId()).thenReturn(Constants.FakeData.FAKE_ACCESS_ID);

		tokenUpdatePluginMock = mock(KeystoneTokenUpdatePlugin.class);
		when(tokenUpdatePluginMock.generateToken()).thenReturn(token);

		spec = new Specification(Constants.FakeData.CLOUD_NAME, Constants.FakeData.COMPUTE_IMAGE_FLAVOR_NAME,
				Constants.FakeData.FOGBOW_USER_NAME, Constants.FakeData.PUBLIC_KEY, Constants.FakeData.PRIVATE_KEY_FILE_PATH);
	}

	@Before
	public void setUp() {
		this.rasRequestsHelperSpy = spy(new RASRequestsHelper(properties, tokenUpdatePluginMock));
		this.httpWrapperMock = Mockito.mock(HttpWrapper.class);
	}

	@Test
	public void testCreateComputeSuccess() throws Exception {
		when(httpWrapperMock.doRequest(eq(HttpGet.METHOD_NAME), eq(Constants.ENDPOINT.getAllImagesEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), any(List.class)))
				.thenReturn(Constants.JSON.Body.IMAGES_RESPONSE);

		when(httpWrapperMock.doRequest(eq(HttpPost.METHOD_NAME), eq(Constants.ENDPOINT.createComputeEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), any(List.class), any(StringEntity.class)))
				.thenReturn(Constants.JSON.Body.COMPUTE_ORDER_ID);

		rasRequestsHelperSpy.setHttpWrapper(httpWrapperMock);

		final String fakeComputeOrderId = this.rasRequestsHelperSpy.createCompute(this.spec);

		verify(rasRequestsHelperSpy, times(1)).makeJsonBody(spec);
		verify(rasRequestsHelperSpy, times(1)).createCompute(spec);

		assertEquals(Constants.FakeData.COMPUTE_ORDER_ID, fakeComputeOrderId);
	}

	@Test(expected = RequestResourceException.class)
	public void testCreateComputeFail() throws Exception {
		when(httpWrapperMock.doRequest(anyString(), anyString(), anyString(), anyList()))
				.thenThrow(new Exception());
		this.rasRequestsHelperSpy.setHttpWrapper(httpWrapperMock);
		this.rasRequestsHelperSpy.createCompute(this.spec);

	}

	@Test
	public void testCreatePublicIpSuccess() throws Exception {
		when(httpWrapperMock.doRequest(eq(HttpPost.METHOD_NAME), eq(Constants.ENDPOINT.createPublicIPEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), any(List.class), any(StringEntity.class)))
				.thenReturn(Constants.JSON.Body.PUBLIC_IP_ORDER_ID);

		rasRequestsHelperSpy.setHttpWrapper(httpWrapperMock);

		final String fakePublicIpOrderId = this.rasRequestsHelperSpy.createPublicIp(Constants.FakeData.COMPUTE_ORDER_ID);

		verify(rasRequestsHelperSpy, times(1)).createPublicIp(Constants.FakeData.COMPUTE_ORDER_ID);

		assertEquals(Constants.FakeData.PUBLIC_IP_ORDER_ID, fakePublicIpOrderId);
	}

	@Test
	public void testGetPublicIpInstanceSuccess() throws Exception {

		when(httpWrapperMock.doRequest(eq(HttpGet.METHOD_NAME), eq(Constants.ENDPOINT.getPublicIpInstanceEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), any(List.class)))
				.thenReturn(Constants.JSON.Body.PUBLIC_IP_INSTANCE_RESPONSE);
		rasRequestsHelperSpy.setHttpWrapper(httpWrapperMock);

		Map<String, Object> publicIpInstance = this.rasRequestsHelperSpy.getPublicIpInstance(Constants.FakeData.PUBLIC_IP_ORDER_ID);

		verify(rasRequestsHelperSpy, times(1)).getPublicIpInstance(Constants.FakeData.PUBLIC_IP_ORDER_ID);

		assertNotNull(publicIpInstance);
		System.out.println(publicIpInstance.toString());

		final int publicIpInstancePropQuantityExpected = 7;
		final int publicIpInstancePropQuantity = publicIpInstance.keySet().size();

		assertEquals(AppUtil.parseJSONStringToMap(Constants.JSON.Body.PUBLIC_IP_INSTANCE_RESPONSE), publicIpInstance);
		assertEquals(publicIpInstancePropQuantityExpected, publicIpInstancePropQuantity);
	}

	@Test
	public void testGetComputeInstanceSuccess() throws Exception {
		when(httpWrapperMock.doRequest(eq(HttpGet.METHOD_NAME), eq(Constants.ENDPOINT.getComputeInstanceEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), anyList()))
				.thenReturn(Constants.JSON.Body.COMPUTE);
		rasRequestsHelperSpy.setHttpWrapper(httpWrapperMock);

		Map<String, Object> computeInstance = rasRequestsHelperSpy.getComputeInstance(Constants.FakeData.COMPUTE_ORDER_ID);

		verify(rasRequestsHelperSpy).getComputeInstance(Constants.FakeData.COMPUTE_ORDER_ID);

		assertEquals(AppUtil.parseJSONStringToMap(Constants.JSON.Body.COMPUTE), computeInstance);
	}

	@Test
	public void testDeleteFogbowResourceSuccess() throws Exception {
		FogbowResource fogbowResourceMock = mock(FogbowResource.class);
		when(fogbowResourceMock.getComputeOrderId()).thenReturn(Constants.FakeData.COMPUTE_ORDER_ID);
		when(fogbowResourceMock.getPublicIpOrderId()).thenReturn(Constants.FakeData.PUBLIC_IP_ORDER_ID);

		final String computeEndpoint = Constants.ENDPOINT.getComputeInstanceEndpoint;
		final String publicIpEndpoint = Constants.ENDPOINT.createPublicIPEndpoint + fogbowResourceMock.getPublicIpOrderId();

		when(httpWrapperMock.doRequest(eq(HttpDelete.METHOD_NAME), eq(computeEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), anyList()))
				.thenReturn("OK");
		when(httpWrapperMock.doRequest(eq(HttpDelete.METHOD_NAME), eq(publicIpEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), anyList()))
				.thenReturn("OK");
		when(httpWrapperMock.doRequest(anyString(), anyString(), anyString(), anyList()))
				.thenThrow(new Exception());

		rasRequestsHelperSpy.setHttpWrapper(httpWrapperMock);

		rasRequestsHelperSpy.deleteFogbowResource(fogbowResourceMock);

		verify(fogbowResourceMock, times(1)).getComputeOrderId();
		verify(fogbowResourceMock, times(2)).getPublicIpOrderId();
		verify(rasRequestsHelperSpy, times(1)).deleteFogbowResource(fogbowResourceMock);

	}

	@Test
	public void testMakeJsonBodySuccess() throws Exception {
		when(httpWrapperMock.doRequest(eq(HttpGet.METHOD_NAME), eq(Constants.ENDPOINT.getAllImagesEndpoint), eq(Constants.FakeData.FAKE_ACCESS_ID), any(List.class)))
				.thenReturn(Constants.JSON.Body.IMAGES_RESPONSE);
		rasRequestsHelperSpy.setHttpWrapper(httpWrapperMock);
		StringEntity stringEntity = rasRequestsHelperSpy.makeJsonBody(spec);

		verify(rasRequestsHelperSpy, times(1)).makeJsonBody(spec);

		assertEquals(Constants.JSON.Body.SPECIFICATION, EntityUtils.toString(stringEntity));
	}
}