/**
 *    Auth:riozenc
 *    Date:2019年2月27日 下午2:51:26
 *    Title:org.gateway.filter.BemServerFilter.java
 **/
package org.gateway.filter;

import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.web.server.ServerWebExchange;

import io.netty.buffer.ByteBufAllocator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class BemServerFilter implements GatewayFilter {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// TODO Auto-generated method stub
		ServerHttpRequest serverHttpRequest = exchange.getRequest();
		String method = serverHttpRequest.getMethodValue();
		if ("POST".equals(method)) {
			// 从请求里获取Post请求体
			String bodyStr = resolveBodyFromRequest(serverHttpRequest);
			// TODO 得到Post请求的请求参数后，做你想做的事

			String value = "{zhaojingdong:zz}";
			// 下面的将请求体再次封装写回到request里，传到下一级，否则，由于请求体已被消费，后续的服务将取不到值
			URI uri = serverHttpRequest.getURI();

			ServerHttpRequest request = serverHttpRequest.mutate().uri(uri).header("Content-Length", value.length() + "").build();

			request.getHeaders().add("Content-Length", value.length() + "");

			DataBuffer bodyDataBuffer = stringBuffer(value);
			Flux<DataBuffer> bodyFlux = Flux.just(bodyDataBuffer);

			request = new ServerHttpRequestDecorator(request) {
				@Override
				public Flux<DataBuffer> getBody() {
					return bodyFlux;
				}
			};
			// 封装request，传给下一级
			return chain.filter(exchange.mutate().request(request).build());
		} else if ("GET".equals(method)) {
			Map requestQueryParams = serverHttpRequest.getQueryParams();
			// TODO 得到Get请求的请求参数后，做你想做的事
			requestQueryParams.put("zhaojingdong", "ZZ");
			return chain.filter(exchange);
		}
		return chain.filter(exchange);

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
		// 获取request body
		return bodyRef.get();
	}

	private DataBuffer stringBuffer(String value) {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);

		NettyDataBufferFactory nettyDataBufferFactory = new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
		DataBuffer buffer = nettyDataBufferFactory.allocateBuffer(bytes.length);
		buffer.write(bytes);
		return buffer;
	}

}
