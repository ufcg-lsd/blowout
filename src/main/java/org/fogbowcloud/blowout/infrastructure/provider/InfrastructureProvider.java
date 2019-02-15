package org.fogbowcloud.blowout.infrastructure.provider;

import java.util.List;

import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.infrastructure.exception.RequestResourceException;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;

public interface InfrastructureProvider {

	String requestResource(Specification specification) throws RequestResourceException;
	
	List<AbstractResource> getAllResources();
	
	AbstractResource getResource(String resourceId);
	
	void deleteResource(String resourceId) throws Exception;
	
}
