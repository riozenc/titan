/**
 *    Auth:riozenc
 *    Date:2019年1月3日 上午10:50:17
 *    Title:org.gateway.GlobalGateWayFilter.java
 **/
package org.gateway.filter;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class GlobalGateWayFilter implements GlobalFilter, Ordered {

	@Autowired
	private RestTemplate restTemplate;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// TODO Auto-generated method stub
		System.out.println("GlobalGateWayFilter---" + exchange.getRequest().getRemoteAddress());

		HttpHeaders httpHeaders = exchange.getRequest().getHeaders();

//		String token = httpHeaders.getFirst("Authorization");
		String token = "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2luZm8iOnsiaWQiOjEsInVzZXJJZCI6InN5c2FkbWluIiwidXNlck5hbWUiOiLns7vnu5_nrqHnkIblkZgiLCJwYXNzd29yZCI6bnVsbCwicGhvbmUiOm51bGwsInNleCI6bnVsbCwic3RhdHVzIjoxLCJtYWlsQWRkcmVzcyI6bnVsbCwiaW1hZ2VVcmwiOm51bGwsInJlbWFyayI6bnVsbCwiY3JlYXRlRGF0ZSI6MTU0ODIxMDg0MDAwMCwidXBkYXRlRGF0ZSI6bnVsbH0sInVzZXJfbmFtZSI6Iuezu-e7n-euoeeQhuWRmCIsInNjb3BlIjpbInVzZXIiXSwiZXhwIjoxNTUwNDQwODY2LCJhdXRob3JpdGllcyI6WyIxIl0sImp0aSI6IjcyM2UwZmU5LTM4ZWQtNGJhOS1hY2Q5LWVmODI0ODMzZDgzMSIsImNsaWVudF9pZCI6InRlc3QifQ.KuNfTb9IYuGuocz9X0tqN39ChWK9MjGZJS69B2E2kStqcWGryotyd5RvThZUwwFmplAOfjgewoq4XmHoVc3K27Y9q4dOj2mFYuOSQDONykf2BwOn_o2f9LveWcRkwdFJt1Zt1LqCus1v1CGbJQvGrErvOIarOG2N1OJzovgIfBz0LYZR0ZQvsEZjxXZIURqnmoQG49yReKW-PxfeVJT0Sm1TpJQOox38Jp9BR3IdO7l3x7PVFSDNeuzjVyQZU_alZ1UN7xOWfKxbfG-BLumQ2DkBRxm1jUBeomVuXg0SyOGMl5Jcvt7191mIHGS77QM0S1b93DFeKUaeMxjVkxwMvQ";

//		URI uri = exchange.getRequest().getURI();
//
//		if (isSecurityURI(uri)) {
//			return chain.filter(exchange);
//		}

		// 认证校验
		String flag = restTemplate.getForObject("http://AUTH-CENTER/auth/extractToken?token=" + token, String.class);

		System.out.println(flag);
		return Mono.error(new Exception("没有权限哦!"));
//		if (flag) {
//			return chain.filter(exchange);
//		} else {
//			return Mono.error(new Exception("没有权限哦!"));
//		}

	}

	public boolean isSecurityURI(URI uri) {
		if (uri.getPath().contains("security")) {
			return true;
		}
		return false;
	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 1;
	}
}
