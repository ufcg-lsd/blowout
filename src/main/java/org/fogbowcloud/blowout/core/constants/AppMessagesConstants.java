package org.fogbowcloud.blowout.core.constants;

public class AppMessagesConstants {

    public static final String RESOURCE_CONNECT_FAILED = "Failed to connect with resource.";
    public static final String FOGBOW_REQUIREMENTS_EXAMPLE = "e.g: [Glue2vCPU >= 1 && Glue2RAM >= " +
            "1024 && Glue2disk >= 20 &&" + " Glue2CloudComputeManagerID ==\"servers.your.domain\"]";

    public static final String ATTRIBUTES_INVALIDS = "Instance attributes invalids.";
    public static final String VALIDATING_ATTRIBUTES = "Validating instance attributes.";

    public static final String RESOURCE_NOT_VALID = "The resource is not a valid. Was never requested or is already deleted";

    public static final String ACT_SOURCE_MESSAGE = "Calling act from the Thread " + Thread.currentThread().getId() +
					" of entity: " + Thread.currentThread().getName();
}
