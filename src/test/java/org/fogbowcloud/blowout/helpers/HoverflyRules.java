package org.fogbowcloud.blowout.helpers;

import io.specto.hoverfly.junit.core.SimulationSource;
import org.fogbowcloud.blowout.core.constants.FogbowConstants;

import static io.specto.hoverfly.junit.dsl.HoverflyDsl.service;
import static io.specto.hoverfly.junit.dsl.ResponseCreators.success;
import static org.fogbowcloud.blowout.helpers.Constants.JSON.Header.Key.FOGBOW_USER_TOKEN;

public class HoverflyRules {

    private static final String getPublicIpInstanceEndpoint = "/" + FogbowConstants.RAS_ENDPOINT_PUBLIC_IP +
            "/" + Constants.FakeData.PUBLIC_IP_ORDER_ID;

    private static final String createPublicIPEndpoint = "/" + FogbowConstants.RAS_ENDPOINT_PUBLIC_IP;

    private static final String getAllImagesEndpoint = "/" + FogbowConstants.RAS_ENDPOINT_IMAGES + "/"
            + Constants.FakeData.RAS_MEMBER_ID + "/" + Constants.FakeData.CLOUD_NAME;

    private static final String createComputeEndpoint = "/" + FogbowConstants.RAS_ENDPOINT_COMPUTE;

    private static final String getComputeInstanceEndpoint = "/" + FogbowConstants.RAS_ENDPOINT_COMPUTE +
            Constants.FakeData.COMPUTE_ORDER_ID;

    private static final String getAsToken = "/" + FogbowConstants.AS_ENDPOINT_TOKEN;

    private static final String getRasPublicKey = "/" + FogbowConstants.JSON_KEY_RAS_PUBLIC_KEY;

    public static SimulationSource simulationSource = SimulationSource.
            dsl(
                    service(Constants.FakeData.RAS_BASE_URL)
                            .post(createPublicIPEndpoint)
                            .header(FOGBOW_USER_TOKEN, Constants.FakeData.FOGBOW_USER_TOKEN)
                            .body(Constants.JSON.Body.PUBLIC_IP_ORDER)
                            .willReturn(success().body(Constants.JSON.Body.PUBLIC_IP_ORDER_ID)),

                    service(Constants.FakeData.RAS_BASE_URL)
                        .get(getPublicIpInstanceEndpoint)
                        .header(FOGBOW_USER_TOKEN, Constants.FakeData.FOGBOW_USER_TOKEN)
                        .willReturn(success().body(Constants.JSON.Body.PUBLIC_IP_INSTANCE_RESPONSE)),

                    service(Constants.FakeData.RAS_BASE_URL)
                        .get(getAllImagesEndpoint)
                        .header(FOGBOW_USER_TOKEN, Constants.FakeData.FOGBOW_USER_TOKEN)
                        .willReturn(success().body(Constants.JSON.Body.IMAGES_RESPONSE)),

                    service(Constants.FakeData.RAS_BASE_URL)
                        .post(createComputeEndpoint)
                        .header(FOGBOW_USER_TOKEN, Constants.FakeData.FOGBOW_USER_TOKEN)
                        .body(Constants.JSON.Body.COMPUTE)
                        .willReturn(success().body(Constants.JSON.Body.COMPUTE_ORDER_ID)),

                    service(Constants.FakeData.RAS_BASE_URL)
                            .post(getComputeInstanceEndpoint)
                            .header(FOGBOW_USER_TOKEN, Constants.FakeData.FOGBOW_USER_TOKEN)
                            .willReturn(success().body(Constants.JSON.Body.COMPUTE)),

                    service(Constants.FakeData.RAS_BASE_URL)
                            .get(getRasPublicKey)
                            .willReturn(success().body(Constants.FakeData.RAS_PUBLIC_KEY)),

                    service(Constants.FakeData.AS_BASE_URL)
                            .get(getAsToken)
                            .body(Constants.JSON.Body.AUTHENTICATE)
                            .willReturn(success().body(Constants.JSON.Body.AUTHENTICATE_RESPONSE))
            );
}
