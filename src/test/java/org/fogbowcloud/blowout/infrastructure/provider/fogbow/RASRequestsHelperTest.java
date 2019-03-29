package org.fogbowcloud.blowout.infrastructure.provider.fogbow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import io.specto.hoverfly.junit.rule.HoverflyRule;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.helpers.Constants;
import org.fogbowcloud.blowout.helpers.HoverflyRules;
import org.fogbowcloud.blowout.infrastructure.exception.RequestResourceException;
import org.fogbowcloud.blowout.infrastructure.token.KeystoneTokenUpdatePlugin;
import org.fogbowcloud.blowout.infrastructure.token.AbstractTokenUpdatePlugin;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class RASRequestsHelperTest {
	private RASRequestsHelper rasRequestsHelper;
	private Specification spec;

	@ClassRule
	public static HoverflyRule hoverflyRule = HoverflyRule.inSimulationMode(HoverflyRules.simulationSource);

	@Before
	public void setUp() throws IOException {
		Properties properties = new Properties();
		properties.load(new FileInputStream(Constants.FILE_PATH_TESTS_CONFIG));
		AbstractTokenUpdatePlugin abstractTokenUpdatePlugin = new KeystoneTokenUpdatePlugin(properties);

		this.rasRequestsHelper = new RASRequestsHelper(properties, abstractTokenUpdatePlugin);
		this.spec = mock(Specification.class);
	}

	@Test
	public void testCreateComputeSuccess() throws RequestResourceException {
		when(this.rasRequestsHelper.createCompute(this.spec))
				.thenReturn(Constants.FakeData.COMPUTE_ORDER_ID);

		final String fakeComputeOrderId = this.rasRequestsHelper.createCompute(this.spec);

		verify(this.rasRequestsHelper, times(Constants.WANTED_NUMBER_OF_INVOCATIONS))
				.createCompute(this.spec);

		assertEquals(Constants.FakeData.COMPUTE_ORDER_ID, fakeComputeOrderId);
	}

	@Test(expected = RequestResourceException.class)
	public void testCreateComputeFail() throws RequestResourceException {
		when(this.rasRequestsHelper.createCompute(this.spec))
				.thenThrow(new RequestResourceException());

		this.rasRequestsHelper.createCompute(this.spec);

		verify(this.rasRequestsHelper, times(Constants.WANTED_NUMBER_OF_INVOCATIONS))
				.createCompute(this.spec);
	}

	@Test
	public void testCreatePublicIpSuccess() throws RequestResourceException, InterruptedException {
		when(this.rasRequestsHelper.createCompute(this.spec))
				.thenReturn(Constants.FakeData.COMPUTE_ORDER_ID);

		final String fakeComputeOrderId = this.rasRequestsHelper.createCompute(this.spec);

		when(this.rasRequestsHelper.createPublicIp(fakeComputeOrderId))
				.thenReturn(Constants.FakeData.PUBLIC_IP_ORDER_ID);

		final String fakePublicIpOrderId = this.rasRequestsHelper.createPublicIp(fakeComputeOrderId);

		verify(this.rasRequestsHelper, times(Constants.WANTED_NUMBER_OF_INVOCATIONS))
				.createPublicIp(fakeComputeOrderId);

		assertEquals(Constants.FakeData.PUBLIC_IP_ORDER_ID, fakePublicIpOrderId);
	}

	@Test
	public void testGetPublicIpInstanceSuccess() throws RequestResourceException, InterruptedException {

		when(this.rasRequestsHelper.createCompute(this.spec))
				.thenReturn(Constants.FakeData.COMPUTE_ORDER_ID);
		final String fakeComputeOrderId = this.rasRequestsHelper.createCompute(this.spec);

		when(this.rasRequestsHelper.createPublicIp(fakeComputeOrderId))
				.thenReturn(Constants.FakeData.PUBLIC_IP_ORDER_ID);

		final String fakePublicIpOrderId = this.rasRequestsHelper.createPublicIp(fakeComputeOrderId);

		when(this.rasRequestsHelper.getPublicIpInstance(fakePublicIpOrderId))
				.thenReturn(new HashMap<>());

		Map<String, Object> publicIpInstance = this.rasRequestsHelper.getPublicIpInstance(fakePublicIpOrderId);

		verify(this.rasRequestsHelper, times(Constants.WANTED_NUMBER_OF_INVOCATIONS))
				.getPublicIpInstance(fakePublicIpOrderId);

		assertNotNull(publicIpInstance);
		System.out.println(publicIpInstance.toString());

		final int publicIpInstancePropQuantityExpected = 7;
		final int publicIpInstancePropQuantity = publicIpInstance.keySet().size();

		assertEquals(publicIpInstancePropQuantityExpected, publicIpInstancePropQuantity);
	}

	@Test
	public void testGetComputeInstanceSuccess() {
		// Todo
		// setUp
		// exercise
		// verify
		// setDown
	}

	@Test
	public void testDeleteFogbowResourceSuccess() {
		// Todo
		// setUp
		// exercise
		// verify
		// setDown
	}

	@Test
	public void testMakeJsonBodySuccess() {
		// Todo
		// setUp
		// exercise
		// verify
		// setDown
	}
}