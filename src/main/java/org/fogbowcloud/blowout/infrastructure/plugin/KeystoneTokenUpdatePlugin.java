package org.fogbowcloud.blowout.infrastructure.plugin;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.scheduler.core.util.AppPropertiesConstants;
import org.fogbowcloud.manager.core.plugins.identity.openstack.KeystoneIdentityPlugin;
import org.fogbowcloud.manager.occi.model.Token;

public class KeystoneTokenUpdatePlugin extends AbstractTokenUpdatePlugin{

	private static final Logger LOGGER = Logger.getLogger(KeystoneTokenUpdatePlugin.class);
	private static final Token.User DEFAULT_USER = new Token.User("9999", "User");
	private static final int DEFAULT_UPDATE_TIME = 6;
	private static final TimeUnit DEFAULT_UPDATE_TIME_UNIT = TimeUnit.HOURS;
	
	private static final String FOGBOW_KEYSTONE_USERNAME = "fogbow.keystone.username";
	private static final String FOGBOW_KEYSTONE_TENANTNAME = "fogbow.keystone.tenantname";
	private static final String FOGBOW_KEYSTONE_PASSWORD = "fogbow.keystone.password";
	private static final String FOGBOW_KEYSTONE_AUTH_URL = "fogbow.keystone.auth.url";
	
	private final String username;
	private final String tenantname;
	private final String password;
	private final String authUrl;
	private Properties properties;
	
	public KeystoneTokenUpdatePlugin(Properties properties){
		
		super(properties);
		
		this.properties = properties;
		
		this.username=properties.getProperty(FOGBOW_KEYSTONE_USERNAME);   
		this.tenantname=properties.getProperty(FOGBOW_KEYSTONE_TENANTNAME);    
		this.password=properties.getProperty(FOGBOW_KEYSTONE_PASSWORD);      
		this.authUrl=properties.getProperty(FOGBOW_KEYSTONE_AUTH_URL);       

		//bash bin/fogbow-cli token --create --type openstack -Dusername=fogbow -Dpassword=nc3SRPS2 -DauthUrl=http://10.5.0.14:5000-DtenantName=Fogbow
	}
	
	@Override
	public Token generateToken() {

		try {
			return createToken(this.properties);
		} catch (Throwable e) {
			LOGGER.error("Error while setting token.", e);
			try {
				return createNewTokenFromFile(
						properties.getProperty(AppPropertiesConstants.INFRA_FOGBOW_TOKEN_PUBLIC_KEY_FILEPATH));
			} catch (IOException e1) {
				LOGGER.error("Error while getting token from file.", e);
			}
		}
		
		
	
		
		
		return null;
	}
	
	protected Token createToken() {
		return createToken(new Properties());
	}
	
	protected Token createToken(Properties properties) {
		KeystoneIdentityPlugin keystoneIdentityPlugin = new KeystoneIdentityPlugin(properties);

		HashMap<String, String> credentials = new HashMap<String, String>();
		
		credentials.put(keystoneIdentityPlugin.AUTH_URL, authUrl);
		credentials.put(keystoneIdentityPlugin.USERNAME, username);
		credentials.put(keystoneIdentityPlugin.PASSWORD, password);
		credentials.put(keystoneIdentityPlugin.TENANT_NAME, tenantname);
		LOGGER.debug("Creating token update with USERNAME="
				+ username + " and PASSWORD="
				+ password);

		Token token = keystoneIdentityPlugin.createToken(credentials);
		LOGGER.debug("Keystone cert updated. New cert is " + token.toString());

		return token;
	}
	
	protected Token createNewTokenFromFile(String certificateFilePath) throws FileNotFoundException, IOException {

		String certificate = IOUtils.toString(new FileInputStream(certificateFilePath)).replaceAll("\n", "");
		Date date = new Date(System.currentTimeMillis() + (long) Math.pow(10, 9));

		return new Token(certificate, DEFAULT_USER, date, new HashMap<String, String>());
	}

}
