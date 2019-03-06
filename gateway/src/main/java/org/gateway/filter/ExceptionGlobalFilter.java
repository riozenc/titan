/**
 *    Auth:riozenc
 *    Date:2019年3月4日 下午7:10:37
 *    Title:org.gateway.filter.ExceptionGlobalFilter.java
 **/
package org.gateway.filter;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR;

import java.nio.charset.Charset;

import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory.ResponseAdapter;
import org.springframework.cloud.gateway.support.BodyInserterContext;
import org.springframework.cloud.gateway.support.CachedBodyOutputMessage;
import org.springframework.cloud.gateway.support.DefaultClientResponse;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.ResponseCookie;
import org.springframework.http.client.reactive.ClientHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ExceptionGlobalFilter implements GlobalFilter, Ordered {

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub

		return -2;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(exchange.getResponse()) {

			@Override
			public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {

				String originalResponseContentType = exchange.getAttribute(ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR);
				HttpHeaders httpHeaders = new HttpHeaders();
				// explicitly add it in this way instead of
				// 'httpHeaders.setContentType(originalResponseContentType)'
				// this will prevent exception in case of using non-standard media types like
				// "Content-Type: image"
				httpHeaders.add(HttpHeaders.CONTENT_TYPE, originalResponseContentType);
				ResponseAdapter responseAdapter = new ResponseAdapter(body, httpHeaders);
				DefaultClientResponse clientResponse = new DefaultClientResponse(responseAdapter,
						ExchangeStrategies.withDefaults());

				// TODO: flux or mono
				Mono<String> modifiedBody = clientResponse.bodyToMono(String.class).flatMap(originalBody -> {
					return Mono.just(originalBody);
				});

				BodyInserter<Mono<String>, ReactiveHttpOutputMessage> bodyInserter = BodyInserters
						.fromPublisher(modifiedBody, String.class);
				CachedBodyOutputMessage outputMessage = new CachedBodyOutputMessage(exchange,
						exchange.getResponse().getHeaders());
				return bodyInserter.insert(outputMessage, new BodyInserterContext()).then(Mono.defer(() -> {
					Flux<DataBuffer> messageBody = outputMessage.getBody();
					HttpHeaders headers = getDelegate().getHeaders();
					if (!headers.containsKey(HttpHeaders.TRANSFER_ENCODING)) {
						messageBody = messageBody.doOnNext(data -> headers.setContentLength(data.readableByteCount()));
					}
					// TODO: use isStreamingMediaType?
					return getDelegate().writeWith(messageBody);
				}));
			}

			@Override
			public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
				return writeWith(Flux.from(body).flatMapSequential(p -> p));
			}
		};

		return chain.filter(exchange.mutate().response(responseDecorator).build());
	}

	public class ResponseAdapter implements ClientHttpResponse {

		private final Flux<DataBuffer> flux;
		private final HttpHeaders headers;

		public ResponseAdapter(Publisher<? extends DataBuffer> body, HttpHeaders headers) {
			this.headers = headers;
			if (body instanceof Flux) {
				flux = (Flux) body;
			} else {
				flux = ((Mono) body).flux();
			}
		}

		@Override
		public Flux<DataBuffer> getBody() {
			return flux;
		}

		@Override
		public HttpHeaders getHeaders() {
			return headers;
		}

		@Override
		public HttpStatus getStatusCode() {
			return null;
		}

		@Override
		public int getRawStatusCode() {
			return 0;
		}

		@Override
		public MultiValueMap<String, ResponseCookie> getCookies() {
			return null;
		}
	}
}
