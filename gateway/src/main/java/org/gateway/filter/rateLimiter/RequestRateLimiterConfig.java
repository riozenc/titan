/**
 * Author : chizf
 * Date : 2020年8月19日 下午6:37:53
 * Title : org.gateway.filter.rateLimiter.RequestRateLimiterConfig.java
 *
**/
package org.gateway.filter.rateLimiter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import reactor.core.publisher.Mono;

@Configuration
public class RequestRateLimiterConfig {

	@Value("${rateLimiter.defaultReplenishRate}")
	private int defaultReplenishRate;
	@Value("${rateLimiter.defaultBurstCapacity}")
	private int defaultBurstCapacity;

	@Bean
	public KeyResolver apiAndIpKeyResolver() {
		return exchange -> Mono
				.just(exchange.getRequest().getRemoteAddress().getHostName() + exchange.getRequest().getPath().value());
	}

	@Bean
	public RedisRateLimiter redisRateLimiter() {

		return new RedisRateLimiter(defaultReplenishRate, defaultBurstCapacity);
	}

	@Bean
	public GatewayFilter rateLimiterFilter(
			RequestRateLimiterGatewayFilterFactory requestRateLimiterGatewayFilterFactory,
			RedisRateLimiter redisRateLimiter, KeyResolver apiAndIpKeyResolver) {

		RequestRateLimiterGatewayFilterFactory.Config config = new RequestRateLimiterGatewayFilterFactory.Config();

		config.setKeyResolver(apiAndIpKeyResolver);
		config.setRateLimiter(redisRateLimiter);

		return requestRateLimiterGatewayFilterFactory.apply(config);
	}
}