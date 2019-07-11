package org.gateway;

import org.gateway.custom.context.SpringContextHolder;
import org.gateway.filter.BemServerFilter;
import org.gateway.filter.BillingServerFilter;
import org.gateway.filter.CfsFilter;
import org.gateway.filter.CimServerFilter;
import org.gateway.filter.PreGatewayFilter;
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
import org.springframework.web.client.RestTemplate;

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
		Builder asyncBuilder = builder.route(r -> r.path("/security/**").filters(f -> f.filter(new PreGatewayFilter()))
				.uri("lb://SECURITY-SERVER/"));
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
		Builder asyncBuilder = builder.route(
				r -> r.path("/auth-data/**").filters(f -> f.filter(new PreGatewayFilter())).uri("lb://AUTH-DATA/"));
		RouteLocator routeLocator = asyncBuilder.build();
		return routeLocator;
	}

	/**
	 * 配置中心
	 * 
	 * @param routeLocatorBuilder
	 * @return
	 */
	@Bean
	public RouteLocator configLocator(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes().route(r -> r.path("/titan-config/**")
				.filters(f -> f.filter(new PreGatewayFilter())).uri("lb://TITAN-CONFIG/")).build();
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
		return routeLocatorBuilder.routes()
				.route(r -> r.path("/bemServer/**")
						.filters(f -> f.filter(SpringContextHolder.getBean(BemServerFilter.class)))
						.uri("lb://BEM-SERVER/"))
				.build();
	}

	/**
	 * 档案服务
	 * 
	 * @param routeLocatorBuilder
	 * @return
	 */
	@Bean
	public RouteLocator cimManagerRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()
				.route(r -> r.path("/cimServer/**")
						.filters(f -> f.filter(SpringContextHolder.getBean(CimServerFilter.class)))
						.uri("lb://CIM-SERVER/"))
				.build();

	}

	/**
	 * billing服务（抄核账）
	 */
	@Bean
	public RouteLocator billingRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes()
				.route(r -> r.path("/billingServer/**")
						.filters(f -> f.filter(SpringContextHolder.getBean(BillingServerFilter.class)))
						.uri("lb://BILLING-SERVER/"))
				.build();

	}

	@Bean
	public RouteLocator cfsRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes().route(r -> r.path("/cfs/**")
				.filters(f -> f.filter(SpringContextHolder.getBean(CfsFilter.class))).uri("lb://CFS/")).build();

	}

}
