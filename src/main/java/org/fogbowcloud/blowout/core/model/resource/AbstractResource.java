package org.fogbowcloud.blowout.core.model.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.fogbowcloud.blowout.core.model.Specification;

public abstract class AbstractResource {
	private final String id;
	private final Map<String, Object> metadata;
	private int timesReused = 0;
	private int connectionFailTries = 0;
	private Specification requestedSpec;
	private ResourceState state;
	
	public AbstractResource(String id, Specification requestedSpec) {
		this.metadata = new HashMap<>();
		this.id = id;
		this.requestedSpec = requestedSpec;
		this.state = ResourceState.NOT_READY;
	}

	public abstract boolean match(Specification spec);

	protected abstract boolean internalCheckConnectivity();
	
	public boolean checkConnectivity(){
		
		boolean success = this.internalCheckConnectivity();
		connectionFailTries = success ? 0 : connectionFailTries+1;
		return success;
	}

	public void putMetadata(String attributeName, Object value) {
		metadata.put(attributeName, value);
	}

	public void putAllMetadata(Map<String, String> instanceAttributes) {
		for (Entry<String, String> entry : instanceAttributes.entrySet()) {
			this.putMetadata(entry.getKey(), entry.getValue());
		}
	}

	public String getMetadataValue(String attributeName) {
		return String.valueOf(metadata.get(attributeName));
	}

	public Map<String, Object> getAllMetadata() {
		return metadata;
	}

	public void copyInformation(AbstractResource resource) {
		this.metadata.clear();
		this.metadata.putAll(resource.getAllMetadata());
	}

	public String getId() {
		return id;
	}

	public void incrementReuse() {
		timesReused++;
	}

	public int getReusedTimes() {
		return this.timesReused;
	}
	
	public int getConnectionFailTries() {
		return this.connectionFailTries;
	}

	public ResourceState getState() {
		return state;
	}

	public synchronized void setState(ResourceState state) {
		this.state = state;
	}

	public Specification getRequestedSpec() {
		return requestedSpec;
	}

	public void setRequestedSpec(Specification requestedSpec) {
		this.requestedSpec = requestedSpec;
	}
	
}
