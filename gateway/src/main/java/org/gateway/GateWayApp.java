package org.gateway;

import org.gateway.custom.context.SpringContextHolder;
import org.gateway.exception.handler.JsonExceptionHandler;
import org.gateway.filter.BemServerFilter;
import org.gateway.filter.ExceptionGlobalFilter;
import org.gateway.filter.PreGatewayFilter;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.config.GlobalCorsProperties;
import org.springframework.cloud.gateway.handler.FilteringWebHandler;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;

import reactor.core.publisher.Mono;

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
	public RouteLocator testLocator(RouteLocatorBuilder routeLocatorBuilder) {
		Builder builder = routeLocatorBuilder.routes();
		Builder asyncBuilder = builder.route(r -> r.path("/security/**")
				.filters(f -> f.modifyResponseBody(String.class, String.class, (exchange, body) -> {
					return Mono.just(body);
				})).uri("lb://SECURITY-SERVER/"));

		RouteLocator routeLocator = asyncBuilder.build();
		return routeLocator;
	}

	/**
	 * 认证中心
	 * 
	 * @param routeLocatorBuilder
	 * @return
	 */
	@Bean
	public RouteLocator authRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
		Builder builder = routeLocatorBuilder.routes();
		Builder asyncBuilder = builder.route(
				r -> r.path("/auth/**/{seg}").filters(f -> f.filter(new PreGatewayFilter())).uri("lb://AUTH-CENTER/"));
		// StripPrefix
		RouteLocator routeLocator = asyncBuilder.build();
		return routeLocator;
	}

	/**
	 * 认证数据服务
	 * 
	 * @param routeLocatorBuilder
	 * @return
	 */
	@Bean
	public RouteLocator authDataRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
		Builder builder = routeLocatorBuilder.routes();
		Builder asyncBuilder = builder.route(r -> r.path("/auth-data/**")
//		Builder asyncBuilder = builder.route(r -> r.path("/auth-data/**/{seg}")
				.filters(f -> f.filter(new PreGatewayFilter())).uri("lb://AUTH-DATA/"));
		RouteLocator routeLocator = asyncBuilder.build();
		return routeLocator;
	}

	/**
	 * 用户管理
	 * 
	 * @param routeLocatorBuilder
	 * @return
	 */
	@Bean
	public RouteLocator userManagerRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {

		return routeLocatorBuilder.routes().route(r -> r.path("/userServer/**/{seg}")
				.filters(f -> f.filter(new PreGatewayFilter())).uri("lb://USER-SERVER/")).build();
	}

	/**
	 * 业扩服务
	 * 
	 * @param routeLocatorBuilder
	 * @return
	 */
	@Bean
	public RouteLocator bemManagerRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
//		return routeLocatorBuilder.routes()
//				.route(r -> r.path("/bemServer/**")
//						.filters(f -> f.filter(SpringContextHolder.getBean(BemServerFilter.class)))
//						.uri("lb://BEM-SERVER/"))
//				.build();

		return routeLocatorBuilder.routes().route(r -> r.readBody(String.class, requestBody -> {
			System.out.println(requestBody);
			// 这里不对body做判断处理
			return true;
		}).and().path("/bemServer/**").filters(f -> f.filter(SpringContextHolder.getBean(BemServerFilter.class)))
				.uri("lb://BEM-SERVER/")).build();

	}
	
	
	

}
