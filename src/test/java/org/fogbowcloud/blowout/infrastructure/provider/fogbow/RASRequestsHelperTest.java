package org.fogbowcloud.blowout.infrastructure.provider.fogbow;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.fogbowcloud.blowout.core.constants.FogbowConstants;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.util.AppUtil;
import org.fogbowcloud.blowout.helpers.Constants;
import org.fogbowcloud.blowout.helpers.HoverflyRules;
import org.fogbowcloud.blowout.infrastructure.exception.RequestResourceException;
import org.fogbowcloud.blowout.infrastructure.http.HttpWrapper;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.infrastructure.model.Token;
import org.fogbowcloud.blowout.infrastructure.token.KeystoneTokenUpdatePlugin;
import org.fogbowcloud.blowout.infrastructure.token.AbstractTokenUpdatePlugin;
import org.fogbowcloud.blowout.infrastructure.model.User;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class RASRequestsHelperTest {
	private RASRequestsHelper rasRequestsHelper;
	private Specification spec;

	private static final String createComputeEndpoint = "/" + FogbowConstants.RAS_ENDPOINT_COMPUTE;

	/*
	@ClassRule
	public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(HoverflyRules.simulationSource);
	*/

	@Before
	public void setUp() throws IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(Constants.FILE_PATH_TESTS_CONFIG));

		Token token = Mockito.mock(Token.class);
		when(token.getAccessId()).thenReturn(Constants.FakeData.FAKE_ACCESS_ID);

		AbstractTokenUpdatePlugin abstractTokenUpdatePlugin = Mockito.mock(KeystoneTokenUpdatePlugin.class);
		when(abstractTokenUpdatePlugin.generateToken()).thenReturn(token);


		//.thenReturn(new Token(Constants.FakeData.FakeUser.FAKE_USER_ID, new User(Constants.FakeData.FakeUser.FAKE_USER_ID, Constants.FakeData.FakeUser.FAKE_USER_NAME, Constants.FakeData.FakeUser.FAKE_USER_PASSWORD)));
		this.rasRequestsHelper = new RASRequestsHelper(properties, abstractTokenUpdatePlugin);
		this.spec = new Specification(Constants.FakeData.CLOUD_NAME, Constants.FakeData.COMPUTE_IMAGE_FLAVOR_NAME,
				Constants.FakeData.FOGBOW_USER_NAME, Constants.FakeData.PUBLIC_KEY, Constants.FakeData.PRIVATE_KEY_FILE_PATH);
	}

	@Test
	public void testCreateComputeSuccess() throws Exception {
		HttpWrapper http = Mockito.mock(HttpWrapper.class);

		when(http.doRequest(eq(HttpGet.METHOD_NAME), eq(Constants.ENDPOINT.getAllImagesEndpoint), anyString(), any(List.class)))
				.thenReturn(Constants.JSON.Body.IMAGES_RESPONSE);

		when(http.doRequest(eq(HttpPost.METHOD_NAME), eq(Constants.ENDPOINT.createComputeEndpoint), anyString(), any(List.class), any(StringEntity.class)))
				.thenReturn(Constants.JSON.Body.COMPUTE_ORDER_ID);

		rasRequestsHelper.setHttpWrapper(http);

		final String fakeComputeOrderId = this.rasRequestsHelper.createCompute(this.spec);

		assertEquals(Constants.FakeData.COMPUTE_ORDER_ID, fakeComputeOrderId);
	}

	@Test(expected = RequestResourceException.class)
	public void testCreateComputeFail() throws Exception {
		HttpWrapper http = mock(HttpWrapper.class);
		when(http.doRequest(anyString(), anyString(), anyString(), anyList()))
				.thenThrow(new Exception());
		this.rasRequestsHelper.setHttpWrapper(http);
		this.rasRequestsHelper.createCompute(this.spec);

	}

	@Test
	public void testCreatePublicIpSuccess() throws Exception {
		HttpWrapper http = Mockito.mock(HttpWrapper.class);
		when(http.doRequest(eq(HttpPost.METHOD_NAME), eq(Constants.ENDPOINT.createPublicIPEndpoint), anyString(), any(List.class), any(StringEntity.class)))
				.thenReturn(Constants.JSON.Body.PUBLIC_IP_ORDER_ID);

		rasRequestsHelper.setHttpWrapper(http);

		final String fakePublicIpOrderId = this.rasRequestsHelper.createPublicIp(Constants.FakeData.COMPUTE_ORDER_ID);

		assertEquals(Constants.FakeData.PUBLIC_IP_ORDER_ID, fakePublicIpOrderId);
	}

	@Test
	public void testGetPublicIpInstanceSuccess() throws Exception { ;
		HttpWrapper http = Mockito.mock(HttpWrapper.class);

		when(http.doRequest(eq(HttpGet.METHOD_NAME), eq(Constants.ENDPOINT.getPublicIpInstanceEndpoint), anyString(), any(List.class)))
				.thenReturn(Constants.JSON.Body.PUBLIC_IP_INSTANCE_RESPONSE);
		rasRequestsHelper.setHttpWrapper(http);

		Map<String, Object> publicIpInstance = this.rasRequestsHelper.getPublicIpInstance(Constants.FakeData.PUBLIC_IP_ORDER_ID);

		assertNotNull(publicIpInstance);
		System.out.println(publicIpInstance.toString());

		final int publicIpInstancePropQuantityExpected = 7;
		final int publicIpInstancePropQuantity = publicIpInstance.keySet().size();

		assertEquals(AppUtil.parseJSONStringToMap(Constants.JSON.Body.PUBLIC_IP_INSTANCE_RESPONSE), publicIpInstance);
		assertEquals(publicIpInstancePropQuantityExpected, publicIpInstancePropQuantity);
	}

	@Test
	public void testGetComputeInstanceSuccess() throws Exception {
		HttpWrapper http = mock(HttpWrapper.class);
		when(http.doRequest(eq(HttpGet.METHOD_NAME), eq(Constants.ENDPOINT.getComputeInstanceEndpoint), anyString(), anyList()))
				.thenReturn(Constants.JSON.Body.COMPUTE);
		rasRequestsHelper.setHttpWrapper(http);

		Map<String, Object> computeInstance = rasRequestsHelper.getComputeInstance(Constants.FakeData.COMPUTE_ORDER_ID);

		assertEquals(AppUtil.parseJSONStringToMap(Constants.JSON.Body.COMPUTE), computeInstance);
	}

	@Test
	public void testDeleteFogbowResourceSuccess() throws Exception {
		FogbowResource fogbowResource = mock(FogbowResource.class);
		when(fogbowResource.getComputeOrderId()).thenReturn(Constants.FakeData.COMPUTE_ORDER_ID);
		when(fogbowResource.getPublicIpOrderId()).thenReturn(Constants.FakeData.PUBLIC_IP_ORDER_ID);

		final String computeEndpoint = Constants.ENDPOINT.getComputeInstanceEndpoint;
		final String publicIpEndpoint = Constants.ENDPOINT.createPublicIPEndpoint + fogbowResource.getPublicIpOrderId();

		HttpWrapper http = mock(HttpWrapper.class);
		when(http.doRequest(eq(HttpDelete.METHOD_NAME), eq(computeEndpoint), anyString(), anyList()))
				.thenReturn("OK");
		when(http.doRequest(eq(HttpDelete.METHOD_NAME), eq(publicIpEndpoint), anyString(), anyList()))
				.thenReturn("OK");
		when(http.doRequest(anyString(), anyString(), anyString(), anyList()))
				.thenThrow(new Exception());

		rasRequestsHelper.setHttpWrapper(http);

		rasRequestsHelper.deleteFogbowResource(fogbowResource);

	}

	@Test
	public void testMakeJsonBodySuccess() throws Exception {
		HttpWrapper http = mock(HttpWrapper.class);
		when(http.doRequest(eq(HttpGet.METHOD_NAME), eq(Constants.ENDPOINT.getAllImagesEndpoint), anyString(), any(List.class)))
				.thenReturn(Constants.JSON.Body.IMAGES_RESPONSE);
		rasRequestsHelper.setHttpWrapper(http);
		StringEntity stringEntity = rasRequestsHelper.makeJsonBody(this.spec);

		assertEquals(Constants.JSON.Body.SPECIFICATION, EntityUtils.toString(stringEntity));
	}
}