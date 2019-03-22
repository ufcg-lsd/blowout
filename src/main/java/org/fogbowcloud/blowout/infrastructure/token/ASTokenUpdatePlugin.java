package org.fogbowcloud.blowout.infrastructure.token;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.constants.AppPropertiesConstants;
import org.fogbowcloud.blowout.core.constants.FogbowConstants;
import org.fogbowcloud.blowout.core.exception.BlowoutException;

import static org.fogbowcloud.blowout.core.constants.AppPropertiesConstants.*;
import static org.fogbowcloud.blowout.core.util.AppUtil.getValueFromJsonStr;
import static org.fogbowcloud.blowout.core.util.AppUtil.makeBodyField;

import org.fogbowcloud.blowout.core.util.AppUtil;
import org.fogbowcloud.blowout.infrastructure.http.HttpWrapper;
import org.fogbowcloud.blowout.infrastructure.model.Token;
import org.fogbowcloud.blowout.infrastructure.model.User;
import org.json.JSONException;
import org.json.JSONObject;



import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Properties;

public class ASTokenUpdatePlugin extends AbstractTokenUpdatePlugin {

    private static final Logger LOGGER = Logger.getLogger(ASTokenUpdatePlugin.class);

    private static final String FOGBOW_USERNAME = AS_TOKEN_PREFIX + AS_TOKEN_USERNAME;
    private static final String FOGBOW_PASSWORD = AS_TOKEN_PREFIX + AS_TOKEN_PASSWORD;
    private static final String FOGBOW_PROJECT_NAME = AS_TOKEN_PREFIX + AS_TOKEN_PROJECT_NAME;
    private static final String FOGBOW_DOMAIN = AS_TOKEN_PREFIX + AS_TOKEN_DOMAIN;

    private final String asBaseUrl;
    private final String userName;
    private final String password;
    private final String projectName;
    private final String domain;

    public ASTokenUpdatePlugin(Properties properties) {
        super(properties);
        this.userName = super.properties.getProperty(FOGBOW_USERNAME);
        this.password = super.properties.getProperty(FOGBOW_PASSWORD);
        this.projectName = super.properties.getProperty(FOGBOW_PROJECT_NAME);
        this.domain = super.properties.getProperty(FOGBOW_DOMAIN);
        this.asBaseUrl =  super.properties.getProperty(AS_BASE_URL);
    }

    @Override
    public Token generateToken() {
        try {
            return createToken();
        } catch (Exception e) {
            LOGGER.error("Error while setting token.", e);
            return null;
        }
    }

    @Override
    public void validateProperties() throws BlowoutException {
        validateProperty(super.properties, TOKEN_UPDATE_PLUGIN);
        validateProperty(super.properties, FOGBOW_USERNAME);
        validateProperty(super.properties, FOGBOW_PASSWORD);
        validateProperty(super.properties, FOGBOW_PROJECT_NAME);
        validateProperty(super.properties, FOGBOW_DOMAIN);
    }

    private Token createToken() throws Exception {
        HttpWrapper httpWrapper = new HttpWrapper();

        final String requestUrl = this.asBaseUrl + "/" + FogbowConstants.AS_ENDPOINT_TOKEN;
        final String publicKeyRAS = getPublicKeyRAS();
        final StringEntity body = makeBodyJson(publicKeyRAS);
        body.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, HttpWrapper.HTTP_CONTENT_JSON));

        final String accessTokenJson = httpWrapper.doRequest(HttpWrapper.HTTP_METHOD_POST, requestUrl, new LinkedList<>(), body);
        final String accessToken = getValueFromJsonStr("token", accessTokenJson);
        final String userId = AppUtil.generateIdentifier();
        User user = new User(userId, this.userName, this.password);

        return new Token(accessToken, user);
    }

    private String getPublicKeyRAS() throws Exception {
        final String requestUrl = this.properties.getProperty(RAS_BASE_URL) + "/" + FogbowConstants.RAS_ENDPOINT_PUBLIC_KEY;
        HttpUriRequest request = new HttpGet(requestUrl);
        HttpResponse response = HttpClients.createMinimal().execute(request);
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        return getValueFromJsonStr(FogbowConstants.JSON_KEY_RAS_PUBLIC_KEY, responseString);
    }


    private StringEntity makeBodyJson(String publicKey) throws JSONException, UnsupportedEncodingException {
        JSONObject json = new JSONObject();

        JSONObject credentials = new JSONObject();
        makeBodyField(credentials, AS_TOKEN_USERNAME, this.userName);
        makeBodyField(credentials, AS_TOKEN_PASSWORD, this.password);
        makeBodyField(credentials, AS_TOKEN_DOMAIN, this.domain);
        makeBodyField(credentials, AS_TOKEN_PROJECT_NAME, this.projectName);

        json.put(AS_TOKEN_CREDENTIALS, credentials);
        makeBodyField(json, AS_TOKEN_PUBLIC_KEY, publicKey);

        return new StringEntity(json.toString());
    }

    private void validateProperty(Properties property, String propertyKey) throws BlowoutException {
        if (property == null || !property.containsKey(propertyKey)) {
            throw new BlowoutException("Required property " + propertyKey + " was not set");
        }
    }
}

