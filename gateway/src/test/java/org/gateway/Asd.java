/**
 *    Auth:riozenc
 *    Date:2019年1月23日 上午11:09:52
 *    Title:org.gateway.Asd.java
 **/
package org.gateway;

import java.util.HashMap;

import com.google.gson.Gson;

public class Asd {
	public static void main(String[] args) {

		String s = "{\"code\":1000,\"message\":\"success.\",\"data\":{\"id\":1,\"userId\":\"sysadmin\",\"userName\":\"系统管理员\",\"password\":null,\"phone\":null,\"sex\":null,\"status\":1,\"mailAddress\":null,\"imageUrl\":null,\"remark\":null,\"createDate\":1548210840000,\"updateDate\":null}}";

		Gson gson = new Gson();

		HashMap<String, Object> map = gson.fromJson(s, HashMap.class);

		System.out.println(map.get("code"));
		
		System.out.println(map.get("code").getClass().getTypeName());
		
		if (1000 == (double) map.get("code")) {
			System.out.println(true);
		}
	}
}
