/**
 *    Auth:riozenc
 *    Date:2019年1月7日 上午11:10:57
 *    Title:org.gateway.PreGatewayFilter.java
 **/
package org.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class PreGatewayFilter implements GatewayFilter{

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// TODO Auto-generated method stub
		
		System.out.println("PreGatewayFilter === filter");
		
		return chain.filter(exchange);
	}

}
