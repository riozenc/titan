/**
 *    Auth:riozenc
 *    Date:2019年3月16日 上午11:11:40
 *    Title:org.gateway.filter.AuthenticationInformationFilter.java
 **/
package org.gateway.filter;

public interface AuthenticationInformationFilter {
	String getUserId(String token) throws Exception;

	String getRoleIds(String token) throws Exception;

	String getDeptIds(String token) throws Exception;

}
