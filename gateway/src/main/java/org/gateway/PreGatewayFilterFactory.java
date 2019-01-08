/**
 *    Auth:riozenc
 *    Date:2019年1月3日 上午8:21:04
 *    Title:org.gateway.PreGatewayFilterFactory.java
 **/
package org.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;


public class PreGatewayFilterFactory extends AbstractGatewayFilterFactory<PreGatewayFilterFactory.Config> {
	public PreGatewayFilterFactory() {
		super(Config.class);
	}

	@Override
	public GatewayFilter apply(Config config) {
		// grab configuration from Config object
		
		System.out.println("asd");
		
		return (exchange, chain) -> {
			// If you want to build a "pre" filter you need to manipulate the
			// request before calling change.filter
			ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
			// use builder to manipulate the request
			return chain.filter(exchange.mutate().request(builder.build()).build());
		};
	}

	public static class Config {
		// Put the configuration properties for your filter here
		private String name;
		private String url;
		private String status;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}
	}
}
