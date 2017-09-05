# Blowout

## What is Blowout?
Blowout is a tool for receiving Bag-of-tasks (BoT) submissions, monitoring requests and interacting with a Infrastructure Provider to execute the received tasks in federated cloud resources. Blowout abstracts away a complex distributed infrastructure and allows the user to focus on the application requirements.

An example of Infrastructure Provider and Task Submitter for Blowout are, respectively, [Fogbow Middleware](http://www.fogbowcloud.org/) and [Arrebol](http://arrebol.lsd.ufcg.edu.br/).

The main Blowout features are:
- **Receive and Request**: receive tasks submissions and request resources from the federated cloud for these tasks.
- **Associate and Execute**: associate to a particular task a resource that match with the task requeriments and request the execution of these task in the resource.
- **Monitor**: monitor the task execution in the associated resource.

See the following topics to understand the Blowout **architecture**, how to **deploy and configure it**, and finally, how to use it to **execute** tasks.

## Blowout Architecture
Blowout works like a scheduler of tasks to computational resources dispersed among clouds that are managed by an Infrastructure Provider.

Blowout has six main components:

- **BlowoutPool**: responsável por gerenciar uma pool de tasks e de resources onde ficam armazenados as tasks e resources, respectivamente. É por meio da pool que os componentes do Blowout tem acesso as tasks e resources armazenados.

- **Scheduler**: responsável por associar e desassociar uma task, que está pronta para ser executada, a um resource que está disponível. Após associar ou desassociar um resource para uma task o Scheduler submete para o Task Monitor a tarefa de criar ou encerrar um processo de execução da task no resource.

- **Infrastructure Manager**: responsável por pedir e adicionar recursos pendentes com base na demanda das tasks que ainda não foram executadas e na não disponibilidade dos resources já existentes.

- **Infrastructure Provider**: é quem interage com o provedor de recursos físicos, sendo responsável por executar os pedidos dos recursos na federated cloud e disponibilizá-los na BlowoutPool.

- **Resource Monitor**: responsável por realizar a requisição da alocação dos recursos pendentes ao Infrastructure Provider e monitorar os estados dos recursos que já estão alocados, gerenciando a disponibilidade desses recursos na pool de resources.

- **Task Monitor**: responsável por criar e encerrar um processo para uma task que está pronta para ser executada, além disso, monitora a execução das tasks que estão em estado de running na federated cloud resource.

## Installation
Before Blowout installation is necessary to get a Blowout dependency, [Fogbow Manager](https://github.com/fogbow/fogbow-manager).

	wget https://github.com/fogbow/fogbow-manager/archive/master.zip

Then, decompress it:
	
	unzip master.zip

To get the lastest stable version of Blowout source code, download it from our repository:

    wget https://github.com/fogbow/blowout/archive/master.zip

Then, decompress it:

    unzip master.zip

After that, get all projects JAR of Fogbow Manager and Blowout. To achieve that, maven or maven2 must be installed in client's machine with commands:

	apt-get install maven

	apt-get install maven2

And then, simply use the following command in each project directory:
	
	mvn -e install -Dmaven.test.skip=true

After installation, you can add and import Blowout to your Task Submitter project.


## Configuring Blowout
[See](https://github.com/fogbow/arrebol/blob/master/sched.conf.example) an example of Blowout configuration file, change it and make your own Blowout configuration file. For more information see [Blowout Configurations](https://github.com/fogbow/blowout/blob/readme/CONF.md).

After set your Blowout configuration file, use Blowout with it.


## Using Blowout
After installation and configuration, you can import and add Blowout into your federated cloud Task Submitter project. The following example illustrates the Blowout usage.

	import org.fogbowcloud.blowout.core.BlowoutController;

	public class TaskSubmitter {

		public TaskSubmitter(File blowoutConf) throws Exception {
			
			Properties properties = new Properties();
			properties.load(new FileInputStream(blowoutConf));
			
			boolean removePreviousResources = true;

			BlowoutController blowout = new BlowoutController(properties);

			blowout.start(removePreviousResources);

			blowout.stop();
		}
	}


### Submitting Tasks
A task in the Blowout is modeled as a Task object. See [Job Description File](http://arrebol.lsd.ufcg.edu.br/use-it.html) and [Arrebol code](https://github.com/fogbow/arrebol/tree/master/src/main/java/org/fogbowcloud/app) to know how construct a Task object from a job description file.

The example below illustrates how to submit a Task to Blowout:

	public void submitTask(Task task) {
		
		blowout.addTask(task);
	}

Is possible to submit a list of tasks, see the example below:

	public void submitTaskList(List<Task> taskList) {
		
		blowout.addTaskList(taskList);
	}


#### Removing Task
Is possible to remove a submitted task:

	public void removeTask(Task task) {
		
		blowout.cleanTask(task);
	}

Two Task are equals when they have the same ID and Specifications.

### Monitoring Tasks
You can know the status of a Task that has been submitted to Blowout.

	blowout.addTask(task);

	TaskState taskState = blowout.getTaskState(task.getId());
	
	System.out.println(taskState.getDesc());

The possible states of a Task are presented in the table below.

Task State | Description 
----------- | -----------
Ready | The Task is ready to be executed
Running | The Task is running on the associated resource
Finished | The Task was finished with sucess
Completed | The Task was finished with sucess and was taken from the running tasks list
Not Created | The Task does not exist in Blowout
Timedout | The Task took timeout


## Customizing Blowout Components
You can adapt some Blowout components to operate with your application needs. For more information see [Customizing Blowout Components](https://github.com/fogbow/blowout/blob/readme/CUSTOM.md).

After customizations and configurations sets, you can use Blowout with your new Custom Components.

