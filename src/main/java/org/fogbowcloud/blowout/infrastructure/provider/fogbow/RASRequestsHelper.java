package org.fogbowcloud.blowout.infrastructure.provider.fogbow;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.constants.AppMessagesConstants;
import org.fogbowcloud.blowout.core.constants.AppPropertiesConstants;
import org.fogbowcloud.blowout.core.constants.FogbowConstants;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.util.AppUtil;
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
    private final Properties properties;
    private String RAS_BASE_URL;
    private HttpWrapper http;
    private Token token;

    public RASRequestsHelper(Properties properties, AbstractTokenUpdatePlugin tokenUpdatePlugin) {
        this.http = new HttpWrapper();
        this.properties = properties;
        this.token = tokenUpdatePlugin.generateToken();
        this.RAS_BASE_URL = this.properties.getProperty(AppPropertiesConstants.RAS_BASE_URL);
    }

    public String createCompute(Specification specification) throws RequestResourceException {
        StringEntity requestBody = null;
        try {
            requestBody = this.makeJsonBody(specification);
        } catch (UnsupportedEncodingException uee) {
            LOGGER.error("Error while requesting resource on Fogbow" + uee.getMessage(), uee);
        } catch (BlowoutException be){
            LOGGER.error("Error while requesting resource on Fogbow: " + be.getMessage(), be);
        }

        if (requestBody != null) {
            requestBody.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, HttpWrapper.HTTP_CONTENT_JSON));
        }

        String computeOrderId;
        try {
            String computerOrderIdResponse = this.doRequest(HttpWrapper.HTTP_METHOD_POST, this.RAS_BASE_URL + "/" +
                    FogbowConstants.RAS_ENDPOINT_COMPUTE, new LinkedList<>(), requestBody);
            computeOrderId = AppUtil.getValueFromJsonStr("id", computerOrderIdResponse);
            LOGGER.info("Compute ID was requested successfully.");
        } catch (Exception e){
            LOGGER.error("Error while requesting resource on Fogbow", e);
            throw new RequestResourceException("Request for Fogbow Resource has FAILED: " + e.getMessage(), e);
        }
        return computeOrderId;
    }

    public String createPublicIp(String computeOrderId) throws InterruptedException {
        final Integer sleepTimeInMillis = 6000;

        sleep(sleepTimeInMillis);
        String publicIpId = null;
        final String cloudName = this.properties.getProperty(AppPropertiesConstants.DEFAULT_CLOUD_NAME);
        final String provider = this.properties.getProperty(AppPropertiesConstants.RAS_MEMBER_ID);
        final String requestUrl = RAS_BASE_URL + "/" + FogbowConstants.RAS_ENDPOINT_PUBLIC_IP;

        Map<String, String> bodyRequestAttrs = new HashMap<>();
        if (computeOrderId != null && !computeOrderId.isEmpty()) {
            bodyRequestAttrs.put(FogbowConstants.JSON_KEY_RAS_CLOUD_NAME, cloudName);
            bodyRequestAttrs.put(FogbowConstants.JSON_KEY_RAS_COMPUTE_ID, computeOrderId);
            bodyRequestAttrs.put(FogbowConstants.JSON_KEY_FOGBOW_PROVIDER, provider);
        }
        try {
            final StringEntity bodyRequest = makeRequestBodyJson(bodyRequestAttrs);
            String publicIpIdResponse = this.doRequest(HttpWrapper.HTTP_METHOD_POST, requestUrl,
                    new LinkedList<>(), bodyRequest);
            publicIpId = AppUtil.getValueFromJsonStr("id", publicIpIdResponse);
            LOGGER.info("Public IP ID was requested successfully.");
        } catch (Exception e) {
            LOGGER.error("Error while getting Public IP for compute order of id " + computeOrderId, e);
        }
        return publicIpId;
    }

    public Map<String, Object> getPublicIpInstance(String publicIpOrderId) {
        String response;
        Map<String, Object> publicIpInstance = new HashMap<>();
        final String requestUrl = RAS_BASE_URL + "/" + FogbowConstants.RAS_ENDPOINT_PUBLIC_IP + "/" + publicIpOrderId;
        final String errorMessage = AppMessagesConstants.ERROR_WHILE_GET_PUBLIC_IP_INSTANCE + publicIpOrderId;

        try {
            response = this.doRequest(HttpWrapper.HTTP_METHOD_GET, requestUrl, new LinkedList<>());
            publicIpInstance = parseJSONStringToMap(response);
            LOGGER.debug("Getting Public Ip instance.");

            LOGGER.debug(publicIpInstance);
        } catch (Exception e) {
            LOGGER.error(errorMessage, e);
        }
        return publicIpInstance;
    }

    public Map<String, Object> getComputeInstance(String computeOrderId) throws Exception {
        final String requestUrl = RAS_BASE_URL + "/" + FogbowConstants.RAS_ENDPOINT_COMPUTE + "/" + computeOrderId;
        final String instanceInformation = this.doRequest(HttpWrapper.HTTP_METHOD_GET, requestUrl, new ArrayList<>());

        return parseJSONStringToMap(instanceInformation);
    }

    public void deleteFogbowResource(FogbowResource fogbowResource) throws Exception {
        final String computeEndpoint = RAS_BASE_URL + "/" + FogbowConstants.RAS_ENDPOINT_COMPUTE +
                "/" + fogbowResource.getComputeOrderId();
        final String publicIpEndpoint = RAS_BASE_URL + "/" + FogbowConstants.RAS_ENDPOINT_PUBLIC_IP +
                "/" + fogbowResource.getPublicIpOrderId();
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

    public StringEntity makeJsonBody(Specification specification) throws UnsupportedEncodingException, BlowoutException {
        JSONObject json = new JSONObject();
        String userName = specification.getUsername();

        if (userName == null || userName.trim().isEmpty()) {
            userName = "Iguassu";
        }

        final String iguassuComputeName = "Compute started by: " + userName;

        String imageName = specification.getImageName();
        String imageId = getImageId(imageName);
        LOGGER.info("Using the image " + imageName + ":" + imageId + " in compute request");

        makeBodyField(json, FogbowConstants.JSON_KEY_RAS_CLOUD_NAME, specification.getCloudName());
        makeBodyField(json, FogbowConstants.JSON_KEY_RAS_DISK, specification.getDisk());
        makeBodyField(json, FogbowConstants.JSON_KEY_RAS_IMAGE_ID, imageId);
        makeBodyField(json, FogbowConstants.JSON_KEY_RAS_MEMORY, specification.getMemory());
        makeBodyField(json, FogbowConstants.JSON_KEY_RAS_COMPUTE_NAME, iguassuComputeName);
        makeBodyField(json, FogbowConstants.JSON_KEY_RAS_PUBLIC_KEY, specification.getPublicKey());
        makeBodyField(json, FogbowConstants.JSON_KEY_RAS_VCPU, specification.getvCPU());

        return new StringEntity(json.toString());
    }

    private String getImageId(String imageName) throws BlowoutException {
        String imageId;
        List<String> images = this.getImagesByName(imageName);
        if(images.isEmpty()){
            throw new BlowoutException("No images found with the name " + imageName);
        }
        imageId = images.get(0);
        return imageId;
    }

    private List<String> getImagesByName(String imageName){
        List<String> images = new ArrayList<>();

        Map<String, Object> imagesMap = getAllImages();
        for(String imageId : imagesMap.keySet()){
            String currentImageName = imagesMap.get(imageId).toString();
            if(currentImageName.equals(imageName)){
                images.add(imageId);
            }
        }
        return images;
    }


    private Map<String, Object> getAllImages(){
        final String cloudName = "cloud4";
        final String memberId = this.properties.getProperty(AppPropertiesConstants.RAS_MEMBER_ID);
        Map<String, Object> imagesMap = new HashMap<>();
        final String requestUrl = RAS_BASE_URL + "/" + FogbowConstants.RAS_ENDPOINT_IMAGES + "/"
                + memberId + "/" + cloudName;
        final String errorMessage = "Error while getting info about images of member with id :" + memberId;

        try {
            final String response = this.doRequest(HttpWrapper.HTTP_METHOD_GET, requestUrl, new LinkedList<>());
            imagesMap = parseJSONStringToMap(response);
        } catch (Exception e) {
            LOGGER.error(errorMessage, e);
        }
        return imagesMap;
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