/**
 * Author : chizf
 * Date : 2020年3月26日 下午9:02:44
 * Title : org.rs.listener.handler.RegisteredEventHandler.java
 *
**/
package org.rs.listener.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.transport.EurekaHttpResponse;
import com.netflix.discovery.shared.transport.EurekaHttpResponse.EurekaHttpResponseBuilder;
import com.netflix.discovery.shared.transport.TransportException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

@Component
public class RegisteredEventHandler {
	private static final Logger logger = LoggerFactory.getLogger(RegisteredEventHandler.class);

	protected final Client jerseyClient = Client.create();

	private final int numberOfRetries = 3;

	public EurekaHttpResponse<Void> execute(Collection<InstanceInfo> gateways, InstanceInfo info) {
		
//		http://172.21.29.75:9922/securityRouteLocator?method=add

		for (int retry = 0; retry < numberOfRetries; retry++) {
			
			

			for (InstanceInfo gatewayInfo : gateways) {
				String serviceUri = "http://" + gatewayInfo.getIPAddr() + ":" + gatewayInfo.getPort() + "/";
				String urlPath = "securityRouteLocator?method=add";
				ClientResponse response = null;
				try {

					

					response = builder.header("Accept-Encoding", "gzip").accept(MediaType.APPLICATION_JSON)
							.post(Entity.entity(info, MediaType.APPLICATION_JSON), ClientResponse.class);

					return anEurekaHttpResponse(response.getStatus()).headers(headersOf(response)).build();
				} catch (Exception e) {
					e.printStackTrace();
					logger.debug("{} gateway is error,change next gateway", gatewayInfo.getAppName());
				} finally {
					if (logger.isDebugEnabled()) {
						logger.debug("Jersey HTTP POST {}/{} with instance {}; statusCode={}", "gateway", urlPath,
								info.getId(), response == null ? "N/A" : response.getStatus());
					}
					if (response != null) {
						response.close();
					}
				}
			}

		}
		throw new TransportException("Retry limit reached; giving up on completing the request");
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
