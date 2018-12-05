/**
 *    Auth:riozenc
 *    Date:2018年12月5日 下午5:25:31
 *    Title:org.eureka.client.HelloController.java
 **/
package org.eureka.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Autowired
	private DiscoveryClient client;

	@RequestMapping(value = "hello", method = RequestMethod.GET)
	public String index() {
		List<String> services = client.getServices();
		for (String temp : services) {
			logger.info("/hello, host:" + temp);
		}
//		logger.info("/hello, host:" + instance.getHost() + ", service_id:" + instance.getServiceId());
		return "Hello World123";
	}
}
