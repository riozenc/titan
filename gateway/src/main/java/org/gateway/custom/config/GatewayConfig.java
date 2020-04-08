/**
 * Author : chizf
 * Date : 2020年3月31日 下午6:33:27
 * Title : org.gateway.custom.config.GatewayConfig.java
 *
**/
package org.gateway.custom.config;

import org.gateway.custom.context.SpringContextHolder;
import org.gateway.filter.ApiFilter;
import org.gateway.filter.BemServerFilter;
import org.gateway.filter.BillingServerFilter;
import org.gateway.filter.CfsFilter;
import org.gateway.filter.CimServerFilter;
import org.gateway.filter.PreGatewayFilter;
import org.gateway.filter.ReportServerFilter;
import org.gateway.filter.factory.AddAuthenticationBodyGatewayFilterFactory;
import org.gateway.filter.factory.DefaultGatewayFilterFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder.Builder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GatewayConfig {
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public AddAuthenticationBodyGatewayFilterFactory addAuthenticationBodyGatewayFilterFactory() {
		return new AddAuthenticationBodyGatewayFilterFactory();
	}

	@Bean
	public DefaultGatewayFilterFactory defaultGatewayFilterFactory() {
		return new DefaultGatewayFilterFactory();
	}

//	@Bean
//	public RouteLocator testLocator(RouteLocatorBuilder routeLocatorBuilder) {
//		Builder builder = routeLocatorBuilder.routes();
//		Builder asyncBuilder = builder.route(r -> r.path("/security/**").filters(f -> f.filter(new PreGatewayFilter()))
//				.uri("lb://SECURITY-SERVER/"));
//		RouteLocator routeLocator = asyncBuilder.build();
//		return routeLocator;
//	}
//
//	/**
//	 * 认证中心
//	 * 
//	 * @param routeLocatorBuilder
//	 * @return
//	 */
//	@Bean
//	public RouteLocator authRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
//		Builder builder = routeLocatorBuilder.routes();
//		Builder asyncBuilder = builder
//				.route(r -> r.path("/auth/**").filters(f -> f.filter(new PreGatewayFilter())).uri("lb://AUTH-CENTER/"));
//		// StripPrefix
//		RouteLocator routeLocator = asyncBuilder.build();
//		return routeLocator;
//	}
//
//	/**
//	 * 认证数据服务
//	 * 
//	 * @param routeLocatorBuilder
//	 * @return
//	 */
//	@Bean
//	public RouteLocator authDataRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
//		Builder builder = routeLocatorBuilder.routes();
//		Builder asyncBuilder = builder.route(
//				r -> r.path("/auth-data/**").filters(f -> f.filter(new PreGatewayFilter())).uri("lb://AUTH/"));
//		RouteLocator routeLocator = asyncBuilder.build();
//		return routeLocator;
//	}
//
	/**
	 * 配置中心
	 * 
	 * @param routeLocatorBuilder
	 * @return
	 */
	@Bean
	public RouteLocator configLocator(RouteLocatorBuilder routeLocatorBuilder) {
		return routeLocatorBuilder.routes().route(r -> r.path("/config-center/**")
				.filters(f -> f.filter(new PreGatewayFilter())).uri("lb://CONFIG-CENTER/")).build();
	}
//
//	/**
//	 * 用户管理
//	 * 
//	 * @param routeLocatorBuilder
//	 * @return
//	 */
//	@Bean
//	public RouteLocator userManagerRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
//		return routeLocatorBuilder.routes().route(r -> r.path("/userServer/**/{seg}")
//				.filters(f -> f.filter(new PreGatewayFilter())).uri("lb://USER-SERVER/")).build();
//	}
//
//	/**
//	 * 业扩服务
//	 * 
//	 * @param routeLocatorBuilder
//	 * @return
//	 */
//	@Bean
//	public RouteLocator bemManagerRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
//		return routeLocatorBuilder.routes()
//				.route(r -> r.path("/bemServer/**")
//						.filters(f -> f.filter(SpringContextHolder.getBean(BemServerFilter.class)))
//						.uri("lb://BEM-SERVER/"))
//				.build();
//	}
//
//	/**
//	 * 档案服务
//	 * 
//	 * @param routeLocatorBuilder
//	 * @return
//	 */
//	@Bean
//	public RouteLocator cimManagerRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
//		return routeLocatorBuilder.routes()
//				.route(r -> r.path("/cimServer/**")
//						.filters(f -> f.filter(SpringContextHolder.getBean(CimServerFilter.class)))
//						.uri("lb://CIM-SERVER/"))
//				.build();
//
//	}
//
//	/**
//	 * billing服务（抄核账）
//	 */
//	@Bean
//	public RouteLocator billingRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
//		return routeLocatorBuilder.routes()
//				.route(r -> r.path("/billingServer/**")
//						.filters(f -> f.filter(SpringContextHolder.getBean(BillingServerFilter.class)))
//						.uri("lb://BILLING-SERVER/"))
//				.build();
//
//	}
//
//	/**
//	 * 算费服务
//	 * 
//	 * @param routeLocatorBuilder
//	 * @return
//	 */
//	@Bean
//	public RouteLocator cfsRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
//		return routeLocatorBuilder.routes().route(r -> r.path("/cfs/**")
//				.filters(f -> f.filter(SpringContextHolder.getBean(CfsFilter.class))).uri("lb://CFS/")).build();
//
//	}
//
//	/**
//	 * 报表服务
//	 * 
//	 * @param routeLocatorBuilder
//	 * @return
//	 */
//	@Bean
//	public RouteLocator reportRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
//		return routeLocatorBuilder.routes()
//				.route(r -> r.path("/report/**")
//						.filters(f -> f.filter(SpringContextHolder.getBean(ReportServerFilter.class)))
//						.uri("lb://TITAN-REPORT/"))
//				.build();
//	}
//
//	/**
//	 * 三方接口服务
//	 * 
//	 * @param routeLocatorBuilder
//	 * @return
//	 */
//	@Bean
//	public RouteLocator apiRouteLocator(RouteLocatorBuilder routeLocatorBuilder) {
//		return routeLocatorBuilder
//				.routes().route(r -> r.path("/api/**")
//						.filters(f -> f.filter(SpringContextHolder.getBean(ApiFilter.class))).uri("lb://TITAN-API/"))
//				.build();
//	}
}
