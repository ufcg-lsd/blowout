package org.fogbowcloud.blowout.infrastructure.manager;

import org.fogbowcloud.blowout.pool.AbstractResource;

public interface ResourceNotifier {
	
	void resourceReady(final AbstractResource resource);
	
	void resourceDeleted(final AbstractResource resource);

}
