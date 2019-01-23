package org.fogbowcloud.blowout.infrastructure.model;

import java.util.Scanner;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.infrastructure.provider.fogbow.FogbowRequirementsHelper;
import org.fogbowcloud.blowout.pool.AbstractResource;

public class FogbowResource extends AbstractResource {

	public final Logger LOGGER = Logger.getLogger(FogbowResource.class);

	private final String computeOrderId;
	private String instanceId;

	public FogbowResource(String id, String computeOrderId, Specification spec) {
		super(id, spec);
		this.computeOrderId = computeOrderId;
	}

	public boolean match(Specification spec) {
		String fogbowRequirement = spec.getRequirementValue(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS);
		String image = spec.getImageId();
		String publicKey = spec.getPublicKey();
		if (fogbowRequirement != null && image != null) {

			if (!FogbowRequirementsHelper.matches(this, fogbowRequirement)) {
				return false;
			}
			if (!image.equalsIgnoreCase(this.getMetadataValue(METADATA_IMAGE))) {
				return false;
			}
            return publicKey.equalsIgnoreCase(this.getMetadataValue(METADATA_PUBLIC_KEY));
		} else {
			return false;
		}
    }

	protected boolean internalCheckConnectivity() {
		String host = this.getMetadataValue(METADATA_SSH_HOST);
		String port = this.getMetadataValue(METADATA_SSH_PORT);

		LOGGER.debug("Checking resource connectivity [host: " + host + ", port: " + port + ".");

		Runtime run = null;
		Process p = null;
		Scanner scanner = null;

		try {
			run = Runtime.getRuntime();
			p = run.exec(new String[] { "/bin/bash", "-c", "echo quit | telnet " + host + " " + port + " 2>/dev/null | grep Connected" });
			p.waitFor();

			LOGGER.debug("Running command: /bin/bash -c echo quit | telnet " + host + " " + port + " 2>/dev/null | grep Connected");

			scanner = new Scanner(p.getInputStream());
			if (scanner.hasNext()) {
				String result = scanner.nextLine();

				LOGGER.debug("Command result: " + result);

				if (result != null && !result.isEmpty()) {

					LOGGER.debug("Resource is alive!");
					return true;
				}
			}
		} catch (Exception e) {
			LOGGER.debug("Failed to connect with resource.");
			e.printStackTrace();
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
		LOGGER.debug("Failed to connect with resource.");
		return false;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getComputeOrderId() {
		return computeOrderId;
	}
	
}
