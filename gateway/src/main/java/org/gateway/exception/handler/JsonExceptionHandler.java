/**
 *    Auth:riozenc
 *    Date:2019年2月26日 上午11:23:51
 *    Title:org.gateway.exception.handler.JsonExceptionHandler.java
 **/
package org.gateway.exception.handler;

import static org.springframework.web.reactive.function.server.RequestPredicates.all;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

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
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;

/**
 * 自定义->异常处理->统一返回json
 * 
 * @author riozenc1
 *
 */

public class JsonExceptionHandler extends DefaultErrorWebExceptionHandler {
	private static final Log logger = LogFactory.getLog(JsonExceptionHandler.class);

	public JsonExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
			ErrorProperties errorProperties, ApplicationContext applicationContext) {
		super(errorAttributes, resourceProperties, errorProperties, applicationContext);
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
		Map<String, Object> error = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));

		return ServerResponse.status(getHttpStatus(error)).contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(error))
				.doOnNext((resp) -> logError(request, HttpStatus.valueOf(getHttpStatus(error))));
	}

	/**
	 * Get the HTTP error status information from the error map.
	 * 
	 * @param errorAttributes the current error information
	 * @return the error HTTP status
	 */
	protected int getHttpStatus(Map<String, Object> errorAttributes) {
		return (int) errorAttributes.get("status");
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

}
