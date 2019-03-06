/**
 *    Auth:riozenc
 *    Date:2019年3月5日 下午3:47:32
 *    Title:org.gateway.filter.BemServerFilter2.java
 **/
package org.gateway.filter;

import java.util.logging.Level;

import org.gateway.handler.AuthorizationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.DefaultServerRequest;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
@Component
public class BemServerFilter2 implements GatewayFilter {
	@Autowired
	private AuthorizationHandler authorizationHandler;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		try {
			ServerRequest serverRequest = new DefaultServerRequest(exchange);
			String userId = getUserId();
			String roleIds = getRoleId(userId);
			// TODO: flux or mono
			Mono<String> modifiedBody = serverRequest.bodyToMono(String.class)
					// .log("modify_request_mono", Level.INFO)
					.flatMap(body -> {
						if (isApplicationJsonType(exchange.getRequest())) {
							body = tamperWithJson(body, userId, roleIds);
						} else {
							body = tamperWithForm(body, userId, roleIds);
						}
						JsonElement jsonElement = body == null ? new JsonObject()
								: new Gson().fromJson(body, JsonElement.class);
						if (jsonElement.isJsonObject()) {
							jsonElement.getAsJsonObject().addProperty(AuthorizationHandler.USER_ID, userId);
							jsonElement.getAsJsonObject().addProperty(AuthorizationHandler.ROLE_IDS, roleIds);
						}
						return Mono.just(jsonElement.toString());
					});
			BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters
					.fromPublisher(modifiedBody, String.class);
			HttpHeaders headers = new HttpHeaders();
			headers.putAll(exchange.getRequest().getHeaders());

			// the new content type will be computed by bodyInserter
			// and then set in the request decorator
			headers.remove(HttpHeaders.CONTENT_LENGTH);
			CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
			
			return bodyInserter.insert(outputMessage, new BodyInserterContext())
					.then(Mono.defer(() -> {
						ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(exchange.getRequest()) {
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
			return Mono.error(e);
		}

	}

	private String getUserId() throws Exception {
//		return authorizationHandler.getUser();
		return "1";
	}

	private String getRoleId(String userId) throws Exception {
//		return authorizationHandler.getRoles(userId);
		return "1,2";
	}

	private boolean isApplicationJsonType(ServerHttpRequest serverHttpRequest) {
		MediaType mediaType = serverHttpRequest.getHeaders().getContentType();
		if (mediaType == null) {
			return true;
		}
		if (!mediaType.includes(MediaType.APPLICATION_FORM_URLENCODED)) {
			return true;
		}
		return mediaType.includes(MediaType.APPLICATION_JSON);
	}

	private String tamperWithJson(String body, String userId, String roleIds) {
		Gson gson = new Gson();
		JsonElement jsonElement = body == null ? new JsonObject() : gson.fromJson(body, JsonElement.class);
		if (jsonElement.isJsonObject()) {
			jsonElement.getAsJsonObject().addProperty(AuthorizationHandler.USER_ID, userId);
			jsonElement.getAsJsonObject().addProperty(AuthorizationHandler.ROLE_IDS, roleIds);
		}
		return jsonElement.toString();
	}

	private String tamperWithForm(String body, String userId, String roleIds) {
		return new StringBuilder(null == body ? "" : body).append("&").append(AuthorizationHandler.USER_ID).append("=")
				.append(userId).append("&").append(AuthorizationHandler.ROLE_IDS).append("=").append(roleIds)
				.toString();
	}
}
