/**
 * Author : chizf
 * Date : 2020年9月16日 上午11:20:01
 * Title : org.gateway.handler.r.GatewayDynamicConfigurationHandler.java
 *
**/
package org.gateway.handler.r;

import java.net.URI;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gateway.route.RouteDefinitionReader;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import reactor.core.publisher.Mono;

@ControllerAdvice
@RequestMapping("gatewayDynamicConfiguration")
public class GatewayDynamicConfigurationHandler implements ApplicationEventPublisherAware {
	private static final Log log = LogFactory.getLog(GatewayDynamicConfigurationHandler.class);

	protected ApplicationEventPublisher publisher;

	protected List<GatewayFilterFactory> GatewayFilters;

	protected List<RoutePredicateFactory> routePredicates;

	private RouteDefinitionReader routeDefinitionReader;

	private RouteDefinitionWriter routeDefinitionWriter;

	public GatewayDynamicConfigurationHandler(List<GatewayFilterFactory> gatewayFilters,
			List<RoutePredicateFactory> routePredicates, RouteDefinitionReader routeDefinitionReader,
			RouteDefinitionWriter routeDefinitionWriter) {
		this.GatewayFilters = gatewayFilters;
		this.routePredicates = routePredicates;
		this.routeDefinitionReader = routeDefinitionReader;
		this.routeDefinitionWriter = routeDefinitionWriter;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@RequestMapping("/routes/refush")
	public Mono<ResponseEntity<Object>> refush() {
		return Mono.defer(() -> routeDefinitionReader.refush().doOnSuccess((a) -> {
			// 更新到内存中
			this.publisher.publishEvent(new RefreshRoutesEvent(this));
		}).then(Mono.defer(() -> Mono.just(ResponseEntity.created(URI.create("/routes/")).build()))))
				.switchIfEmpty(Mono.defer(() -> Mono.just(ResponseEntity.badRequest().build())));
	}

	@PostMapping("/routes/save/{id}")
	public Mono<ResponseEntity<String>> save(@PathVariable String id, @RequestBody RouteDefinition route) {

		return Mono.just(route).filter(this::validateRouteDefinition)
				.flatMap(routeDefinition -> this.routeDefinitionWriter.save(Mono.just(routeDefinition).map(r -> {
					r.setId(id);
					log.debug("Saving route: " + route);
					return r;
				})).then(Mono.defer(() -> Mono.just(ResponseEntity.created(URI.create("/routes/" + id)).body("成功")))))
				.switchIfEmpty(Mono.defer(() -> Mono.just(ResponseEntity.badRequest().body("新增失败."))));
	}

//	@PostMapping("/routes/save/{id}")
//	@SuppressWarnings("unchecked")
//	public Mono<ResponseEntity<Object>> save(@PathVariable String id, @RequestBody RouteDefinition route) {
//
//		return Mono.just(route).filter(this::validateRouteDefinition)
//				.flatMap(routeDefinition -> this.routeDefinitionWriter.save(Mono.just(routeDefinition).map(r -> {
//					r.setId(id);
//					log.debug("Saving route: " + route);
//					return r;
//				})).then(Mono.defer(() -> Mono.just(ResponseEntity.created(URI.create("/routes/" + id)).build()))))
//				.switchIfEmpty(Mono.defer(() -> Mono.just(ResponseEntity.badRequest().build())));
//	}

	private boolean validateRouteDefinition(RouteDefinition routeDefinition) {
		boolean hasValidFilterDefinitions = routeDefinition.getFilters().stream()
				.allMatch(filterDefinition -> GatewayFilters.stream().anyMatch(
						gatewayFilterFactory -> filterDefinition.getName().equals(gatewayFilterFactory.name())));

		boolean hasValidPredicateDefinitions = routeDefinition.getPredicates().stream()
				.allMatch(predicateDefinition -> routePredicates.stream()
						.anyMatch(routePredicate -> predicateDefinition.getName().equals(routePredicate.name())));
		log.debug("FilterDefinitions valid: " + hasValidFilterDefinitions);
		log.debug("PredicateDefinitions valid: " + hasValidPredicateDefinitions);
		return hasValidFilterDefinitions && hasValidPredicateDefinitions;
	}
}
