/**
 *    Auth:riozenc
 *    Date:2019年1月7日 上午11:10:57
 *    Title:org.gateway.PreGatewayFilter.java
 **/
package org.gateway.filter;

import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;

import reactor.core.publisher.Mono;

public class PreGatewayFilter implements GatewayFilter {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// TODO Auto-generated method stub

		PathPattern.PathMatchInfo pp = exchange.getAttribute(ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

		Map<String, MultiValueMap<String, String>> m1 = pp.getMatrixVariables();
		Map<String, String> m2 = pp.getUriVariables();
//		Map<String, String> uriVariables = ServerWebExchangeUtils.getPathPredicateVariables(exchange);
//
//		String segment = uriVariables.get("segment");
//
//		System.out.println(segment);

		return chain.filter(exchange);
	}

}
