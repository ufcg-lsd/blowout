package org.fogbowcloud.blowout.infrastructure.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.jetty.http.HttpStatus;
import org.fogbowcloud.blowout.scheduler.core.util.AppPropertiesConstants;
import org.fogbowcloud.manager.core.plugins.identity.naf.NAFIdentityPlugin;
import org.fogbowcloud.manager.occi.model.Token;

public class NAFTokenUpdatePlugin implements TokenUpdatePluginInterface {
	private static final int DEFAULT_UPDATE_TIME = 6;
	private static final TimeUnit DEFAULT_UPDATE_TIME_UNIT = TimeUnit.HOURS;
	private static final String USERNAME_PARAMETER = "username";
	private static final String PASSWORD_PARAMETER = "password";
	private static final String HOUR_PARAMETER = "hour";
	
	private Properties properties;
	
	public NAFTokenUpdatePlugin(Properties properties) {
		this.properties = properties;
	}

	@Override
	public Token generateToken() {
		try {
			NAFIdentityPlugin nafIdentityPlugin = new NAFIdentityPlugin(properties);
			String accessId = requestTokenFromGenerator();
			Token token = nafIdentityPlugin.getToken(accessId);
			return token;
		} catch (Exception e) {
			return null;
		}
	}
	
	private String requestTokenFromGenerator() {
		String tokenGeneratorUrl = properties.getProperty(AppPropertiesConstants.NAF_IDENTITY_TOKEN_GENERATOR_URL);
		String userName = properties.getProperty(AppPropertiesConstants.NAF_IDENTITY_TOKEN_USERNAME);
		String password = properties.getProperty(AppPropertiesConstants.NAF_IDENTITY_TOKEN_PASSWORD);
		int hours = getTokenUpdateTimeInHours();
		
		if (userName == null || password == null || tokenGeneratorUrl == null) {
			return null;
		}
		
		try {
			CloseableHttpClient httpClient = HttpClients.createMinimal();
			HttpPost httpPost = new HttpPost(tokenGeneratorUrl);
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair(USERNAME_PARAMETER, userName));
			parameters.add(new BasicNameValuePair(PASSWORD_PARAMETER, password));
			parameters.add(new BasicNameValuePair(HOUR_PARAMETER, String.valueOf(hours)));
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);
			httpPost.setEntity(formEntity);
			
			CloseableHttpResponse response = httpClient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == HttpStatus.OK_200) {
				return IOUtils.toString(response.getEntity().getContent());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public int getUpdateTime() {
		try{
			return Integer.parseInt(properties.getProperty(AppPropertiesConstants.TOKEN_UPDATE_TIME));
		}catch(Exception e){
			return DEFAULT_UPDATE_TIME;
		}
	}

	@Override
	public TimeUnit getUpdateTimeUnits() {
		String timeUnit = properties.getProperty(AppPropertiesConstants.TOKEN_UPDATE_TIME_UNIT);
		
		if(UpdateTimeUnitsEnum.HOUR.getValue().equalsIgnoreCase(timeUnit)){
			return TimeUnit.HOURS;
		}else if(UpdateTimeUnitsEnum.MINUTES.getValue().equalsIgnoreCase(timeUnit)){
			return TimeUnit.MINUTES;
		}else if(UpdateTimeUnitsEnum.SECONDS.getValue().equalsIgnoreCase(timeUnit)){
			return TimeUnit.SECONDS;
		}else if(UpdateTimeUnitsEnum.MILLISECONDS.getValue().equalsIgnoreCase(timeUnit)){
			return TimeUnit.MILLISECONDS;
		}
		return DEFAULT_UPDATE_TIME_UNIT;
	}
	
	private int getTokenUpdateTimeInHours() {
		
		int hourInMinutes = 60;
		int hourInSeconds = hourInMinutes * 60;
		int hourInMiliseconds = hourInSeconds * 1000;
		
		int updateTime = getUpdateTime();
		TimeUnit updateTimeUnits = getUpdateTimeUnits();
		if (updateTimeUnits.equals(TimeUnit.MINUTES)) {
			updateTime = updateTime / hourInMinutes;
		} else if (updateTimeUnits.equals(TimeUnit.SECONDS)) {
			updateTime = updateTime / hourInSeconds;
		} else if (updateTimeUnits.equals(TimeUnit.MILLISECONDS)) {
			updateTime = updateTime / hourInMiliseconds;
		}
		
		return Math.max(1, updateTime);
	}

}
