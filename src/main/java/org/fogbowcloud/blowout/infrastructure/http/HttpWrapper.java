package org.fogbowcloud.blowout.infrastructure.http;

import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.fogbowcloud.manager.occi.model.HeaderUtils;
import org.fogbowcloud.manager.occi.model.OCCIHeaders;
import org.fogbowcloud.manager.occi.order.OrderConstants;

public class HttpWrapper {

    private static final int SERVER_SIDE_ERRO_MAX = 505;
	private static final int CLIENT_SIDE_CODE_ERRO_INIT = 400;

	public static final String HTTP_CONTENT_JSON = "application/json";

	public static final String HTTP_METHOD_POST = HttpPost.METHOD_NAME;
	public static final String HTTP_METHOD_GET = HttpGet.METHOD_NAME;
	public static final String HTTP_METHOD_DELETE = HttpDelete.METHOD_NAME;

    private static HttpClient createHttpClient() {
		return HttpClients.createMinimal();
    }

    public String doRequest(String method, String endpoint, String authToken, List<Header> additionalHeaders) throws Exception {
        return doRequest(method, endpoint, authToken, additionalHeaders, null);
    }

    public String doRequest(String method, String endpoint, String authToken, List<Header> additionalHeaders, StringEntity bodyJsonStringEntity) throws Exception {

    	HttpUriRequest request = null;
        
    	if (method.equals(HTTP_METHOD_GET)) {
            request = new HttpGet(endpoint);
        } else if (method.equals(HTTP_METHOD_DELETE)) {
            request = new HttpDelete(endpoint);
        } else if (method.equals(HTTP_METHOD_POST)) {
            request = new HttpPost(endpoint);
            ((HttpPost) request).setEntity(bodyJsonStringEntity);
        }
    	
        request.addHeader(OCCIHeaders.CONTENT_TYPE, OCCIHeaders.OCCI_CONTENT_TYPE);

        if (authToken != null) {
            String FEDERATION_TOKEN_VALUE_HEADER_KEY = "federationTokenValue"; // TODO: move to constant
            request.addHeader(FEDERATION_TOKEN_VALUE_HEADER_KEY, authToken);
        }
        for (Header header : additionalHeaders) {
            request.addHeader(header);
        }

        HttpResponse response = createHttpClient().execute(request);
        HttpEntity entity = null;
        
        try {
        	
            entity = response.getEntity();
            
            int statusCode = response.getStatusLine().getStatusCode();
            
            if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED) {

                // TODO: check if this part of code is still up with new fogbow response
                // Does it make sense to check the location header still? Does Fogbow RAS return it?
                Header locationHeader = getLocationHeader(response.getAllHeaders());
                
                if (locationHeader != null && locationHeader.getValue().contains(OrderConstants.TERM)) {
                    return generateLocationHeaderResponse(locationHeader);
                } else {
                    return EntityUtils.toString(response.getEntity());
                }
                
            }else if(statusCode >= CLIENT_SIDE_CODE_ERRO_INIT && statusCode <= SERVER_SIDE_ERRO_MAX){
            	throw new Exception("Erro on request - Method ["+method+"] Endpoit: ["+endpoint+"] - Status: "+statusCode+" -  Msg: "+response.getStatusLine().toString());
            }else {
                return response.getStatusLine().toString();
            }
            
        } finally {
            try {
                if (entity != null) {
                	EntityUtils.toString(entity);
                }
            } catch (Exception e) {
                // Do nothing
            }
        }
    }

    protected static Header getLocationHeader(Header[] headers) {
        Header locationHeader = null;
        for (Header header : headers) {
            if (header.getName().equals("Location")) {
                locationHeader = header;
            }
        }
        return locationHeader;
    }

    protected static String generateLocationHeaderResponse(Header header) {
        String[] locations = header.getValue().split(",");
        String response = "";
        for (String location : locations) {
            response += HeaderUtils.X_OCCI_LOCATION_PREFIX + location + "\n";
        }
        return response.trim();
    }

}
