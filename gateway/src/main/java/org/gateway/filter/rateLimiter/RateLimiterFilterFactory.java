/**
 * Author : chizf
 * Date : 2020年8月19日 上午9:59:12
 * Title : org.gateway.filter.rateLimiter.RateLimiterFilterFactory.java
 *
**/
package org.gateway.filter.rateLimiter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.stereotype.Component;

@Component
public class RateLimiterFilterFactory {
	private static final String REDIS_RATE_FILTER = "RequestRateLimiter";

	public RateLimiterFilterFactory() {
		System.out.println("?");
	}

	/**
	 * redis 限速过滤器 单一 ip+api 每秒最大访问3次
	 * 
	 * @return
	 */
	public FilterDefinition getRedisGatewayFilter() {
		FilterDefinition filterDefinition = new FilterDefinition();
		Map<String, String> filterParams = new HashMap<>(8);
		// 名称是固定的，spring gateway会根据名称找对应的FilterFactory
		filterDefinition.setName(REDIS_RATE_FILTER);
		// 每秒最大访问次数
		filterParams.put("redis-rate-limiter.replenishRate", "5");
		// 令牌桶最大容量
		filterParams.put("redis-rate-limiter.burstCapacity", "20");
		// 限流策略(#{@BeanName})
//		filterParams.put("key-resolver", "#{@apiAndIpKeyResolver}");
//		filterParams.put("description", "limit");
		// 自定义限流器(#{@BeanName})
		// filterParams.put("rate-limiter", "#{@redisRateLimiter}");
		filterDefinition.setArgs(filterParams);
		return filterDefinition;
	}

	/**
	 * 转换为filtesList
	 * 
	 * @param definitions
	 * @return
	 */
	public List<FilterDefinition> asFiltersList(FilterDefinition... definitions) {
		return Arrays.asList(definitions);
	}
}
