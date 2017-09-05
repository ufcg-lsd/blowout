## Configuring Blowout
[See](https://github.com/fogbow/arrebol/blob/master/sched.conf.example) an example of Blowout configuration file, change it and make your own Blowout configuration file.

The following properties show all possible Blowout configurations with a brief description of them. 


### Implementation Plugins
	impl_blowout_pool_class_name=org.fogbowcloud.blowout.pool.DefaultBlowoutPool
	impl_scheduler_class_name=org.fogbowcloud.blowout.infrastructure.manager.DefaultInfrastructureManager
	impl_infra_manager_class_name=org.fogbowcloud.blowout.core.StandardScheduler
	infra_provider_class_name=org.fogbowcloud.blowout.scheduler.infrastructure.fogbow.FogbowInfrastructureProvider

Configuration Field | Description | Required (Default values in the example)
-------------------------- | -------------------- | --------
Blowout Pool Class Name | The Blowout Poll **Implementation** class package path | No
Scheduler Class Name | The Scheduler **Implementation** class package path | No
Infrastructure Manager Class Name | The Infrastructure Manager **Implementation** class package path | No
Infrastructure Provider Class Name | The Infrastructure Provider **Implementation** class package path | No


### Infrastructure Constants
	infra_is_elastic=true
	infra_monitor_period=30000
	infra_resource_connection_timeout=20000
	infra_resource_idle_lifetime=120000
	max_resource_reuse=4
	max_resource_connection_retry=4
	local_command_interpreter=/bin/bash

Configuration Field | Description | Required
-------------------------- | -------------------- | ----
Infrastructure Elasticity | Tells whether the infrastructure will be elastic or not | **Yes**
Infrastructure Monitor Period | Periods of resources monitoring | No (Default value: 30000)
Resource Connection Timeout | Timeout for an attempt to connect to a resource | **Yes**
Resource Idle Life Time | Time that the resource will be available after your leverage | No (Default value: 0)
Max Resourse Reuse | Maximum amount of use of the resource to execute tasks | No (Default value: 1)
Max Resource Connection Retry | Maximum amount of connections retry to a resource | No (Default value: 1)
Local Command Interpreter | The Resource Task Command Interpreter | **Yes**


### Fogbow Infrastructure Constant
	infra_fogbow_manager_base_url=

Configuration Field | Description | Required
-------------------------- | -------------------- | ------
Infrastructure Fogbow Manager Base URL | URL to Fogbow Manager | **Yes**


### Database Constant
	blowout_datastore_url=blowoutdb.db

Configuration Field | Description | Required
-------------------------- | -------------------- | ------
Blowout Datastore Url | Blowout resource database URL | **Yes**


### Authentication Token Properties

#### General Authentication Token Properties
	token_update_time=2
	token_update_time_unit=H

Configuration Field | Description | Required
-------------------------- | -------------------- | -------
Token Update Time | Period of time to update the authentication token | No (Default value: 6)
Token Update Time Unit | Time Unit of Token Update Time, use (**H** for hours, **M** for minutes, **S** for seconds and **MS** for miliseconds) | No (Default value: H)


#### Authentication Token Properties - Case LDAP
	infra_auth_token_update_plugin=org.fogbowcloud.blowout.infrastructure.token.LDAPTokenUpdatePlugin
	auth_token_prop_ldap_username=
	auth_token_prop_ldap_password=
	auth_token_prop_ldap_auth_url=
	auth_token_prop_ldap_base=
	auth_token_prop_ldap_encrypt_type=
	auth_token_prop_ldap_private_key=
	auth_token_prop_ldap_public_key=

Configuration Field | Description | Required
-------------------------- | -------------------- | -------
LDAP Infrastructure Token Update Plugin |	?? | **Yes**
LDAP Username |	?? | **Yes**
LDAP Password |	?? | **Yes**
LDAP Authentication URL | ?? | **Yes**
LDAP Base |	?? | **Yes**
LDAP Encrypt Type |	?? | **Yes**
LDAP Private Key | ?? | **Yes**
LDAP Public Key |	?? | **Yes**


#### Authentication Token Properties - Case Keystone
	infra_auth_token_update_plugin=org.fogbowcloud.blowout.infrastructure.token.KeystoneTokenUpdatePlugin
	auth_token_prop_keystone_username=
	auth_token_prop_keystone_tenantname=
	auth_token_prop_keystone_password=
	auth_token_prop_keystone_auth_url=

Configuration Field | Description | Required
-------------------------- | -------------------- | -------
Keystone Infrastructure Token Update Plugin |	?? | **Yes**
Keystone Username	|	?? | **Yes**
Keystone Tenantname	|	?? | **Yes**
Keystone Password	|	?? | **Yes**
Keystone Authentication URL	|	?? | **Yes**


#### Authentication Token Properties - Case NAF
	infra_auth_token_update_plugin=org.fogbowcloud.blowout.infrastructure.token.KeystoneTokenUpdatePlugin
	auth_token_prop_naf_identity_private_key=
	auth_token_prop_naf_identity_public_key=
	auth_token_prop_naf_identity_token_username=
	auth_token_prop_naf_identity_token_password=
	auth_token_prop_naf_identity_token_generator_endpoint=

Configuration Field | Description | Required
-------------------------- | -------------------- | -------
NAF Infrastructure Token Update Plugin |	?? | **Yes**
NAF Identity Private Key |	?? | **Yes**
NAF Identity Public Key	|	?? | **Yes**
NAF Identity Token Username	|	?? | **Yes**
NAF Identity Token Password	|	?? | **Yes**
NAF Identity Token Generator Endpoint	|	?? | **Yes**


#### Authentication Token Properties - Case VOMS
	infra_auth_token_update_plugin=org.fogbowcloud.blowout.infrastructure.token.KeystoneTokenUpdatePlugin
	auth_token_prop_voms_certificate_file_path
	auth_token_prop_voms_certificate_password=
	auth_token_prop_voms_server=

Configuration Field | Description | Required
-------------------------- | -------------------- | -------
VOMS Infrastructure Token Update Plugin	|	?? | **Yes**
VOMS Cerfiticate File Path | ?? | **Yes**
VOMS Cerfiticate Password	|	?? | **Yes**
VOMS Cerfiticate Server	|	?? | **Yes**


After set your own Blowout configuration file, use Blowout with it.
