/**
 * Author : chizf
 * Date : 2020年8月19日 上午10:16:13
 * Title : org.gateway.filter.log.RecorderServerHttpRequestDecorator.java
 *
**/
package org.gateway.filter.log;

import java.util.LinkedList;
import java.util.List;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RecorderServerHttpRequestDecorator extends ServerHttpRequestDecorator {

	private final List<DataBuffer> dataBuffers = new LinkedList<>();
	private boolean bufferCached = false;
	private Mono<Void> progress = null;

	public RecorderServerHttpRequestDecorator(ServerHttpRequest delegate) {
		super(delegate);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 重新request请求
	 */
	@Override
	public Flux<DataBuffer> getBody() {
		synchronized (dataBuffers) {
			if (bufferCached) {
				return copy();
			}

			if (progress == null) {
				progress = cache();
			}

			return progress.thenMany(Flux.defer(this::copy));
		}
	}

	private Flux<DataBuffer> copy() {
		return Flux.fromIterable(dataBuffers).map(buf -> buf.factory().wrap(buf.asByteBuffer()));
	}

	private Mono<Void> cache() {
		return super.getBody().map(dataBuffers::add).then(Mono.defer(() -> {
			bufferCached = true;
			progress = null;
			return Mono.empty();
		}));
	}
}
