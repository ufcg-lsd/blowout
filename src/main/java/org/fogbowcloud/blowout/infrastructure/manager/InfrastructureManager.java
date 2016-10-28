package org.fogbowcloud.blowout.infrastructure.manager;

import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.infrastructure.model.AbstractResource;

public interface InfrastructureManager {
	
	
	/**
	 * This method is responsible for initialize the Manager of the infrastructure. If the executions has any initial resource, these ones will
	 * be created in this method. The parameter "blockWhileInitializing" is used to inform if the initialization must wait for all initial resources
	 * be started to continue with the execution of the Blowout. The "removePrevious" parameter indicates if resources from previuos execution must
	 * be removed.
	 * @param blockWhileInitializing
	 * @param removePrevious
	 * @throws Exception
	 */
	public void start(boolean blockWhileInitializing, boolean removePrevious) throws Exception ;

	public void stop(boolean deleteResource) throws Exception;

	public void request(Specification specification, ResourceNotifier resourceNotifier, int resourceNumber) ;

	public void release(AbstractResource resource);

	public void releaseAll();
}
