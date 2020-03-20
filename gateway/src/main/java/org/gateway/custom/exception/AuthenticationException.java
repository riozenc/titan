/**
 * Author : chizf
 * Date : 2020年3月20日 上午9:40:21
 * Title : org.gateway.custom.exception.AuthenticationException.java
 *
**/
package org.gateway.custom.exception;

import org.gateway.custom.exception.annotation.HttpStatusAnnotation;

@HttpStatusAnnotation(401)
public class AuthenticationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2010694955555647906L;

	public AuthenticationException() {
		super();
	}

	public AuthenticationException(String message) {
		super(message);
	}

	public AuthenticationException(Throwable cause) {
		super(cause);
	}

	public AuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}
}
