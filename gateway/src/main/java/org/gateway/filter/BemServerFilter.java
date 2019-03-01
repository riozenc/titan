/**
 *    Auth:riozenc
 *    Date:2019年2月27日 下午2:51:26
 *    Title:org.gateway.filter.BemServerFilter.java
 **/
package org.gateway.filter;

import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.client.utils.URIBuilder;
import org.gateway.custom.CustomServerHttpRequest;
import org.gateway.handler.AuthorizationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class BemServerFilter implements GatewayFilter {

	@Autowired
	private AuthorizationHandler authorizationHandler;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// TODO Auto-generated method stub
		ServerHttpRequest serverHttpRequest = exchange.getRequest();
		CustomServerHttpRequest customServerHttpRequest = new CustomServerHttpRequest(serverHttpRequest);

		try {
			String userId = getUserId();
			String roleIds = getRoleId(userId);
			if (HttpMethod.GET.equals(serverHttpRequest.getMethod())) {
				URI uri = serverHttpRequest.getURI();
				URIBuilder uriBuilder = new URIBuilder(uri).addParameter(AuthorizationHandler.USER_ID, userId)
						.addParameter(AuthorizationHandler.ROLE_IDS, roleIds);
				customServerHttpRequest.uri(uriBuilder.build());
			} else if (HttpMethod.POST.equals(serverHttpRequest.getMethod())) {
				String bodyStr = resolveBodyFromRequest(serverHttpRequest);
				String params = null;
				if (isApplicationJsonType(serverHttpRequest)) {
					params = tamperWithJson(bodyStr, userId, roleIds);
				} else {
					params = tamperWithForm(bodyStr, userId, roleIds);
				}

				DataBuffer bodyDataBuffer = stringBuffer(params);
				Flux<DataBuffer> bodyFlux = Flux.just(bodyDataBuffer);
				customServerHttpRequest.header("Content-Length", String.valueOf(bodyDataBuffer.capacity()));
				customServerHttpRequest.body(bodyFlux);
			}
			return chain.filter(exchange.mutate().request(customServerHttpRequest.build()).build());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return Mono.error(e);
		}

	}

	/**
	 * 从Flux<DataBuffer>中获取字符串的方法
	 * 
	 * @return 请求体
	 */
	private String resolveBodyFromRequest(ServerHttpRequest serverHttpRequest) {
		// 获取请求体
		Flux<DataBuffer> body = serverHttpRequest.getBody();
		AtomicReference<String> bodyRef = new AtomicReference<>();
		body.subscribe(buffer -> {
			CharBuffer charBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer());
			DataBufferUtils.release(buffer);
			bodyRef.set(charBuffer.toString());
		});
		return bodyRef.get();
	}

	/**
	 * 组装DataBuffer
	 * 
	 * @param value
	 * @return
	 */
	private DataBuffer stringBuffer(String value) {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

		NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
		DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
		buffer.write(bytes);
		return buffer;
	}

	private String getUserId() {
		return authorizationHandler.getUser();
	}

	private String getRoleId(String userId) throws Exception {
		return authorizationHandler.getRoles(userId);
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
		// {"type":"COMMON_PARAM"}
		Gson gson = new Gson();
		JsonElement jsonElement = gson.fromJson(body, JsonElement.class);
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
