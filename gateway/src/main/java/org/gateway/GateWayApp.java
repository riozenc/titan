package org.gateway;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * Hello world!
 *
 */
@RestController
@SpringBootApplication
@EnableEurekaClient
public class GateWayApp {

	private static final Logger LOGGER = LoggerFactory.getLogger(GateWayApp.class);

	public static void main(String[] args) {
//		RoutePredicateHandlerMapping
		SpringApplication.run(GateWayApp.class, args);
		LOGGER.info(" Start GateWayApp Done");
	}

	@Bean
	public RouteLocator routeLocator(RouteLocatorBuilder routeLocatorBuilder) {
		Builder builder = routeLocatorBuilder.routes();

		Builder asyncBuilder = builder
				.route(r -> r.path("/get").uri("http://www.baidu.com"));

		RouteLocator routeLocator = asyncBuilder.build();
		return routeLocator;
	}

}
