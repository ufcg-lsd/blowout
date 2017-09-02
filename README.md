# Blowout

## What is Blowout?
Blowout is a tool for receiving job submission, monitoring requests and interacting with the [Fogbow Middleware](http://www.fogbowcloud.org/) to execute the jobs in the federated cloud resources. Blowout abstracts away a complex distributed infra-structure and allows the user to focus on the application requirements.

An example of Blowout job submitter is [Arrebol](http://arrebol.lsd.ufcg.edu.br/).

The main Blowout features are:
- feature 1 -- receive jobs submissions and request resources from the federated cloud for these jobs.
- feature 2 -- associate to a particular job a resource that match with the job requeriments and request the execution of these job in the resource.
- feature 3 -- monitor the execution of the job in the associated resource.

See the following topics to understand the Blowout **architecture**, how to **deploy and configure it**, and finally, how to use it to **execute** jobs.

## Blowout Architecture
Blowout works like a scheduler of jobs to the computational resources dispersed among the cloud that are managed by the fogbow middleware.

Blowout has six main components:

- BlowoutPool: responsável por gerenciar uma pool de tasks e de resources onde ficam armazenados as tasks e resources, respectivamente. É por meio da pool que os componentes do Blowout tem acesso às tasks e resources armazenados.

- Scheduler: responsável por associar e desassociar uma task, que está pronta para ser executada, a um resource que está disponível. Após associar ou desassociar um resource para uma task o Scheduler submete para o Task Monitor a tarefa de criar ou encerrar um processo de execução da task no resource.

- Task Monitor: responsável por criar e encerrar um processo para uma task que está pronta para ser executada, além disso, monitora a execução das tasks que estão em estado de running na federated cloud resource.

- Resource Monitor: responsável por realizar a requisição da alocação dos recursos pendentes ao Infrastructure Provider e monitorar os estados dos recursos que já estão alocados, gerenciando a disponibilidade desses recursos na pool de resources.

- Infrastructure Manager: responsável por pedir e adicionar recursos pendentes com base na demanda das tasks que ainda não foram executadas e na não disponibilidade dos resources já existentes.

- Infrastructure Provider: é quem conversa com o provedor de recursos físicos, sendo responsável por executar os pedidos dos recursos na federated cloud e disponibilizá-los na BlowoutPool.

## Installation
To get the lastest stable version of the Arrebol source code, download it from our repository:

    wget https://github.com/fogbow/blowout/archive/master.zip

Then, decompress it:

    unzip master.zip

After unpacking Blowout source code, you can import Blowout to your job submitter and use it.

## How to configure Blowout?
This is a sample of Blowout configuration. Change it to use your own configuration values.

		infra_is_elastic=true
		infra_provider_class_name=org.fogbowcloud.blowout.scheduler.infrastructure.fogbow.FogbowInfrastructureProvider
		infra_order_service_time=100000
		infra_resource_service_time=100000
		infra_resource_connection_timeout=300000
		infra_resource_idle_lifetime=30000
			
		infra_initial_specs_block_creating=true
		infra_initial_specs_remove_previous_resources=true
			
		infra_fogbow_manager_base_url=https://10.11.4.234:8183
		infra_fogbow_token_public_key_filepath=/tmp/x509up_u1350
		infra_fogbow_token_update_plugin=org.fogbowcloud.blowout.infrastructure.plugin.NAFTokenUpdatePlugin
		naf_identity_private_key=/local/keylocation/NAF/private_key.pem
		naf_identity_public_key=/local/keylocation/NAF/public_key.pem
		naf_identity_token_username=fogbow
		naf_identity_token_password=password
		naf_identity_token_generator_endpoint=http://localhost:8080
		token_update.time=2
		token_update.time.unit=H

		accounting_datastore_url=jdbc:h2:/tmp/sebalsched.orders
		execution_monitor_period=60000
		local_output=/tmp/arrebol
		local_command_interpreter=/bin/bash

		rest_server_port=44444

		public_key=ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAACAQDG2U8rz4I31LIyDBPpe01WJdGt0JBowZ0Zq7Nxq7mol3G4cW5OJt9v3aQLRU8zanceXXSagNg8O4v2ppFzROYlIOgg2KN3Zu6Tj7Evmfp++J160dwshnP3aQCSLIDSBnMsZyPRprIbaL2LifVmrKcOfG3QcRQHZx2HRWJp+lty0IqP+FBaobB7nXzF58ibOJ84Fk9QpQmS5JK3AXdwCISmN8bgfcjoUJB2FMB5OU8ilkIyG4HDZmI82z+6hUS2sVd/ss8biIN6qGfRVxEDhVlDw3o+XqL+HQ7udd2Q61oHs8iBa711SWG64Eie6HAm8SIOsL7dvPx1rBfBsp3Dq3gjnIpTZqwluiTE8q9S6rTiDQndCGWvAnSU01BePD51ZnMEckluYTOhNLgCMtNTXZJgYSHPVsLWXa5xdGSffL73a4gIupE36tnZlNyiAQGDJUrWh+ygEc2ALdQfpOVWo+CMkTBswvrHYSJdFC7r1U8ACrOlsLE02/uqqBbp7fTUuuMk77J8t0ocxuz48tVKOlog0ajS5nphPLfPGnP2PVTh7GXNTLOnqGVwMrjFIAHj7ukd+l36wUAIHR7Y4YWKVaIBvTZS/fQNn0cOGon2DnNL3wNAUc6pthhXlNY33aU2ky55mZR4drAdbRGRdEZQF0YHEFnzP0x2GucHwg6ZtMJ2Aw== igorvcs@bobo
		fogbow_username=fogbow
		private_key_filepath=/local/keylocation/.ssh/id_rsa
		remote_output_folder=/tmp

## use it
- 
 
## infra
- doc infra script
- doc spec files
 
## task bootstrap
- doc script
- doc input file

## monitor
- doc how to check scheduler, fetcher, crawler statuses

## Submitting Tasks

## DEPLOY
put the classes in the file of Properties (blowout..conf.example) see how arrebol do it.

- BlowoutPool
- SchedulerInterface
- InfrastructureManager
- InfrastructureProvider