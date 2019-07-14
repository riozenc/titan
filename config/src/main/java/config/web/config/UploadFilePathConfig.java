package config.web.config;

import com.riozenc.titanTool.properties.Global;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class UploadFilePathConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //判断系统
        String os = System.getProperty("os.name");
        String sysFile= Global.getConfig("linux.project.path");
        if (os.toLowerCase().startsWith("win")) {
            sysFile=Global.getConfig("win.project.path");
            registry.addResourceHandler(Global.getConfig("staticResourceHandler")).
                    addResourceLocations("file:"+sysFile+ File.separator);
        }else{
            registry.addResourceHandler(Global.getConfig("staticResourceHandler")).
                    addResourceLocations("file:"+sysFile+ File.separator);
        }
    }
}
