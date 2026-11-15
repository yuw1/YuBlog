package cn.net.wangshifu.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    private final YuBlogProperties properties;

    public WebConfig(YuBlogProperties properties) {
        this.properties = properties;
        createUploadDirectory(properties.getUploadDir());
    }

    /** BCrypt replaces the salted MD5 that the 2018 version computed inside a stored procedure. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Avatars are served from a directory outside the application jar / web root. */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = properties.getUploadDir().toAbsolutePath().normalize().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler(properties.getUploadUrlPrefix() + "/**")
                .addResourceLocations(location);
    }

    private static void createUploadDirectory(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            log.warn("cannot create upload directory {}, avatar upload will fail", dir, e);
        }
    }
}