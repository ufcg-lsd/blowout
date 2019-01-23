package org.fogbowcloud.blowout.infrastructure.provider.fogbow;

import org.fogbowcloud.blowout.core.constants.AppPropertiesConstants;

import java.util.Properties;

public class RASHelper {

    private final String rasBaseUrl;

    public RASHelper(Properties properties) {
        this.rasBaseUrl = properties.getProperty(AppPropertiesConstants.INFRA_FOGBOW_MANAGER_BASE_URL);
    }

    public String getRasBaseUrl() {
        return rasBaseUrl;
    }
}
