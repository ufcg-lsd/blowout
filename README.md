# Blowout

## What is Blowout?

//Talk about Blowout usage in a general way or directed to fogbow middleware?

//Contextualize the Infrastructure Provider

//Reference? Tasks or Jobs?

//Others configurations (figure out what is it!!!)

//Infrastructure Monitor Period == Execution Monitor Period??

//infra_monitor_period == execution_monitor_period??

Blowout is a tool for receiving job submission, monitoring requests and interacting with the [Fogbow Middleware](http://www.fogbowcloud.org/) to execute the jobs in the federated cloud resources. Blowout abstracts away a complex distributed infrastructure and allows the user to focus on the application requirements.

An example of Blowout job submitter is [Arrebol](http://arrebol.lsd.ufcg.edu.br/).

The main Blowout features are:
- **Receive and Request**: receive jobs submissions and request resources from the federated cloud for these jobs.
- **Associate and Execute**: associate to a particular job a resource that match with the job requeriments and request the execution of these job in the resource.
- **Monitor**: monitor the job execution in the associated resource.

See the following topics to understand the Blowout **architecture**, how to **deploy and configure it**, and finally, how to use it to **execute** jobs.

## Blowout Architecture
Blowout works like a scheduler of jobs to the computational resources dispersed among the cloud that are managed by the fogbow middleware.

Blowout has six main components:

- **BlowoutPool**: responsável por gerenciar uma pool de tasks e de resources onde ficam armazenados as tasks e resources, respectivamente. É por meio da pool que os componentes do Blowout tem acesso às tasks e resources armazenados.

- **Scheduler**: responsável por associar e desassociar uma task, que está pronta para ser executada, a um resource que está disponível. Após associar ou desassociar um resource para uma task o Scheduler submete para o Task Monitor a tarefa de criar ou encerrar um processo de execução da task no resource.

- **Infrastructure Manager**: responsável por pedir e adicionar recursos pendentes com base na demanda das tasks que ainda não foram executadas e na não disponibilidade dos resources já existentes.

- **Infrastructure Provider**: é quem conversa com o provedor de recursos físicos, sendo responsável por executar os pedidos dos recursos na federated cloud e disponibilizá-los na BlowoutPool.

- **Resource Monitor**: responsável por realizar a requisição da alocação dos recursos pendentes ao Infrastructure Provider e monitorar os estados dos recursos que já estão alocados, gerenciando a disponibilidade desses recursos na pool de resources.

- **Task Monitor**: responsável por criar e encerrar um processo para uma task que está pronta para ser executada, além disso, monitora a execução das tasks que estão em estado de running na federated cloud resource.

## Installation
To get the lastest stable version of the Arrebol source code, download it from our repository:

    wget https://github.com/fogbow/blowout/archive/master.zip

Then, decompress it:

    unzip master.zip

After unpacking Blowout source code, you can import Blowout to your job submitter and use it.

## How to configure Blowout?
[See](https://github.com/fogbow/arrebol/blob/master/sched.conf.example) an example of Blowout configuration file. The following properties show all possible Blowout configurations with a brief description of them. Change it to use your own configuration values.


### Implementation Plugins
	infra_provider_class_name=org.fogbowcloud.blowout.scheduler.infrastructure.fogbow.FogbowInfrastructureProvider
	impl_infra_manager_class_name=org.fogbowcloud.blowout.core.StandardScheduler
	impl_scheduler_class_name=org.fogbowcloud.blowout.infrastructure.manager.DefaultInfrastructureManager
	impl_blowout_pool_class_name=org.fogbowcloud.blowout.pool.DefaultBlowoutPool

Configuration Field | Description
-------------------------- | --------------------
Infrastructure Provider Class Name | The Infrastructure Provider **Implementation** class package path 
Infrastructure Manager Class Name | The Infrastructure Manager **Implementation** class package path 
Scheduler Class Name | The Scheduler **Implementation** class package path
Blowout Pool Class Name | The Blowout Poll **Implementation** class package path


### Infrastructure Constants
	infra_is_elastic=true
	infra_initial_specs_block_creating=true
	infra_initial_specs_remove_previous_resources=true
	infra_order_service_time=100000
	infra_resource_service_time=100000
	infra_monitor_period=30000
	execution_monitor_period=60000
	infra_resource_connection_timeout=20000
	infra_resource_idle_lifetime=120000
	max_resource_reuse=4
	max_resource_connection_retry=4
	local_output=/tmp/arrebol
	local_command_interpreter=/bin/bash

Configuration Field | Description
-------------------------- | --------------------
Infrastructure Elasticity | Tells whether the infrastructure will be elastic or not
Infrastructure Initial Specification of Block Creating | ??
Infrastructure Initial Specification of Remove Previous Resource | Tells whether remove or not the previous resources already allocated before Blowout initiation
Infrastructure Order Service | ??
Infrastructure Resource Service Time | ??
Infrastructure Monitor Period | Periods of monitoring
Execution Monitor Period | Periods of monitoring
Resource Connection Timeout | Timeout for an attempt to connect to a resource
Resource Idle Life Time | Time that the resource will be available after your leverage
Max Resourse Reuse | Maximum amount of use of the resource to execute jobs
Max Resource Connection Retry | Maximum amount of connections retry to a resource
Local Output | ??
Local Command Interpreter | The Resource Job Command Interpreter


### Fogbow Infrastructure Constants
	infra_fogbow_manager_base_url=
	infra_fogbow_token_public_key_filepath=/tmp/x509up_u1350
	infra_fogbow_token_update_plugin=org.fogbowcloud.blowout.infrastructure.plugin.NAFTokenUpdatePlugin

Configuration Field | Description
-------------------------- | --------------------
Infrastructure Fogbow Manager Base URL | Infrastructure Provider Fogbow Manager Base URL
Infrastructure Fogbow Token Public Key File Path | ??
Infrastructure Fogbow Token Update Plugin | ??


### Database Constants
	blowout_datastore_url=blowoutdb.db
	blowout_rest_server_port=
	accounting_datastore_url=jdbc:h2:/tmp/sebalsched.orders

Configuration Field | Description
-------------------------- | --------------------
Blowout Datastore Url | Blowout resource Database URL
Blowout Rest Server Port | ??
Accounting Datastore URL | ??


### Token Properties
	token_update_time=2
	token_update_time_unit=h

Configuration Field | Description
-------------------------- | --------------------
Token Update Time | ??
Token Update Time Unit | ?? use (h - hours, m - minutes, s - seconds)


### Authentication Token Properties - LDAP
	infra_auth_token_update_plugin=org.fogbowcloud.blowout.infrastructure.token.LDAPTokenUpdatePlugin
	auth_token_prop_ldap_username=
	auth_token_prop_ldap_password=
	auth_token_prop_ldap_auth_url=
	auth_token_prop_ldap_base=
	auth_token_prop_ldap_encrypt_type=
	auth_token_prop_ldap_private_key=
	auth_token_prop_ldap_public_key=

Configuration Field | Description
-------------------------- | --------------------
LDAP Infrastructure Token Update Plugin | ??
LDAP Username | ??
LDAP Password | ??
LDAP Authentication URL | ??
LDAP Base | ??
LDAP Encrypt Type | ??
LDAP Private Key | ??
LDAP Public Key | ??


### Authentication Token Properties - Keystone
	infra_auth_token_update_plugin=org.fogbowcloud.blowout.infrastructure.token.KeystoneTokenUpdatePlugin
	auth_token_prop_keystone_username=
	auth_token_prop_keystone_tenantname=
	auth_token_prop_keystone_password=
	auth_token_prop_keystone_auth_url=

Configuration Field | Description
-------------------------- | --------------------
Keystone Infrastructure Token Update Plugin | ??
Keystone Username | ??
Keystone Tenantname | ??
Keystone Password | ??
Keystone Authentication URL | ??


### Authentication Token Properties - NAF
	infra_auth_token_update_plugin=org.fogbowcloud.blowout.infrastructure.token.KeystoneTokenUpdatePlugin
	auth_token_prop_naf_identity_private_key=
	auth_token_prop_naf_identity_public_key=
	auth_token_prop_naf_identity_token_username=
	auth_token_prop_naf_identity_token_password=
	auth_token_prop_naf_identity_token_generator_endpoint=

Configuration Field | Description
-------------------------- | --------------------
NAF Infrastructure Token Update Plugin | ??
NAF Identity Private Key | ??
NAF Identity Public Key | ??
NAF Identity Token Username | ??
NAF Identity Token Password | ??
NAF Identity Token Generator Endpoint | ??


### Authentication Token Properties - VOMS
	infra_auth_token_update_plugin=org.fogbowcloud.blowout.infrastructure.token.KeystoneTokenUpdatePlugin
	auth_token_prop_voms_certificate_file_path
	auth_token_prop_voms_certificate_password=
	auth_token_prop_voms_server=

Configuration Field | Description
-------------------------- | --------------------
VOMS Infrastructure Token Update Plugin | ??
VOMS Cerfiticate File Path | ??
VOMS Cerfiticate Password | ??
VOMS Cerfiticate Server | ??


### Others
	auth_token_prop_=
	X-auth-nonce=
	X-auth-username=
	X-auth-hash=
	rest_server_port=44444
	fogbow_username=fogbow
	private_key_filepath=/local/keylocation/.ssh/id_rsa
	remote_output_folder=/tmp
	public_key=ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDG2U8rz4I31LIyDBPpe01WJdGt0JBowZ0Zq7Nxq7mol3G4cW5OJt9v3aQLRU8zanceXXSagNg8O4v2ppFzROYlIOgg2KN3Zu6Tj7Evmfp++J160dwshnP3aQCSLIDSBnMsZyPRprIbaL2LifVmrKcOfG3QcRQHZx2HRWJp+lty0IqP+FBaobB7nXzF58ibOJ84Fk9QpQmS5JK3AXdwCISmN8bgfcjoUJB2FMB5OU8ilkIyG4HDZmI82z+6hUS2sVd/ss8biIN6qGfRVxEDhVlDw3o+XqL+HQ7udd2Q61oHs8iBa711SWG64Eie6HAm8SIOsL7dvPx1rBfBsp3Dq3gjnIpTZqwluiTE8q9S6rTiDQndCGWvAnSU01BePD51ZnMEckluYTOhNLgCMtNTXZJgYSHPVsLWXa5xdGSffL73a4gIupE36tnZlNyiAQGDJUrWh+ygEc2ALdQfpOVWo+CMkTBswvrHYSJdFC7r1U8ACrOlsLE02/uqqBbp7fTUuuMk77J8t0ocxuz48tVKOlog0ajS5nphPLfPGnP2PVTh7GXNTLOnqGVwMrjFIAHj7ukd+l36wUAIHR7Y4YWKVaIBvTZS/fQNn0cOGon2DnNL3wNAUc6pthhXlNY33aU2ky55mZR4drAdbRGRdEZQF0YHEFnzP0x2GucHwg6ZtMJ2Aw== igorvcs@bobo

Configuration Field | Description
-------------------------- | --------------------
Authentication Token Property | Infrastructure Authentication Token Prefix
X Authentication Nonce | ??
X Authentication Username | ??
X Authentication Hash | ??
Rest Server Port | ??
Private Key File Path | ??
Remove Output | ??
Public Key | ??


After setting your own Blowout configuration, save your file blowout.properties and use it.


## Using Blowout
- How to use blowout

### Submitting Tasks
- how submit tasks

### Monitor
- doc how to check scheduler, fetcher, crawler statuses


## Deploy
Implement Interfaces...

Put the classes in the file of Properties (blowout..conf.example) see how arrebol do it.

- BlowoutPool
- SchedulerInterface
- InfrastructureManager
- InfrastructureProvider


## infra
- doc infra script
- doc spec files
 
## task bootstrap
- doc script
- doc input file
