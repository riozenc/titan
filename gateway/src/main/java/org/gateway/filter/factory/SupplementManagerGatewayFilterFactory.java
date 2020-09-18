/**
 * Author : chizf
 * Date : 2020年9月18日 下午3:02:05
 * Title : org.gateway.filter.factory.SupplementManagerFilterFactory.java
 *
**/
package org.gateway.filter.factory;

import static org.springframework.cloud.gateway.support.GatewayToStringStyler.filterToStringCreator;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gateway.handler.AuthorizationHandler;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.style.ToStringCreator;
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

public class SupplementManagerGatewayFilterFactory
		extends AbstractGatewayFilterFactory<SupplementManagerGatewayFilterFactory.Config> {
	private static final Log log = LogFactory.getLog(SupplementManagerGatewayFilterFactory.class);
	private final List<HttpMessageReader<?>> messageReaders;
	private AuthorizationHandler authorizationHandler;

	public SupplementManagerGatewayFilterFactory(AuthorizationHandler authorizationHandler) {
		super(Config.class);
		this.messageReaders = HandlerStrategies.withDefaults().messageReaders();
		this.authorizationHandler = authorizationHandler;
	}

	@Override
	public GatewayFilter apply(Config config) {

		return new GatewayFilter() {
			@Override
			public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

				if (!config.isChange(exchange.getRequest())) {
					return chain.filter(exchange);
				}

				String heardsToken = exchange.getRequest().getHeaders().getFirst(AuthorizationHandler.HEARDS_TOKEN);
				ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);
				try {
					String managerId = authorizationHandler.getUser(heardsToken);
					String roleIds = authorizationHandler.getRoles(heardsToken);
					String deptIds = authorizationHandler.getDepts(heardsToken);
					// TODO: flux or mono
					Mono<String> modifiedBody = serverRequest.bodyToMono(String.class)
							// .log("modify_request_mono", Level.INFO)
							.flatMap(body -> {
								String params = config.changeBody(exchange.getRequest(), body, managerId, roleIds,
										deptIds);
								return Mono.just(params);
							}).defaultIfEmpty(
									config.changeBody(exchange.getRequest(), null, managerId, roleIds, deptIds));

					BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters
							.fromPublisher(modifiedBody, String.class);
					HttpHeaders headers = new HttpHeaders();
					headers.putAll(exchange.getRequest().getHeaders());
					headers.remove(HttpHeaders.CONTENT_LENGTH);

					CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);

					return bodyInserter.insert(outputMessage, new BodyInserterContext())
							// .log("modify_request", Level.INFO)
							.then(Mono.defer(() -> {
								ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(
										exchange.getRequest()) {
									@Override
									public HttpHeaders getHeaders() {
										long contentLength = headers.getContentLength();
										HttpHeaders httpHeaders = new HttpHeaders();
										httpHeaders.putAll(super.getHeaders());
										if (contentLength > 0) {
											httpHeaders.setContentLength(contentLength);
										} else {
											// TODO: this causes a 'HTTP/1.1 411 Length Required' on httpbin.org
											httpHeaders.set(HttpHeaders.TRANSFER_ENCODING, "chunked");
										}
										return httpHeaders;
									}

									@Override
									public Flux<DataBuffer> getBody() {
										return outputMessage.getBody();
									}
								};
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
				return filterToStringCreator(SupplementManagerGatewayFilterFactory.this).toString();
			}
		};

	}

	public static class Config {

		boolean isChange(ServerHttpRequest serverHttpRequest) {
			MediaType mediaType = serverHttpRequest.getHeaders().getContentType();
			if (mediaType == null) {
				return true;
			}

			if (mediaType.includes(MediaType.APPLICATION_FORM_URLENCODED)) {
				return true;
			}

			if (mediaType.includes(MediaType.APPLICATION_JSON)) {
				return true;
			}

			if (mediaType.includes(MediaType.APPLICATION_JSON_UTF8)) {
				return true;
			}
			return false;
		}

		String changeBody(ServerHttpRequest serverHttpRequest, String body, String managerId, String roleIds,
				String deptIds) {
			MediaType mediaType = serverHttpRequest.getHeaders().getContentType();
			if (mediaType == null) {
				return tamperWithJson(body, managerId, roleIds, deptIds);
			}

			if (mediaType.includes(MediaType.APPLICATION_FORM_URLENCODED)) {
				return tamperWithForm(body, managerId, roleIds, deptIds);
			}

			return tamperWithJson(body, managerId, roleIds, deptIds);
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

		@Override
		public String toString() {
			return new ToStringCreator(this).toString();
		}

	}
}