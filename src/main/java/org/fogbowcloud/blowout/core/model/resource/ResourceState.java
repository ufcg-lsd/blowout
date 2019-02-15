package org.fogbowcloud.blowout.core.model.resource;

public enum ResourceState {
	
	NOT_READY("Not Ready"), IDLE("Idle"), BUSY("Busy"), FAILED("Failed"), TO_REMOVE("To Remove"); 

	private String desc;
	
	ResourceState(String desc){
		this.desc = desc;
	}
	
	public String getDesc(){
		return desc;
	}
	
	public ResourceState getResourceStateByDesc(String desc){
		for(ResourceState state : ResourceState.values()){
			if(state.getDesc().equals(desc)){
				return state;
			}
		}
		
		return null;
	}
}
