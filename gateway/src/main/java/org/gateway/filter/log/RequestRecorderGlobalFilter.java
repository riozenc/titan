/**
 * Author : chizf
 * Date : 2020年8月18日 下午3:23:07
 * Title : org.gateway.filter.log.RequestRecorderGlobalFilter.java
 *
**/
package org.gateway.filter.log;

import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class RequestRecorderGlobalFilter implements GlobalFilter, Ordered {
	private static final Log log = LogFactory.getLog(RequestRecorderGlobalFilter.class);

	private static final String SUPPORT_SCHEME_HTTP = "http";
	private static final String SUPPORT_SCHEME_HTTPS = "https";
	private final static String REQUEST_RECORDER_LOG_BUFFER = "RequestRecorderGlobalFilter.log";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest originalRequest = exchange.getRequest();
		URI originalRequestUrl = originalRequest.getURI();

		// 只记录http的请求
		String scheme = originalRequestUrl.getScheme();
		if ((!SUPPORT_SCHEME_HTTP.equals(scheme) && !SUPPORT_SCHEME_HTTPS.equals(scheme))) {
			return chain.filter(exchange);
		}

		// 如果是文件获取直接跳过
		if (originalRequestUrl.getPath().contains("static")) {
			return chain.filter(exchange);
		}

		RecorderServerHttpResponseDecorator response = new RecorderServerHttpResponseDecorator(exchange.getResponse());

		ServerWebExchange ex = exchange.mutate().request(new RecorderServerHttpRequestDecorator(exchange.getRequest()))
				.response(response).build();

		return recorderOriginalRequest(ex).then(chain.filter(ex)).then();
	}

	private Mono<Void> recorderOriginalRequest(ServerWebExchange exchange) {
		StringBuffer logBuffer = new StringBuffer("\n------------开始时间 ").append(System.currentTimeMillis())
				.append("------------");
		exchange.getAttributes().put(REQUEST_RECORDER_LOG_BUFFER, logBuffer);

		ServerHttpRequest request = exchange.getRequest();
		return recorderRequest(request, request.getURI(), logBuffer.append("\n原始请求：\n"));
	}

	private Mono<Void> recorderRequest(ServerHttpRequest request, URI uri, StringBuffer logBuffer) {
		if (uri == null) {
			uri = request.getURI();
		}

		HttpMethod method = request.getMethod();
		HttpHeaders headers = request.getHeaders();

		logBuffer.append(method.toString()).append(' ').append(uri.toString()).append('\n');

		logBuffer.append("------------请求头------------\n");
		headers.forEach((name, values) -> {
			values.forEach(value -> {
				logBuffer.append(name).append(":").append(value).append('\n');
			});
		});

		Charset bodyCharset = null;
		if (hasBody(method)) {
			long length = headers.getContentLength();
			if (length <= 0) {
				logBuffer.append("------------无body------------\n");
			} else {
				logBuffer.append("------------body 长度:").append(length).append(" contentType:");
				MediaType contentType = headers.getContentType();
				if (contentType == null) {
					logBuffer.append("null，不记录body------------\n");
				} else if (!shouldRecordBody(contentType)) {
					logBuffer.append(contentType.toString()).append("，不记录body------------\n");
				} else {
					bodyCharset = getMediaTypeCharset(contentType);
					logBuffer.append(contentType.toString()).append("------------\n");
				}
			}
		}

		if (bodyCharset != null) {
			return doRecordBody(logBuffer, request.getBody(), bodyCharset).then(Mono.defer(() -> {
				logBuffer.append("\n------------ end ------------\n\n");
				return Mono.empty();
			}));
		} else {
			logBuffer.append("------------ end ------------\n\n");
			return Mono.empty();
		}
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

	private boolean hasBody(HttpMethod method) {
		// 只记录这3种谓词的body
		if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH)
			return true;

		return false;
	}

	// 记录简单的常见的文本类型的request的body和response的body
	private boolean shouldRecordBody(MediaType contentType) {
		String type = contentType.getType();
		String subType = contentType.getSubtype();

		if ("application".equals(type)) {
			return "json".equals(subType) || "x-www-form-urlencoded".equals(subType) || "xml".equals(subType)
					|| "atom+xml".equals(subType) || "rss+xml".equals(subType);
		} else if ("text".equals(type)) {
			return true;
		}

		// 暂时不记录form
		return false;
	}

	private Mono<Void> doRecordBody(StringBuffer logBuffer, Flux<DataBuffer> body, Charset charset) {
		return DataBufferUtils.join(body).doOnNext(buffer -> {
			CharBuffer charBuffer = charset.decode(buffer.asByteBuffer());
			logBuffer.append(charBuffer.toString());
			DataBufferUtils.release(buffer);
		}).then();
	}

	private Charset getMediaTypeCharset(@Nullable MediaType mediaType) {
		if (mediaType != null && mediaType.getCharset() != null) {
			return mediaType.getCharset();
		} else {
			return StandardCharsets.UTF_8;
		}
	}
}