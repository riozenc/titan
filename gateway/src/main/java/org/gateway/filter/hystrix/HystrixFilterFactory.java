/**
 * Author : chizf
 * Date : 2020年8月19日 下午6:27:42
 * Title : org.gateway.filter.hystrix.HystrixFilterFactory.java
 *
**/
package org.gateway.filter.hystrix;

import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.stereotype.Component;

@Component
public class HystrixFilterFactory {
	private static final String HYSTRIX_FILTER = "Hystrix";

	/**
	 * 熔断策略配置
	 * 
	 * @return
	 */
	public FilterDefinition getHystrixGatewayFilter() {
		FilterDefinition filterDefinition = new FilterDefinition();
		filterDefinition.setName(HYSTRIX_FILTER);
		Map<String, String> filterParams = new HashMap<>(8);
		// 默认熔断策略
		filterParams.put("name", "default");
		// 默认跳转url
		filterParams.put("fallbackUri", "forward:/fallback");
		filterParams.put("description", "熔断过滤规则");
		filterDefinition.setArgs(filterParams);
		return filterDefinition;
	}
}
