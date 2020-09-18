/**
 * Author : chizf
 * Date : 2020年9月16日 上午10:16:27
 * Title : org.gateway.config.CustomGatewayAutoConfiguration.java
 *
**/
package org.gateway.config;

import org.gateway.filter.factory.PreGatewayFilterFactory;
import org.gateway.filter.factory.SupplementManagerGatewayFilterFactory;
import org.gateway.handler.AuthorizationHandler;
import org.gateway.route.InRedisRouteDefinitionRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.gateway.route.InMemoryRouteDefinitionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

@Configuration
public class CustomGatewayAutoConfiguration {

//	@Bean
//	public GatewayDynamicConfigurationHandler gatewayDynamicConfigurationHandler(List<GlobalFilter> globalFilters,
//			List<GatewayFilterFactory> gatewayFilters, List<RoutePredicateFactory> routePredicates,
//			RouteDefinitionReader routeDefinitionReader, RouteLocator routeLocator) {
//		return new GatewayDynamicConfigurationHandler(gatewayFilters, routePredicates, routeDefinitionReader);
//	}

	@Bean
	@ConditionalOnMissingBean(InMemoryRouteDefinitionRepository.class)
	public InRedisRouteDefinitionRepository inRedisRouteDefinitionRepository(
			ReactiveStringRedisTemplate redisTemplate) {
		return new InRedisRouteDefinitionRepository(redisTemplate);
	}

	@Bean
	public PreGatewayFilterFactory preGatewayFilterFactory() {
		return new PreGatewayFilterFactory();
	}

	@Bean
	public SupplementManagerGatewayFilterFactory supplementManagerGatewayFilterFactory(
			AuthorizationHandler authorizationHandler) {
		return new SupplementManagerGatewayFilterFactory(authorizationHandler);
	}

}
