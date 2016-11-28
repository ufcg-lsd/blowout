package org.fogbowcloud.blowout.core.util;

public class AppPropertiesConstants {
	
	//-------------------IMPLEMENTATION PLUGINS----------------------------//
	public static final String IMPLEMENTATION_INFRA_MANAGER = "impl_infra_manager_class_name";
	public static final String IMPLEMENTATION_SCHEDULER = "impl_scheduler_class_name";
	public static final String IMPLEMENTATION_BLOWOUT_POOL = "impl_blowout_pool_class_name";
	public static final String IMPLEMENTATION_INFRA_PROVIDER = "infra_provider_class_name";
	public static final String BLOWOUT_CONFIG_FILE = "blowout.configuration";
	public static final String DEFAULT_BLOWOUT_CONFIG_FILE = "blowout.conf";
	

	// __________ INFRASTRUCTURE CONSTANTS __________ //
	public static final String INFRA_IS_STATIC = "infra_is_elastic";
	public static final String INFRA_RESOURCE_CONNECTION_TIMEOUT = "infra_resource_connection_timeout";
	public static final String INFRA_RESOURCE_IDLE_LIFETIME = "infra_resource_idle_lifetime";
	public static final String INFRA_RESOURCE_REUSE_TIMES = "max_resource_reuse";
	public static final String INFRA_RESOURCE_CONNECTION_RETRY = "max_resource_connection_retry";
	public static final String INFRA_MONITOR_PERIOD = "infra_monitor_period";
	public static final String LOCAL_COMMAND_INTERPRETER = "local_command_interpreter";
	public static final String INFRA_AUTH_TOKEN_PREFIX = "auth_token_prop_";
	public static final String INFRA_AUTH_TOKEN_UPDATE_PLUGIN = "infra_auth_token_update_plugin";
	

	// __________ FOGBOW INFRASTRUCTURE CONSTANTS __________ //
	public static final String INFRA_FOGBOW_MANAGER_BASE_URL = "infra_fogbow_manager_base_url";
	//public static final String INFRA_FOGBOW_TOKEN_PUBLIC_KEY_FILEPATH = "infra_fogbow_token_public_key_filepath";

	//---------------TOKEN PROPERTIES ------------------//
	public static final String TOKEN_UPDATE_TIME = "token_update_time";
	public static final String TOKEN_UPDATE_TIME_UNIT = "token_update_time_unit";
	

	// ___________ DB CONSTANTS ______________//
	public static final String DB_DATASTORE_URL = "blowout_datastore_url";

	// ___________ APPLICATION HEADERS ____//

	public static final String X_AUTH_NONCE = "X-auth-nonce";
	public static final String X_AUTH_USER = "X-auth-username";
	public static final String X_AUTH_HASH = "X-auth-hash";

	
}
