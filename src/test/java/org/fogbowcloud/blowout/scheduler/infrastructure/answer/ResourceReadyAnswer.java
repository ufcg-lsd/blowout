package org.fogbowcloud.blowout.scheduler.infrastructure.answer;

import org.fogbowcloud.blowout.core.core.model.Resource;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * This class simulate the Scheduler Resources Hold's structure
 * @author gustavorag
 *
 */
public class ResourceReadyAnswer implements Answer<FogbowResource>{

	private FogbowResource resourceReady = null;
	
	@Override
	public FogbowResource answer(InvocationOnMock invocation) throws Throwable {
		
		resourceReady = (FogbowResource) invocation.getArguments()[0];
		
		return null;
	}

	public FogbowResource getResourceReady() {
		return resourceReady;
	}
	
}
