package org.gateway;

import org.gateway.filter.AuthTokenFilter;
import org.gateway.filter.CustomFilter;
import org.gateway.filter.PreGatewayFilter;
import org.gateway.handler.AuthorizationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Hello world!
 *
 */

@EnableEurekaClient
@SpringBootApplication
public class GateWayApp {

	private static final Logger LOGGER = LoggerFactory.getLogger(GateWayApp.class);

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(GateWayApp.class, args);
		LOGGER.info("Start GateWayApp Done");
	}

	@Bean
	public RouteLocator securityRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
		Builder builder = routeLocatorBuilder.routes();

		Builder asyncBuilder = builder.route(r -> r.path("/security/{seg}")
				.filters(f -> f.filter(new PreGatewayFilter())).uri("lb://SECURITY-SERVER/"));

		RouteLocator routeLocator = asyncBuilder.build();
		return routeLocator;
	}

	@Bean
	public RouteLocator authRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
		Builder builder = routeLocatorBuilder.routes();
		Builder asyncBuilder = builder.route(
				r -> r.path("/auth/**/{seg}").filters(f -> f.filter(new PreGatewayFilter())).uri("lb://AUTH-CENTER/"));
		// StripPrefix

		RouteLocator routeLocator = asyncBuilder.build();

		return routeLocator;
	}

}
