/**
 *    Auth:riozenc
 *    Date:2019年3月8日 下午4:00:27
 *    Title:com.riozenc.cim.CimApplication.java
 **/
package config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication(scanBasePackages = "config")
@EnableConfigServer
@EnableEurekaClient
public class ConfigApplication {
	public static void main(String[] args) {
		SpringApplication.run(ConfigApplication.class, args);
	}
}
