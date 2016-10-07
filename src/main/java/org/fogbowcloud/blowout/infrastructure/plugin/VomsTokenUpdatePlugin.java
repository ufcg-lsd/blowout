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
import org.fogbowcloud.manager.core.plugins.identity.voms.VomsIdentityPlugin;
import org.fogbowcloud.manager.occi.model.Token;

public class VomsTokenUpdatePlugin extends AbstractTokenUpdatePlugin{

	private static final String FOGBOW_VOMS_CERTIFICATE_PASSWORD = "fogbow.voms.certificate.password";
	private static final String FOGBOW_VOMS_SERVER = "fogbow.voms.server";
	private static final Logger LOGGER = Logger.getLogger(VomsTokenUpdatePlugin.class);
	private static final Token.User DEFAULT_USER = new Token.User("9999", "User");
	private static final int DEFAULT_UPDATE_TIME = 6;
	private static final TimeUnit DEFAULT_UPDATE_TIME_UNIT = TimeUnit.HOURS;
	
	private final String vomsServer;
	private final String password;
	private Properties properties;
	
	public VomsTokenUpdatePlugin(Properties properties){
		super(properties);
		this.vomsServer = properties.getProperty(FOGBOW_VOMS_SERVER);
		this.password = properties.getProperty(FOGBOW_VOMS_CERTIFICATE_PASSWORD) ;
		this.properties = properties;
	}
	
	@Override
	public Token generateToken() {

		try {
			return createToken();
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
		VomsIdentityPlugin vomsIdentityPlugin = new VomsIdentityPlugin(new Properties());

		HashMap<String, String> credentials = new HashMap<String, String>();
		credentials.put("password", password);
		credentials.put("serverName", vomsServer);
		LOGGER.debug("Creating token update with serverName="
				+ vomsServer + " and password="
				+ password);

		Token token = vomsIdentityPlugin.createToken(credentials);
		LOGGER.debug("VOMS proxy updated. New proxy is " + token.toString());

		return token;
	}
	
	protected Token createNewTokenFromFile(String certificateFilePath) throws FileNotFoundException, IOException {

		String certificate = IOUtils.toString(new FileInputStream(certificateFilePath)).replaceAll("\n", "");
		Date date = new Date(System.currentTimeMillis() + (long) Math.pow(10, 9));

		return new Token(certificate, DEFAULT_USER, date, new HashMap<String, String>());
	}
}

