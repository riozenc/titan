/**
 * Author : chizf
 * Date : 2020年3月31日 下午3:51:43
 * Title : org.gateway.filter.factory.AddAuthenticationBodyGatewayFilterFactory.java
 *
**/
package org.gateway.filter.factory;

import static org.springframework.cloud.gateway.support.GatewayToStringStyler.filterToStringCreator;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gateway.handler.AuthorizationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AddAuthenticationBodyGatewayFilterFactory
		extends AbstractGatewayFilterFactory<AddAuthenticationBodyGatewayFilterFactory.Config> {

	private static final Log log = LogFactory.getLog(AddAuthenticationBodyGatewayFilterFactory.class);
	private final List<HttpMessageReader<?>> messageReaders;
	@Autowired
	private AuthorizationHandler authorizationHandler;

	public AddAuthenticationBodyGatewayFilterFactory() {
		super(Config.class);
		this.messageReaders = HandlerStrategies.withDefaults().messageReaders();
	}

	public AddAuthenticationBodyGatewayFilterFactory(List<HttpMessageReader<?>> messageReaders) {
		super(Config.class);
		this.messageReaders = messageReaders;
	}

	private String getUserId(String token) throws Exception {
		return authorizationHandler.getUser(token);
	}

	private String getRoleIds(String token) throws Exception {
		return authorizationHandler.getRoles(token);
	}

	private String getDeptIds(String token) throws Exception {
		return authorizationHandler.getDepts(token);
	}

	@Override
	public GatewayFilter apply(Config config) {
		return new GatewayFilter() {

			@Override
			public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

				String token = exchange.getRequest().getHeaders().getFirst(AuthorizationHandler.HEARDS_TOKEN);
				ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);
				try {
					String managerId = getUserId(token);
					String roleIds = getRoleIds(token);
					String deptIds = getDeptIds(token);
					// TODO: flux or mono
					Mono<String> modifiedBody = serverRequest.bodyToMono(String.class)
							// .log("modify_request_mono", Level.INFO)
							.flatMap(body -> {
								String params = changeBody(exchange.getRequest(), body, managerId, roleIds, deptIds);
								return Mono.just(params);
							}).defaultIfEmpty(tamperWithJson(null, managerId, roleIds, deptIds));

					BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters
							.fromPublisher(modifiedBody, String.class);
					HttpHeaders headers = new HttpHeaders();
					headers.putAll(exchange.getRequest().getHeaders());
					headers.remove(HttpHeaders.CONTENT_LENGTH);
					CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);

					return bodyInserter.insert(outputMessage, new BodyInserterContext())
							// .log("modify_request", Level.INFO)
							.then(Mono.defer(() -> {
								ServerHttpRequest decorator = decorate(exchange, headers, outputMessage);
								return chain.filter(exchange.mutate().request(decorator).build());
							}));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					log.error(e);
					return Mono.error(e);
				}

			}

			@Override
			public String toString() {
				return filterToStringCreator(AddAuthenticationBodyGatewayFilterFactory.this).append("").toString();
			}

		};
	}

	private ServerHttpRequestDecorator decorate(ServerWebExchange exchange, HttpHeaders headers,
			CachedBodyOutputMessage outputMessage) {
		return new ServerHttpRequestDecorator(exchange.getRequest()) {
			@Override
			public HttpHeaders getHeaders() {
				long contentLength = headers.getContentLength();
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.putAll(headers);
				if (contentLength > 0) {
					httpHeaders.setContentLength(contentLength);
				} else {
					// TODO: this causes a 'HTTP/1.1 411 Length Required' // on
					// httpbin.org
					httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
				}
				return httpHeaders;
			}

			@Override
			public Flux<DataBuffer> getBody() {
				return outputMessage.getBody();
			}
		};
	}

	private String changeBody(ServerHttpRequest serverHttpRequest, String body, String managerId, String roleIds,
			String deptIds) {
		MediaType mediaType = serverHttpRequest.getHeaders().getContentType();
		if (mediaType == null) {
			return tamperWithJson(body, managerId, roleIds, deptIds);
		}
		if (mediaType.includes(MediaType.MULTIPART_FORM_DATA)) {
			return body;
		}
		if (!mediaType.includes(MediaType.APPLICATION_FORM_URLENCODED)) {
			return tamperWithJson(body, managerId, roleIds, deptIds);
		} else {
			return tamperWithForm(body, managerId, roleIds, deptIds);
		}
	}

	private String tamperWithJson(String body, String userId, String roleIds, String deptIds) {
		Gson gson = new Gson();
		JsonElement jsonElement = body == null ? new JsonObject() : gson.fromJson(body, JsonElement.class);
		if (jsonElement.isJsonObject()) {
			jsonElement.getAsJsonObject().addProperty(AuthorizationHandler.MANAGER_ID, userId);
			jsonElement.getAsJsonObject().addProperty(AuthorizationHandler.ROLE_IDS, roleIds);
			jsonElement.getAsJsonObject().addProperty(AuthorizationHandler.DEPT_IDS, deptIds);
		}
		return jsonElement.toString();
	}

	private String tamperWithForm(String body, String userId, String roleIds, String deptIds) {
		return new StringBuilder(null == body ? "" : body).append("&").append(AuthorizationHandler.MANAGER_ID)
				.append("=").append(userId).append("&").append(AuthorizationHandler.ROLE_IDS).append("=")
				.append(roleIds).append("&").append(AuthorizationHandler.DEPT_IDS).append("=").append(deptIds)
				.toString();
	}

	public static class Config {

		private boolean userId;
		private boolean roleIds;
		private boolean deptIds;

		public boolean getUserId() {
			return userId;
		}

		public Config setUserId(boolean userId) {
			this.userId = userId;
			return this;
		}

		public boolean getRoleIds() {
			return roleIds;
		}

		public Config setRoleIds(boolean roleIds) {
			this.roleIds = roleIds;
			return this;
		}

		public boolean getDeptIds() {
			return deptIds;
		}

		public Config setDeptIds(boolean deptIds) {
			this.deptIds = deptIds;
			return this;
		}

	}

}
