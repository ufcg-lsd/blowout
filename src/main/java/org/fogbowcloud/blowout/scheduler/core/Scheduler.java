package org.fogbowcloud.blowout.scheduler.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.infrastructure.ResourceNotifier;
import org.fogbowcloud.blowout.scheduler.core.model.Job;
import org.fogbowcloud.blowout.scheduler.core.model.Job.TaskState;
import org.fogbowcloud.blowout.scheduler.core.model.Resource;
import org.fogbowcloud.blowout.scheduler.core.model.Specification;
import org.fogbowcloud.blowout.scheduler.core.model.Task;
import org.fogbowcloud.blowout.scheduler.core.model.TaskProcess;
import org.fogbowcloud.blowout.scheduler.core.model.TaskProcessImpl;
import org.fogbowcloud.blowout.scheduler.infrastructure.InfrastructureManager;

public class Scheduler implements Runnable, ResourceNotifier {

	private final String id;
	private ArrayList<Job> jobList = new ArrayList<Job>();
	private InfrastructureManager infraManager;
	private Map<String, Resource> runningTasks = new HashMap<String, Resource>();
	private ExecutorService taskExecutor = Executors.newCachedThreadPool();
	private List<TaskProcess> runningProcesses = new ArrayList<TaskProcess>();

	private List<TaskProcess> processQueue = new ArrayList<TaskProcess>();

	private Map<TaskProcess, Task> allProcesses = new HashMap<TaskProcess, Task>();

	private static final Logger LOGGER = Logger.getLogger(Scheduler.class);

	public Scheduler(InfrastructureManager infraManager, Job... jobs) {
		for (Job aJob : jobs) {
			aJob.restart();
			jobList.add(aJob);
		}
		this.infraManager = infraManager;
		this.id = UUID.randomUUID().toString();

	}

	protected Scheduler(InfrastructureManager infraManager, ExecutorService taskExecutor, Job... jobs) {
		this(infraManager, jobs);
		this.taskExecutor = taskExecutor;
	}

	@Override
	public void run() {
		LOGGER.info("Running scheduler...");
		Map<Specification, Integer> specDemand = new HashMap<Specification, Integer>();

		for (Job job : jobList) {
			generateProcessForJob(job);
		}
		//FIXME: avoid duplicated log lines (when they are related)
		LOGGER.debug("There are " + this.processQueue.size() + " ready tasks.");
		LOGGER.debug("Scheduler running tasks is " + runningTasks.size());

		for (TaskProcess taskProcess : this.processQueue) {
			Specification taskSpec = taskProcess.getSpecification();
			if (!specDemand.containsKey(taskSpec)) {
				specDemand.put(taskSpec, 0);
			}

			if (specDemand.get(taskSpec) < this.processQueue.size()) {
				int currentDemand = specDemand.get(taskSpec);
				specDemand.put(taskSpec, ++currentDemand);
				LOGGER.debug("Current Demand is: " + currentDemand);
			}
		}

		LOGGER.debug("Current job demand is " + specDemand);
		for (Specification spec : specDemand.keySet()) {
			infraManager.orderResource(spec, this, specDemand.get(spec));
		}
	}

	protected void generateProcessForJob(Job job) {
		if (!job.isCreated()) {
			for (Task task : job.getTasks().values()) {
				if (!task.isFinished()) {
					TaskProcess tp = createTaskProcess(task, job.getUUID());
					this.processQueue.add(tp);
					this.allProcesses.put(tp, task);
					task.addProcessId(tp.getProcessId());
				}
			}
			job.setCreated();
		}
	}

	@Override
	public void resourceReady(final Resource resource) {
		LOGGER.debug("Receiving resource ready [ID:" + resource.getId() + "]");
		for (final TaskProcess taskProcess : this.processQueue) {
			if (resource.match(taskProcess.getSpecification())) {

				LOGGER.debug("Relating resource [ID:" + resource.getId() + "] with task [ID:" + taskProcess.getTaskId()
				+ "]");
				runningTasks.put(taskProcess.getTaskId(), resource);
				processQueue.remove(taskProcess);
				runningProcesses.add(taskProcess);
				taskExecutor.submit(new Runnable() {
					@Override
					public void run() {
						try {
							taskProcess.executeTask(resource);
						} catch (Throwable e) {
							LOGGER.error("Error while executing task.", e);
						}
					}
				});
				return;
			}
		}

		infraManager.releaseResource(resource);
	}

	public void taskProcessFailed(TaskProcess taskProcess) {
		LOGGER.debug("Task " + taskProcess.getTaskId() + " failed and will be cloned");
		Job job = getJobOfFailedTask(taskProcess);
		if (job != null) {
			Task task = allProcesses.get(taskProcess);
			TaskProcess tp = createTaskProcess(task, job.getUUID());
			task.addProcessId(tp.getProcessId());
			allProcesses.put(tp, task);
			processQueue.add(tp);
		} else {
			LOGGER.error("Task was from a non-existing or removed Job");
		}
		if (runningTasks.containsKey(taskProcess.getTaskId())) {
			infraManager.releaseResource(runningTasks.get(taskProcess.getTaskId()));
			runningTasks.remove(taskProcess.getTaskId());
		}
		runningProcesses.remove(taskProcess);

	}

	protected TaskProcess createTaskProcess(Task task, String UUID) {
		TaskProcess tp = new TaskProcessImpl(task.getId(), task.getAllCommands(), task.getSpecification(),
				this.infraManager.getLocalInterpreter(),  this.infraManager.getToken().getUser());
		return tp;
	}

	private Job getJobOfFailedTask(TaskProcess taskProcess) {
		for (Job job : jobList) {
			if (job.getTasks().containsKey(taskProcess.getTaskId())) {
				LOGGER.debug("Failed task " + taskProcess.getTaskId() + " is from job " + job.getId());
				return job;
			}
		}
		return null;
	}

	public Resource getAssociateResource(Task task) {
		return runningTasks.get(task.getId());
	}

	protected Map<String, Resource> getRunningTasks() {
		return runningTasks;
	}

	protected String getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Scheduler other = (Scheduler) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public void addJob(Job job) {
		this.jobList.add(job);
	}

	public ArrayList<Job> getJobs() {
		return this.jobList;
	}

	public Job getJobById(String jobId) {
		if (jobId == null) {
			return null;
		}
		for (Job job : this.jobList) {
			if (jobId.equals(job.getId())) {
				return job;
			}
		}
		return null;
	}

	public Job removeJob(String jobId) {
		Job toBeRemoved = getJobById(jobId);

		this.jobList.remove(toBeRemoved);
		for (Task task : toBeRemoved.getTasks().values()) {
			removeProcessesFromTask(task);
		}
		return toBeRemoved;
	}

	private void removeProcessesFromTask(Task task) {
		List<TaskProcess> toRemove = new ArrayList<TaskProcess>();

		for (TaskProcess tp : getAllProcs()) {
			if (tp.getTaskId().equals(task.getId())) {
				taskProcessFailed(tp);
				toRemove.add(tp);
			}
		}
		for (TaskProcess procDeleted : toRemove) {
			this.allProcesses.remove(procDeleted);
		}

	}

	public List<TaskProcess> getAllProcs() {
		List<TaskProcess> procList = new ArrayList<TaskProcess>();
		procList.addAll(this.allProcesses.keySet());
		return procList;

	}

	public List<TaskProcess> getRunningProcs() {
		List<TaskProcess> procList = new ArrayList<TaskProcess>();
		procList.addAll(this.runningProcesses);
		return procList;

	}

	public void taskFailed(TaskProcess tp) {
		taskProcessFailed(tp);
	}

	public void taskCompleted(TaskProcess tp) {
		LOGGER.info("Task " + tp.getTaskId() + " was completed.");
		infraManager.releaseResource(runningTasks.get(tp.getTaskId()));
		runningTasks.remove(tp.getTaskId());
		runningProcesses.remove(tp);
	}

	//FIXME: not a good name, infer is kind of a reserved word in CS
	public TaskState inferTaskState(Task task) {
		List<TaskProcess> tpList = getProcessFromTask(task);
		for (TaskProcess tp : tpList) {
			if (tp.getStatus().equals(TaskProcessImpl.State.READY)) {
				return TaskState.READY;
			}
			if (tp.getStatus().equals(TaskProcessImpl.State.RUNNING)) {
				return TaskState.RUNNING;
			}
			if (tp.getStatus().equals(TaskProcessImpl.State.FINNISHED)) {
				return TaskState.COMPLETED;
			}

		}
		return TaskState.FAILED;
	}

	protected List<TaskProcess> getProcessFromTask(Task task) {
		List<TaskProcess> tpList = new ArrayList<TaskProcess>();
		for (String tpId : task.getProcessId()) {
			for (TaskProcess tp : getAllProcs()) {
				if (tp.getProcessId().equals(tpId)) {
					tpList.add(tp);
					break;
				}
			}
		}
		return tpList;
	}

	public Task getTaskFromTaskProcess(TaskProcess tp) {
		return this.allProcesses.get(tp);
	}
}
