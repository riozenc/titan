/**
 * Author : czy
 * Date : 2019年5月15日 下午5:50:06
 * Title : org.gateway.filter.BillingServerFilter.java
 *
**/
package org.gateway.filter;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gateway.handler.AuthorizationHandler;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.rewrite.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class BillingServerFilter extends DefaultAuthenticationInformationFilter implements GatewayFilter {
	private static final Log log = LogFactory.getLog(BillingServerFilter.class);
	private final List<HttpMessageReader<?>> messageReaders;

	public BillingServerFilter() {
		this.messageReaders = HandlerStrategies.withDefaults().messageReaders();
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// TODO Auto-generated method stub

		String token = exchange.getRequest().getHeaders().getFirst(AuthorizationHandler.HEARDS_TOKEN);
		ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);
		try {
			String managerId = super.getUserId(token);
			String roleIds = super.getRoleIds(token);
			String deptIds = super.getDeptIds(token);
			// TODO: flux or mono
			Mono<String> modifiedBody = serverRequest.bodyToMono(String.class)
					// .log("modify_request_mono", Level.INFO)
					.flatMap(body -> {
						String params = changeBody(exchange.getRequest(), body, managerId, roleIds, deptIds);
						return Mono.just(params);
					}).defaultIfEmpty(changeBody(exchange.getRequest(), null, managerId, roleIds, deptIds));

			BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters
					.fromPublisher(modifiedBody, String.class);
			HttpHeaders headers = new HttpHeaders();
			headers.putAll(exchange.getRequest().getHeaders());
			headers.remove(HttpHeaders.CONTENT_LENGTH);
			CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange, headers);
			return bodyInserter.insert(outputMessage, new BodyInserterContext())
					// .log("modify_request", Level.INFO)
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
			log.error(e);
			return Mono.error(e);
		}

	}

//	private boolean isApplicationJsonType(ServerHttpRequest serverHttpRequest) {
//		MediaType mediaType = serverHttpRequest.getHeaders().getContentType();
//		if (mediaType == null) {
//			return true;
//		}
//		if (!mediaType.includes(MediaType.APPLICATION_FORM_URLENCODED)) {
//			return true;
//		}
//		return mediaType.includes(MediaType.APPLICATION_JSON);
//	}
//
//	private String tamperWithJson(String body, String userId, String roleIds, String deptIds) {
//		Gson gson = new Gson();
//		JsonElement jsonElement = body == null ? new JsonObject() : gson.fromJson(body, JsonElement.class);
//		if (jsonElement.isJsonObject()) {
//			jsonElement.getAsJsonObject().addProperty(AuthorizationHandler.MANAGER_ID, userId);
//			jsonElement.getAsJsonObject().addProperty(AuthorizationHandler.ROLE_IDS, roleIds);
//			jsonElement.getAsJsonObject().addProperty(AuthorizationHandler.DEPT_IDS, deptIds);
//		}
//		return jsonElement.toString();
//	}
//
//	private String tamperWithForm(String body, String userId, String roleIds, String deptIds) {
//		return new StringBuilder(null == body ? "" : body).append("&").append(AuthorizationHandler.MANAGER_ID)
//				.append("=").append(userId).append("&").append(AuthorizationHandler.ROLE_IDS).append("=")
//				.append(roleIds).append("&").append(AuthorizationHandler.DEPT_IDS).append("=").append(deptIds)
//				.toString();
//	}
}
