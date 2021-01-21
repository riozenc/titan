/**
 * Author : czy
 * Date : 2019年6月11日 下午2:15:45
 * Title : org.gateway.entity.LoginEntity.java
 *
**/
package org.gateway.entity;

public class LoginEntity {
	private String username;
	private String password;
	private String deviceType;
	private String loginType;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getLoginType() {
		return loginType;
	}

	public void setLoginType(String loginType) {
		this.loginType = loginType;
	}
}
