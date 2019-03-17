package org.fogbowcloud.blowout.infrastructure.model;

import java.util.Scanner;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.constants.AppMessagesConstants;
import org.fogbowcloud.blowout.core.constants.FogbowConstants;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;
import org.fogbowcloud.blowout.infrastructure.provider.fogbow.FogbowRequirementsHelper;

public class FogbowResource extends AbstractResource {

	public final Logger LOGGER = Logger.getLogger(FogbowResource.class);

	private final String computeOrderId;
	private String instanceId;
	private String publicIpOrderId;

	public FogbowResource(String id, String computeOrderId, Specification spec) {
		super(id, spec);
		this.computeOrderId = computeOrderId;
	}

	public FogbowResource(String id, String computeOrderId, Specification spec, String publicIpOrderId) {
		this(id, computeOrderId, spec);
		this.publicIpOrderId = publicIpOrderId;
	}

	public boolean match(Specification spec) {
		String fogbowRequirement = spec.getRequirementValue(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS);
		String imageName = spec.getImageName();
		String publicKey = spec.getPublicKey();
		if (fogbowRequirement != null && imageName != null) {

			if (!FogbowRequirementsHelper.matches(this, fogbowRequirement)) {
				return false;
			}
			if (!imageName.equalsIgnoreCase(this.getMetadataValue(METADATA_IMAGE_NAME))) {
				return false;
			}
            return publicKey.equalsIgnoreCase(this.getMetadataValue(METADATA_PUBLIC_KEY));
		} else {
			return false;
		}
    }

	protected boolean internalCheckConnectivity() {
		final String host = super.getMetadataValue(METADATA_SSH_PUBLIC_IP);

		LOGGER.debug("Checking resource connectivity [host: " + host + "].");

		Runtime run;
		Process p = null;
		Scanner scanner = null;

		try {
			run = Runtime.getRuntime();
			p = run.exec(new String[] { "/bin/bash", "-c",
					"echo quit | telnet " + host + " 2>/dev/null | grep Connected" });
			p.waitFor();

			LOGGER.info("Running command: /bin/bash -c echo quit | telnet " + host + " 2>/dev/null | grep Connected");

			scanner = new Scanner(p.getInputStream());
			if (scanner.hasNext()) {
				String result = scanner.nextLine();

				LOGGER.info("Command result: " + result);

				if (result != null && !result.isEmpty()) {

					LOGGER.info("Resource is alive!");
					return true;
				}
			}
		} catch (Exception e) {
			LOGGER.error(AppMessagesConstants.RESOURCE_CONNECT_FAILED);
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

	public String getInstanceId() {
		return this.instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getComputeOrderId() {
		return this.computeOrderId;
	}

	public String getPublicIpOrderId() { return this.publicIpOrderId; }
}
