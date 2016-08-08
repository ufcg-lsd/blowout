package org.fogbowcloud.blowout.scheduler.core.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.scheduler.core.TaskExecutionResult;
import org.fogbowcloud.blowout.scheduler.core.util.DateUtils;
import org.fogbowcloud.blowout.scheduler.infrastructure.fogbow.FogbowRequirementsHelper;

//TODO this class should be abstract???
public class Resource {

	// Environment variables to be replaced at prologue and epilogue scripts
	// TODO how we should treat them?
	public static final String ENV_HOST = "HOST";
	public static final String ENV_SSH_PORT = "SSH_PORT";
	public static final String ENV_SSH_USER = "SSH_USER";
	public static final String ENV_PRIVATE_KEY_FILE = "PRIVATE_KEY_FILE";

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

	private String id;
	private Map<String, String> metadata = new HashMap<String, String>();
	private int timesReused = 0;

	// private SshClientWrapper sshClientWrapper;

	private static final Logger LOGGER = Logger.getLogger(Resource.class);

	public Resource(String id, Properties properties) {
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
	public boolean match(Specification spec) {

		// TODO
		// Do we need to try connect to resource using spec username and
		// privateKey?
		String fogbowRequirement = spec.getRequirementValue(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS);
		String image = spec.getImage();
		String publicKey = spec.getPublicKey();
		if (fogbowRequirement != null && image != null) {

			if (!FogbowRequirementsHelper.matches(this, fogbowRequirement)) {
				return false;
			}
			if (!image.equalsIgnoreCase(this.getMetadataValue(METADATA_IMAGE))) {
				return false;
			}
			if (!publicKey.equalsIgnoreCase(this.getMetadataValue(METADATA_PUBLIC_KEY))) {
				return false;
			}
		} else {
			return false;
		}

		return true;
	}

	public boolean checkConnectivity() {

		String host = this.getMetadataValue(METADATA_SSH_HOST);
		String port = this.getMetadataValue(METADATA_SSH_PORT);

		Runtime run = null;
		Process p = null;
		Scanner scanner = null;

		try {
			run = Runtime.getRuntime();
			p = run.exec(new String[] { "/bin/bash", "-c",
					"echo quit | telnet " + host + " " + port + " 2>/dev/null | grep Connected" });
			p.waitFor();
			scanner = new Scanner(p.getInputStream());
			if (scanner.hasNext()) {
				String result = scanner.nextLine();
				if (result != null && !result.isEmpty()) {
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		} finally {
			run = null;
			if (p != null) {
				p.destroy();
			}
			if (scanner != null) {
				scanner.close();
			}
		}
		return false;
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

	public void copyInformations(Resource resource) {
		this.metadata.clear();
		this.metadata.putAll(resource.getAllMetadata());
	}

	// ----------------------------------- GETTERS and SETTERS
	// -----------------------------------//
	public String getId() {
		return id;
	}

	public void incReuse() {
		timesReused++;
	}

	public int getReusedTimes() {
		return this.timesReused;
	}

}
