package org.eureka.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */

@SpringBootApplication
@EnableEurekaClient
//@EnableDiscoveryClient
@RestController
public class App {
	public static void main(String[] args) {
		SpringApplication.run(App.class, args);
		System.out.println("Hello World!");
	}
}
