package org.fogbowcloud.blowout.infrastructure.provider.fogbow;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.fogbowcloud.blowout.core.model.Specification;
import org.fogbowcloud.blowout.constants.AppPropertiesConstants;

import static org.fogbowcloud.blowout.core.util.AppUtil.isStringEmpty;
import static org.fogbowcloud.blowout.core.util.AppUtil.makeBodyField;
import org.fogbowcloud.blowout.database.FogbowResourceDatastore;
import org.fogbowcloud.blowout.infrastructure.exception.InfrastructureException;
import org.fogbowcloud.blowout.infrastructure.exception.RequestResourceException;
import org.fogbowcloud.blowout.infrastructure.http.HttpWrapper;
import org.fogbowcloud.blowout.infrastructure.model.FogbowResource;
import org.fogbowcloud.blowout.infrastructure.model.Token;
import org.fogbowcloud.blowout.infrastructure.provider.InfrastructureProvider;
import org.fogbowcloud.blowout.infrastructure.token.AbstractTokenUpdatePlugin;
import org.fogbowcloud.blowout.pool.AbstractResource;
import org.json.JSONObject;

public class FogbowInfrastructureProvider implements InfrastructureProvider {
	// TODO: put in resource the user, token and localCommand

	private static final Logger LOGGER = Logger.getLogger(FogbowInfrastructureProvider.class);

	public static final String INSTANCE_ATTRIBUTE_MEMORY_SIZE = "ram";
	public static final String INSTANCE_ATTRIBUTE_VCORE = "vCPU";
	public static final String INSTANCE_ATTRIBUTE_HOSTNAME = "name";
	public static final String INSTANCE_ATTRIBUTE_PUBLIC_IP = "ip";
	public static final String INSTANCE_ATTRIBUTE_STATE = "state";
	public static final String INSTANCE_ATTRIBUTE_PROVIDER = "provider";
	public static final String DEFAULT_INSTANCE_ATTRIBUTE_SHH_USERNAME = "fogbow";

	public static final String FOGBOW_RAS_COMPUTE_ENDPOINT = "computes";
	public static final String FOGBOW_RAS_PUBLIC_ID_ENDPOINT = "publicIps";

	private HttpWrapper httpWrapper;
	private String managerUrl;
	private Token token;
	private Properties properties;
	private AbstractTokenUpdatePlugin tokenUpdatePlugin;
	private FogbowResourceDatastore frDatastore;

	private Map<String, FogbowResource> resourcesMap = new ConcurrentHashMap<String, FogbowResource>();

    protected FogbowInfrastructureProvider(Properties properties, ScheduledExecutorService handleTokeUpdateExecutor,
                                           AbstractTokenUpdatePlugin tokenUpdatePlugin) throws Exception {
        httpWrapper = new HttpWrapper();
        this.properties = properties;
        this.managerUrl = properties.getProperty(AppPropertiesConstants.INFRA_FOGBOW_MANAGER_BASE_URL);
        this.tokenUpdatePlugin = tokenUpdatePlugin;

        this.token = tokenUpdatePlugin.generateToken();

        ScheduledExecutorService handleTokenUpdateExecutor = handleTokeUpdateExecutor;
        handleTokenUpdate(handleTokenUpdateExecutor);
    }

	protected FogbowInfrastructureProvider(Properties properties, ScheduledExecutorService handleTokeUpdateExecutor,
			boolean cleanPrevious) throws Exception {
		this(properties, handleTokeUpdateExecutor, createTokenUpdatePlugin(properties));
		frDatastore = new FogbowResourceDatastore(properties);

        verifyPreviousResource(cleanPrevious);
	}

	public FogbowInfrastructureProvider(Properties properties, boolean removePrevious) throws Exception {
		this(properties, Executors.newScheduledThreadPool(1), removePrevious);
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
		LOGGER.debug("Turning on handle token update.");

		// TODO: Could be a better strategy call this method from
		// InfrastructureManager on its main service thread ?
		// The main thread could check if the token has expired and so call this
		// method.
		handleTokenUpdateExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				setToken(tokenUpdatePlugin.generateToken());
			}
		}, tokenUpdatePlugin.getUpdateTime(), tokenUpdatePlugin.getUpdateTime(),
				tokenUpdatePlugin.getUpdateTimeUnits());
	}

	@Override
	public String requestResource(Specification spec) throws RequestResourceException {

		LOGGER.debug("Requesting resource on Fogbow with specifications: " + spec.toString());

		String orderId;

		try {
			this.validateSpecification(spec);

			StringEntity bodyRequest = makeBodyJson(spec);
			bodyRequest.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, HttpWrapper.HTTP_CONTENT_JSON));

			orderId = this.doRequest("post", managerUrl + "/" + FOGBOW_RAS_COMPUTE_ENDPOINT, new LinkedList<Header>(), bodyRequest);

		} catch (Exception e) {
			LOGGER.error("Error while requesting resource on Fogbow", e);
			e.printStackTrace();
			throw new RequestResourceException("Request for Fogbow Resource has FAILED: " + e.getMessage(), e);
		}

		String resourceId = String.valueOf(UUID.randomUUID());

		FogbowResource fogbowResource = new FogbowResource(resourceId, orderId, spec);
		String requestType = spec.getRequirementValue(FogbowRequirementsHelper.METADATA_FOGBOW_REQUEST_TYPE);
		fogbowResource.putMetadata(AbstractResource.METADATA_REQUEST_TYPE, requestType);
		fogbowResource.putMetadata(AbstractResource.METADATA_IMAGE, spec.getImageId());
		fogbowResource.putMetadata(AbstractResource.METADATA_PUBLIC_KEY, spec.getPublicKey());

		resourcesMap.put(resourceId, fogbowResource);
		frDatastore.addFogbowResource(fogbowResource);

		LOGGER.debug("Request for Fogbow Resource was Successful. Resource ID: [" + resourceId + "] Order ID: ["
				+ orderId + "]");
		return fogbowResource.getId();
	}

	@Override
	public AbstractResource getResource(String resourceId) {

		LOGGER.debug("Getting resource from request id: [" + resourceId + "]");
		try {
			FogbowResource resource = getFogbowResource(resourceId);
			LOGGER.debug("Returning Resource from Resource id: [" + resourceId + "] - Instance ID : ["
					+ resource.getInstanceId() + "]");
			return resource;
		} catch (Exception e) {
			LOGGER.error("Error while getting resource with id: [" + resourceId + "] ");
			return null;
		}
	}

	public FogbowResource getFogbowResource(String resourceId) throws InfrastructureException {

		LOGGER.debug("Initiating Resource Instanciation - Resource id: [" + resourceId + "]");
		String instanceId;
		String fogbowPublicIpOrderId;
		Map<String, String> instanceAttributes;

		FogbowResource fogbowResource = resourcesMap.get(resourceId);

		if (fogbowResource == null) {
			String errorMsg = "The resource is not a valid. Was never requested or is already deleted";
			LOGGER.error(errorMsg);
			throw new InfrastructureException(errorMsg);
		}

		try {
			LOGGER.debug("Getting request attributes - Retrieve Instace ID.");

			instanceAttributes = getFogbowInstanceAttributes(fogbowResource.getOrderId());
			instanceId = instanceAttributes.get(INSTANCE_ATTRIBUTE_HOSTNAME);

			fogbowPublicIpOrderId = requestInstancePublicIp(fogbowResource.getOrderId());
			Map<String, String> sshInfo = getInstancePublicIp(fogbowPublicIpOrderId);

			for (Map.Entry<String, String> entry : sshInfo.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				instanceAttributes.putIfAbsent(key, value);
			}

			if (instanceId != null && !instanceId.isEmpty()) {
				LOGGER.debug("Instance ID returned: " + instanceId);

				fogbowResource.setInstanceId(instanceId);

				if (this.validateInstanceAttributes(instanceAttributes)) {

					LOGGER.debug("Getting Instance attributes.");

					fogbowResource.setLocalCommandInterpreter(
							properties.getProperty(AppPropertiesConstants.LOCAL_COMMAND_INTERPRETER));

					fogbowResource.putMetadata(AbstractResource.METADATA_SSH_HOST,
							instanceAttributes.get(INSTANCE_ATTRIBUTE_PUBLIC_IP));
					fogbowResource.putMetadata(AbstractResource.METADATA_SSH_USERNAME_ATT,
							DEFAULT_INSTANCE_ATTRIBUTE_SHH_USERNAME);

					fogbowResource.putMetadata(AbstractResource.METADATA_VCPU,
							instanceAttributes.get(INSTANCE_ATTRIBUTE_VCORE));
					fogbowResource.putMetadata(AbstractResource.METADATA_MEN_SIZE,
							instanceAttributes.get(INSTANCE_ATTRIBUTE_MEMORY_SIZE));
					fogbowResource.putMetadata(AbstractResource.METADATA_DISK_SIZE,
							instanceAttributes.get(INSTANCE_ATTRIBUTE_MEMORY_SIZE));

					LOGGER.debug("New Fogbow Resource created - Instace ID: [" + instanceId + "]");

					frDatastore.updateFogbowResource(fogbowResource);
					return fogbowResource;

				} else {
					LOGGER.debug("Instance attributes not yet ready for instance: [" + instanceId + "]");
				}
			}

		} catch (Exception e) {
			LOGGER.error("Error while getting resource from Order id: [" + fogbowResource.getOrderId() + "]", e);
		}
		return null;
	}
	
	@Override
	public List<AbstractResource> getAllResources(){
		return new ArrayList<AbstractResource>(resourcesMap.values());
	}

	@Override
	public void deleteResource(String resourceId) throws InfrastructureException {

		FogbowResource fogbowResource = resourcesMap.get(resourceId);

		if (fogbowResource == null) {
			throw new InfrastructureException("The resource is not a valid. Was never requested or is already deleted");
		}

		LOGGER.debug("Deleting resource with ID = " + fogbowResource.getId());

		try {
			if (fogbowResource.getInstanceId() != null) {
				this.doRequest(HttpWrapper.HTTP_METHOD_DELETE, managerUrl + "/" + FOGBOW_RAS_COMPUTE_ENDPOINT + "/" + fogbowResource.getInstanceId(),
						new ArrayList<Header>());
			}
			resourcesMap.remove(resourceId);
			frDatastore.deleteFogbowResourceById(fogbowResource);
			LOGGER.debug("Resource " + fogbowResource.getId() + " deleted successfully");
		} catch (Exception e) {
			throw new InfrastructureException("Error when trying to delete resource id[" + fogbowResource.getId() + "]",
					e);
		}
	}

	private Map<String, String> getFogbowInstanceAttributes(String orderId) throws Exception {
		String endpoint = managerUrl + "/" +  FOGBOW_RAS_COMPUTE_ENDPOINT + "/" + orderId;
		String instanceInformation = doRequest(HttpWrapper.HTTP_METHOD_GET, endpoint, new ArrayList<Header>());

		Map<String, String> attrs = parseAttributes(instanceInformation);
		return attrs;
	}

	private void validateSpecification(Specification specification) throws RequestResourceException {

		if (specification.getImageId() == null || specification.getImageId().isEmpty()) {

			throw new RequestResourceException();
		}
		if (specification.getPublicKey() == null || specification.getPublicKey().isEmpty()) {

			throw new RequestResourceException();
		}

		String fogbowRequirements = specification
				.getRequirementValue(FogbowRequirementsHelper.METADATA_FOGBOW_REQUIREMENTS);

		if (!FogbowRequirementsHelper.validateFogbowRequirementsSyntax(fogbowRequirements)) {
			LOGGER.debug("FogbowRequirements [" + fogbowRequirements
					+ "] is not in valid format. e.g: [Glue2vCPU >= 1 && Glue2RAM >= 1024 && Glue2disk >= 20 && Glue2CloudComputeManagerID ==\"servers.your.domain\"]");
			throw new RequestResourceException("FogbowRequirements [" + fogbowRequirements
					+ "] is not in valid format. e.g: [Glue2vCPU >= 1 && Glue2RAM >= 1024 && Glue2disk >= 20 && Glue2CloudComputeManagerID ==\"servers.your.domain\"]");
		}
	}

	private String doRequest(String method, String endpoint, List<Header> headers, StringEntity bodyJson) throws Exception {
		return httpWrapper.doRequest(method, endpoint, this.token.getAccessId(), headers, bodyJson);
	}

	private String doRequest(String method, String endpoint, List<Header> headers) throws Exception {
		return httpWrapper.doRequest(method, endpoint, this.token.getAccessId(), headers);
	}

	private boolean validateInstanceAttributes(Map<String, String> instanceAttributes) {

		LOGGER.debug("Validating instance attributes.");

		boolean isValid = true;

		if (instanceAttributes != null && !instanceAttributes.isEmpty()) {

			String sshInformation = instanceAttributes.get(INSTANCE_ATTRIBUTE_PUBLIC_IP);
			String vcore = instanceAttributes.get(INSTANCE_ATTRIBUTE_VCORE);
			String memorySize = instanceAttributes.get(INSTANCE_ATTRIBUTE_MEMORY_SIZE);

			// If any of these attributes are empty, then return invalid.
			// TODO: add to "isStringEmpty diskSize and memberId when fogbow
			// being returning this two attributes.
			isValid = !isStringEmpty(sshInformation, vcore, memorySize);
			if (!isValid) {
				LOGGER.debug("Instance attributes invalids.");
				return false;
			}

		} else {
			LOGGER.debug("Instance attributes invalids.");
			isValid = false;
		}

		return isValid;
	}

	private Map<String, String> parseAttributes(String response) {
		Map<String, String> atts = new HashMap<>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			atts = mapper.readValue(response, HashMap.class);
		} catch (IOException e) {
			LOGGER.debug("String reponse is not valid.");
		}
		return atts;
	}

	private static AbstractTokenUpdatePlugin createTokenUpdatePlugin(Properties properties) throws Exception {

		String providerClassName = properties.getProperty(AppPropertiesConstants.INFRA_AUTH_TOKEN_UPDATE_PLUGIN);

		Object clazz = Class.forName(providerClassName).getConstructor(Properties.class).newInstance(properties);
		if (!(clazz instanceof AbstractTokenUpdatePlugin)) {
			throw new Exception("Provider Class Name is not a TokenUpdatePluginInterface implementation");
		}
		AbstractTokenUpdatePlugin tokenUpdatePlugin = (AbstractTokenUpdatePlugin) clazz;
		tokenUpdatePlugin.validateProperties();
		return tokenUpdatePlugin;
	}

	public void setHttpWrapper(HttpWrapper httpWrapper) {
		this.httpWrapper = httpWrapper;
	}

	protected void setToken(Token token) {
		this.token = token;
	}

	protected void setResourcesMap(Map<String, FogbowResource> resourcesMap) {
		this.resourcesMap = resourcesMap;
	}

	protected void setFrDatastore(FogbowResourceDatastore frDatastore) {
		this.frDatastore = frDatastore;
	}

	protected StringEntity makeBodyJson(Specification spec) throws UnsupportedEncodingException {
		JSONObject json = new JSONObject();

		makeBodyField(json, FogbowRequirementsHelper.JSON_KEY_FOGBOW_REQUIREMENTS_PUBLIC_KEY, spec.getPublicKey());
		makeBodyField(json, FogbowRequirementsHelper.JSON_KEY_FOGBOW_REQUIREMENTS_MEMORY, spec.getMemory());
		makeBodyField(json, FogbowRequirementsHelper.JSON_KEY_FOGBOW_REQUIREMENTS_DISK, spec.getDisk());
		makeBodyField(json, FogbowRequirementsHelper.JSON_KEY_FOGBOW_REQUIREMENTS_IMAGE_ID, spec.getImageId());
		makeBodyField(json, FogbowRequirementsHelper.JSON_KEY_FOGBOW_REQUIREMENTS_VCPU, spec.getvCPU());

		return new StringEntity(json.toString());
	}

	protected StringEntity makeRequestBodyJson(Map<String, String> bodyRequestAttrs) throws UnsupportedEncodingException {
		JSONObject json = new JSONObject();

		for (String jsonKey : bodyRequestAttrs.keySet()) {
			String jsonValue = bodyRequestAttrs.get(jsonKey);
			json.put(jsonKey, jsonValue);
		}

		StringEntity se = new StringEntity(json.toString());
		se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, HttpWrapper.HTTP_CONTENT_JSON));

		return se;
	}

	protected String requestInstancePublicIp(String computeOrderId) {
		String publicOrderID = null;
		String requestUrl = managerUrl + "/" + FOGBOW_RAS_PUBLIC_ID_ENDPOINT;

		Map<String, String> bodyRequestAttrs = new HashMap<>();
		if (computeOrderId != null && !computeOrderId.isEmpty()) {
			bodyRequestAttrs.put(FogbowRequirementsHelper.JSON_KEY_FOGBOW_PUBLICIP_COMPUTER_ORDER_ID, computeOrderId);
		}

		try {
			StringEntity bodyRequest = makeRequestBodyJson(bodyRequestAttrs);
			publicOrderID = this.doRequest(HttpWrapper.HTTP_METHOD_POST, requestUrl, new LinkedList<Header>(), bodyRequest);
		} catch (Exception e) {
			LOGGER.error("Error while getting public ip for computer order of id " + computeOrderId, e);
		}
		return publicOrderID;
	}

	protected Map<String, String> getInstancePublicIp(String publicIpOrderId) {
		String publicOrderStringResponse;
		Map<String, String> sshInfo = new HashMap<>();
		String requestUrl = managerUrl + "/" + FOGBOW_RAS_PUBLIC_ID_ENDPOINT + "/" + publicIpOrderId;

		try {
			publicOrderStringResponse = this.doRequest(HttpWrapper.HTTP_METHOD_GET, requestUrl, new LinkedList<Header>());
			sshInfo = parseAttributes(publicOrderStringResponse);
		} catch (Exception e) {
			LOGGER.error("Error while getting info about public instance of order with id " + publicIpOrderId, e);
		}

		return sshInfo;
	}
}