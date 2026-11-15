package cn.net.wangshifu.service;

import cn.net.wangshifu.config.YuBlogProperties;
import cn.net.wangshifu.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Stores avatars outside of the web application directory.
 *
 * <p>The 2018 version wrote {@code userId + "." + originalExtension} straight into
 * {@code request.getServletContext().getRealPath("/img/user-pic")}. Uploading {@code a.jsp} therefore
 * produced an executable file inside the deployed webapp (remote code execution). Here the
 * extension is whitelisted, the file must actually decode as an image, the name is a random UUID and
 * the target directory is configured independently of the deployment.
 */
@Service
public class AvatarStorage {

    private static final Logger log = LoggerFactory.getLogger(AvatarStorage.class);
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final int SIZE = 150;
    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private final YuBlogProperties properties;

    public AvatarStorage(YuBlogProperties properties) {
        this.properties = properties;
    }

    public String store(MultipartFile file, int userId) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("请选择要上传的图片");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ValidationException("图片不能超过 5MB");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ValidationException("只支持 jpg / png / gif / webp 格式的图片");
        }
        BufferedImage source = decode(file);
        BufferedImage scaled = scaleToSquare(source);

        String name = "u" + userId + "-" + UUID.randomUUID().toString().replace("-", "") + ".png";
        Path target = properties.getUploadDir().toAbsolutePath().normalize().resolve(name);
        try {
            Files.createDirectories(target.getParent());
            try (OutputStream out = Files.newOutputStream(target)) {
                ImageIO.write(scaled, "png", out);
            }
        } catch (IOException e) {
            throw new IllegalStateException("保存头像失败", e);
        }
        log.debug("stored avatar {} for user {}", target, userId);
        return properties.getUploadUrlPrefix() + "/" + name;
    }

    public void deleteQuietly(String url) {
        if (url == null || !url.startsWith(properties.getUploadUrlPrefix() + "/")) {
            return;
        }
        String name = url.substring(properties.getUploadUrlPrefix().length() + 1);
        Path file = properties.getUploadDir().toAbsolutePath().normalize().resolve(name).normalize();
        if (!file.startsWith(properties.getUploadDir().toAbsolutePath().normalize())) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("could not delete avatar {}", file, e);
        }
    }

    private static String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static BufferedImage decode(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                throw new ValidationException("文件不是有效的图片");
            }
            return image;
        } catch (IOException e) {
            throw new ValidationException("文件不是有效的图片");
        }
    }

    private static BufferedImage scaleToSquare(BufferedImage source) {
        int side = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - side) / 2;
        int y = (source.getHeight() - side) / 2;
        BufferedImage square = source.getSubimage(x, y, side, side);
        BufferedImage target = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = target.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(square, 0, 0, SIZE, SIZE, null);
        } finally {
            g.dispose();
        }
        return target;
    }
}