/**
 *    Auth:riozenc
 *    Date:2019年1月3日 上午10:50:17
 *    Title:org.gateway.ToeknGlobalGateWayFilter.java
 **/
package org.gateway.filter;

import java.net.URI;

import org.gateway.handler.AuthorizationHandler.RestObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import com.google.gson.Gson;

import reactor.core.publisher.Mono;

//@Component
public class ToeknGlobalGateWayFilter implements GlobalFilter, Ordered {

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// TODO Auto-generated method stub

		HttpHeaders httpHeaders = exchange.getRequest().getHeaders();

		String token = httpHeaders.getFirst("Authorization");

		if (null == token) {
			return Mono.error(new Exception("token is null!"));
		}

		// 认证校验
		String result = restTemplate.getForObject("http://AUTH-CENTER/auth/extractToken?token=" + token, String.class);
		if (result == null) {
			return Mono.error(new NullPointerException("extractToken result = " + result));
		}
		RestObject restObject = new Gson().fromJson(result, RestObject.class);

		if (restObject.isSuccess()) {
			return chain.filter(exchange);
		}
		return Mono.error(new Exception(result));
	}

	public boolean isSecurityURI(URI uri) {
		if (uri.getPath().contains("security")) {
			return true;
		}
		return false;
	}

	@Override
	public int getOrder() {
		// 最高优先级
		return HIGHEST_PRECEDENCE;
	}
}
