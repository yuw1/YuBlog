package cn.net.wangshifu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@ConfigurationProperties(prefix = "yublog")
public class YuBlogProperties {

    /** Directory used to store uploaded avatars. Must live outside the web root. */
    private Path uploadDir = Path.of("./data/user-pic");

    /** Avatar used when a user has not uploaded one. Served from static resources. */
    private String defaultAvatar = "/img/user-pic/default.jpg";

    /** Public URL prefix the upload directory is exposed under. */
    private String uploadUrlPrefix = "/avatars";

    public Path getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(Path uploadDir) {
        this.uploadDir = uploadDir;
    }

    public String getDefaultAvatar() {
        return defaultAvatar;
    }

    public void setDefaultAvatar(String defaultAvatar) {
        this.defaultAvatar = defaultAvatar;
    }

    public String getUploadUrlPrefix() {
        return uploadUrlPrefix;
    }

    public void setUploadUrlPrefix(String uploadUrlPrefix) {
        this.uploadUrlPrefix = uploadUrlPrefix;
    }
}