/**
 *    Auth:riozenc
 *    Date:2019年1月3日 上午11:45:04
 *    Title:org.gateway.CustomFilter.java
 **/
package org.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;


public class CustomFilter implements GlobalFilter, Ordered {

	private final Logger logger = LoggerFactory.getLogger(getClass());
	private static final String COUNT_Start_TIME = "countStartTime";

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// TODO Auto-generated method stub
		exchange.getAttributes().put(COUNT_Start_TIME, System.currentTimeMillis());
		return chain.filter(exchange).then(Mono.fromRunnable(() -> {
			Long startTime = exchange.getAttribute(COUNT_Start_TIME);
			Long endTime = (System.currentTimeMillis() - startTime);
			if (startTime != null) {
				logger.info("CustomFilter---"+exchange.getRequest().getURI().getRawPath() + ": " + endTime + "ms");
			}
		}));
	}

}
