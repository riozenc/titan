/**
 * Author : chizf
 * Date : 2020年9月15日 下午6:04:14
 * Title : org.gateway.handler.r.TestGatewayService.java
 *
**/
package org.gateway.handler.r;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Mono;

@Service
public class TestGatewayService implements ApplicationEventPublisherAware {

	@Autowired
	private RouteDefinitionWriter routeDefinitionWriter;
	private ApplicationEventPublisher publisher;

	public String save() {
		RouteDefinition definition = new RouteDefinition();
		PredicateDefinition predicate = new PredicateDefinition();
		Map<String, String> predicateParams = new HashMap<>(8);

		definition.setId("baiduRoute");
		predicate.setName("Path");
		// 请替换成本地可访问的路径
		predicateParams.put("pattern", "/baidu");
		// 请替换成本地可访问的路径
		predicateParams.put("pathPattern", "/baidu");
		predicate.setArgs(predicateParams);
		definition.setPredicates(Arrays.asList(predicate));
		// 请替换成本地可访问的域名
		URI uri = UriComponentsBuilder.fromHttpUrl("http://www.baidu.com").build().toUri();
		definition.setUri(uri);
		routeDefinitionWriter.save(Mono.just(definition)).subscribe();
		this.publisher.publishEvent(new RefreshRoutesEvent(this));
		return "success";
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.publisher = applicationEventPublisher;
	}
}