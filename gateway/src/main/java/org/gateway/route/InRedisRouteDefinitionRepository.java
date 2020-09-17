/**
 * Author : chizf
 * Date : 2020年9月16日 上午9:55:49
 * Title : org.gateway.route.InRedisRouteDefinitionRepository.java
 *
**/
package org.gateway.route;

import static java.util.Collections.synchronizedMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class InRedisRouteDefinitionRepository implements RouteDefinitionRepository, RouteDefinitionReader {
	private final Map<String, RouteDefinition> routes = synchronizedMap(new LinkedHashMap<String, RouteDefinition>());

	private ReactiveStringRedisTemplate redisTemplate;

	

	public InRedisRouteDefinitionRepository(ReactiveStringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public Flux<RouteDefinition> getRouteDefinitions() {
		return Flux.fromIterable(routes.values());
	}

	@Override
	public Mono<Void> save(Mono<RouteDefinition> route) {
		return route.flatMap(r -> {
			if (StringUtils.isEmpty(r.getId())) {
				return Mono.error(new IllegalArgumentException("id may not be empty"));
			}
			routes.put(r.getId(), r);

			return Mono.defer(() -> redisSave(r.getId(), r));
		});
	}

	@Override
	public Mono<Void> delete(Mono<String> routeId) {
		return routeId.flatMap(id -> {
			if (routes.containsKey(id)) {
				routes.remove(id);
				return Mono.empty();
			}
			return Mono.defer(() -> Mono.error(new NotFoundException("RouteDefinition not found: " + routeId)));
		});
	}

	@Override
	public Mono<Void> read(Mono<String> routeId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Mono<Void> refush() {

		// 获取所有路由信息

		this.redisTemplate.scan().flatMap(key -> {
			// 根据key获取缓存中的val
			return this.redisTemplate.opsForValue().get(key).map(value -> {
				RouteDefinition routeDefinition = new Gson().fromJson(value, RouteDefinition.class);
				return routeDefinition;
			});

		}).subscribe(routeDefinition -> {
			System.out.println(routeDefinition.getId());
		});

	
		return Mono.empty();

	}

	private Mono<Void> redisSave(String key, RouteDefinition value) {

		return this.redisTemplate.hasKey(key).flatMap(r -> {
			if (r == true) {
				return Mono.defer(() -> Mono.error(new NotFoundException("redis refush error!")));
			} else {

				return this.redisTemplate.opsForValue().set(key, new Gson().toJson(value));
			}
		}).then();

	}

}
