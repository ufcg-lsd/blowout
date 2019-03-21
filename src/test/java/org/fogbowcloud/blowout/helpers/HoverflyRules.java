package org.fogbowcloud.blowout.helpers;

import io.specto.hoverfly.junit.core.SimulationSource;
import org.fogbowcloud.blowout.core.constants.FogbowConstants;

import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;

public class HoverflyRules {

    private static final String getPublicIpInstanceEndpoint = "/" + FogbowConstants.RAS_ENDPOINT_PUBLIC_IP +
            "/" + Constants.FAKE_PUBLIC_IP_ORDER_ID;

    private static final String getAllImagesEndpoint = "/" + FogbowConstants.RAS_ENDPOINT_IMAGES + "/"
            + Constants.FAKE_RAS_MEMBER_ID + "/" + Constants.FAKE_CLOUD_NAME;

    public static SimulationSource simulationSource = SimulationSource.
            dsl(
                    service(Constants.FAKE_RAS_BASE_URL)
                        .get(getPublicIpInstanceEndpoint)
                        .willReturn(success().body(Constants.JSON_BODY_RAS_PUBLIC_IP_INSTANCE_RESPONSE)),
                    service(Constants.FAKE_RAS_BASE_URL)
                        .get(getAllImagesEndpoint)
                        .willReturn(success().body())

            );
}
