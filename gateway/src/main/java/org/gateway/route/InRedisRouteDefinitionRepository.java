/**
 * Author : chizf
 * Date : 2020年9月16日 上午9:55:49
 * Title : org.gateway.route.InRedisRouteDefinitionRepository.java
 *
**/
package org.gateway.route;

import static java.util.Collections.synchronizedMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class InRedisRouteDefinitionRepository implements RouteDefinitionRepository, RouteDefinitionReader {
	private static final Log log = LogFactory.getLog(InRedisRouteDefinitionRepository.class);
	private final Map<String, RouteDefinition> routes = synchronizedMap(new LinkedHashMap<String, RouteDefinition>());

	private ReactiveStringRedisTemplate redisTemplate;

	public InRedisRouteDefinitionRepository(ReactiveStringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
		init();
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
				return Mono.defer(() -> this.redisTemplate.delete(id).then());
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
		return this.redisTemplate.scan().flatMap(key -> {
			// 根据key获取缓存中的route
			return this.redisTemplate.opsForValue().get(key).map(value -> {
				RouteDefinition r = new Gson().fromJson(value, RouteDefinition.class);
				routes.put(r.getId(), r);
				return r;
			});

		}).then();
	}

	private Mono<Void> redisSave(String key, RouteDefinition value) {
		return this.redisTemplate.hasKey(key).flatMap(r -> {
//			if (r == true) {
//				return Mono.defer(() -> Mono.error(new NotFoundException("redis refush error!")));
//			} else {
//				return this.redisTemplate.opsForValue().set(key, new Gson().toJson(value));
//			}
			
			
			return this.redisTemplate.opsForValue().set(key, new Gson().toJson(value));
		}).then();
	}

	/**
	 * 从json中获取数据，丢到redis中，防止出现新环境redis中无数据的情况
	 */
	private void init() {
		File jsonFile;
		try {
			jsonFile = ResourceUtils.getFile("classpath:Routes.json");

			Scanner scanner = null;
			StringBuilder buffer = new StringBuilder();
			try {
				scanner = new Scanner(jsonFile, "utf-8");
				while (scanner.hasNextLine()) {
					buffer.append(scanner.nextLine());
				}
				String json = buffer.toString();

				Type typeOfT = TypeToken.getParameterized(List.class, RouteDefinition.class).getType();
				List<RouteDefinition> routeDefinitions = GsonBuilderUtils.gsonBuilderWithBase64EncodedByteArrays()
						.create().fromJson(json, typeOfT);

				Flux.fromIterable(routeDefinitions)
						.flatMap(routeDefinition -> this.save(Mono.just(routeDefinition).map(route -> {
							log.debug("Saving route: " + route);
							return route;
						}))).subscribe();

			} finally {
				if (scanner != null) {
					scanner.close();
				}
			}

		} catch (FileNotFoundException e) {
			log.error(e.getMessage());

		}

	}

}
