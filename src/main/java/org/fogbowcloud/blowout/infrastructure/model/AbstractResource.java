package org.fogbowcloud.blowout.infrastructure.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.fogbowcloud.blowout.core.model.Specification;

public abstract class AbstractResource {

	public static enum ResourceStatus{
		READY, NOT_READY
	}
	
	public static final String ENV_HOST = "HOST";
	public static final String ENV_SSH_PORT = "SSH_PORT";
	public static final String ENV_SSH_USER = "SSH_USER";
	public static final String ENV_PRIVATE_KEY_FILE = "PRIVATE_KEY_FILE";
	
	public static final String METADATA_TOKEN_USER = "tokenUser";
	public static final String METADATA_LOCAL_COMMAND_INTERPRETER = "metadataLocalCommandInterpreter";
	
	public static final String METADATA_SSH_HOST = "metadataSSHHost";
	public static final String METADATA_SSH_PORT = "metadataSSHPort";
	public static final String METADATA_SSH_USERNAME_ATT = "metadateSshUsername";
	public static final String METADATA_EXTRA_PORTS_ATT = "metadateExtraPorts";

	public static final String METADATA_IMAGE = "metadataImage";
	public static final String METADATA_PUBLIC_KEY = "metadataPublicKey";

	public static final String METADATA_VCPU = "metadataVcpu";
	public static final String METADATA_MEN_SIZE = "metadataMenSize";
	public static final String METADATA_DISK_SIZE = "metadataDiskSize";
	public static final String METADATA_LOCATION = "metadataLocation";

	public static final String METADATA_REQUEST_TYPE = "metadataRequestType";
	
	private ResourceStatus state = ResourceStatus.NOT_READY;

	private String id;
	private Map<String, String> metadata = new HashMap<String, String>();
	private int timesReused = 0;
	private int connectionFailTries = 0;
	private String localCommandInterpreter;
	
	public AbstractResource(String id, Properties properties) {
		this.id = id;
	}

	/**
	 * This method receives a wanted specification and verifies if this resource
	 * matches with it. <br>
	 * Is used to match the Fogbow requirements (VM.Cores >= Specs.Cores,
	 * VM.MenSize >= Specs.MenSize, VM.DiskSize >= Specs.DiskSize, VM.Location
	 * >= Specs.Location) and the Image (VM.image == Specs.image)
	 * 
	 * @param spec
	 * @return
	 */
	public abstract boolean match(Specification spec);

	protected abstract boolean internalCheckConnectivity();
	
	public boolean checkConnectivity(){
		
		boolean success = this.internalCheckConnectivity();
		connectionFailTries = success ? 0 : connectionFailTries+1;
		return success;
		
	}

	public void putMetadata(String attributeName, String value) {
		metadata.put(attributeName, value);
	}

	public void putAllMetadatas(Map<String, String> instanceAttributes) {
		for (Entry<String, String> entry : instanceAttributes.entrySet()) {
			this.putMetadata(entry.getKey(), entry.getValue());
		}
	}

	public String getMetadataValue(String attributeName) {
		return metadata.get(attributeName);
	}

	public Map<String, String> getAllMetadata() {
		return metadata;
	}

	public void copyInformations(AbstractResource resource) {
		this.metadata.clear();
		this.metadata.putAll(resource.getAllMetadata());
	}

	// ----------------------------------- GETTERS and SETTERS
	// -----------------------------------//
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

	public ResourceStatus getState() {
		return state;
	}

	public void setState(ResourceStatus state) {
		this.state = state;
	}
	
	public String getLocalCommandInterpreter() {
		return localCommandInterpreter;
	}

	public void setLocalCommandInterpreter(String localCommandInterpreter) {
		this.localCommandInterpreter = localCommandInterpreter;
	}
	
}
