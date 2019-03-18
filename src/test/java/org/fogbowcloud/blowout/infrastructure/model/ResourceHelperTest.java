package org.fogbowcloud.blowout.infrastructure.model;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.fogbowcloud.blowout.core.model.Specification;
import org.mockito.Mockito;

public class ResourceHelperTest {

	public static FogbowResource generateMockResource(String resourceId, Map<String, String> resourceMetadata, boolean connectivity){

		FogbowResource fakeResource = mock(FogbowResource.class);

		// Environment
		doReturn(connectivity).when(fakeResource).checkConnectivity();
		doReturn(resourceMetadata).when(fakeResource).getAllMetadata();
		doReturn(resourceId).when(fakeResource).getId();
		doReturn(resourceMetadata.get(FogbowResource.METADATA_REQUEST_TYPE)).when(fakeResource)
		.getMetadataValue(Mockito.eq(FogbowResource.METADATA_REQUEST_TYPE));
		doReturn(resourceMetadata.get(FogbowResource.METADATA_SSH_HOST)).when(fakeResource)
		.getMetadataValue(Mockito.eq(FogbowResource.METADATA_SSH_HOST));
		doReturn(resourceMetadata.get(FogbowResource.METADATA_SSH_USERNAME_ATT)).when(fakeResource)
		.getMetadataValue(Mockito.eq(FogbowResource.METADATA_SSH_USERNAME_ATT));
		doReturn(resourceMetadata.get(FogbowResource.METADATA_EXTRA_PORTS_ATT)).when(fakeResource)
		.getMetadataValue(Mockito.eq(FogbowResource.METADATA_EXTRA_PORTS_ATT));
		// Flavor
		doReturn(resourceMetadata.get(FogbowResource.METADATA_IMAGE_NAME)).when(fakeResource)
		.getMetadataValue(Mockito.eq(FogbowResource.METADATA_IMAGE_NAME));
		doReturn(resourceMetadata.get(FogbowResource.METADATA_PUBLIC_KEY)).when(fakeResource)
		.getMetadataValue(Mockito.eq(FogbowResource.METADATA_PUBLIC_KEY));
		doReturn(resourceMetadata.get(FogbowResource.METADATA_VCPU)).when(fakeResource)
		.getMetadataValue(Mockito.eq(FogbowResource.METADATA_VCPU));
		doReturn(resourceMetadata.get(FogbowResource.METADATA_MEM_SIZE)).when(fakeResource)
		.getMetadataValue(Mockito.eq(FogbowResource.METADATA_MEM_SIZE));
		doReturn(resourceMetadata.get(FogbowResource.METADATA_DISK_SIZE)).when(fakeResource)
		.getMetadataValue(Mockito.eq(FogbowResource.METADATA_DISK_SIZE));
		doReturn(resourceMetadata.get(FogbowResource.METADATA_LOCATION)).when(fakeResource)
		.getMetadataValue(Mockito.eq(FogbowResource.METADATA_LOCATION));

		when(fakeResource.match(Mockito.any(Specification.class))).thenCallRealMethod();
		
		return fakeResource;
	}
	
	public static Map<String, String> generateResourceMetadata(String host, String port, String userName,
			String extraPorts, String image, String publicKey, String cpuSize, String menSize,
			String diskSize, String location) {

		Map<String, String> resourceMetadata = new HashMap<String, String>();
		resourceMetadata.put(FogbowResource.METADATA_SSH_HOST, host);
		resourceMetadata.put(FogbowResource.METADATA_SSH_USERNAME_ATT, userName);
		resourceMetadata.put(FogbowResource.METADATA_EXTRA_PORTS_ATT, extraPorts);
		resourceMetadata.put(FogbowResource.METADATA_IMAGE_NAME, image);
		resourceMetadata.put(FogbowResource.METADATA_PUBLIC_KEY, publicKey);
		resourceMetadata.put(FogbowResource.METADATA_VCPU, cpuSize);
		resourceMetadata.put(FogbowResource.METADATA_MEM_SIZE, menSize);
		resourceMetadata.put(FogbowResource.METADATA_DISK_SIZE, diskSize);
		resourceMetadata.put(FogbowResource.METADATA_LOCATION, location);

		return resourceMetadata;
	}

}
