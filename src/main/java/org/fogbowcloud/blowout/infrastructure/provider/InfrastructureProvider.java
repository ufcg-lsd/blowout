package org.fogbowcloud.blowout.infrastructure.provider;

import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.infrastructure.exception.RequestResourceException;
import org.fogbowcloud.blowout.infrastructure.model.AbstractResource;

public interface InfrastructureProvider {

	/**
	 * Creates new Request for resource and return the Request ID
	 * @param specification
	 * @return Request's ID
	 */
	String requestResource(Specification specification) throws RequestResourceException;
	
	AbstractResource getResource(String requestID);
	
	//TODO is used?
	String getResourceComputeId();
	
	void deleteResource(String resourceId) throws Exception;
}