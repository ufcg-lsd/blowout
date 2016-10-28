package org.fogbowcloud.blowout.infrastructure.model;

import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.infrastructure.manager.ResourceNotifier;

public class Requisition{

	public static enum RequisitionState{
		OPEN,ORDERED,FULFILLED
	}
	
	private ResourceNotifier resourceNotifier;
	private Specification specification;
	private RequisitionState state;
	private String requestId;
	
	
	public Requisition(ResourceNotifier resourceNotifier, Specification specification) {
		this.resourceNotifier = resourceNotifier;
		this.specification = specification;
		this.state = RequisitionState.OPEN;
	}
	
	public ResourceNotifier getResourceNotifier() {
		return resourceNotifier;
	}

	public Specification getSpecification() {
		return specification;
	}

	public RequisitionState getState() {
		return state;
	}

	public void setState(RequisitionState state) {
		this.state = state;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Requisition other = (Requisition) obj;
		if (requestId == null) {
			if (other.requestId != null)
				return false;
		} else if (!requestId.equals(other.requestId))
			return false;
		return true;
	}

	
	
}
