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

- **BlowoutPool**: responsible for managing a pool of tasks and resources. It is through the Blowoutpool that the components have access to the received tasks and resources that were raised by the Infrastructure Provider.

- **Scheduler**: responsible for associating and disassociating a ready to be executed task to a resource that is available. After associating or disassociating a resource to a task, the Scheduler delegates to Task Monitor the assignment of create or terminate the task execution process in the resource.

- **Infrastructure Manager**: responsible for request and add pending resources based on the demand of tasks that have not yet been executed, the availability and specifications of existing resources.

- **Infrastructure Provider**: is the one who interacts with the physical resource provider, being responsible for executing the resource requests and making them available in the BlowoutPool.

- **Resource Monitor**: responsible for requesting the allocation of pending resources to the Infrastructure Provider and monitoring the status of resources that are already allocated, managing the availability of these resources in the pool.

- **Task Monitor**: responsible for creating and closing a process for a task that is ready to be executed, in addition, monitors the execution of tasks that are in running state.

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

