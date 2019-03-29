package org.fogbowcloud.blowout.core.constants;

public class FogbowConstants {

    /*
     * Authentication Service endpoints.
     */
    public static final String AS_ENDPOINT_TOKEN = "tokens";

    /*
     * Resource Allocation Service endpoints.
     */
    public static final String RAS_ENDPOINT_COMPUTE = "computes";
    public static final String RAS_ENDPOINT_PUBLIC_IP = "publicIps";
    public static final String RAS_ENDPOINT_PUBLIC_KEY = "publicKey";
    public static final String RAS_ENDPOINT_IMAGES = "images";

    /*
     * Resource Allocation Service fields.
     */
    public static final String JSON_KEY_RAS_CLOUD_NAME = "cloudName";
    public static final String JSON_KEY_RAS_DISK = "disk";
    public static final String JSON_KEY_RAS_IMAGE_ID = "imageId";
    public static final String JSON_KEY_RAS_MEMORY = "memory";
    public static final String JSON_KEY_RAS_COMPUTE_NAME = "name";
    public static final String JSON_KEY_RAS_PUBLIC_KEY = "publicKey";
    public static final String JSON_KEY_RAS_COMPUTE_ID = "computeId";
    public static final String JSON_KEY_RAS_VCPU = "vCPU";
    public static final String JSON_KEY_FOGBOW_PROVIDER = "provider";
    public static final String JSON_KEY_FOGBOW_PUBLIC_IP = "ip";

    public static final String METADATA_FOGBOW_REQUIREMENTS = "FogbowRequirements";
    public static final String METADATA_FOGBOW_REQUIREMENTS_Glue2vCPU = "Glue2vCPU";
    public static final String METADATA_FOGBOW_REQUIREMENTS_Glue2RAM = "Glue2RAM";
    public static final String METADATA_FOGBOW_REQUIREMENTS_Glue2disk = "Glue2disk";
    public static final String METADATA_FOGBOW_REQUIREMENTS_1Glue2CloudComputeManagerID = "Glue2CloudComputeManagerID";
    public static final String METADATA_REQUEST_TYPE = "RequestType";

    public static final String INSTANCE_ATTRIBUTE_DEFAULT_SHH_USERNAME = "fogbow";
    public static final String INSTANCE_ATTRIBUTE_MEMORY_SIZE = "memory";
    public static final String INSTANCE_ATTRIBUTE_VCPU = "vCPU";
    public static final String INSTANCE_ATTRIBUTE_STATE = "state";
    public static final String INSTANCE_ATTRIBUTE_NAME = "name";
    public static final String INSTANCE_ATTRIBUTE_DISK_SIZE = "disk";


}
