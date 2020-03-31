/**
 * Author : chizf
 * Date : 2020年3月26日 下午2:36:54
 * Title : org.gateway.handler.SecurityRouteLocatorHandler.java
 *
**/
package org.gateway.handler;

import java.net.URI;
import java.util.Arrays;

import org.gateway.handler.entity.RegistrationGatewayEntity;
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

	@RequestMapping(params = "method=add")
	@ResponseBody
	public String add() {

		RouteDefinition routeDefinition = new RouteDefinition();
		FilterDefinition filterDefinition = new FilterDefinition();
		filterDefinition.setName("TestFactory");
		filterDefinition.addArg("regexp", "/CONFIG-CLIENT/(?<remaining>.*)");
		filterDefinition.addArg("replacement", "/${remaining}");

		PredicateDefinition predicateDefinition = new PredicateDefinition("Path=/config-client/**");

		routeDefinition.setId("asdc");
		routeDefinition.setFilters(Arrays.asList(filterDefinition));
		routeDefinition.setOrder(1);
		routeDefinition.setPredicates(Arrays.asList(predicateDefinition));
		routeDefinition.setUri(URI.create("lb://CONFIG-CLIENT/"));

		routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
		notifyChanged();// 发布事件
		return "success";
	}

	@RequestMapping(params = "method=add1")
	@ResponseBody
	public Object add1(@RequestBody RegistrationGatewayEntity registrationGatewayEntity) {

		RouteDefinition routeDefinition = new RouteDefinition();
		for (String filter : registrationGatewayEntity.getFilters().split(",")) {
			FilterDefinition filterDefinition = new FilterDefinition();
			filterDefinition.setName(filter);// ***关键，需要匹配gatewayFilterFactory里的类
			filterDefinition.addArg("regexp",
					"/" + registrationGatewayEntity.getAppName().toUpperCase() + REGEXP_STRING);// "/CONFIG-CLIENT"
			filterDefinition.addArg("replacement", REPLACEMENT_STRING);
			routeDefinition.getFilters().add(filterDefinition);
		}

		// Path=/config-client/**
		PredicateDefinition definition = new PredicateDefinition("Path=" + registrationGatewayEntity.getPredicates());
		routeDefinition.getPredicates().add(definition);

		routeDefinition.setId(registrationGatewayEntity.getId());
		routeDefinition.setOrder(registrationGatewayEntity.getOrder());
		routeDefinition.setUri(URI.create(registrationGatewayEntity.getUri()));

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

	public static void main(String[] args) {
		String s = "czy=http://localhost:8888,path=/u,path=/a";
		RouteDefinition routeDefinition = new RouteDefinition(s);
		System.out.println(s);
	}

}
