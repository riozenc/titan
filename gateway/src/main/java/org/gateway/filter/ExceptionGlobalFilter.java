/**
 *    Auth:riozenc
 *    Date:2019年3月4日 下午7:10:37
 *    Title:org.gateway.filter.ExceptionGlobalFilter.java
 **/
package org.gateway.filter;

import java.nio.charset.Charset;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

//@Component
public class ExceptionGlobalFilter implements GlobalFilter, Ordered {

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub

		return -2;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// TODO Auto-generated method stub

		ServerHttpResponse serverHttpResponse = exchange.getResponse();
		ServerHttpResponseDecorator serverHttpResponseDecorator = new ServerHttpResponseDecorator(serverHttpResponse) {
			@Override
			public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
				// TODO Auto-generated method stub
				if (body instanceof Flux) {
					Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
					DataBufferFactory bufferFactory = serverHttpResponse.bufferFactory();
					return super.writeWith(fluxBody.map(dataBuffer -> {
						// probably should reuse buffers
						byte[] content = new byte[dataBuffer.readableByteCount()];
						dataBuffer.read(content);
						// 释放掉内存
						DataBufferUtils.release(dataBuffer);
						String s = new String(content, Charset.forName("UTF-8"));
						System.out.println(s);
						// TODO，s就是response的值，想修改、查看就随意而为了
						byte[] uppedContent = new String(content, Charset.forName("UTF-8")).getBytes();
						return bufferFactory.wrap(uppedContent);
					}));

				}
				// if body is not a flux. never got there.
				return super.writeWith(body);

			}
		};

		return chain.filter(exchange.mutate().response(serverHttpResponseDecorator).build());
	}

}
