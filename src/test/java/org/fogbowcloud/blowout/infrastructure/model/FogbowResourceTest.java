package org.fogbowcloud.blowout.infrastructure.model;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.fogbowcloud.blowout.helpers.Constants.*;

import org.fogbowcloud.blowout.core.constants.BlowoutConstants;
import org.fogbowcloud.blowout.core.constants.FogbowConstants;
import org.fogbowcloud.blowout.core.model.Specification;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FogbowResourceTest {
	private FogbowResource fogbowResource;
	private Specification spec;
	
	@Before
	public void setUp() throws Exception {
		this.spec = new Specification(FAKE_CLOUD_NAME, FAKE_IMAGE_FLAVOR_NAME,
				FAKE_FOGBOW_USER_NAME, FAKE_PUBLIC_KEY,FAKE_PRIVATE_KEY_FILE_PATH);
		this.fogbowResource = spy(new FogbowResource(FAKE_RESOURCE_ID,FAKE_ORDER_ID, spec));
	}    

	@After
	public void setDown() throws Exception {
		this.fogbowResource.getAllMetadata().clear();
		this.fogbowResource = null;
	}
	
	@Test
	public void matchTest() {
		this.spec.setUserDataFile(USER_DATA_FILE);
		this.spec.setUserDataType(USER_DATA_TYPE);

		this.spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_A);

		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_IMAGE_NAME, FAKE_IMAGE_FLAVOR_NAME);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_PUBLIC_KEY, FAKE_PUBLIC_KEY);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_VCPU, EXAMPLE_CORE_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_MEM_SIZE, EXAMPLE_MEM_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_DISK_SIZE, EXAMPLE_DISK_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_LOCATION, EXAMPLE_LOCATION);

		assertTrue(fogbowResource.match(spec));
		this.spec.getAllRequirements().clear();
	}
	
	@Test
	public void matchTestRequirementNotMach() {
		this.spec.setUserDataFile(USER_DATA_FILE);
		this.spec.setUserDataType(USER_DATA_TYPE);

		this.spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_A);

		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_IMAGE_NAME, FAKE_IMAGE_FLAVOR_NAME);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_IMAGE_ID, FAKE_IMAGE_FLAVOR_ID);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_PUBLIC_KEY, FAKE_PUBLIC_KEY);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_VCPU, EXAMPLE_CORE_SIZE );
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_MEM_SIZE, EXAMPLE_MEM_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_DISK_SIZE, EXAMPLE_DISK_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_LOCATION, EXAMPLE_LOCATION);
		
		assertTrue(fogbowResource.match(spec));

		this.spec.getAllRequirements().clear();
		this.spec = null;
	}
	
	@Test
	public void matchTestImageNotMatch() {
		Specification specB = new Specification(FAKE_CLOUD_NAME+POSTFIX_B,
				FAKE_IMAGE_FLAVOR_NAME+POSTFIX_B, FAKE_FOGBOW_USER_NAME+POSTFIX_B,
				FAKE_PUBLIC_KEY+POSTFIX_B, FAKE_PRIVATE_KEY_FILE_PATH+POSTFIX_B,
				USER_DATA_FILE+POSTFIX_B, USER_DATA_TYPE+POSTFIX_B);

		specB.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_A);

		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_IMAGE_NAME, FAKE_IMAGE_FLAVOR_NAME);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_IMAGE_ID, FAKE_IMAGE_FLAVOR_ID);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_PUBLIC_KEY, FAKE_PUBLIC_KEY);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_VCPU, EXAMPLE_CORE_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_MEM_SIZE, EXAMPLE_MEM_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_DISK_SIZE, EXAMPLE_DISK_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_LOCATION, EXAMPLE_LOCATION);
		
		assertFalse(this.fogbowResource.match(specB));
	
		specB.getAllRequirements().clear();
	}
	
	@Test
	public void matchTestPublicKeyNotMatch() {
		this.spec.setUserDataType(USER_DATA_TYPE);
		this.spec.setUserDataFile(USER_DATA_FILE);

		this.spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, FOGBOW_REQUIREMENT_A);

		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_IMAGE_NAME, FAKE_IMAGE_FLAVOR_ID);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_PUBLIC_KEY, FAKE_PUBLIC_KEY);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_VCPU, EXAMPLE_CORE_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_MEM_SIZE, EXAMPLE_MEM_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_DISK_SIZE, EXAMPLE_DISK_SIZE);
		this.fogbowResource.putMetadata(BlowoutConstants.METADATA_LOCATION, EXAMPLE_LOCATION);

		assertFalse(this.fogbowResource.match(this.spec));

		this.spec.getAllRequirements().clear();
		this.spec = null;
	}
}
