/**
 * Author : chizf
 * Date : 2020年3月26日 下午8:33:19
 * Title : org.rs.listener.EurekaInstanceEventListener.java
 *
**/
package org.rs.listener;

import static java.util.Collections.synchronizedMap;

import java.util.LinkedHashMap;
import java.util.Map;

import org.rs.listener.handler.RegisteredEventHandler;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.netflix.appinfo.InstanceInfo;

@Component
public class EurekaInstanceEventListener {

	private RegisteredEventHandler registeredEventHandler;

	private final Map<String, InstanceInfo> gateways = synchronizedMap(new LinkedHashMap<String, InstanceInfo>());

	public EurekaInstanceEventListener(RegisteredEventHandler registeredEventHandler) {

		this.registeredEventHandler = registeredEventHandler;
	}

	@EventListener
	public void registered(EurekaInstanceRegisteredEvent event) throws InterruptedException {

		InstanceInfo info = event.getInstanceInfo();

		System.out.println(info);

		while (true) {
			synchronized (gateways) {

				if (isBusinessServiceGroup(info.getAppGroupName())) {
					if (this.gateways.size() == 0) {
						gateways.wait(5000);
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

		System.out.println(event.getAppName() + " canceled");
	}

	private boolean isGateWay(String appName) {
		return "GATEWAY".contentEquals(appName);
	}

	private boolean isBusinessServiceGroup(String appGroupName) {
		return "BUSINESS".contentEquals(appGroupName);
	}
}
