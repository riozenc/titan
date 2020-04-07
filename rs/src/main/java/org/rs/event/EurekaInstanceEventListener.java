/**
 * Author : chizf
 * Date : 2020年3月26日 下午8:33:19
 * Title : org.rs.listener.EurekaInstanceEventListener.java
 *
**/
package org.rs.event;

import static java.util.Collections.synchronizedMap;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.netflix.appinfo.InstanceInfo;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

@Component
public class EurekaInstanceEventListener {

	private static final long WAITING_TIME = 10000;

	private RegisteredEventHandler registeredEventHandler;

	private final Map<String, InstanceInfo> gateways = synchronizedMap(new LinkedHashMap<String, InstanceInfo>());

	public EurekaInstanceEventListener(RegisteredEventHandler registeredEventHandler) {

		this.registeredEventHandler = registeredEventHandler;
	}

	@EventListener
	public void registered(EurekaInstanceRegisteredEvent event)
			throws InterruptedException, UniformInterfaceException, ClientHandlerException, JsonProcessingException {
		InstanceInfo info = event.getInstanceInfo();
		while (true) {
			synchronized (gateways) {
				if (isBusinessServiceGroup(info.getAppGroupName())) {
					if (this.gateways.size() == 0) {
						gateways.wait(WAITING_TIME);
					} else {
						this.registeredEventHandler.execute(gateways.values(), info);
						return;
					}
				} else {
					if (isGateWay(info.getAppName())) {
						this.gateways.putIfAbsent(info.getInstanceId(), info);
						gateways.notifyAll();
					}
					return;
				}
			}
		}
	}

	@EventListener
	public void canceled(EurekaInstanceCanceledEvent event) {
		if (gateways.keySet().contains(event.getServerId())) {
			gateways.remove(event.getServerId());
		}
	}

	private boolean isGateWay(String appName) {
		return "GATEWAY".contentEquals(appName);
	}

	private boolean isBusinessServiceGroup(String appGroupName) {
		return "BUSINESS".contentEquals(appGroupName);
	}
}
