package org.fogbowcloud.blowout.infrastructure.token;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.fogbowcloud.blowout.constants.AppPropertiesConstants;
import static org.fogbowcloud.blowout.core.util.AppUtil.makeBodyField;
import org.fogbowcloud.blowout.infrastructure.http.HttpWrapper;
import org.fogbowcloud.blowout.infrastructure.model.Token;
import org.fogbowcloud.blowout.infrastructure.model.User;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Properties;
import java.util.UUID;

public class KeystoneTokenUpdatePlugin extends AbstractTokenUpdatePlugin {

    private static final Logger LOGGER = Logger.getLogger(KeystoneTokenUpdatePlugin.class);

    private static final String FOGBOW_USERNAME = AppPropertiesConstants.INFRA_AUTH_TOKEN_PREFIX +
            AppPropertiesConstants.INFRA_AUTH_TOKEN_USERNAME;
    private static final String FOGBOW_PASSWORD = AppPropertiesConstants.INFRA_AUTH_TOKEN_PREFIX +
            AppPropertiesConstants.INFRA_AUTH_TOKEN_PASSWORD;
    private static final String FOGBOW_PROJECT_NAME = AppPropertiesConstants.INFRA_AUTH_TOKEN_PREFIX +
            AppPropertiesConstants.INFRA_AUTH_TOKEN_PROJECT_NAME;
    private static final String FOGBOW_DOMAIN = AppPropertiesConstants.INFRA_AUTH_TOKEN_PREFIX +
            AppPropertiesConstants.INFRA_AUTH_TOKEN_DOMAIN;

    public static final String FOGBOW_RAS_TOKEN_ENDPOINT = "tokens";

    private final String rasBaseUrl;
    private final String userName;
    private final String password;
    private final String projectName;
    private final String domain;

    public KeystoneTokenUpdatePlugin(Properties properties) {
        super(properties);
        this.userName = super.properties.getProperty(FOGBOW_USERNAME);
        this.password = super.properties.getProperty(FOGBOW_PASSWORD);
        this.projectName = super.properties.getProperty(FOGBOW_PROJECT_NAME);
        this.domain = super.properties.getProperty(FOGBOW_DOMAIN);
        this.rasBaseUrl =  super.properties.getProperty(AppPropertiesConstants.INFRA_FOGBOW_MANAGER_BASE_URL);;
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

    private Token createToken() throws Exception {
        HttpWrapper httpWrapper = new HttpWrapper();

        String endpoint = this.rasBaseUrl + "/" + FOGBOW_RAS_TOKEN_ENDPOINT;
        StringEntity body = makeBodyJson();

        body.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, HttpWrapper.HTTP_CONTENT_JSON));

        String acessToken = httpWrapper.doRequest("post", endpoint, new LinkedList<Header>(), body);
        String userId = String.valueOf(UUID.randomUUID());
        User user = new User(userId, this.userName, this.password);
        Token token = new Token(acessToken, user);

        return token;
    }

    private StringEntity makeBodyJson() throws JSONException, UnsupportedEncodingException {
        JSONObject json = new JSONObject();

        makeBodyField(json, AppPropertiesConstants.INFRA_AUTH_TOKEN_USERNAME, this.userName);
        makeBodyField(json, AppPropertiesConstants.INFRA_AUTH_TOKEN_PASSWORD, this.password);
        makeBodyField(json, AppPropertiesConstants.INFRA_AUTH_TOKEN_DOMAIN, this.domain);
        makeBodyField(json, AppPropertiesConstants.INFRA_AUTH_TOKEN_PROJECT_NAME, this.projectName);

        return new StringEntity(json.toString());
    }



    @Override
    public void validateProperties() throws BlowoutException {
        validateProperty(super.properties, AppPropertiesConstants.INFRA_AUTH_TOKEN_UPDATE_PLUGIN);
        validateProperty(super.properties, FOGBOW_USERNAME);
        validateProperty(super.properties, FOGBOW_PASSWORD);
        validateProperty(super.properties, FOGBOW_PROJECT_NAME);
        validateProperty(super.properties, FOGBOW_DOMAIN);
    }

    private void validateProperty(Properties property, String propertyKey) throws BlowoutException {
        if (property == null || !property.containsKey(propertyKey)) {
            throw new BlowoutException("Required property " + property.toString() + " was not set");
        }
    }
}

