package org.fogbowcloud.blowout.infrastructure.model;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.fogbowcloud.blowout.helpers.Constants.*;

import org.fogbowcloud.blowout.core.constants.BlowoutConstants;
import org.fogbowcloud.blowout.core.constants.FogbowConstants;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.helpers.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FogbowResourceTest {
	private FogbowResource fogbowResource;
	private Specification spec;
	
	@Before
	public void setUp() throws Exception {
		this.spec = new Specification(Constants.FakeData.CLOUD_NAME, Constants.FakeData.COMPUTE_IMAGE_FLAVOR_NAME,
				Constants.FakeData.FOGBOW_USER_NAME, Constants.FakeData.PUBLIC_KEY,Constants.FakeData.PRIVATE_KEY_FILE_PATH);
		this.fogbowResource = spy(new FogbowResource(Constants.FakeData.RESOURCE_ID,Constants.FakeData.ORDER_ID, spec));
	}    

	@After
	public void setDown() throws Exception {
		this.fogbowResource.getAllMetadata().clear();
		this.fogbowResource = null;
	}
	
	@Test
	public void matchTest() {
		this.spec.setUserDataFile(Constants.FakeData.USER_DATA_FILE);
		this.spec.setUserDataType(Constants.FakeData.USER_DATA_TYPE);

		this.spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_A);

		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_IMAGE_NAME, Constants.FakeData.COMPUTE_IMAGE_FLAVOR_NAME);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_PUBLIC_KEY, Constants.FakeData.PUBLIC_KEY);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_VCPU, Constants.FakeData.CORE_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_MEM_SIZE, Constants.FakeData.MEM_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_DISK_SIZE, Constants.FakeData.DISK_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_LOCATION, Constants.FakeData.LOCATION);

		assertTrue(fogbowResource.match(spec));
		this.spec.getAllRequirements().clear();
	}
	
	@Test
	public void matchTestRequirementNotMach() {
		this.spec.setUserDataFile(Constants.FakeData.USER_DATA_FILE);
		this.spec.setUserDataType(Constants.FakeData.USER_DATA_TYPE);

		this.spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_A);

		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_IMAGE_NAME, Constants.FakeData.COMPUTE_IMAGE_FLAVOR_NAME);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_IMAGE_ID, Constants.FakeData.COMPUTE_IMAGE_ID);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_PUBLIC_KEY, Constants.FakeData.PUBLIC_KEY);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_VCPU, Constants.FakeData.CORE_SIZE );
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_MEM_SIZE, Constants.FakeData.MEM_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_DISK_SIZE, Constants.FakeData.DISK_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_LOCATION, Constants.FakeData.LOCATION);
		
		assertTrue(fogbowResource.match(spec));

		this.spec.getAllRequirements().clear();
		this.spec = null;
	}
	
	@Test
	public void matchTestImageNotMatch() {
		Specification specB = new Specification(Constants.FakeData.CLOUD_NAME+POSTFIX_B,
				Constants.FakeData.COMPUTE_IMAGE_FLAVOR_NAME +POSTFIX_B, Constants.FakeData.FOGBOW_USER_NAME+POSTFIX_B,
				Constants.FakeData.PUBLIC_KEY+POSTFIX_B, Constants.FakeData.PRIVATE_KEY_FILE_PATH+POSTFIX_B,
				Constants.FakeData.USER_DATA_FILE+POSTFIX_B, Constants.FakeData.USER_DATA_TYPE+POSTFIX_B);

		specB.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_A);

		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_IMAGE_NAME, Constants.FakeData.COMPUTE_IMAGE_FLAVOR_NAME);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_IMAGE_ID, Constants.FakeData.COMPUTE_IMAGE_ID);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_PUBLIC_KEY, Constants.FakeData.PUBLIC_KEY);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_VCPU, Constants.FakeData.CORE_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_MEM_SIZE, Constants.FakeData.MEM_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_DISK_SIZE, Constants.FakeData.DISK_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_LOCATION, Constants.FakeData.LOCATION);
		
		assertFalse(this.fogbowResource.match(specB));
	
		specB.getAllRequirements().clear();
	}
	
	@Test
	public void matchTestPublicKeyNotMatch() {
		this.spec.setUserDataType(Constants.FakeData.USER_DATA_TYPE);
		this.spec.setUserDataFile(Constants.FakeData.USER_DATA_FILE);

		this.spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_A);

		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_IMAGE_NAME,Constants.FakeData.COMPUTE_IMAGE_ID);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_PUBLIC_KEY, Constants.FakeData.PUBLIC_KEY);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_VCPU, Constants.FakeData.CORE_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_MEM_SIZE, Constants.FakeData.MEM_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_DISK_SIZE, Constants.FakeData.DISK_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_LOCATION, Constants.FakeData.LOCATION);

		assertFalse(this.fogbowResource.match(this.spec));

		this.spec.getAllRequirements().clear();
		this.spec = null;
	}
}
