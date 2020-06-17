/**
 *    Auth:riozenc
 *    Date:2019年3月16日 上午11:12:35
 *    Title:org.gateway.filter.DefaultAuthenticationInformationFilter.java
 **/
package org.gateway.filter;

import org.gateway.handler.AuthorizationHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DefaultAuthenticationInformationFilter implements AuthenticationInformationFilter {
	@Autowired
	private AuthorizationHandler authorizationHandler;

	@Override
	public String getUserId(String token) throws Exception {
		return authorizationHandler.getUser(token);
	}

	@Override
	public String getRoleIds(String token) throws Exception {
		return authorizationHandler.getRoles(token);
	}

	@Override
	public String getDeptIds(String token) throws Exception {
		return authorizationHandler.getDepts(token);
	}

	protected boolean isChange(ServerHttpRequest serverHttpRequest) {
		MediaType mediaType = serverHttpRequest.getHeaders().getContentType();
		if (mediaType == null) {
			return true;
		}

		if (mediaType.includes(MediaType.APPLICATION_FORM_URLENCODED)) {
			return true;
		}

		if (mediaType.includes(MediaType.APPLICATION_JSON)) {
			return true;
		}

		if (mediaType.includes(MediaType.APPLICATION_JSON_UTF8)) {
			return true;
		}
		return false;
	}

	protected String changeBody(ServerHttpRequest serverHttpRequest, String body, String managerId, String roleIds,
			String deptIds) {
		MediaType mediaType = serverHttpRequest.getHeaders().getContentType();
		if (mediaType == null) {
			return tamperWithJson(body, managerId, roleIds, deptIds);
		}

		if (mediaType.includes(MediaType.APPLICATION_FORM_URLENCODED)) {
			return tamperWithForm(body, managerId, roleIds, deptIds);
		}

		return tamperWithJson(body, managerId, roleIds, deptIds);
	}

	private String tamperWithJson(String body, String userId, String roleIds, String deptIds) {
		Gson gson = new Gson();
		JsonElement jsonElement = body == null ? new JsonObject() : gson.fromJson(body, JsonElement.class);
		if (jsonElement.isJsonObject()) {
			jsonElement.getAsJsonObject().addProperty(AuthorizationHandler.MANAGER_ID, userId);
			jsonElement.getAsJsonObject().addProperty(AuthorizationHandler.ROLE_IDS, roleIds);
			jsonElement.getAsJsonObject().addProperty(AuthorizationHandler.DEPT_IDS, deptIds);
		}
		return jsonElement.toString();
	}

	private String tamperWithForm(String body, String userId, String roleIds, String deptIds) {
		return new StringBuilder(null == body ? "" : body).append("&").append(AuthorizationHandler.MANAGER_ID)
				.append("=").append(userId).append("&").append(AuthorizationHandler.ROLE_IDS).append("=")
				.append(roleIds).append("&").append(AuthorizationHandler.DEPT_IDS).append("=").append(deptIds)
				.toString();
	}

}
