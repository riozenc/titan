/**
 * Author : chizf
 * Date : 2020年3月26日 下午2:36:54
 * Title : org.gateway.handler.SecurityRouteLocatorHandler.java
 *
**/
package org.gateway.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@RequestMapping("securityRouteLocator")
public class SecurityRouteLocatorHandler implements ApplicationEventPublisherAware {

	private ApplicationEventPublisher publisher;

	@Autowired
	private RouteDefinitionWriter routeDefinitionWriter;

	@RequestMapping(params = "method=add")
	@ResponseBody
	public String add(@RequestBody String json) {
		
		System.out.println(json);
		
//		Builder builder = routeLocatorBuilder.routes();
//		Builder asyncBuilder = builder.route(r -> r.path("/security/**").filters(f -> f.filter(new PreGatewayFilter()))
//				.uri("lb://SECURITY-SERVER/"));
//		RouteLocator routeLocator = asyncBuilder.build();
		
		
		
//		RouteDefinition routeDefinition = new RouteDefinition();
//		
//		routeDefinition.setFilters(new PreGatewayFilter());
//		routeDefinition.setId(id);
//		routeDefinition.setOrder(order);
//		routeDefinition.setPredicates(predicates);
//		routeDefinition.setUri(uri);;

//		routeDefinitionWriter.save(Mono.just(routeDefinition));
//		notifyChanged();// 发布事件

		return "success";
	}

	private void notifyChanged() {
		this.publisher.publishEvent(new RefreshRoutesEvent(this));
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.publisher = applicationEventPublisher;
	}

}
