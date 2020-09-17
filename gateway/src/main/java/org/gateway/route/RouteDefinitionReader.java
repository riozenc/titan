/**
 * Author : chizf
 * Date : 2020年9月16日 下午3:13:52
 * Title : org.gateway.route.RouteDefinitionReader.java
 *
**/
package org.gateway.route;

import reactor.core.publisher.Mono;

public interface RouteDefinitionReader {

	Mono<Void> read(Mono<String> routeId);

	Mono<Void> refush();

}
