/**
 * Author : chizf
 * Date : 2020年4月8日 上午9:35:38
 * Title : org.gateway.filter.factory.DefaultGatewayFilterFactory.java
 *
**/
package org.gateway.filter.factory;

import static org.springframework.cloud.gateway.support.GatewayToStringStyler.filterToStringCreator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class DefaultGatewayFilterFactory extends AbstractGatewayFilterFactory<DefaultGatewayFilterFactory.Config> {

	private static final Log log = LogFactory.getLog(DefaultGatewayFilterFactory.class);

	public DefaultGatewayFilterFactory() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {

		String replacement = config.replacement.replace("$\\", "$");
		return new GatewayFilter() {
			@Override
			public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

				log.debug(this.toString());

				return chain.filter(exchange);
			}

			@Override
			public String toString() {
				return filterToStringCreator(DefaultGatewayFilterFactory.this).append(config.getRegexp(), replacement)
						.toString();
			}
		};

	}

	public static class Config {

		private String regexp;

		private String replacement;

		public String getRegexp() {
			return regexp;
		}

		public Config setRegexp(String regexp) {
			this.regexp = regexp;
			return this;
		}

		public String getReplacement() {
			return replacement;
		}

		public Config setReplacement(String replacement) {
			this.replacement = replacement;
			return this;
		}

	}

}