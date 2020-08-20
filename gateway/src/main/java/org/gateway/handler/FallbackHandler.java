/**
 * Author : chizf
 * Date : 2020年8月19日 上午9:13:20
 * Title : org.gateway.handler.FallbackHandler.java
 *
**/
package org.gateway.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ServerWebExchange;

@ControllerAdvice
@RequestMapping()
public class FallbackHandler {
	/**
	 * 默认熔断消息
	 * 
	 * @return
	 */
	@RequestMapping("/fallback")
	@ResponseBody
	public Map<String, String> fallback(ServerWebExchange exchange) {

		Map<String, String> map = new HashMap<>();

		map.put("statusCode", "500");
		map.put("message", "\"服务暂时不可用, 请稍候尝试 !\"");

		return map;
	}
}
