package org.fogbowcloud.blowout.infrastructure.model;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.fogbowcloud.blowout.helpers.Constants.*;

import org.fogbowcloud.blowout.core.constants.FogbowConstants;
import org.fogbowcloud.blowout.core.model.Specification;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FogbowResourceTest {
	private FogbowResource resource;
	private Specification spec;
	
	@Before
	public void setUp() throws Exception {
		this.spec = new Specification(FAKE_CLOUD_NAME, FAKE_IMAGE_FLAVOR_NAME,
				FAKE_FOGBOW_USER_NAME, FAKE_PUBLIC_KEY,FAKE_PRIVATE_KEY_FILE_PATH);
		this.resource = spy(new FogbowResource(FAKE_RESOURCE_ID,FAKE_ORDER_ID, spec));
	}    

	@After
	public void setDown() throws Exception {
		this.resource.getAllMetadata().clear();
		this.resource = null;
	}
	
	@Test
	public void matchTest() {
		this.spec.setUserDataFile(USER_DATA_FILE);
		this.spec.setUserDataType(USER_DATA_TYPE);

		this.spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, EXAMPLE_FOGBOW_REQUIREMENT);

		this.resource.putMetadata(FogbowResource.METADATA_IMAGE, FAKE_IMAGE_FLAVOR_ID);
		this.resource.putMetadata(FogbowResource.METADATA_PUBLIC_KEY, FAKE_PUBLIC_KEY);
		this.resource.putMetadata(FogbowResource.METADATA_VCPU, EXAMPLE_CORE_SIZE);
		this.resource.putMetadata(FogbowResource.METADATA_MEM_SIZE, EXAMPLE_MEM_SIZE);
		this.resource.putMetadata(FogbowResource.METADATA_DISK_SIZE, EXAMPLE_DISK_SIZE);
		this.resource.putMetadata(FogbowResource.METADATA_LOCATION, EXAMPLE_LOCATION);

		assertTrue(resource.match(spec));
		this.spec.getAllRequirements().clear();
	}
	
	@Test
	public void matchTestRequirementNotMach() {
		this.spec.setUserDataFile(USER_DATA_FILE);
		this.spec.setUserDataType(USER_DATA_TYPE);

		this.spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, EXAMPLE_FOGBOW_REQUIREMENT);

		this.resource.putMetadata(FogbowResource.METADATA_IMAGE, FAKE_IMAGE_FLAVOR_ID);
		this.resource.putMetadata(FogbowResource.METADATA_PUBLIC_KEY, FAKE_PUBLIC_KEY);
		this.resource.putMetadata(FogbowResource.METADATA_VCPU, EXAMPLE_CORE_SIZE );
		this.resource.putMetadata(FogbowResource.METADATA_MEM_SIZE, EXAMPLE_MEM_SIZE);
		this.resource.putMetadata(FogbowResource.METADATA_DISK_SIZE, EXAMPLE_DISK_SIZE);
		this.resource.putMetadata(FogbowResource.METADATA_LOCATION, EXAMPLE_LOCATION);
		
		assertTrue(resource.match(spec));

		this.spec.getAllRequirements().clear();
		this.spec = null;
	}
	
	@Test
	public void matchTestImageNotMatch() {
		Specification specB = new Specification(FAKE_CLOUD_NAME+POSTFIX_B,
				FAKE_IMAGE_FLAVOR_NAME+POSTFIX_B, FAKE_FOGBOW_USER_NAME+POSTFIX_B,
				FAKE_PUBLIC_KEY+POSTFIX_B, FAKE_PRIVATE_KEY_FILE_PATH+POSTFIX_B,
				USER_DATA_FILE+POSTFIX_B, USER_DATA_TYPE+POSTFIX_B);

		specB.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, EXAMPLE_FOGBOW_REQUIREMENT);
		
		resource.putMetadata(FogbowResource.METADATA_IMAGE, FAKE_IMAGE_FLAVOR_ID);
		resource.putMetadata(FogbowResource.METADATA_PUBLIC_KEY, FAKE_PUBLIC_KEY);
		resource.putMetadata(FogbowResource.METADATA_VCPU, EXAMPLE_CORE_SIZE);
		resource.putMetadata(FogbowResource.METADATA_MEM_SIZE, EXAMPLE_MEM_SIZE);
		resource.putMetadata(FogbowResource.METADATA_DISK_SIZE, EXAMPLE_DISK_SIZE);
		resource.putMetadata(FogbowResource.METADATA_LOCATION, EXAMPLE_LOCATION);
		
		assertFalse(resource.match(specB));
	
		specB.getAllRequirements().clear();
	}
	
	@Test
	public void matchTestPublicKeyNotMatch() {
		this.spec.setUserDataType(USER_DATA_TYPE);
		this.spec.setUserDataFile(USER_DATA_FILE);

		spec.addRequirement(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS, EXAMPLE_FOGBOW_REQUIREMENT);

		resource.putMetadata(FogbowResource.METADATA_IMAGE, FAKE_IMAGE_FLAVOR_ID);
		resource.putMetadata(FogbowResource.METADATA_PUBLIC_KEY, FAKE_PUBLIC_KEY);
		resource.putMetadata(FogbowResource.METADATA_VCPU, EXAMPLE_CORE_SIZE);
		resource.putMetadata(FogbowResource.METADATA_MEM_SIZE, EXAMPLE_MEM_SIZE);
		resource.putMetadata(FogbowResource.METADATA_DISK_SIZE, EXAMPLE_DISK_SIZE);
		resource.putMetadata(FogbowResource.METADATA_LOCATION, EXAMPLE_LOCATION);

		assertFalse(resource.match(spec));

		spec.getAllRequirements().clear();
		spec = null;
	}
}
