## Customizing Blowout Components
To ensure the correct Blowout operation with new customized parts, a series of interfaces have been made available, ensuring the existence of all the necessary methods in each class that can be customized.

### Blowout Pool
To customize Blowout Pool is necessary that your new BlowoutPool class implements the interface BlowoutPool.

After that, you can set the following property in the Blowout configuration file with the class name of your new Blowout Pool:

	impl_blowout_pool_class_name="my_new_blowout_pool_class_name"

### Scheduler
To customize Scheduler is necessary that your new Scheduler class implements the interface SchedulerInterface.

After that, you can set the following property in the Blowout configuration file with the class name of your new Scheduler:

	impl_scheduler_class_name="my_new_scheduler_class_name"

### Infrastructure Manager
To customize Infrastructure Manager is necessary that your new InfrastructureManager class implements the interface InfraManager.

After that, you can set the following property in the Blowout configuration file with the class name of your new Infrastructure Manager:

	impl_infra_manager_class_name="my_new_infra_manager_class_name"

### Infrastructure Provider
To customize Infrastructure Provider is necessary that your new InfrastructureProvider class implements the interface InfrastructureProvider.

After that, you can set the following property in the Blowout configuration file with the class name of your new Infrastructure Provider:

	infra_provider_class_name="my_new_infra_provider_class_name"

To know more about what should be implemented in each Component method, see the source code.
