/**
 * Author : chizf
 * Date : 2020年9月16日 下午7:42:49
 * Title : org.gateway.filter.factory.PreGatewayFilterFactory.java
 *
**/
package org.gateway.filter.factory;

import static org.springframework.cloud.gateway.support.GatewayToStringStyler.filterToStringCreator;

import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.style.ToStringCreator;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class PreGatewayFilterFactory extends AbstractGatewayFilterFactory<PreGatewayFilterFactory.Config> {

	public PreGatewayFilterFactory() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {

		return new GatewayFilter() {
			@Override
			public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

				Map<String, String> map = exchange
						.getAttribute(ServerWebExchangeUtils.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

				return chain.filter(exchange);
			}

			@Override
			public String toString() {
				return filterToStringCreator(PreGatewayFilterFactory.this).toString();
			}
		};

	}

	public static class Config {

		private String method;

		public String getMethod() {
			return method;
		}

		public Config setMethod(String method) {
			this.method = method;
			return this;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this).append("method", method).toString();
		}

	}
}
