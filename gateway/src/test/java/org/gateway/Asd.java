/**
 *    Auth:riozenc
 *    Date:2019年1月23日 上午11:09:52
 *    Title:org.gateway.Asd.java
 **/
package org.gateway;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;

import reactor.core.publisher.Flux;

public class Asd {
	public static void main(String[] args) throws URISyntaxException {
		List<String> list = new ArrayList<>();
		list.add("a");
		list.add("b");
		list.add("c");

		Flux.fromIterable(list).flatMap(body -> {
			System.out.println(body.getBytes().length);
			return null;
		}).then();

	}
}

class RestObject {

	private Integer code;
	private String message;
	private JsonElement data;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public JsonElement getData() {
		return data;
	}

	public void setData(JsonElement data) {
		this.data = data;
	}

}

class UserInfo {

	private Long id;
	private String userId;
	private String userName;
	private String phone;
	private Byte sex;
	private Byte status;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public Byte getSex() {
		return sex;
	}

	public void setSex(Byte sex) {
		this.sex = sex;
	}

	public Byte getStatus() {
		return status;
	}

	public void setStatus(Byte status) {
		this.status = status;
	}

}

class UserRoleRel {
	private String userId;
	private String roleId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

}
