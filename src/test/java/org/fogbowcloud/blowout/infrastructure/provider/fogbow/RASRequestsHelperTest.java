package org.fogbowcloud.blowout.infrastructure.provider.fogbow;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.helpers.Constants;
import org.fogbowcloud.blowout.infrastructure.exception.RequestResourceException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

public class RASRequestsHelperTest {
	private RASRequestsHelper rasRequestsHelper;
	private Specification spec;

	// setUp
	// exercise
	// verify
	// setDown

	@Before
	public void setUp() {
		this.rasRequestsHelper = mock(RASRequestsHelper.class);
		this.spec = mock(Specification.class);
	}

	@Test
	public void testCreateComputeSuccess() throws RequestResourceException {
		when(this.rasRequestsHelper.createCompute(this.spec))
				.thenReturn(Constants.FAKE_COMPUTE_ORDER_ID);

		final String fakeComputeOrderId = this.rasRequestsHelper.createCompute(this.spec);

		verify(this.rasRequestsHelper, times(Constants.WANTED_NUMBER_OF_INVOCATIONS))
				.createCompute(this.spec);

		assertEquals(Constants.FAKE_COMPUTE_ORDER_ID, fakeComputeOrderId);
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
				.thenReturn(Constants.FAKE_COMPUTE_ORDER_ID);

		final String fakeComputeOrderId = this.rasRequestsHelper.createCompute(this.spec);

		when(this.rasRequestsHelper.createPublicIp(fakeComputeOrderId))
				.thenReturn(Constants.FAKE_PUBLIC_IP_ORDER_ID);

		final String fakePublicIpOrderId = this.rasRequestsHelper.createPublicIp(fakeComputeOrderId);

		verify(this.rasRequestsHelper, times(Constants.WANTED_NUMBER_OF_INVOCATIONS))
				.createPublicIp(fakeComputeOrderId);

		assertEquals(Constants.FAKE_PUBLIC_IP_ORDER_ID, fakePublicIpOrderId);
	}

	@Test
	public void testGetPublicIpInstanceSuccess() throws RequestResourceException, InterruptedException {
		when(this.rasRequestsHelper.createCompute(this.spec))
				.thenReturn(Constants.FAKE_COMPUTE_ORDER_ID);

		final String fakeComputeOrderId = this.rasRequestsHelper.createCompute(this.spec);

		when(this.rasRequestsHelper.createPublicIp(fakeComputeOrderId))
				.thenReturn(Constants.FAKE_PUBLIC_IP_ORDER_ID);

		final String fakePublicIpOrderId = this.rasRequestsHelper.createPublicIp(fakeComputeOrderId);

		when(this.rasRequestsHelper.getPublicIpInstance(fakePublicIpOrderId))
				.thenReturn(new HashMap<>());

		Map<String, Object> publicIpInstance = this.rasRequestsHelper.getPublicIpInstance(fakePublicIpOrderId);
	}

	@Test
	public void testGetComputeInstanceSuccess() {
	}

	@Test
	public void testDeleteFogbowResourceSuccess() {
	}

	@Test
	public void testMakeJsonBodySuccess() {
	}
}