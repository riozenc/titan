/**
 *    Auth:riozenc
 *    Date:2019年3月4日 下午3:00:13
 *    Title:org.gateway.custom.error.JsonErrorAttributes.java
 **/
package org.gateway.custom.error;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.gateway.handler.AuthorizationHandler.RestObject;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

public class JsonErrorAttributes implements ErrorAttributes {

	private static final String ERROR_ATTRIBUTE = DefaultErrorAttributes.class.getName() + ".ERROR";

	private final boolean includeException;

	/**
	 * Create a new {@link DefaultErrorAttributes} instance that does not include
	 * the "exception" attribute.
	 */
	public JsonErrorAttributes() {
		this(false);
	}

	/**
	 * Create a new {@link DefaultErrorAttributes} instance.
	 * 
	 * @param includeException whether to include the "exception" attribute
	 */
	public JsonErrorAttributes(boolean includeException) {
		this.includeException = includeException;
	}

	@Override
	public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
		Map<String, Object> errorAttributes = new LinkedHashMap<>();
		errorAttributes.put("timestamp", new Date());
		errorAttributes.put("path", request.path());
		Throwable error = getError(request);

		HttpStatus errorStatus = determineHttpStatus(error);
		errorAttributes.put("status", errorStatus.value());
		errorAttributes.put("error", errorStatus.getReasonPhrase());

		errorAttributes.put("message", determineMessage(error));
		handleException(errorAttributes, determineException(error), includeStackTrace);
		return errorAttributes;
	}

	private HttpStatus determineHttpStatus(Throwable error) {

		try {
			RestObject restObject = GsonBuilderUtils.gsonBuilderWithBase64EncodedByteArrays().create()
					.fromJson(error.getMessage(), RestObject.class);
			if (restObject != null) {
				return HttpStatus.valueOf(restObject.getStatus());
			}
		} catch (Exception e) {
			
		}

		if (error instanceof ResponseStatusException) {
			return ((ResponseStatusException) error).getStatus();
		}
		ResponseStatus responseStatus = AnnotatedElementUtils.findMergedAnnotation(error.getClass(),
				ResponseStatus.class);
		if (responseStatus != null) {
			return responseStatus.code();
		}

		return HttpStatus.INTERNAL_SERVER_ERROR;
	}

	private String determineMessage(Throwable error) {
		if (error instanceof WebExchangeBindException) {
			return error.getMessage();
		}
		if (error instanceof ResponseStatusException) {
			return ((ResponseStatusException) error).getReason();
		}
		ResponseStatus responseStatus = AnnotatedElementUtils.findMergedAnnotation(error.getClass(),
				ResponseStatus.class);
		if (responseStatus != null) {
			return responseStatus.reason();
		}
		return error.getMessage();
	}

	private Throwable determineException(Throwable error) {
		if (error instanceof ResponseStatusException) {
			return (error.getCause() != null) ? error.getCause() : error;
		}
		return error;
	}

	private void addStackTrace(Map<String, Object> errorAttributes, Throwable error) {
		StringWriter stackTrace = new StringWriter();
		error.printStackTrace(new PrintWriter(stackTrace));
		stackTrace.flush();
		errorAttributes.put("trace", stackTrace.toString());
	}

	private void handleException(Map<String, Object> errorAttributes, Throwable error, boolean includeStackTrace) {
		if (this.includeException) {
			errorAttributes.put("exception", error.getClass().getName());
		}
		if (includeStackTrace) {
			addStackTrace(errorAttributes, error);
		}
		if (error instanceof BindingResult) {
			BindingResult result = (BindingResult) error;
			if (result.getErrorCount() > 0) {
				errorAttributes.put("errors", result.getAllErrors());
			}
		}
	}

	@Override
	public Throwable getError(ServerRequest request) {
		return (Throwable) request.attribute(ERROR_ATTRIBUTE)
				.orElseThrow(() -> new IllegalStateException("Missing exception attribute in ServerWebExchange"));
	}

	@Override
	public void storeErrorInformation(Throwable error, ServerWebExchange exchange) {
		exchange.getAttributes().putIfAbsent(ERROR_ATTRIBUTE, error);
	}

}
