/**
 *    Auth:riozenc
 *    Date:2019年3月8日 下午4:00:27
 *    Title:com.riozenc.cim.CimApplication.java
 **/
package config;

import com.riozenc.titanTool.properties.Global;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

import javax.servlet.MultipartConfigElement;
import java.io.File;

@EnableEurekaClient
@SpringBootApplication(scanBasePackages = "config")
public class ConfigApplication {
	public static void main(String[] args) {
		SpringApplication.run(ConfigApplication.class, args);
	}
	//10天以上临时文件夹失效问题
	@Bean
	MultipartConfigElement multipartConfigElement(){
		MultipartConfigFactory factory = new MultipartConfigFactory();
		String systemName = System.getProperty("os.name");
		System.out.println("Current System Is : " + systemName);
		String tempPath="";
		if(!StringUtils.isBlank(systemName) && systemName.toLowerCase().contains("linux")){
			// Linux临时路径
			tempPath=Global.getConfig("linux.project.path")+File.separator +"temp";
		}else{
			// Windows临时路径
			tempPath=Global.getConfig("win.project.path")+File.separator +"temp";
		}
		factory.setLocation(tempPath);
		File filepath = new File(tempPath,"existsFile");
		// 判断路径是否存在，不存在则新创建一个
		if (!filepath.getParentFile().exists()) {
			filepath.getParentFile().mkdirs();
		}
		return factory.createMultipartConfig();
	}
}
