/**
 *    Auth:riozenc
 *    Date:2019年2月26日 上午11:23:51
 *    Title:org.gateway.exception.handler.JsonExceptionHandler.java
 **/
package org.gateway.exception.handler;

import static org.springframework.web.reactive.function.server.RequestPredicates.all;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 自定义->异常处理->统一返回json
 * 
 * @author riozenc1
 *
 */

public class JsonExceptionHandler extends DefaultErrorWebExceptionHandler {
	private static final Log logger = LogFactory.getLog(JsonExceptionHandler.class);
	private static final MediaType TEXT_HTML_UTF8 = new MediaType("text", "html", StandardCharsets.UTF_8);
	private static final Map<HttpStatus.Series, String> SERIES_VIEWS;
	private final ErrorProperties errorProperties;

	static {
		Map<HttpStatus.Series, String> views = new EnumMap<>(HttpStatus.Series.class);
		views.put(HttpStatus.Series.CLIENT_ERROR, "4xx");
		views.put(HttpStatus.Series.SERVER_ERROR, "5xx");
		SERIES_VIEWS = Collections.unmodifiableMap(views);
	}

	public JsonExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
			ErrorProperties errorProperties, ApplicationContext applicationContext) {
		super(errorAttributes, resourceProperties, errorProperties, applicationContext);
		this.errorProperties = errorProperties;
	}

	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
//		return route(acceptsTextHtml(), this::renderErrorView).andRoute(all(), this::renderErrorResponse);
		return route(all(), this::renderErrorResponse);
	}

	/**
	 * Render the error information as a JSON payload.
	 * 
	 * @param request the current request
	 * @return a {@code Publisher} of the HTTP response
	 */
	protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
		boolean includeStackTrace = isIncludeStackTrace(request, MediaType.TEXT_HTML);
		Map<String, Object> error = getErrorAttributes(request, includeStackTrace);
		int errorStatus = getHttpStatus(error);
		ServerResponse.BodyBuilder responseBody = ServerResponse.status(errorStatus).contentType(TEXT_HTML_UTF8);
		return Flux.just(getData(errorStatus).toArray(new String[] {}))
				.flatMap((viewName) -> renderErrorView(viewName, responseBody, error))
				.switchIfEmpty(
						this.errorProperties.getWhitelabel().isEnabled() ? renderDefaultErrorView(responseBody, error)
								: Mono.error(getError(request)))
				.next();
	}

	/**
	 * Get the HTTP error status information from the error map.
	 * 
	 * @param errorAttributes the current error information
	 * @return the error HTTP status
	 */
	protected int getHttpStatus(Map<String, Object> errorAttributes) {
		int statusCode = (int) errorAttributes.get("status");
		return statusCode;
	}

	/**
	 * Predicate that checks whether the current request explicitly support
	 * {@code "text/html"} media type.
	 * <p>
	 * The "match-all" media type is not considered here.
	 * 
	 * @return the request predicate
	 */
	protected RequestPredicate acceptsTextHtml() {
		return (serverRequest) -> {
			try {
				List<MediaType> acceptedMediaTypes = serverRequest.headers().accept();
				acceptedMediaTypes.remove(MediaType.ALL);
				MediaType.sortBySpecificityAndQuality(acceptedMediaTypes);
				return acceptedMediaTypes.stream().anyMatch(MediaType.TEXT_HTML::isCompatibleWith);
			} catch (InvalidMediaTypeException ex) {
				return false;
			}
		};
	}

	/**
	 * Log the original exception if handling it results in a Server Error or a Bad
	 * Request (Client Error with 400 status code) one.
	 * 
	 * @param request     the source request
	 * @param errorStatus the HTTP error status
	 */
	protected void logError(ServerRequest request, HttpStatus errorStatus) {
		Throwable ex = getError(request);
		log(request, ex, (errorStatus.is5xxServerError() ? logger::error : logger::warn));
	}

	private void log(ServerRequest request, Throwable ex, BiConsumer<Object, Throwable> logger) {
		if (ex instanceof ResponseStatusException) {
			logger.accept(buildMessage(request, ex), null);
		} else {
			logger.accept(buildMessage(request, null), ex);
		}
	}

	private String buildMessage(ServerRequest request, Throwable ex) {
		StringBuilder message = new StringBuilder("Failed to handle request [");
		message.append(request.methodName());
		message.append(" ");
		message.append(request.uri());
		message.append("]");
		if (ex != null) {
			message.append(": ");
			message.append(ex.getMessage());
		}
		return message.toString();
	}

	private List<String> getData(int errorStatus) {
		List<String> data = new ArrayList<>();
		data.add("error/" + errorStatus);
		HttpStatus.Series series = HttpStatus.Series.resolve(errorStatus);
		if (series != null) {
			data.add("error/" + SERIES_VIEWS.get(series));
		}
		data.add("error/error");
		return data;
	}
}
