/**
 * Author : chizf
 * Date : 2020年4月16日 下午8:35:18
 * Title : org.rs.web.WebSecurityConfig.java
 *
**/
package org.rs.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable(); // 关闭csrf
		super.configure(http); // 开启认证
		System.out.println("CSRF关闭并启动认证");
	}
}