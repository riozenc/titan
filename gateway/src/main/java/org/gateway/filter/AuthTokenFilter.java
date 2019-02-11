/**
 *    Auth:riozenc
 *    Date:2019年1月26日 上午10:14:56
 *    Title:org.gateway.filter.AuthTokenFilter.java
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

public class AuthTokenFilter implements GatewayFilter {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// TODO Auto-generated method stub
		
//		AUTH-CENTER  /auth/oauth/token_key
		System.out.println("AuthTokenFilter === filter");

		PathPattern.PathMatchInfo pp = exchange.getAttribute(ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

		Map<String, MultiValueMap<String, String>> m1 = pp.getMatrixVariables();
		Map<String, String> m2 = pp.getUriVariables();

		return chain.filter(exchange);
	}

}
