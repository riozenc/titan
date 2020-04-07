/**
 * Author : chizf
 * Date : 2020年3月26日 下午9:02:44
 * Title : org.rs.listener.handler.RegisteredEventHandler.java
 *
**/
package org.rs.event;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.transport.EurekaHttpResponse;
import com.netflix.discovery.shared.transport.EurekaHttpResponse.EurekaHttpResponseBuilder;
import com.netflix.discovery.shared.transport.TransportException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

@Component
public class RegisteredEventHandler {
	private static final Logger logger = LoggerFactory.getLogger(RegisteredEventHandler.class);

	private final int numberOfRetries = 3;

	private final Client client = Client.create();

	public EurekaHttpResponse<Void> execute(Collection<InstanceInfo> gateways, InstanceInfo info)
			throws UniformInterfaceException, ClientHandlerException, JsonProcessingException {

		for (int retry = 0; retry < numberOfRetries; retry++) {
			for (InstanceInfo gatewayInfo : gateways) {
				logger.debug("try register {} to {} - numberOfRetries : {} ", info.getAppName(),
						gatewayInfo.getIPAddr(), retry);

				WebResource webResource = client.resource(gatewayInfo.getHomePageUrl()).path("securityRouteLocator")
						.queryParam("method", "saveRoute");

				Builder requestBuilder = webResource.getRequestBuilder().header("Accept-Encoding", "gzip")
						.accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE);

				ClientResponse response = requestBuilder.post(ClientResponse.class,
						new ObjectMapper().writeValueAsString(createEntity(info)));

				String result = response.getEntity(String.class);
				if (response.getStatus() == Status.OK.getStatusCode() && response.hasEntity()) {
					logger.debug("Registered [{}] {} to {} - response : {} ", response.getStatus(), info.getAppName(),
							gatewayInfo.getIPAddr(), result);
					return anEurekaHttpResponse(response.getStatus()).headers(headersOf(response)).build();
				} else {
					logger.debug("Registered [{}] {} to {} - response : {} ", response.getStatus(), info.getAppName(),
							gatewayInfo.getIPAddr(), result);
				}

			}

		}
		throw new TransportException("Retry limit reached; giving up on completing the request");
	}

	private RouteRegistrationEntity createEntity(InstanceInfo instanceInfo) {
		RouteRegistrationEntity entity = new RouteRegistrationEntity();
		entity.setId(instanceInfo.getId());
		entity.setAppName(instanceInfo.getAppName());
		entity.setOrder(instanceInfo.hashCode());
		entity.setUri("lb://" + instanceInfo.getAppName().toUpperCase() + "/");
		entity.setFilters(instanceInfo.getMetadata().get("filters"));
		entity.setPredicates(instanceInfo.getMetadata().get("predicates") == null
				? "/" + instanceInfo.getAppName().toLowerCase() + "/**"
				: instanceInfo.getMetadata().get("predicates"));
		return entity;
	}

	private static Map<String, String> headersOf(ClientResponse response) {
		MultivaluedMap<String, String> jerseyHeaders = response.getHeaders();
		if (jerseyHeaders == null || jerseyHeaders.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, String> headers = new HashMap<>();
		for (Entry<String, List<String>> entry : jerseyHeaders.entrySet()) {
			if (!entry.getValue().isEmpty()) {
				headers.put(entry.getKey(), entry.getValue().get(0));
			}
		}
		return headers;
	}

	public static EurekaHttpResponseBuilder<Void> anEurekaHttpResponse(int statusCode) {

		return EurekaHttpResponse.anEurekaHttpResponse(statusCode);
	}
}
