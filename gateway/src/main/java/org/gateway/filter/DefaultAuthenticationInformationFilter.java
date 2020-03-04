/**
 *    Auth:riozenc
 *    Date:2019年3月16日 上午11:12:35
 *    Title:org.gateway.filter.DefaultAuthenticationInformationFilter.java
 **/
package org.gateway.filter;

import org.gateway.handler.AuthorizationHandler;
import org.springframework.beans.factory.annotation.Autowired;

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

}
