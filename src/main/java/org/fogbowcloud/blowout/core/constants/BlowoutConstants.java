package org.fogbowcloud.blowout.core.constants;

public class BlowoutConstants {
    public static final String DEFAULT_IMPLEMENTATION_BLOWOUT_POOL = "org.fogbowcloud.blowout.pool.DefaultBlowoutPool";
    public static final String DEFAULT_IMPLEMENTATION_SCHEDULER = "org.fogbowcloud.blowout.scheduler.DefaultScheduler";
    public static final String DEFAULT_IMPLEMENTATION_INFRA_MANAGER = "org.fogbowcloud.blowout.infrastructure.manager.DefaultInfrastructureManager";
    public static final String DEFAULT_IMPLEMENTATION_INFRA_PROVIDER = "org.fogbowcloud.blowout.infrastructure.provider.fogbow.FogbowInfrastructureProvider";

    public static final String FOGBOW_REQUIREMENTS_EXAMPLE = "e.g: [Glue2vCPU >= 1 && Glue2RAM >= " +
            "1024 && Glue2disk >= 20 &&" + " Glue2CloudComputeManagerID ==\"servers.your.domain\"]";
}
