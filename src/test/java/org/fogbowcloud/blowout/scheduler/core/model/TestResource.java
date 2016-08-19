package org.fogbowcloud.blowout.scheduler.core.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.util.Properties;

import org.fogbowcloud.blowout.scheduler.infrastructure.fogbow.FogbowRequirementsHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestResource {
	
	Resource resource;
	
	private Properties properties;
	
	@Before
	public void setUp() throws Exception {
		
		
		
		resource = spy(new Resource("resource_01",properties));
	}    

	@After
	public void setDown() throws Exception {
		
		resource.getAllMetadata().clear();
		resource = null;
		properties = null;
		
	}
	
	@Test
	public void matchTest() {
		
		String image = "image";
		String userName = "userName";
		String publicKey = "publicKey";
		String privateKey = "privateKey";
		String fogbowRequirement = "Glue2vCPU >= 1 && Glue2RAM >= 1024 ";
		String userDataFile = "scripts/lvl-user-data.sh";
		String userDataType = "text/x-shellscript";
		
		String coreSize = "1";
		String menSize = "1024";
		String diskSize = "20";
		String location = "edu.ufcg.lsd.cloud_1s";
		
		Specification spec = new Specification(image, userName, publicKey, privateKey, userDataFile, userDataType);
		spec.addRequirement(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS, fogbowRequirement);
		
		resource.putMetadata(Resource.METADATA_IMAGE, image);
		resource.putMetadata(Resource.METADATA_PUBLIC_KEY, publicKey);
		resource.putMetadata(Resource.METADATA_VCPU, coreSize);
		resource.putMetadata(Resource.METADATA_MEN_SIZE, menSize);
		resource.putMetadata(Resource.METADATA_DISK_SIZE, diskSize);
		resource.putMetadata(Resource.METADATA_LOCATION, location);
		
		assertTrue(resource.match(spec));
		
		spec.getAllRequirements().clear();
		spec = null;
		
	}
	
	@Test
	public void matchTestRequirementNotMach() {
		
		String image = "image";
		String userName = "userName";
		String publicKey = "publicKey";
		String privateKey = "privateKey";
		String fogbowRequirement = "Glue2vCPU > 1 || Glue2RAM = 1024 ";
		String userDataFile = "scripts/lvl-user-data.sh";
		String userDataType = "text/x-shellscript";
		
		String coreSize = "1";
		String menSize = "2048";
		String diskSize = "20";
		String location = "edu.ufcg.lsd.cloud_1s";
		
		Specification spec = new Specification(image, userName, publicKey, privateKey, userDataFile, userDataType);
		spec.addRequirement(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS, fogbowRequirement);
		
		resource.putMetadata(Resource.METADATA_IMAGE, image);
		resource.putMetadata(Resource.METADATA_PUBLIC_KEY, publicKey);
		resource.putMetadata(Resource.METADATA_VCPU, coreSize);
		resource.putMetadata(Resource.METADATA_MEN_SIZE, menSize);
		resource.putMetadata(Resource.METADATA_DISK_SIZE, diskSize);
		resource.putMetadata(Resource.METADATA_LOCATION, location);
		
		assertFalse(resource.match(spec));
		
		spec.getAllRequirements().clear();
		spec = null;
	}
	
	@Test
	public void matchTestImageNotMatch() {
		
		String imageA = "imageA";
		String imageB = "imageB";
		String userName = "userName";
		String publicKey = "publicKey";
		String privateKey = "privateKey";
		String fogbowRequirement = "Glue2vCPU >= 1 && Glue2RAM >= 1024 ";
		String userDataFile = "scripts/lvl-user-data.sh";
		String userDataType = "text/x-shellscript";
		
		String coreSize = "1";
		String menSize = "1024";
		String diskSize = "20";
		String location = "edu.ufcg.lsd.cloud_1s";
		
		Specification spec = new Specification(imageB, userName, publicKey, privateKey, userDataFile, userDataType);
		spec.addRequirement(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS, fogbowRequirement);
		
		resource.putMetadata(Resource.METADATA_IMAGE, imageA);
		resource.putMetadata(Resource.METADATA_PUBLIC_KEY, publicKey);
		resource.putMetadata(Resource.METADATA_VCPU, coreSize);
		resource.putMetadata(Resource.METADATA_MEN_SIZE, menSize);
		resource.putMetadata(Resource.METADATA_DISK_SIZE, diskSize);
		resource.putMetadata(Resource.METADATA_LOCATION, location);
		
		assertFalse(resource.match(spec));
	
		spec.getAllRequirements().clear();
		spec = null;
	}
	
	@Test
	public void matchTestPublicKeyNotMatch() {
		
		String image = "image";
		String userName = "userName";
		String publicKeyA = "publicKeyA";
		String publicKeyB = "publicKeyB";
		String privateKey = "privateKey";
		String fogbowRequirement = "Glue2vCPU >= 1 && Glue2RAM >= 1024 ";
		String userDataFile = "scripts/lvl-user-data.sh";
		String userDataType = "text/x-shellscript";

		String coreSize = "1";
		String menSize = "1024";
		String diskSize = "20";
		String location = "edu.ufcg.lsd.cloud_1s";

		Specification spec = new Specification(image, userName, publicKeyB, privateKey, userDataFile, userDataType);
		spec.addRequirement(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS, fogbowRequirement);

		resource.putMetadata(Resource.METADATA_IMAGE, image);
		resource.putMetadata(Resource.METADATA_PUBLIC_KEY, publicKeyA);
		resource.putMetadata(Resource.METADATA_VCPU, coreSize);
		resource.putMetadata(Resource.METADATA_MEN_SIZE, menSize);
		resource.putMetadata(Resource.METADATA_DISK_SIZE, diskSize);
		resource.putMetadata(Resource.METADATA_LOCATION, location);

		assertFalse(resource.match(spec));

		spec.getAllRequirements().clear();
		spec = null;
	}

	

}
