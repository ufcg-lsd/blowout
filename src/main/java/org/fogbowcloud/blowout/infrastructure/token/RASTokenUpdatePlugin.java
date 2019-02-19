package org.fogbowcloud.blowout.infrastructure.token;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.constants.FogbowConstants;
import org.fogbowcloud.blowout.core.exception.BlowoutException;

import static org.fogbowcloud.blowout.core.constants.AppPropertiesConstants.*;
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

public class RASTokenUpdatePlugin extends AbstractTokenUpdatePlugin {

    private static final Logger LOGGER = Logger.getLogger(RASTokenUpdatePlugin.class);

    private static final String FOGBOW_USERNAME = RAS_TOKEN_PREFIX + RAS_TOKEN_USERNAME;
    private static final String FOGBOW_PASSWORD = RAS_TOKEN_PREFIX + RAS_TOKEN_PASSWORD;
    private static final String FOGBOW_PROJECT_NAME = RAS_TOKEN_PREFIX + RAS_TOKEN_PROJECT_NAME;
    private static final String FOGBOW_DOMAIN = RAS_TOKEN_PREFIX + RAS_TOKEN_DOMAIN;

    private final String rasBaseUrl;
    private final String userName;
    private final String password;
    private final String projectName;
    private final String domain;

    public RASTokenUpdatePlugin(Properties properties) {
        super(properties);
        this.userName = super.properties.getProperty(FOGBOW_USERNAME);
        this.password = super.properties.getProperty(FOGBOW_PASSWORD);
        this.projectName = super.properties.getProperty(FOGBOW_PROJECT_NAME);
        this.domain = super.properties.getProperty(FOGBOW_DOMAIN);
        this.rasBaseUrl =  super.properties.getProperty(RAS_BASE_URL);
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

        String requestUrl = this.rasBaseUrl + "/" + FogbowConstants.RAS_ENDPOINT_TOKEN;
        StringEntity body = makeBodyJson();

        body.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, HttpWrapper.HTTP_CONTENT_JSON));

        String accessToken = httpWrapper.doRequest("post", requestUrl, new LinkedList<>(), body);
        String userId = AppUtil.generateIdentifier();
        User user = new User(userId, this.userName, this.password);

        return new Token(accessToken, user);
    }

    private StringEntity makeBodyJson() throws JSONException, UnsupportedEncodingException {
        JSONObject json = new JSONObject();

        makeBodyField(json, RAS_TOKEN_USERNAME, this.userName);
        makeBodyField(json, RAS_TOKEN_PASSWORD, this.password);
        makeBodyField(json, RAS_TOKEN_DOMAIN, this.domain);
        makeBodyField(json, RAS_TOKEN_PROJECT_NAME, this.projectName);

        return new StringEntity(json.toString());
    }

    private void validateProperty(Properties property, String propertyKey) throws BlowoutException {
        if (property == null || !property.containsKey(propertyKey)) {
            throw new BlowoutException("Required property " + propertyKey + " was not set");
        }
    }
}

