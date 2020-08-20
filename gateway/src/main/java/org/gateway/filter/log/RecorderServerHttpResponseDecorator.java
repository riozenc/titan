/**
 * Author : chizf
 * Date : 2020年8月19日 上午10:10:41
 * Title : org.gateway.filter.log.RecorderServerHttpResponseDecorator.java
 *
**/
package org.gateway.filter.log;

import java.nio.charset.Charset;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RecorderServerHttpResponseDecorator extends ServerHttpResponseDecorator {

	private static final Log log = LogFactory.getLog(RecorderServerHttpResponseDecorator.class);

	public RecorderServerHttpResponseDecorator(ServerHttpResponse delegate) {
		super(delegate);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 基于netty,我这里需要显示的释放一次dataBuffer,但是slice出来的byte是不需要释放的,
	 * 与下层共享一个字符串缓冲池,gateway过滤器使用的是nettyWrite类,会发生response数据多次才能返回完全。 在
	 * ServerHttpResponseDecorator 之后会释放掉另外一个refCount.
	 */
	@Override
	public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
		DataBufferFactory bufferFactory = this.bufferFactory();
		if (body instanceof Flux) {
			Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
			Publisher<? extends DataBuffer> re = fluxBody.map(dataBuffer -> {
				// probably should reuse buffers
				byte[] content = new byte[dataBuffer.readableByteCount()];
				// 数据读入数组
				dataBuffer.read(content);
				// 释放掉内存
				DataBufferUtils.release(dataBuffer);
				// 记录返回值
				String resBody = new String(content, Charset.forName("UTF-8"));

				try {
					log.info("--date:" + new Date() + "--" + resBody + "----");
				} catch (Exception e) {
					log.error("Response值修改日志记录出现错误->{}", e);
				}
				byte[] uppedContent = new String(content, Charset.forName("UTF-8")).getBytes();
				return bufferFactory.wrap(uppedContent);
			});
			return super.writeWith(re);
		}
		return super.writeWith(body);
	}

	@Override
	public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
		return writeWith(Flux.from(body).flatMapSequential(p -> p));
	}

}