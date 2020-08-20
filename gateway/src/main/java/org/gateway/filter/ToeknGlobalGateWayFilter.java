/**
 *    Auth:riozenc
 *    Date:2019年1月3日 上午10:50:17
 *    Title:org.gateway.ToeknGlobalGateWayFilter.java
 **/
package org.gateway.filter;

import java.net.URI;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gateway.handler.AuthorizationHandler;
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

@Component
public class ToeknGlobalGateWayFilter implements GlobalFilter, Ordered {
	private static final Log log = LogFactory.getLog(ToeknGlobalGateWayFilter.class);
	@Autowired
	private RestTemplate restTemplate;

	private boolean isBoot = false;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		if (isIntranetPenetration(exchange.getRequest().getHeaders())) {
			return chain.filter(exchange);
		}

		if (isStaticResources(exchange.getRequest().getURI())) {
			return chain.filter(exchange);
		}

		HttpHeaders httpHeaders = exchange.getRequest().getHeaders();

		if (isBoot) {
			String token = httpHeaders.getFirst(AuthorizationHandler.HEARDS_TOKEN);
			if (log.isDebugEnabled()) {
				log.info(token);
			}

			if (null == token) {
				log.error("ERROR : " + exchange.getRequest().getId() + ":" + exchange.getRequest().getURI()
						+ " token is null!");
				return Mono.error(new Exception("token is null!"));
			}

			// 认证校验
			String result = restTemplate.getForObject("http://AUTH-CENTER/auth/extractToken?token=" + token,
					String.class);
			if (result == null) {
				log.error("ERROR : 空指针异常 extractToken result = " + result);
				return Mono.error(new NullPointerException("extractToken result = " + result));
			}
			try {
				RestObject restObject = new Gson().fromJson(result, RestObject.class);
				if (restObject.isSuccess()) {
					return chain.filter(exchange);
				}
			} catch (Exception e) {
				log.error("ERROR : " + result + " exception:" + e);
			}
			return Mono.error(new Exception(result));
		} else {
			return chain.filter(exchange);
		}

	}

	public boolean isStaticResources(URI uri) {
		if (uri.getPath().contains("static")) {
			return true;
		}

		return false;
	}

	private boolean isIntranetPenetration(HttpHeaders httpHeaders) {
		List<String> tokens = httpHeaders.get("sign");
		if (tokens == null) {
			return false;
		}
		for (String token : tokens) {
			if ("hegang".equals(token)) {
				return true;
			}
		}
		return false;
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
