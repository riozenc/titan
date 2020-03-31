/**
 * Author : chizf
 * Date : 2020年3月31日 上午8:45:39
 * Title : org.gateway.filter.factory.TestFactory.java
 *
**/
package org.gateway.filter.factory;

import static org.springframework.cloud.gateway.support.GatewayToStringStyler.filterToStringCreator;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.RewritePathGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class TestFactory extends AbstractGatewayFilterFactory<TestFactory.Config> {

	/**
	 * Regexp key.
	 */
	public static final String REGEXP_KEY = "regexp";

	/**
	 * Replacement key.
	 */
	public static final String REPLACEMENT_KEY = "replacement";

	public TestFactory() {
		super(Config.class);
	}

	@Override
	public List<String> shortcutFieldOrder() {
		return Arrays.asList(REGEXP_KEY, REPLACEMENT_KEY);
	}

//	@Override
//	public GatewayFilter apply(Config config) {
//		System.out.println(config);
//		return new PreGatewayFilter();
//	}

	@Override
	public GatewayFilter apply(Config config) {
		String replacement = config.replacement.replace("$\\", "$");
		return new GatewayFilter() {
			@Override
			public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
				ServerHttpRequest req = exchange.getRequest();
				addOriginalRequestUrl(exchange, req.getURI());
				String path = req.getURI().getRawPath();
				String newPath = path.replaceAll(config.regexp, replacement);

				ServerHttpRequest request = req.mutate().path(newPath).build();

				exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, request.getURI());

				return chain.filter(exchange.mutate().request(request).build());
			}

			@Override
			public String toString() {
				return filterToStringCreator(TestFactory.this)
						.append(config.getRegexp(), replacement).toString();
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
