package org.fogbowcloud.blowout.infrastructure.model;

import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.infrastructure.manager.ResourceNotifier;

public class Request{

	public static enum RequestState{
		OPEN,ORDERED,FULFILLED
	}
	
	private ResourceNotifier resourceNotifier;
	private Specification specification;
	private RequestState state;
	private final String requestId;
	
	
	public Request(String requestId, ResourceNotifier resourceNotifier, Specification specification) {
		this.requestId = requestId;
		this.resourceNotifier = resourceNotifier;
		this.specification = specification;
		this.state = RequestState.OPEN;
	}
	
	public ResourceNotifier getResourceNotifier() {
		return resourceNotifier;
	}

	public Specification getSpecification() {
		return specification;
	}

	public RequestState getState() {
		return state;
	}

	public void setState(RequestState state) {
		this.state = state;
	}

	public String getRequestId() {
		return requestId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Request other = (Request) obj;
		if (requestId == null) {
			if (other.requestId != null)
				return false;
		} else if (!requestId.equals(other.requestId))
			return false;
		return true;
	}

	
	
}
