/**
 * Author : chizf
 * Date : 2020年3月26日 下午2:36:54
 * Title : org.gateway.handler.SecurityRouteLocatorHandler.java
 *
**/
package org.gateway.handler;

import java.net.URI;

import org.gateway.handler.entity.RouteRegistrationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import reactor.core.publisher.Mono;

@ControllerAdvice
@RequestMapping("securityRouteLocator")
public class SecurityRouteLocatorHandler implements ApplicationEventPublisherAware {

	private final static String REGEXP_STRING = "/(?<remaining>.*)";
	private final static String REPLACEMENT_STRING = "/${remaining}";

	private ApplicationEventPublisher publisher;

	@Autowired
	private RouteDefinitionWriter routeDefinitionWriter;

	@RequestMapping(params = "method=saveRoute")
	@ResponseBody
	public Object saveRoute(@RequestBody RouteRegistrationEntity routeRegistrationEntity) {

		RouteDefinition routeDefinition = new RouteDefinition();
		for (String filter : routeRegistrationEntity.getFilters().split(",")) {
			FilterDefinition filterDefinition = new FilterDefinition();
			filterDefinition.setName(filter);// ***关键，需要匹配gatewayFilterFactory里的类
			filterDefinition.addArg("regexp", "/" + routeRegistrationEntity.getAppName().toUpperCase() + REGEXP_STRING);// "/CONFIG-CLIENT"
			filterDefinition.addArg("replacement", REPLACEMENT_STRING);
			routeDefinition.getFilters().add(filterDefinition);
		}

		// Path=/config-client/**
		PredicateDefinition definition = new PredicateDefinition("Path=" + routeRegistrationEntity.getPredicates());
		routeDefinition.getPredicates().add(definition);

		routeDefinition.setId(routeRegistrationEntity.getId());
		routeDefinition.setOrder(routeRegistrationEntity.getOrder());
		routeDefinition.setUri(URI.create(routeRegistrationEntity.getUri()));

		routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
		notifyChanged();// 发布事件
		return routeDefinition;
	}

	private void notifyChanged() {
		this.publisher.publishEvent(new RefreshRoutesEvent(this));
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.publisher = applicationEventPublisher;
	}

}
