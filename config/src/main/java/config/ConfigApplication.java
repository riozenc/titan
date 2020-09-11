/**
 *    Auth:riozenc
 *    Date:2019年3月8日 下午4:00:27
 *    Title:com.riozenc.cim.CimApplication.java
 **/
package config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication(scanBasePackages = "config",exclude = MongoAutoConfiguration.class)
public class ConfigApplication {
	public static void main(String[] args) {
		SpringApplication.run(ConfigApplication.class, args);
	}
}
