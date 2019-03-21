package org.fogbowcloud.blowout.infrastructure.provider.fogbow;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.fogbowcloud.blowout.helpers.Constants.FakeData.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.constants.AppPropertiesConstants;
import org.fogbowcloud.blowout.helpers.Constants;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.infrastructure.model.ResourceHelperTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class FogbowRequirementsHelperTest {
	private Properties properties;
	private Map<String, String> requirements;
	private Specification spec;
	private FogbowResource suitableResource;

	@Before
	public void setUp() throws Exception {

		this.generateDefaultProperties();

		this.requirements = new HashMap<>();
		this.spec = new Specification(CLOUD_NAME, COMPUTE_IMAGE_FLAVOR_NAME, FOGBOW_USER_NAME,
				PUBLIC_KEY, PRIVATE_KEY_FILE_PATH, USER_DATA_FILE, USER_DATA_TYPE);
		this.suitableResource = mock(FogbowResource.class);
		doReturn("request_01").when(this.suitableResource).getId();
		when(this.suitableResource.match(Mockito.any(Specification.class))).thenCallRealMethod();
	}

	@After
	public void setDown() throws Exception {
		requirements.clear();
		requirements = null;
		spec.getAllRequirements().clear();
		spec = null;
		suitableResource.getAllMetadata().clear();
		suitableResource = null;
		properties.clear();
		properties = null;
	}

	// TODO: delete this test
	@Test
	public void validateFogbowRequirementsSyntaxSucessTest() {

		String fogbowRequirementA = "Glue2vCPU >= 1 && Glue2RAM >= 1024 ";
		String fogbowRequirementB = "Glue2vCPU >= 1 && Glue2RAM >= 1024 && Glue2disk >= 20 ";
		String fogbowRequirementC = "Glue2vCPU >= 1 && Glue2RAM >= 1024 && Glue2disk >= 20 && Glue2CloudComputeManagerID ==\"servers.your.domain\"";
		String fogbowRequirementD = "Glue2vCPU >= 1 && Glue2RAM == 1024 || Glue2RAM == 2048 && Glue2disk >= 20 && Glue2CloudComputeManagerID ==\"servers.your.domain\"";
		String fogbowRequirementE = "";

		assertTrue(FogbowRequirementsHelper.validateFogbowRequirementsSyntax(fogbowRequirementA));
		assertTrue(FogbowRequirementsHelper.validateFogbowRequirementsSyntax(fogbowRequirementB));
		assertTrue(FogbowRequirementsHelper.validateFogbowRequirementsSyntax(fogbowRequirementC));
		assertTrue(FogbowRequirementsHelper.validateFogbowRequirementsSyntax(fogbowRequirementD));
		assertTrue(FogbowRequirementsHelper.validateFogbowRequirementsSyntax(fogbowRequirementE));
		assertTrue(FogbowRequirementsHelper.validateFogbowRequirementsSyntax(null));
	}

	@Test
	public void validateFogbowRequirementsSyntaxFailTest() {

		String fogbowRequirementA = "X (r =x) 1 && w = y";
		assertFalse(FogbowRequirementsHelper.validateFogbowRequirementsSyntax(fogbowRequirementA));

	}

	@Test
	public void matchesResourceSuccessA(){
		
		String requestId = "FakeRequestID1";
		String host = "100.10.1.1";
		String port = "9898";
		String userName = "user";
		String extraPorts = "";
		String cpuSize = "1";
		String menSize = "1024";
		String diskSize = "30";
		String location = "servers.your.domain";
		
		Map<String, String> resourceMetadata = ResourceHelperTest.generateResourceMetadata(host, port, userName,
				extraPorts, spec.getImageName(), spec.getPublicKey(), cpuSize, menSize, diskSize,
				location);

		suitableResource = ResourceHelperTest.generateMockResource(requestId, resourceMetadata, true);
		
		assertTrue(FogbowRequirementsHelper.matches(suitableResource, Constants.FOGBOW_REQUIREMENT_A));
	}

	@Test
	public void matchesResourceSuccessB() {

		String fogbowRequirements = "Glue2vCPU >= 2 && Glue2RAM >= 2048 && Glue2disk >= 20 && Glue2CloudComputeManagerID ==\"servers.your.domain\"";

		String requestId = "FakeRequestID1";
		String host = "100.10.1.1";
		String port = "8008";
		String userName = "userName";
		String extraPorts = "7060,8070";
		String cpuSize = "2";
		String menSize = "8192";
		String diskSize = "30";
		String location = "\"servers.your.domain\"";

		Map<String, String> resourceMetadata = ResourceHelperTest.generateResourceMetadata(host, port, userName,
				extraPorts, spec.getImageName(), spec.getPublicKey(), cpuSize, menSize, diskSize,
				location);

		suitableResource = ResourceHelperTest.generateMockResource(requestId, resourceMetadata, true);

		assertTrue(FogbowRequirementsHelper.matches(suitableResource, fogbowRequirements));
	}

	@Test
	public void matchesResourceSuccessC(){
		
		String fogbowRequirements = " ";
		
		String requestId = "FakeRequestID1";
		String host = "100.10.1.1";
		String port = "9898";
		String userName = "user";
		String extraPorts = "";
		String cpuSize = "1";
		String menSize = "1024";
		String diskSize = "30";
		String location = "servers.your.domain";
		
		Map<String, String> resourceMetadata = ResourceHelperTest.generateResourceMetadata(host, port, userName,
				extraPorts, spec.getImageName(), spec.getPublicKey(), cpuSize, menSize, diskSize,
				location);

		suitableResource = ResourceHelperTest.generateMockResource(requestId, resourceMetadata, true);
		
		assertTrue(FogbowRequirementsHelper.matches(suitableResource, fogbowRequirements));
		
	}
	
	@Test
	public void matchesResourceSucessVCpuOrMenSize() {

		String fogbowRequirementsB = "Glue2vCPU >= 2 || Glue2RAM >= 2048";

		String requestId = "FakeRequestID1";
		String host = "100.10.1.1";
		String port = "8008";
		String userName = "userName";
		String extraPorts = "7060,8070";
		String cpuSize = "1";
		String menSize = "8192";
		String diskSize = "";
		String location = "";

		Map<String, String> resourceMetadata = ResourceHelperTest.generateResourceMetadata(host, port, userName,
				extraPorts, spec.getImageName(), spec.getPublicKey(), cpuSize, menSize, diskSize,
				location);
		suitableResource = ResourceHelperTest.generateMockResource(requestId, resourceMetadata, true);

		assertTrue(FogbowRequirementsHelper.matches(suitableResource, fogbowRequirementsB));
	}

	@Test
	public void matchesResourceVcpuFail() {

		String fogbowRequirementsB = "Glue2vCPU >= 2 && Glue2RAM >= 2048";

		String requestId = "FakeRequestID1";
		String host = "100.10.1.1";
		String port = "8008";
		String userName = "userName";
		String extraPorts = "7060,8070";
		String cpuSize = "1";
		String menSize = "8192";
		String diskSize = "";
		String location = "";
		
		Map<String, String> resourceMetadata = ResourceHelperTest.generateResourceMetadata(host, port, userName,
				extraPorts, spec.getImageName(), spec.getPublicKey(), cpuSize, menSize, diskSize,
				location);
		suitableResource = ResourceHelperTest.generateMockResource(requestId, resourceMetadata, true);

		assertFalse(FogbowRequirementsHelper.matches(suitableResource, fogbowRequirementsB));

	}

	@Test
	public void matchesResourceVcpuFailOutOfRange() {

		String fogbowRequirementsB = "Glue2vCPU >= 2 && Glue2vCPU <= 4 && Glue2RAM >= 2048";

		FogbowRequirementsHelper.validateFogbowRequirementsSyntax(fogbowRequirementsB);

		String requestId = "FakeRequestID1";
		String host = "100.10.1.1";
		String port = "8008";
		String userName = "userName";
		String extraPorts = "7060,8070";
		String cpuSize = "5";// Out of range
		String menSize = "8192";
		String diskSize = "";
		String location = "";
		
		Map<String, String> resourceMetadata = ResourceHelperTest.generateResourceMetadata(host, port, userName,
				extraPorts, spec.getImageName(), spec.getPublicKey(), cpuSize, menSize, diskSize,
				location);
		suitableResource = ResourceHelperTest.generateMockResource(requestId, resourceMetadata, true);

		assertFalse(FogbowRequirementsHelper.matches(suitableResource, fogbowRequirementsB));

	}

	@Test
	public void matchesResourceMenSizeFail() {

		String fogbowRequirementsB = "Glue2vCPU >= 2 && Glue2RAM >= 2048";

		String requestId = "FakeRequestID1";
		String host = "100.10.1.1";
		String port = "8008";
		String userName = "userName";
		String extraPorts = "7060,8070";
		String cpuSize = "2";
		// Test low boundary value.
		String menSize = "2047";
		String diskSize = "";
		String location = "";
		
		Map<String, String> resourceMetadata = ResourceHelperTest.generateResourceMetadata(host, port, userName,
				extraPorts, spec.getImageName(), spec.getPublicKey(), cpuSize, menSize, diskSize,
				location);
		suitableResource = ResourceHelperTest.generateMockResource(requestId, resourceMetadata, true);

		assertFalse(FogbowRequirementsHelper.matches(suitableResource, fogbowRequirementsB));

	}

	@Test
	public void matchesResourceMenSizeFailOutOfRange() {

		String fogbowRequirementsB = "Glue2vCPU >= 2 && Glue2RAM >= 2048 && Glue2RAM <= 8192";

		String requestId = "FakeRequestID1";
		String host = "100.10.1.1";
		String port = "8008";
		String userName = "userName";
		String extraPorts = "7060,8070";
		String cpuSize = "2";
		// Out of range - Test boundary value.
		String menSize = "8193";
		String diskSize = "";
		String location = "";
		
		Map<String, String> resourceMetadata = ResourceHelperTest.generateResourceMetadata(host, port, userName,
				extraPorts, spec.getImageName(), spec.getPublicKey(), cpuSize, menSize, diskSize,
				location);
		suitableResource = ResourceHelperTest.generateMockResource(requestId, resourceMetadata, true);

		assertFalse(FogbowRequirementsHelper.matches(suitableResource, fogbowRequirementsB));

	}
	
	@Test
	public void matchesResourceDiskSizeFail() {

		String fogbowRequirements = "Glue2vCPU >= 2 && Glue2RAM >= 2048 && Glue2disk >= 40 && Glue2CloudComputeManagerID ==\"servers.your.domain\"";

		String requestId = "FakeRequestID1";
		String host = "100.10.1.1";
		String port = "8008";
		String userName = "userName";
		String extraPorts = "7060,8070";
		String cpuSize = "2";
		String menSize = "8192";
		String diskSize = "39";
		String location = "servers.your.domain";

		Map<String, String> resourceMetadata = ResourceHelperTest.generateResourceMetadata(host, port, userName,
				extraPorts, spec.getImageName(), spec.getPublicKey(), cpuSize, menSize, diskSize,
				location);

		suitableResource = ResourceHelperTest.generateMockResource(requestId, resourceMetadata, true);

		assertFalse(FogbowRequirementsHelper.matches(suitableResource, fogbowRequirements));

	}
	
	@Test
	public void matchesResourceLocationFail() {

		String fogbowRequirements = "Glue2vCPU >= 2 && Glue2RAM >= 2048 && Glue2disk >= 40 && Glue2CloudComputeManagerID ==\"servers.your.domainB\"";

		String requestId = "FakeRequestID1";
		String host = "100.10.1.1";
		String port = "8008";
		String userName = "userName";
		String extraPorts = "7060,8070";
		String cpuSize = "2";
		String menSize = "8192";
		String diskSize = "40";
		String location = "servers.your.domainA";

		Map<String, String> resourceMetadata = ResourceHelperTest.generateResourceMetadata(host, port, userName,
				extraPorts,spec.getImageName(), spec.getPublicKey(), cpuSize, menSize, diskSize,
				location);

		suitableResource = ResourceHelperTest.generateMockResource(requestId, resourceMetadata, true);

		assertFalse(FogbowRequirementsHelper.matches(suitableResource, fogbowRequirements));

	}

	private void generateDefaultProperties() {

		properties = new Properties();

		properties.setProperty(AppPropertiesConstants.INFRA_IS_ELASTIC, "false");
		properties.setProperty(AppPropertiesConstants.INFRA_PROVIDER_PLUGIN,
				"org.fogbowcloud.scheduler.infrastructure.fogbow.FogbowInfrastructureProvider");
		properties.setProperty(AppPropertiesConstants.INFRA_RESOURCE_CONNECTION_TIMEOUT, "10000");
		properties.setProperty(AppPropertiesConstants.INFRA_RESOURCE_IDLE_LIFETIME, "300000");
		properties.setProperty(AppPropertiesConstants.RAS_BASE_URL, "100_02_01_01:8098");

	}

}
