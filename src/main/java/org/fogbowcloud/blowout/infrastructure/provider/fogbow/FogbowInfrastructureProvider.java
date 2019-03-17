package org.fogbowcloud.blowout.infrastructure.provider.fogbow;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.http.entity.StringEntity;
import org.apache.log4j.Logger;
import org.fogbowcloud.blowout.core.constants.AppMessagesConstants;
import org.fogbowcloud.blowout.core.constants.BlowoutConstants;
import org.fogbowcloud.blowout.core.constants.FogbowConstants;
import org.fogbowcloud.blowout.core.exception.BlowoutException;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.core.constants.AppPropertiesConstants;

import org.fogbowcloud.blowout.database.FogbowResourceDatastore;
import org.fogbowcloud.blowout.infrastructure.exception.InfrastructureException;
import org.fogbowcloud.blowout.infrastructure.exception.RequestResourceException;
import org.fogbowcloud.blowout.infrastructure.http.HttpWrapper;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.infrastructure.model.Token;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.blowout.infrastructure.token.AbstractTokenUpdatePlugin;
import org.fogbowcloud.blowout.core.model.resource.AbstractResource;

import static java.lang.Thread.sleep;
import static org.fogbowcloud.blowout.core.util.AppUtil.generateIdentifier;
import static org.fogbowcloud.blowout.core.util.AppUtil.isStringEmpty;

public class FogbowInfrastructureProvider implements InfrastructureProvider {
	// TODO: put in resource the user, token and localCommand

	private static final Logger LOGGER = Logger.getLogger(FogbowInfrastructureProvider.class);

	private final RASRequestsHelper requestsHelper;
	private final Properties properties;
	private final AbstractTokenUpdatePlugin tokenUpdatePlugin;
	private FogbowResourceDatastore frDatastore;
	private Map<String, FogbowResource> resourcesMap;

	public FogbowInfrastructureProvider(Properties properties, ScheduledExecutorService handleTokeUpdateExecutor,
										AbstractTokenUpdatePlugin tokenUpdatePlugin) {
		this.resourcesMap = new ConcurrentHashMap<>();
		this.properties = properties;
		this.frDatastore = new FogbowResourceDatastore(properties);
		this.tokenUpdatePlugin =  tokenUpdatePlugin;
		this.requestsHelper = new RASRequestsHelper(this.properties, this.tokenUpdatePlugin);
		this.handleTokenUpdate(handleTokeUpdateExecutor);
	}

	protected FogbowInfrastructureProvider(Properties properties, ScheduledExecutorService handleTokeUpdateExecutor,
			boolean cleanPrevious) throws Exception {
		this(properties, handleTokeUpdateExecutor, createTokenUpdatePlugin(properties));
        this.verifyPreviousResource(cleanPrevious);
	}

	public FogbowInfrastructureProvider(Properties properties, boolean removePrevious) throws Exception {
		this(properties, Executors.newScheduledThreadPool(1), removePrevious);
	}

	@Override
	public String requestResource(Specification specification) throws RequestResourceException {
		LOGGER.info("Requesting resource on Fogbow with specifications: " + specification.toString());
		this.validateSpecification(specification);

		String computeOrderId = this.requestsHelper.createCompute(specification);
		String publicIpId = null;

		try {
			publicIpId = this.requestsHelper.createPublicIp(computeOrderId);
		} catch (InterruptedException e) {
			LOGGER.error("Error while requesting Public IP.");
		}

		String resourceId = generateIdentifier();
		FogbowResource fogbowResource = new FogbowResource(resourceId, computeOrderId, specification, publicIpId);
		this.putMetadata(fogbowResource, specification);
		this.resourcesMap.put(resourceId, fogbowResource);
		this.frDatastore.addFogbowResource(fogbowResource);

		LOGGER.info("Request for Fogbow Resource was Successful. Resource ID: [" + fogbowResource.getId() + "] " +
				"Order ID: [" + fogbowResource.getComputeOrderId() + "]");
		return fogbowResource.getId();
	}

	@Override
	public AbstractResource getResource(String resourceId) {
		LOGGER.info("Getting resource from request id: [" + resourceId + "]");
		try {
			FogbowResource resource = getFogbowResource(resourceId);
			LOGGER.info("Returning Resource from Resource id: [" + resourceId + "] - Instance ID : ["
					+ resource.getInstanceId() + "]");
			return resource;
		} catch (Exception e) {
			LOGGER.error("Error while getting resource with id: [" + resourceId + "] ");
			return null;
		}
	}

	@Override
	public void deleteResource(String resourceId) throws InfrastructureException {
		FogbowResource fogbowResource = resourcesMap.get(resourceId);

		if (fogbowResource == null) {
			throw new InfrastructureException(AppMessagesConstants.ATTRIBUTES_INVALIDS);
		}

		LOGGER.info("Deleting resource with ID = " + fogbowResource.getId());

		try {
            this.requestsHelper.deleteFogbowResource(fogbowResource);
			this.resourcesMap.remove(resourceId);
			this.frDatastore.deleteFogbowResourceById(fogbowResource);
			LOGGER.info("Resource " + fogbowResource.getId() + " deleted successfully");
		} catch (Exception e) {
			throw new InfrastructureException("Error when trying to delete resource id[" +
					fogbowResource.getId() + "]", e);
		}
	}

	@Override
	public List<AbstractResource> getAllResources(){
		return new ArrayList<>(resourcesMap.values());
	}

    public FogbowResource getFogbowResource(String resourceId) throws InfrastructureException {
        LOGGER.info("Initiating Resource Instantiation - Resource id: [" + resourceId + "]");

        String instanceId;
        Map<String, Object> instanceAttributes;
        FogbowResource fogbowResource = this.resourcesMap.get(resourceId);
        validateFogbowResource(fogbowResource);

        try {
            LOGGER.info("Getting request attributes - Retrieve Instance ID.");

            instanceAttributes = this.requestsHelper.getComputeInstance(fogbowResource.getComputeOrderId());
            instanceId = String.valueOf(instanceAttributes.get(FogbowConstants.INSTANCE_ATTRIBUTE_NAME));

            Map<String, Object> sshInfo = getPublicIpInstance(fogbowResource.getPublicIpOrderId());

            this.populateInstanceAttributes(instanceAttributes, sshInfo);

            if (instanceId != null && !instanceId.isEmpty()) {
                LOGGER.debug("Instance ID returned: " + instanceId);

                fogbowResource.setInstanceId(instanceId);

                if (this.validateInstanceAttributes(instanceAttributes)) {
                    LOGGER.debug("Getting Instance attributes.");
                    putMetadata(fogbowResource, instanceAttributes);
                    LOGGER.debug("New Fogbow Resource created - Instance ID: [" + instanceId + "]");
                    frDatastore.updateFogbowResource(fogbowResource);
                } else {
                    LOGGER.debug("Instance attributes not yet ready for instance: [" + instanceId + "]");
                }
            }
            return fogbowResource;

        } catch (Exception e) {
            LOGGER.error("Error while getting resource from Order id: [" + fogbowResource.getComputeOrderId() + "]", e);
        }
        return null;
    }

    private void validateFogbowResource(AbstractResource fogbowResource) throws InfrastructureException {
        if (fogbowResource == null) {
            LOGGER.error(AppMessagesConstants.RESOURCE_NOT_VALID);
            throw new InfrastructureException(AppMessagesConstants.RESOURCE_NOT_VALID);
        }
    }

    private void populateInstanceAttributes(Map<String, Object> instanceAttributes, Map<String, Object> sshInfo) {
        for (Map.Entry<String, Object> entry : sshInfo.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            instanceAttributes.putIfAbsent(key, value);
        }
    }

    private boolean validateInstanceAttributes(Map<String, Object> instanceAttributes) {
        LOGGER.info(AppMessagesConstants.VALIDATING_ATTRIBUTES);

        boolean isValid = true;

        if (instanceAttributes != null && !instanceAttributes.isEmpty()) {

            String sshInformation = String.valueOf(instanceAttributes.get(
                    FogbowConstants.JSON_KEY_FOGBOW_PUBLIC_IP));
            String vCPU = String.valueOf(instanceAttributes.get(
                    FogbowConstants.INSTANCE_ATTRIBUTE_VCPU));
            String memorySize = String.valueOf(instanceAttributes.get(
                    FogbowConstants.INSTANCE_ATTRIBUTE_MEMORY_SIZE));

            isValid = !isStringEmpty(sshInformation, vCPU, memorySize);
            if (!isValid) {
                LOGGER.error(AppMessagesConstants.ATTRIBUTES_INVALIDS);
                return false;
            }
        } else {
            LOGGER.error(AppMessagesConstants.ATTRIBUTES_INVALIDS);
            isValid = false;
        }
        return isValid;
    }

    private void putMetadata(AbstractResource fogbowResource, Specification specification) {
        String requestType = specification.getRequirementValue(FogbowConstants.METADATA_REQUEST_TYPE);

        fogbowResource.putMetadata(AbstractResource.METADATA_REQUEST_TYPE, requestType);
        fogbowResource.putMetadata(AbstractResource.METADATA_IMAGE_NAME, specification.getImageName());
        fogbowResource.putMetadata(AbstractResource.METADATA_PUBLIC_KEY, specification.getPublicKey());
    }

    private void putMetadata(AbstractResource fogbowResource, Map<String, Object> instanceAttributes) {

        fogbowResource.putMetadata(AbstractResource.METADATA_SSH_PUBLIC_IP,
                instanceAttributes.get(FogbowConstants.JSON_KEY_FOGBOW_PUBLIC_IP));

        fogbowResource.putMetadata(AbstractResource.METADATA_SSH_USERNAME_ATT,
                FogbowConstants.INSTANCE_ATTRIBUTE_DEFAULT_SHH_USERNAME);

        fogbowResource.putMetadata(AbstractResource.METADATA_VCPU,
                instanceAttributes.get(FogbowConstants.INSTANCE_ATTRIBUTE_VCPU));

        fogbowResource.putMetadata(AbstractResource.METADATA_MEM_SIZE,
                instanceAttributes.get(FogbowConstants.INSTANCE_ATTRIBUTE_MEMORY_SIZE));

        fogbowResource.putMetadata(AbstractResource.METADATA_DISK_SIZE,
                instanceAttributes.get(FogbowConstants.INSTANCE_ATTRIBUTE_DISK_SIZE));
    }

	protected Map<String, Object> getPublicIpInstance(String publicIpOrderId) throws InterruptedException {
        return this.requestsHelper.getPublicIpInstance(publicIpOrderId);

	}

	private void validateSpecification(Specification specification) throws RequestResourceException {
		if (specification.getImageName() == null || specification.getImageName().isEmpty()) {
			throw new RequestResourceException();
		}
		if (specification.getPublicKey() == null || specification.getPublicKey().isEmpty()) {
			throw new RequestResourceException();
		}

		String fogbowRequirements = specification
				.getRequirementValue(FogbowConstants.METADATA_FOGBOW_REQUIREMENTS);

		if (!FogbowRequirementsHelper.validateFogbowRequirementsSyntax(fogbowRequirements)) {
			LOGGER.debug("FogbowRequirements [" + fogbowRequirements
					+ "] is not in valid format." + BlowoutConstants.FOGBOW_REQUIREMENTS_EXAMPLE);
			throw new RequestResourceException("FogbowRequirements [" + fogbowRequirements
					+ "] is not in valid format." + BlowoutConstants.FOGBOW_REQUIREMENTS_EXAMPLE);
		}
	}

	public void setHttpWrapper(HttpWrapper httpWrapper) {
		this.requestsHelper.setHttpWrapper(httpWrapper);
	}

	protected void setToken(Token token) {
		this.requestsHelper.setToken(token);
	}

	protected void setResourcesMap(Map<String, FogbowResource> resourcesMap) {
		this.resourcesMap = resourcesMap;
	}

	protected void setFrDatastore(FogbowResourceDatastore frDatastore) {
		this.frDatastore = frDatastore;
	}

	protected StringEntity makeJsonBody(Specification spec) throws UnsupportedEncodingException, BlowoutException {
        return requestsHelper.makeJsonBody(spec);
	}

	private void verifyPreviousResource(boolean cleanPrevious) {
		for (FogbowResource fogbowResource : frDatastore.getAllFogbowResources()) {
			resourcesMap.put(fogbowResource.getId(), fogbowResource);
			if(cleanPrevious){
				try {
					this.deleteResource(fogbowResource.getId());
				} catch (Exception e) {
					LOGGER.error("Error while trying to delete resource on initialization: "+fogbowResource.getId());
				}
			}
		}
	}

	protected void handleTokenUpdate(ScheduledExecutorService handleTokenUpdateExecutor) {
		LOGGER.info("Turning on handle token update.");

		handleTokenUpdateExecutor.scheduleWithFixedDelay(
				() -> setToken(tokenUpdatePlugin.generateToken()),
				tokenUpdatePlugin.getUpdateTime(),
				tokenUpdatePlugin.getUpdateTime(),
				tokenUpdatePlugin.getUpdateTimeUnits());
	}

	private static AbstractTokenUpdatePlugin createTokenUpdatePlugin(Properties properties) throws Exception {

		String providerClassName = properties.getProperty(AppPropertiesConstants.TOKEN_UPDATE_PLUGIN);

		Object clazz = Class.forName(providerClassName).getConstructor(Properties.class).newInstance(properties);
		if (!(clazz instanceof AbstractTokenUpdatePlugin)) {
			throw new Exception("Provider Class Name is not a TokenUpdatePluginInterface implementation");
		}
		AbstractTokenUpdatePlugin tokenUpdatePlugin = (AbstractTokenUpdatePlugin) clazz;
		tokenUpdatePlugin.validateProperties();
		return tokenUpdatePlugin;
	}
}