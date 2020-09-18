/**
 * Author : chizf
 * Date : 2020年9月16日 上午11:20:01
 * Title : org.gateway.handler.r.GatewayDynamicConfigurationHandler.java
 *
**/
package org.gateway.handler.r;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gateway.route.RouteDefinitionReader;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.cloud.gateway.handler.predicate.RoutePredicateFactory;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import reactor.core.publisher.Mono;

@ControllerAdvice
@RequestMapping("gatewayDynamicConfiguration")
public class GatewayDynamicConfigurationHandler implements ApplicationEventPublisherAware {
	private static final Log log = LogFactory.getLog(GatewayDynamicConfigurationHandler.class);

	private RouteDefinitionLocator routeDefinitionLocator;

	private List<GlobalFilter> globalFilters;

	// TODO change casing in next major release
	private List<GatewayFilterFactory> GatewayFilters;

	private List<RoutePredicateFactory> routePredicates;

	private RouteDefinitionWriter routeDefinitionWriter;

	private RouteLocator routeLocator;

	private ApplicationEventPublisher publisher;

	private RouteDefinitionReader routeDefinitionReader;

	public GatewayDynamicConfigurationHandler(RouteDefinitionLocator routeDefinitionLocator,
			List<GlobalFilter> globalFilters, List<GatewayFilterFactory> gatewayFilters,
			List<RoutePredicateFactory> routePredicates, RouteDefinitionReader routeDefinitionReader,
			RouteDefinitionWriter routeDefinitionWriter, RouteLocator routeLocator) {
		this.routeDefinitionLocator = routeDefinitionLocator;
		this.globalFilters = globalFilters;
		this.GatewayFilters = gatewayFilters;
		this.routePredicates = routePredicates;
		this.routeDefinitionReader = routeDefinitionReader;
		this.routeDefinitionWriter = routeDefinitionWriter;
		this.routeLocator = routeLocator;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@GetMapping("/routes")
	@ResponseBody
	public Mono<List<Map<String, Object>>> routes() {
		Mono<Map<String, RouteDefinition>> routeDefs = this.routeDefinitionLocator.getRouteDefinitions()
				.collectMap(RouteDefinition::getId);
		Mono<List<Route>> routes = this.routeLocator.getRoutes().collectList();
		return Mono.zip(routeDefs, routes).map(tuple -> {
			Map<String, RouteDefinition> defs = tuple.getT1();
			List<Route> routeList = tuple.getT2();
			List<Map<String, Object>> allRoutes = new ArrayList<>();

			routeList.forEach(route -> {
				HashMap<String, Object> r = new HashMap<>();
				r.put("route_id", route.getId());
				r.put("order", route.getOrder());

				if (defs.containsKey(route.getId())) {
					r.put("route_definition", defs.get(route.getId()));
				} else {
					HashMap<String, Object> obj = new HashMap<>();

					obj.put("predicate", route.getPredicate().toString());

					if (!route.getFilters().isEmpty()) {
						ArrayList<String> filters = new ArrayList<>();
						for (GatewayFilter filter : route.getFilters()) {
							filters.add(filter.toString());
						}

						obj.put("filters", filters);
					}

					if (!CollectionUtils.isEmpty(route.getMetadata())) {
						obj.put("metadata", route.getMetadata());
					}

					if (!obj.isEmpty()) {
						r.put("route_object", obj);
					}
				}
				allRoutes.add(r);
			});

			return allRoutes;
		});
	}

	@RequestMapping("/routes/refush")
	public Mono<ResponseEntity<String>> refush() {
		return Mono.defer(() -> routeDefinitionReader.refush().doFinally((a) -> {
			System.out.println(a);
			log.info("Refush route: " + a);
			this.publisher.publishEvent(new RefreshRoutesEvent(this));
		}).then(Mono.defer(() -> Mono.just(ResponseEntity.created(URI.create("/routes/")).body("刷新成功.")))))
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

	@DeleteMapping("/routes/{id}")
	public Mono<ResponseEntity<Object>> delete(@PathVariable String id) {
		return this.routeDefinitionWriter.delete(Mono.just(id))
				.then(Mono.defer(() -> Mono.just(ResponseEntity.ok().build())))
				.onErrorResume(t -> t instanceof NotFoundException, t -> Mono.just(ResponseEntity.notFound().build()));
	}

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
