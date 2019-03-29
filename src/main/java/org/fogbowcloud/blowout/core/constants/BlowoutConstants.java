package org.fogbowcloud.blowout.core.constants;

public class BlowoutConstants {
    public static final String DEFAULT_IMPLEMENTATION_BLOWOUT_POOL = "org.fogbowcloud.blowout.pool.DefaultBlowoutPool";
    public static final String DEFAULT_IMPLEMENTATION_SCHEDULER = "org.fogbowcloud.blowout.scheduler.DefaultScheduler";
    public static final String DEFAULT_IMPLEMENTATION_INFRA_MANAGER = "org.fogbowcloud.blowout.infrastructure.manager.DefaultInfrastructureManager";
    public static final String DEFAULT_IMPLEMENTATION_INFRA_PROVIDER = "org.fogbowcloud.blowout.infrastructure.provider.fogbow.FogbowInfrastructureProvider";

    public static final String FOGBOW_REQUIREMENTS_EXAMPLE = "e.g: [Glue2vCPU >= 1 && Glue2RAM >= " +
            "1024 && Glue2disk >= 20 &&" + " Glue2CloudComputeManagerID ==\"servers.your.domain\"]";

    public static final String ENV_PRIVATE_KEY_FILE = "PRIVATE_KEY_FILE";
    public static final String ENV_HOST = "HOST";
    public static final String ENV_SSH_USER = "SSH_USER";
    public static final String ENV_UUID = "UUID";

    public static final String METADATA_SSH_HOST = "metadataSSHHost";
    public static final String METADATA_SSH_USERNAME_ATT = "metadataSshUsername";
    public static final String METADATA_EXTRA_PORTS_ATT = "metadataExtraPorts";
    public static final String METADATA_PUBLIC_IP = "metadataPublicIp";

    public static final String METADATA_IMAGE_NAME = "metadataImageName";
    public static final String METADATA_IMAGE_ID = "metadataImageId";
    public static final String METADATA_PUBLIC_KEY = "metadataPublicKey";

    public static final String METADATA_VCPU = "metadataVCPU";
    public static final String METADATA_MEM_SIZE = "metadataMenSize";
    public static final String METADATA_DISK_SIZE = "metadataDiskSize";
    public static final String METADATA_LOCATION = "metadataLocation";

    public static final String METADATA_REQUEST_TYPE = "metadataRequestType";
}
