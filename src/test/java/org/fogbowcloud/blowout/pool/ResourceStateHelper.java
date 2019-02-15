package org.fogbowcloud.blowout.pool;

import org.fogbowcloud.blowout.core.model.resource.AbstractResource;
import org.fogbowcloud.blowout.core.model.resource.ResourceState;

public class ResourceStateHelper {
	
	public static AbstractResource changeResourceToState(AbstractResource resource, ResourceState state){
		resource.setState(state);
		return resource;
	}

}
