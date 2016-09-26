package org.fogbowcloud.blowout.scheduler.infrastructure;

import org.fogbowcloud.blowout.scheduler.core.model.Resource;
import org.fogbowcloud.blowout.scheduler.core.model.Specification;
import org.fogbowcloud.blowout.scheduler.infrastructure.exceptions.RequestResourceException;
import org.fogbowcloud.manager.occi.model.Token;

public interface InfrastructureProvider {

	/**
	 * Creates new Request for resource and return the Request ID
	 * @param specification
	 * @return Request's ID
	 */
	public String requestResource(Specification specification) throws RequestResourceException;
	
	public Resource getResource(String requestID);
	
	public Resource getFogbowResource(String requestID);
	
	public String getResourceComputeId();
	
	public void deleteResource(String resourceId) throws Exception;

	public Token getToken();
}
