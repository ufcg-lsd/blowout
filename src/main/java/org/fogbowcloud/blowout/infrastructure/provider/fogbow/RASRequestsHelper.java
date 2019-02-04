package org.fogbowcloud.blowout.infrastructure.provider.fogbow;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.constants.AppPropertiesConstants;
import org.fogbowcloud.blowout.core.constants.FogbowConstants;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.infrastructure.exception.RequestResourceException;
import org.fogbowcloud.blowout.infrastructure.http.HttpWrapper;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.infrastructure.model.Token;
import org.fogbowcloud.blowout.infrastructure.token.AbstractTokenUpdatePlugin;
import org.json.JSONObject;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static java.lang.Thread.sleep;
import static org.fogbowcloud.blowout.core.util.AppUtil.*;

public class RASRequestsHelper {
    private final Logger LOGGER = Logger.getLogger(RASRequestsHelper.class);
    private final String RAS_BASE_URL;
    private final Properties properties;

    private HttpWrapper http;
    private Token token;

    public RASRequestsHelper(Properties properties, AbstractTokenUpdatePlugin tokenUpdatePlugin) {
        this.http = new HttpWrapper();
        this.properties = properties;
        this.token = tokenUpdatePlugin.generateToken();
        this.RAS_BASE_URL = this.properties.getProperty(AppPropertiesConstants.INFRA_RAS_BASE_URL);
    }

    public String getComputeOrderId(Specification specification) throws RequestResourceException {
        StringEntity requestBody = null;
        try {
            requestBody = this.makeJsonBody(specification);
        } catch (UnsupportedEncodingException uee) {
            LOGGER.error("Error while requesting resource on Fogbow" + uee.getMessage(), uee);
        }

        requestBody.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, HttpWrapper.HTTP_CONTENT_JSON));
        String computeOrderId;
        try {
            computeOrderId = this.doRequest(HttpWrapper.HTTP_METHOD_POST, this.RAS_BASE_URL + "/" +
                    FogbowConstants.RAS_ENDPOINT_COMPUTE, new LinkedList<>(), requestBody);
            LOGGER.info("Compute ID was requested successfully.");
        } catch (Exception e){
            LOGGER.error("Error while requesting resource on Fogbow", e);
            throw new RequestResourceException("Request for Fogbow Resource has FAILED: " + e.getMessage(), e);
        }
        return computeOrderId;
    }

    public String getPublicIpId(String computeOrderId) {
        String publicIpId = null;
        String provider = this.properties.getProperty(AppPropertiesConstants.INFRA_AUTH_TOKEN_PROJECT_NAME);
        String requestUrl = RAS_BASE_URL + "/" + FogbowConstants.RAS_ENDPOINT_PUBLIC_IP;

        Map<String, String> bodyRequestAttrs = new HashMap<>();
        if (computeOrderId != null && !computeOrderId.isEmpty()) {
            bodyRequestAttrs.put(FogbowConstants.JSON_KEY_FOGBOW_COMPUTE_ID, computeOrderId);
            bodyRequestAttrs.put(FogbowConstants.JSON_KEY_FOGBOW_PROVIDER, provider);
        }
        try {
            StringEntity bodyRequest = makeRequestBodyJson(bodyRequestAttrs);
            publicIpId = this.doRequest(HttpWrapper.HTTP_METHOD_POST, requestUrl,
                    new LinkedList<>(), bodyRequest);
            LOGGER.info("Public IP ID was requested successfully.");
        } catch (Exception e) {
            LOGGER.error("Error while getting Public IP for compute order of id " + computeOrderId, e);
        }
        return publicIpId;
    }

    public Map<String, Object> getPublicIpInstance(String publicIpId) throws InterruptedException {
        sleep(6000);
        String response;
        Map<String, Object> sshInfo = new HashMap<>();
        String requestUrl = RAS_BASE_URL + "/" + FogbowConstants.RAS_ENDPOINT_PUBLIC_IP + "/" + publicIpId;

        final String state = "state";
        final String desiredState = "READY";
        final int maxRequestsTries = 5;
        int counter = 0;

        final String errorMessage = "Error while getting info about public instance of order with id " + publicIpId;

        while (counter <= maxRequestsTries && !sshInfo.get(state).equals(desiredState)) {
            counter++;
            try {
                response = this.doRequest(HttpWrapper.HTTP_METHOD_GET, requestUrl, new LinkedList<>());
                sshInfo = parseAttributes(response);
                LOGGER.debug("Getting SSH information.");
                LOGGER.debug(sshInfo);
            } catch (Exception e) {
                LOGGER.error(errorMessage, e);
            }
        }

        if (counter == maxRequestsTries && !sshInfo.get(state).equals(desiredState)) {
            LOGGER.error(errorMessage);
        }

        return sshInfo;
    }

    public Map<String, Object> getComputeInstance(String computeOrderId) throws Exception {
        String requestUrl = RAS_BASE_URL + "/" + FogbowConstants.RAS_ENDPOINT_COMPUTE + "/" + computeOrderId;
        String instanceInformation = this.doRequest(HttpWrapper.HTTP_METHOD_GET, requestUrl, new ArrayList<>());

        return parseAttributes(instanceInformation);
    }

    public void deleteFogbowResource(FogbowResource fogbowResource) throws Exception {
        final String computeEndpoint = RAS_BASE_URL + "/" + FogbowConstants.RAS_ENDPOINT_COMPUTE +
                "/" + fogbowResource.getComputeOrderId();
        final String publicIpEndpoint = RAS_BASE_URL + "/" + FogbowConstants.RAS_ENDPOINT_PUBLIC_IP +
                "/" + fogbowResource.getPublicIpId();
        try {
            this.doRequest(HttpWrapper.HTTP_METHOD_DELETE, computeEndpoint, new ArrayList<>());
            LOGGER.info("Compute was deleted successfully.");
        } catch (Exception e) {
            LOGGER.error("Error while trying to delete the Compute order.");
        }

        try {
            this.doRequest(HttpWrapper.HTTP_METHOD_DELETE, publicIpEndpoint, new ArrayList<>());
            LOGGER.info("Public IP was deleted successfully.");
        } catch (Exception e){
            LOGGER.error("Error while trying to delete the Public IP.");
        }
    }

    public void setHttpWrapper(HttpWrapper httpWrapper) {
        this.http = httpWrapper;
    }

    public void setToken(Token token) { this.token = token; }

    private String doRequest(String method, String endpoint, List<Header> headers, StringEntity bodyJson) throws Exception {
        return this.http.doRequest(method, endpoint, this.token.getAccessId(), headers, bodyJson);
    }

    private String doRequest(String method, String endpoint, List<Header> headers) throws Exception {
        return this.http.doRequest(method, endpoint, this.token.getAccessId(), headers);
    }

    public StringEntity makeJsonBody(Specification specification) throws UnsupportedEncodingException {
        JSONObject json = new JSONObject();

        makeBodyField(json, FogbowConstants.JSON_KEY_FOGBOW_REQUIREMENTS_PUBLIC_KEY, specification.getPublicKey());
        makeBodyField(json, FogbowConstants.JSON_KEY_FOGBOW_REQUIREMENTS_MEMORY, specification.getMemory());
        makeBodyField(json, FogbowConstants.JSON_KEY_FOGBOW_REQUIREMENTS_DISK, specification.getDisk());
        makeBodyField(json, FogbowConstants.JSON_KEY_FOGBOW_REQUIREMENTS_IMAGE_ID, specification.getImageId());
        makeBodyField(json, FogbowConstants.JSON_KEY_FOGBOW_REQUIREMENTS_VCPU, specification.getvCPU());

        return new StringEntity(json.toString());
    }

    private Map<String, Object> parseAttributes(String response) throws ScriptException {
        ScriptEngine engine;
        ScriptEngineManager sem = new ScriptEngineManager();
        engine = sem.getEngineByName("javascript");

        String script = "Java.asJSONCompatible(" + response + ")";
        Object result = engine.eval(script);

        Map<String, Object> contents = (Map<String, Object>) result;

        return new HashMap<>(contents);
    }

    private StringEntity makeRequestBodyJson(Map<String, String> bodyRequestAttrs) throws UnsupportedEncodingException {
        JSONObject json = new JSONObject();

        for (String jsonKey : bodyRequestAttrs.keySet()) {
            String jsonValue = bodyRequestAttrs.get(jsonKey);
            json.put(jsonKey, jsonValue);
        }

        StringEntity se = new StringEntity(json.toString());
        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, HttpWrapper.HTTP_CONTENT_JSON));

        return se;
    }
}