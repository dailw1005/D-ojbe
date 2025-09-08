package com.dailw.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源配置类
 * 配置文件上传目录的静态资源访问
 * 
 * @author trave
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    /**
     * 头像上传路径
     */
    @Value("${file.upload.avatar.path:uploads/avatar/}")
    private String avatarUploadPath;

    /**
     * 头像访问URL前缀
     */
    @Value("${file.upload.avatar.url-prefix:/uploads/avatar/}")
    private String avatarUrlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置头像文件的静态资源访问
        registry.addResourceHandler(avatarUrlPrefix + "**")
                .addResourceLocations("file:" + avatarUploadPath);
    }
}