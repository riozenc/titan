/**
 *    Auth:riozenc
 *    Date:2019年2月11日 上午9:47:34
 *    Title:org.gateway.handler.AuthorizationHandler.java
 **/
package org.gateway.handler;

import java.util.ArrayList;
import java.util.List;

import org.gateway.entity.AuthorizationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

@ControllerAdvice
@RequestMapping("authorization")
public class AuthorizationHandler {

	@Autowired
	private RestTemplate restTemplate;

	public final static String USER_ID = "userId";
	public final static String ROLE_IDS = "roleIds";

	@ResponseBody
	@RequestMapping(params = "method=login")
	public String login(AuthorizationEntity authorizationEntity) {

		if (ObjectUtils.isEmpty(authorizationEntity.getUsername())) {
			return "username 为 空";
		}
		if (ObjectUtils.isEmpty(authorizationEntity.getPassword())) {
			return "password 为 空";
		}

		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Authorization", "Basic dGVzdDp0ZXN0");

		// body
		MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
		requestBody.add("username", authorizationEntity.getUsername());
		requestBody.add("password", authorizationEntity.getPassword());
		requestBody.add("grant_type", "password");

		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<MultiValueMap<String, String>>(
				requestBody, httpHeaders);

		String result = restTemplate.postForObject("http://AUTH-CENTER/auth/oauth/token", requestEntity, String.class);

		System.out.println(result);
		return result;
	}

	@ResponseBody
	@RequestMapping(params = "method=getUser")
	public String getUser() {
		String token = "bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2luZm8iOnsiaWQiOjEsInVzZXJJZCI6InN5c2FkbWluIiwidXNlck5hbWUiOiLns7vnu5_nrqHnkIblkZgiLCJwYXNzd29yZCI6bnVsbCwicGhvbmUiOiIxNTExMjM0NTY3OCIsInNleCI6bnVsbCwic3RhdHVzIjoxLCJtYWlsQWRkcmVzcyI6bnVsbCwiaW1hZ2VVcmwiOm51bGwsInJlbWFyayI6bnVsbCwiY3JlYXRlRGF0ZSI6MTU0ODIxMDg0MDAwMCwidXBkYXRlRGF0ZSI6MTU1MDY0NDQzNTAwMH0sInVzZXJfbmFtZSI6Iuezu-e7n-euoeeQhuWRmCIsInNjb3BlIjpbInVzZXIiXSwiZXhwIjoxNTUxMzgyNjYyLCJhdXRob3JpdGllcyI6WyIxIl0sImp0aSI6IjJiNzc2OWJiLTkyYWItNDYzMy04ZmQ5LTkwNDg4YmExZmQyMyIsImNsaWVudF9pZCI6InRlc3QifQ.CK-7n5-sbj52jv2FWoLwv8VUYAQEYq9ZLouh64C-1sCc0DSgu0futtZnffRJry7i4a6_8oQBcvhGhIbzwcadiOC5yqbR28_kN79Zq8pS8rXttIFVZs2A1RYEhZvLcCz3nF2u5gV1NWJUhDuzW62V7Rywlk-fndR04iaQBFCVnvbT1UVjlFOkq1gDRV4mUk_WIQ_IRLaULUZiv-xqDjOxyyPDMW0L3vXCp-qyN2weDQdFZZ7ohDcihy4FUMsa4ySCylGxbLQjrf3Kg83jzxk2spc0npgmSjfvVJwaxo2UJQ8H46P3oZKp0WqyP9-OCiYiQoqiXxqHbjyqGyCZh5LtsA";

		String result = restTemplate.getForObject("http://AUTH-CENTER/auth/extractToken?token=" + token, String.class);
		RestObject rest = GsonBuilderUtils.gsonBuilderWithBase64EncodedByteArrays().create().fromJson(result,
				RestObject.class);

		UserInfo userInfo = new Gson().fromJson(rest.getData(), UserInfo.class);

		return userInfo.getId().toString();
	}

	@ResponseBody
	@GetMapping(params = "method=getRoles")
	public String getRoles(String userId) throws Exception {
// 

		String token = "bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2luZm8iOnsiaWQiOjEsInVzZXJJZCI6InN5c2FkbWluIiwidXNlck5hbWUiOiLns7vnu5_nrqHnkIblkZgiLCJwYXNzd29yZCI6bnVsbCwicGhvbmUiOiIxNTExMjM0NTY3OCIsInNleCI6bnVsbCwic3RhdHVzIjoxLCJtYWlsQWRkcmVzcyI6bnVsbCwiaW1hZ2VVcmwiOm51bGwsInJlbWFyayI6bnVsbCwiY3JlYXRlRGF0ZSI6MTU0ODIxMDg0MDAwMCwidXBkYXRlRGF0ZSI6MTU1MDY0NDQzNTAwMH0sInVzZXJfbmFtZSI6Iuezu-e7n-euoeeQhuWRmCIsInNjb3BlIjpbInVzZXIiXSwiZXhwIjoxNTUxMzgyNjYyLCJhdXRob3JpdGllcyI6WyIxIl0sImp0aSI6IjJiNzc2OWJiLTkyYWItNDYzMy04ZmQ5LTkwNDg4YmExZmQyMyIsImNsaWVudF9pZCI6InRlc3QifQ.CK-7n5-sbj52jv2FWoLwv8VUYAQEYq9ZLouh64C-1sCc0DSgu0futtZnffRJry7i4a6_8oQBcvhGhIbzwcadiOC5yqbR28_kN79Zq8pS8rXttIFVZs2A1RYEhZvLcCz3nF2u5gV1NWJUhDuzW62V7Rywlk-fndR04iaQBFCVnvbT1UVjlFOkq1gDRV4mUk_WIQ_IRLaULUZiv-xqDjOxyyPDMW0L3vXCp-qyN2weDQdFZZ7ohDcihy4FUMsa4ySCylGxbLQjrf3Kg83jzxk2spc0npgmSjfvVJwaxo2UJQ8H46P3oZKp0WqyP9-OCiYiQoqiXxqHbjyqGyCZh5LtsA";

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.add("Authorization", token);
		HttpEntity<String> requestEntity = new HttpEntity<String>(null, requestHeaders);

//		ResponseEntity<String> responseEntity = restTemplate.exchange("http://AUTH-DATA/auth-data/user/role/" + userId,
//				HttpMethod.GET, requestEntity, String.class);
		ResponseEntity<String> responseEntity = restTemplate.exchange("http://AUTH-DATA/auth-data/role/auth/table",
				HttpMethod.GET, requestEntity, String.class);

		RestObject rest = GsonBuilderUtils.gsonBuilderWithBase64EncodedByteArrays().create()
				.fromJson(responseEntity.getBody(), RestObject.class);

		if (rest.getCode() != 1000) {
			throw new Exception(new Exception(rest.getMessage()));
		}

		List<String> roleIdList = new ArrayList<>();
		rest.getData().getAsJsonArray().forEach(json -> {
			roleIdList.add(json.getAsJsonObject().get("id").getAsString());
		});

		String ids = String.join(",", roleIdList);

		return ids;

	}

	public void getToken() {
	}

	public void isValid() {

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

}
