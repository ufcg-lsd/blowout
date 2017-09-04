# Blowout

## What is Blowout?

//Talk about Blowout usage in a general way or directed to fogbow middleware?

//Contextualize the Infrastructure Provider

//Reference? Tasks or Jobs?

//Others configurations (figure out what is it!!!)

//Change the name BlowoutController to Blowout.

//LOCAL_COMMAND_INTERPRETER is required? Because apparently is optional, but doesn't have any default value

//DEFAULT VALUE FOR IDLE_LIFE_TIME: 0???

//Query: Some Blowout properties checks are in Arrebol... Example: INFRA_PROVIDER_CLASS_NAME

Blowout is a tool for receiving job submission, monitoring requests and interacting with the [Fogbow Middleware](http://www.fogbowcloud.org/) to execute the received jobs in the federated cloud resources. Blowout abstracts away a complex distributed infrastructure and allows the user to focus on the application requirements.

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
To get the lastest stable version of Blowout source code, download it from our repository:

    wget https://github.com/fogbow/blowout/archive/master.zip

Then, decompress it:

    unzip master.zip

After unpacking Blowout source code, you can add and import Blowout to your federated cloud job submitter project.

## Configuring Blowout
[See](https://github.com/fogbow/arrebol/blob/master/sched.conf.example) an example of Blowout configuration file. The following properties show all possible Blowout configurations with a brief description of them. 

Change it and make your own Blowout configuration file.


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
	infra_initial_specs_file_path=
	infra_provider_class_name=FogbowInfrastructureProvider
	infra_specs_block_creating=true

Configuration Field | Description | Required
-------------------------- | -------------------- | ----
Infrastructure Elasticity | Tells whether the infrastructure will be elastic or not | **Yes**
Infrastructure Monitor Period | Periods of monitoring... (Complete it) | No (Default value: 30000)
Resource Connection Timeout | Timeout for an attempt to connect to a resource | **Yes**
Resource Idle Life Time | Time that the resource will be available after your leverage | No (Default value: 0)
Max Resourse Reuse | Maximum amount of use of the resource to execute jobs | No (Default value: 1)
Max Resource Connection Retry | Maximum amount of connections retry to a resource | No (Default value: 1)
Local Command Interpreter | The Resource Job Command Interpreter | **NO**
Infrastructure Initial Specifications File Path | Initial Specifications File Path of the Infrastructure | **NO**
Infrastructure Provider Class Name | Class name of the Infrastructure Provider | **NO**
Infrastructure Specifications Block Creating | ?? | No **(Never used in any code)**


### Fogbow Infrastructure Constants
	infra_fogbow_manager_base_url=

Configuration Field | Description | Required
-------------------------- | -------------------- | ------
Infrastructure Fogbow Manager Base URL | Infrastructure Provider Fogbow Manager Base URL | **NO** (but in the code is)


### Database Constants
	blowout_datastore_url=blowoutdb.db
	blowout_rest_server_port=

Configuration Field | Description | Required
-------------------------- | -------------------- | ------
Blowout Datastore Url | Blowout resource Database URL | **NO** (but in the code is)
Blowout Rest Server Port | ?? | No **(Never used in any code)**


### Token Properties
	token_update_time=2
	token_update_time_unit=H

Configuration Field | Description | Required
-------------------------- | -------------------- | -------
Token Update Time | ?? | No (Default value: 6)
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
LDAP Authentication URL |	?? | **Yes**
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


### Application Headers
	X-auth-nonce=
	X-auth-username=
	X-auth-hash=

Configuration Field | Description | Required
-------------------------- | -------------------- | -----
X Authentication Nonce | ?? | No
X Authentication Username | ?? | No
X Authentication Hash | ?? | No


After set your own Blowout configuration file, use Blowout with it.


## Using Blowout
After downloading and setting up the Blowout you can import and add Blowout into your federated cloud job submitter project. The following example illustrates the Blowout usage.

	import org.fogbowcloud.blowout.core.BlowoutController;

	public class JobSubmitter {

		public JobSubmitter(File blowoutConf) throws Exception {
			Properties properties = new Properties();
			properties.load(new FileInputStream(blowoutConf));
			
			boolean removePreviousResources = true;

			BlowoutController blowout = new BlowoutController(properties);

			blowout.start(removePreviousResources);

			blowout.stop();
		}
	}


### Submitting Tasks
O Blowout trata um job como uma entidade Task, onde nela estao inseridos uma serie de comandos que devem ser executados na federated cloud resource e as especificacoes dos recursos necessarios para executa-la. Para conhecer mais sobre a entidade Task do Blowout [veja](http://arrebol.lsd.ufcg.edu.br/use-it.html)

Depois de iniciar a execucao do Blowout, voce pode comecar a submeter suas Tasks. O exemplo abaixo ilustra como submeter Tasks para o Blowout

	blowout.start(removePreviousResources);

	Task task = new TaskImpl(id, specification, uuid);

	blowout.addTask(task);

Eh possivel submeter uma Lista de Task, veja o exemplo:

	blowout.start(removePreviousResources);

	Task task = new TaskImpl(id, specification, uuid);

	List<Task>taskList = new ArrayList<Task>();
	
	taskList.add(task);
	
	blowout.addTaskList(taskList);

#### Removing a Task

	blowout.start(removePreviousResources);

	Task task = new TaskImpl(id, specification, uuid);

	blowout.addTask(task);

	blowout.cleanTask(task);


### Monitoring Tasks
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
