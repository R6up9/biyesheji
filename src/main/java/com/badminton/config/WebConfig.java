package com.badminton.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println("===== WebConfig starting =====");
        
        String userDir = System.getProperty("user.dir");
        System.out.println("WebConfig - user.dir: " + userDir);
        
        // 简单直接的方式：优先使用当前目录
        String basePath;
        File currentDirPom = new File(userDir, "pom.xml");
        if (currentDirPom.exists()) {
            basePath = userDir;
        } else {
            basePath = userDir + File.separator + "chongxin";
        }
        
        System.out.println("WebConfig - basePath: " + basePath);
        
        String uploadDir = basePath + File.separator + "uploads" + File.separator;
        System.out.println("WebConfig - uploadDir: " + uploadDir);
        
        // 确保文件夹存在
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
            System.out.println("WebConfig - Created upload directory: " + uploadDir);
        }
        
        // Windows路径处理
        String resourcePath = uploadDir.replace("\\", "/");
        String resourceLocation = "file:/" + resourcePath;
        if (!resourcePath.startsWith("/")) {
            resourceLocation = "file:" + resourcePath;
        }
        
        System.out.println("WebConfig - Mapping /uploads/** to: " + resourceLocation);
        
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(0);
        
        System.out.println("===== WebConfig done =====");
    }
}
