/**
 * Author : chizf
 * Date : 2020年8月19日 上午9:19:15
 * Title : org.gateway.interceptor.HttpRequestInterceptor.java
 *
**/
package org.gateway.interceptor;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class HttpRequestInterceptor implements ClientHttpRequestInterceptor {
	private static final Log log = LogFactory.getLog(HttpRequestInterceptor.class);

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("\n rest  request  ,uri==>%s,method==>%s,params==>%s", request.getURI(),
				request.getMethod(), new String(body, "UTF-8")));
		log.debug(sb.toString());
		ClientHttpResponse httpResponse = execution.execute(request, body);
		log.debug(String.format("\n rest response , statusCode==>%s,statusText==>%s",
				httpResponse.getStatusCode().value(), httpResponse.getStatusText()));
		return httpResponse;
	}

}
