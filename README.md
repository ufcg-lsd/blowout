# Blowout

## What is Blowout?
Blowout is a tool for receiving job submission, monitoring requests and interacting with the [Fogbow Middleware](http://www.fogbowcloud.org/) to execute the jobs in the federated cloud resources. Blowout abstracts away a complex distributed infra-structure and allows the user to focus on the application requirements.

An example of Blowout job submitter is the [Arrebol](http://arrebol.lsd.ufcg.edu.br/)

The main features of the Blowout are:
- feature 1 -- receive jobs submissions and request resources from the federated cloud for these jobs.
- feature 2 -- associate to a particular job a resource that match with the job requeriments and request the execution of these job in the resource.
- feature 3 -- monitor the execution of the job in the associated resource.

## Blowout architecture
Blowout works like a scheduler of jobs to the computational resources dispersed among the cloud that are managed by the fogbow middleware.

Blowout has six main components:

- BlowoutPool: responsável por gerenciar uma pool de tasks e de resources onde ficam armazenados as tasks e resources, respectivamente. É por meio da pool que os componentes do Blowout tem acesso às tasks e resources armazenados.

- Scheduler: responsável por associar e desassociar uma task, que está pronta para ser executada, a um resource que está disponível. Após associar ou desassociar um resource para uma task o Scheduler submete para o Task Monitor a tarefa de criar ou encerrar um processo de execução da task no resource.

Desassociar (ou quando a task não está mais disponível ou quando o resource não está mais disponível).

- Task Monitor: responsável por criar e encerrar um processo para uma task que está pronta para ser executada, além disso, monitora a execução das tasks que estão em estado de running na federated cloud resource.

- Resource Monitor: responsável por realizar a requisição da alocação dos recursos pendentes ao Infrastructure Provider e monitorar os estados dos recursos que já estão alocados, gerenciando a disponibilidade desses recursos na pool de resources.

- Infrastructure Manager: responsável por pedir e adicionar recursos pendentes com base na demanda das tasks que ainda não foram executadas e na não disponibilidade dos resources já existentes.

- Infrastructure Provider: é quem conversa com o provedor de recursos físicos, sendo responsável por executar os pedidos dos recursos na federated cloud e disponibilizá-los na BlowoutPool.


See the following topics to understand the Blowout **architecture**, how to **deploy and configure it**, and finally, how to use it to **execute** jobs.

The fogbow middleware aggregates and manages the computational resources dispersed among the cloud. In this section, we overview of the SEBAL-scheduler components used to execute its workflow and the interaction with the fogbow middleware.

## instalation
- doc it
 
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
- Resource
- Task
- TaskProcess
- AbstractTokenUpdatePlugin