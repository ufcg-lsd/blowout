package org.fogbowcloud.blowout.infrastructure;

import org.fogbowcloud.blowout.scheduler.core.model.Resource;

public interface ResourceNotifier {
	
	public void resourceReady(final Resource resource);

}
