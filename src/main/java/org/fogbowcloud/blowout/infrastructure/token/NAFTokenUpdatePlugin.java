package org.fogbowcloud.blowout.infrastructure.token;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.bouncycastle.util.encoders.Base64;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.fogbowcloud.manager.core.plugins.identity.naf.NAFIdentityPlugin;
import org.fogbowcloud.manager.core.plugins.identity.naf.RSAUtils;
import org.fogbowcloud.manager.occi.model.Token;

import com.google.gson.JsonObject;

public class NAFTokenUpdatePlugin extends AbstractTokenUpdatePlugin {

    private static final Logger LOGGER = Logger.getLogger(NAFTokenUpdatePlugin.class);

    private static final String USERNAME_PARAMETER = "username";
	private static final String PASSWORD_PARAMETER = "password";
	public static final String NAF_IDENTITY_PRIVATE_KEY = AppPropertiesConstants.INFRA_AUTH_TOKEN_PREFIX
			+ "naf_identity_private_key";
	public static final String NAF_IDENTITY_PUBLIC_KEY = AppPropertiesConstants.INFRA_AUTH_TOKEN_PREFIX
			+ "naf_identity_private_key";
	public static final String NAF_IDENTITY_TOKEN_USERNAME = AppPropertiesConstants.INFRA_AUTH_TOKEN_PREFIX
			+ "naf_identity_token_username";
	public static final String NAF_IDENTITY_TOKEN_PASSWORD = AppPropertiesConstants.INFRA_AUTH_TOKEN_PREFIX
			+ "naf_identity_token_password";
	public static final String NAF_IDENTITY_TOKEN_GENERATOR_URL = AppPropertiesConstants.INFRA_AUTH_TOKEN_PREFIX
			+ "naf_identity_token_generator_endpoint";
	private static final String HOUR_PARAMETER = "hour";

	public NAFTokenUpdatePlugin(Properties properties) {
		super(properties);
		validateProperties();
	}

	@Override
	public Token generateToken() {
		LOGGER.debug("Creating NAF Token.");
		Token token = null;
		try {
			NAFIdentityPlugin nafIdentityPlugin = new NAFIdentityPlugin(super.getProperties());
			String accessId = this.requestTokenFromGenerator();
			Token token = nafIdentityPlugin.getToken(accessId);
			return token;
		} catch (Exception e) {
			return null;
		}
	}

	private String requestTokenFromGenerator() throws Exception {
		Properties properties = super.getProperties();

		String tokenGeneratorUrl = properties.getProperty(NAF_IDENTITY_TOKEN_GENERATOR_URL);
		String userName = properties.getProperty(NAF_IDENTITY_TOKEN_USERNAME);
		String password = properties.getProperty(NAF_IDENTITY_TOKEN_PASSWORD);

		int hours = this.getTokenUpdateTimeInHours();

		if (userName == null || password == null || tokenGeneratorUrl == null) {
			return null;
		}

		CloseableHttpClient httpClient = null;
		CloseableHttpResponse response = null;
		try {
			httpClient = HttpClients.createMinimal();
			HttpPost httpPost = new HttpPost(tokenGeneratorUrl);
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();
			parameters.add(new BasicNameValuePair(USERNAME_PARAMETER, userName));
			parameters.add(new BasicNameValuePair(PASSWORD_PARAMETER, password));
			parameters.add(new BasicNameValuePair(HOUR_PARAMETER, String.valueOf(hours)));
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);
			httpPost.setEntity(formEntity);

			response = httpClient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				return IOUtils.toString(response.getEntity().getContent());
			}
		} finally {
			if (httpClient != null) {
				httpClient.close();
			}
			if (response != null) {
				response.close();
			}
		}

		return null;
	}

	private static String getKey(String filename) throws IOException {
		// Read key from file
		String strKeyPEM = "";
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = br.readLine()) != null) {
			strKeyPEM += line;
		}
		br.close();
		return strKeyPEM;
	}

	private static RSAPrivateKey getPrivateKey(String filename) throws Exception {
		String privateKeyPEM = getKey(filename);

		// Remove the first and last lines
		privateKeyPEM = privateKeyPEM
				.replace("-----BEGIN PRIVATE KEY-----", "");
		privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");

		// Base64 decode data
		byte[] encoded = org.bouncycastle.util.encoders.Base64
				.decode(privateKeyPEM);

		KeyFactory kf = KeyFactory.getInstance("RSA");
		RSAPrivateKey privKey = (RSAPrivateKey) kf
				.generatePrivate(new PKCS8EncodedKeySpec(encoded));
		return privKey;
	}


	protected String createAccessId() {
		JsonObject jsonObject = new JsonObject();
		String name = this.properties.getProperty(TOKEN_PLUGIN_USERNAME);
		jsonObject.addProperty(NAME_KEY_JSON, name != null ? name: DEFAULT_NAME);
		long infinitTime = new Date(Long.MAX_VALUE).getTime();
		jsonObject.addProperty(TOKEN_ETIME_KEY_JSON, String.valueOf(infinitTime));
		jsonObject.add(SAML_ATTRIBUTES_KEY_JSON, new JsonObject());

		return jsonObject.toString();
	}

	@Override
	public void validateProperties() throws BlowoutException {
		Properties properties = super.getProperties();

		if (!properties.containsKey(NAF_IDENTITY_PRIVATE_KEY)) {
			throw new BlowoutException(
					"Required property " + NAF_IDENTITY_PRIVATE_KEY + " was not set");
		}

		if (!properties.containsKey(NAF_IDENTITY_PUBLIC_KEY)) {
			throw new BlowoutException(
					"Required property " + NAF_IDENTITY_PUBLIC_KEY + " was not set");
		}

		if (!properties.containsKey(NAF_IDENTITY_TOKEN_GENERATOR_URL)) {
			throw new BlowoutException(
					"Required property " + NAF_IDENTITY_TOKEN_GENERATOR_URL + " was not set");
		}

		if (!properties.containsKey(NAF_IDENTITY_TOKEN_USERNAME)) {
			throw new BlowoutException(
					"Required property " + NAF_IDENTITY_TOKEN_USERNAME + " was not set");
		}

		if (!properties.containsKey(NAF_IDENTITY_TOKEN_PASSWORD)) {
			throw new BlowoutException(
					"Required property " + NAF_IDENTITY_TOKEN_PASSWORD + " was not set");
		}
	}
}
