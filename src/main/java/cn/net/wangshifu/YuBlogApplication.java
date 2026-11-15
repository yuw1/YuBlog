package cn.net.wangshifu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class YuBlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(YuBlogApplication.class, args);
    }
}