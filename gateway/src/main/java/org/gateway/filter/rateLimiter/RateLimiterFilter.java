/**
 * Author : chizf
 * Date : 2020年8月20日 上午9:32:33
 * Title : org.gateway.filter.rateLimiter.RateLimiterFilter.java
 *
**/
package org.gateway.filter.rateLimiter;

import java.util.List;

import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RateLimiterFilter {

//	@Bean
//	@Primary
//	public DiscoveryLocatorProperties rateLimiterLocatorProperties2(
//			DiscoveryLocatorProperties discoveryLocatorProperties) {
//
//		List<FilterDefinition> list = discoveryLocatorProperties.getFilters();
//
//		RateLimiterFilterFactory rateLimiterFilterFactory = new RateLimiterFilterFactory();
//
//		list.add(rateLimiterFilterFactory.getRedisGatewayFilter());
//
//		discoveryLocatorProperties.setFilters(list);
//
//		return discoveryLocatorProperties;
//	}

	@Bean
	public RedisRateLimiter rateLimiter() {

		return new RedisRateLimiter(5, 20);
	}
}
