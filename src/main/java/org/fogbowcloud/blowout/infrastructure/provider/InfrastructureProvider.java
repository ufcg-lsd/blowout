package org.fogbowcloud.blowout.infrastructure.provider;

import java.util.List;

import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.infrastructure.exception.RequestResourceException;
import org.fogbowcloud.blowout.pool.AbstractResource;

public interface InfrastructureProvider {

	/**
	 * Creates new Request for resource and return the Request ID
	 * @param specification
	 * @return The requested resource
	 */
	String requestResource(Specification specification) throws RequestResourceException;
	
	List<AbstractResource> getAllResources();
	
	AbstractResource getResource(String resourceId);
	
	void deleteResource(String resourceId) throws Exception;
	
}
