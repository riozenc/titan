/**
 * File     :  org.rs.web rs
 * Author   :  rioze
 * DateTime :  2018年12月9日 下午8:16:03
**/
package org.rs.web;

import org.rs.web.interceptor.BaseInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebAppConfig implements WebMvcConfigurer {
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new BaseInterceptor()).addPathPatterns("/**");
	}
}
