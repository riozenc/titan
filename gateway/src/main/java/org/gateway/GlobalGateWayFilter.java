/**
 *    Auth:riozenc
 *    Date:2019年1月3日 上午10:50:17
 *    Title:org.gateway.GlobalGateWayFilter.java
 **/
package org.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class GlobalGateWayFilter implements GlobalFilter {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// TODO Auto-generated method stub
		
		System.out.println("GlobalGateWayFilter---"+exchange.getRequest().getRemoteAddress());
		
		return chain.filter(exchange);
	}

}
