package org.fogbowcloud.blowout.infrastructure.token;

import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.fogbowcloud.blowout.core.util.AppPropertiesConstants;
import org.fogbowcloud.manager.core.plugins.identity.ldap.LdapIdentityPlugin;
import org.fogbowcloud.manager.occi.model.Token;

public class LDAPTokenUpdatePlugin extends AbstractTokenUpdatePlugin {

	private static final Logger LOGGER = Logger.getLogger(LDAPTokenUpdatePlugin.class);
	private static final String LDAP_USERNAME = AppPropertiesConstants.INFRA_AUTH_TOKEN_PREFIX + "ldap_username";
	private static final String LDAP_PASSWORD = AppPropertiesConstants.INFRA_AUTH_TOKEN_PREFIX + "ldap_password";
	private static final String LDAP_AUTH_URL = AppPropertiesConstants.INFRA_AUTH_TOKEN_PREFIX + "ldap_auth_url";
	private static final String LDAP_BASE = AppPropertiesConstants.INFRA_AUTH_TOKEN_PREFIX + "ldap_base";
	private static final String LDAP_ENCRYPT_TYPE = AppPropertiesConstants.INFRA_AUTH_TOKEN_PREFIX
			+ "ldap_encrypt_type";
	private static final String LDAP_PRIVATE_KEY = AppPropertiesConstants.INFRA_AUTH_TOKEN_PREFIX
			+ "ldap_private_key";
	private static final String LDAP_PUBLIC_KEY = AppPropertiesConstants.INFRA_AUTH_TOKEN_PREFIX+"ldap_public_key";

	private String userName;
	private String password;
	private String ldapUrl;
	private String ldapBase;
	private String encrypt;
	private String privateKeyPath;
	private String publicKeyPath;

	public LDAPTokenUpdatePlugin(Properties properties) {
		super(properties);

		userName = properties.getProperty(LDAP_USERNAME);
		password = properties.getProperty(LDAP_PASSWORD);
		ldapUrl = properties.getProperty(LDAP_AUTH_URL);
		ldapBase = properties.getProperty(LDAP_BASE);
		encrypt = properties.getProperty(LDAP_ENCRYPT_TYPE);
		privateKeyPath = properties.getProperty(LDAP_PRIVATE_KEY);
		publicKeyPath = properties.getProperty(LDAP_PUBLIC_KEY);

	}

	@Override
	public Token generateToken() {

		LdapIdentityPlugin ldapIdentityPlugin = new LdapIdentityPlugin(new Properties());

		HashMap<String, String> credentials = new HashMap<String, String>();

		credentials.put(ldapIdentityPlugin.CRED_USERNAME, userName);
		credentials.put(ldapIdentityPlugin.CRED_PASSWORD, password);
		credentials.put(ldapIdentityPlugin.CRED_AUTH_URL, ldapUrl);
		credentials.put(ldapIdentityPlugin.CRED_LDAP_BASE, ldapBase);
		credentials.put(ldapIdentityPlugin.CRED_LDAP_ENCRYPT, encrypt);
		credentials.put(ldapIdentityPlugin.CRED_PRIVATE_KEY, privateKeyPath);
		credentials.put(ldapIdentityPlugin.CRED_PUBLIC_KEY, publicKeyPath);

		LOGGER.debug("Creating token update with USERNAME=" + userName + " and PASSWORD=" + password);

		Token token = ldapIdentityPlugin.createToken(credentials);
		LOGGER.debug("LDAP token updated. New token is " + token.toString());

		return token;
	}

	@Override
	public void validateProperties() throws BlowoutException {
		if (!properties.containsKey(LDAP_USERNAME)) {
			throw new BlowoutException("Required property " + LDAP_USERNAME + " was not set");
		}
		if (!properties.containsKey(LDAP_PASSWORD)) {
			throw new BlowoutException("Required property " + LDAP_PASSWORD + " was not set");
		}
		if (!properties.containsKey(LDAP_AUTH_URL)) {
			throw new BlowoutException("Required property " + LDAP_AUTH_URL + " was not set");
		}
		if (!properties.containsKey(LDAP_BASE)) {
			throw new BlowoutException("Required property " + LDAP_BASE + " was not set");
		}
		if (!properties.containsKey(LDAP_ENCRYPT_TYPE)) {
			throw new BlowoutException("Required property " + LDAP_ENCRYPT_TYPE + " was not set");
		}
		if (!properties.containsKey(LDAP_PRIVATE_KEY)) {
			throw new BlowoutException("Required property " + LDAP_PRIVATE_KEY + " was not set");
		}
		if (!properties.containsKey(LDAP_PUBLIC_KEY)) {
			throw new BlowoutException("Required property " + LDAP_PUBLIC_KEY + " was not set");
		}
	}
}
