package org.fogbowcloud.blowout.infrastructure.plugin;

import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fogbowcloud.manager.core.plugins.identity.ldap.LdapIdentityPlugin;
import org.fogbowcloud.manager.occi.model.Token;

public class LDAPTokenUpdatePlugin extends AbstractTokenUpdatePlugin{

	private static final Logger LOGGER = Logger.getLogger(LDAPTokenUpdatePlugin.class);
	private static final String FOGBOW_LDAP_USERNAME = "fogbow.ldap.username";
	private static final String FOGBOW_LDAP_PASSWORD = "fogbow.ldap.password";
	private static final String FOGBOW_LDAP_AUTH_URL = "fogbow.ldap.auth.url";
	private static final String FOGBOW_LDAP_BASE = "fogbow.ldap.base";
	private static final String FOGBOW_LDAP_ENCRYPT_TYPE = "fogbow.ldap.encrypt.type";
	private static final String FOGBOW_LDAP_PRIVATE_KEY = "fogbow.ldap.private.key";
	private static final String FOGBOW_LDAP_PUBLIC_KEY = "fogbow.ldap.public.key";
	
	private String userName;
	private String password;
	private String ldapUrl;
	private String ldapBase;
	private String encrypt;
	private String privateKeyPath;
	private String publicKeyPath;
	
	public LDAPTokenUpdatePlugin(Properties properties) {
		super(properties);
		
		userName = properties.getProperty(FOGBOW_LDAP_USERNAME);
		password = properties.getProperty(FOGBOW_LDAP_PASSWORD);
		ldapUrl = properties.getProperty(FOGBOW_LDAP_AUTH_URL);
		ldapBase = properties.getProperty(FOGBOW_LDAP_BASE);
		encrypt = properties.getProperty(FOGBOW_LDAP_ENCRYPT_TYPE);
		privateKeyPath = properties.getProperty(FOGBOW_LDAP_PRIVATE_KEY);
		publicKeyPath = properties.getProperty(FOGBOW_LDAP_PUBLIC_KEY);
		
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
		
		LOGGER.debug("Creating token update with USERNAME="
				+ userName + " and PASSWORD="
				+ password);
		
		Token token = ldapIdentityPlugin.createToken(credentials);
		LOGGER.debug("LDAP token updated. New token is " + token.toString());

		return token;
	}

	

}
