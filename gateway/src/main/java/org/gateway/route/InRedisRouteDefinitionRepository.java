/**
 * Author : chizf
 * Date : 2020年9月16日 上午9:55:49
 * Title : org.gateway.route.InRedisRouteDefinitionRepository.java
 *
**/
package org.gateway.route;

import static java.util.Collections.synchronizedMap;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class InRedisRouteDefinitionRepository implements RouteDefinitionRepository, RouteDefinitionReader {
	private static final Log log = LogFactory.getLog(InRedisRouteDefinitionRepository.class);
	private final Map<String, RouteDefinition> routes = synchronizedMap(new LinkedHashMap<String, RouteDefinition>());

	private ReactiveStringRedisTemplate redisTemplate;

	private final static String REDIS_GATEWAY = "REDIS_GATEWAY";

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
				return Mono.defer(() -> this.redisTemplate.opsForHash().remove(REDIS_GATEWAY, id).then());
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

		return this.redisTemplate.opsForHash().entries(REDIS_GATEWAY).map(m -> {

			RouteDefinition r = new Gson().fromJson(m.getValue().toString(), RouteDefinition.class);
			routes.put(r.getId(), r);
			return r;

		}).then();

	}

	private Mono<Void> redisSave(String key, RouteDefinition value) {

		return this.redisTemplate.opsForHash().hasKey(REDIS_GATEWAY, key).flatMap(r -> {
			if (r == true) {
				return Mono.empty();
			} else {
//				return this.redisTemplate.opsForValue().set(key, new Gson().toJson(value));

				return this.redisTemplate.opsForHash().put(REDIS_GATEWAY, key, new Gson().toJson(value));
			}
		}).switchIfEmpty(Mono.defer(() -> {
			log.debug(key + " route is exist!");
			return Mono.empty();
		}))
//		.switchIfEmpty(Mono.error(new RedisSystemException("Cannot rename key that does not exist",
//		new RedisException("ERR no such key."))))
				.then();

	}

	/**
	 * 从json中获取数据，丢到redis中，防止出现新环境redis中无数据的情况
	 */
	private void init() {

		try {
			Resource fileRource = new ClassPathResource("Routes.json");

			InputStream input = fileRource.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(input);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

			StringBuilder buffer = new StringBuilder();

			try {
				bufferedReader.lines().forEach(s -> {
					buffer.append(s);
				});

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
				if (bufferedReader != null)
					bufferedReader.close();
				if (inputStreamReader != null)
					inputStreamReader.close();
				if (input != null)
					input.close();
			}

		} catch (FileNotFoundException e) {
			log.error(e.getMessage());

		} catch (IOException e) {
			log.error(e.getMessage());
		}

	}

}
