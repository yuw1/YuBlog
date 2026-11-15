package cn.net.wangshifu;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The 2018 upload endpoint let you write {@code something.jsp} into the deployed webapp.
 */
class UploadAndProfileTest extends AbstractWebTest {

    private static final Path UPLOAD_DIR = Path.of("target/test-avatars");

    @Test
    void jspUploadIsRejectedAndNothingIsWritten() throws Exception {
        var session = login("alice", "alice123456");
        long before = countFiles();
        MockMultipartFile payload = new MockMultipartFile("file", "shell.jsp",
                "application/octet-stream", "<% out.print(1); %>".getBytes());

        mvc.perform(multipart("/changeUserInfo")
                        .file(payload)
                        .session(session)
                        .with(csrf())
                        .param("user-name", "alice"))
                .andExpect(status().is3xxRedirection());

        // 回到个人页并说明原因，而不是把用户丢到一个错误页上
        mvc.perform(get("/user?page=user").session(session))
                .andExpect(content().string(containsString("只支持 jpg / png / gif / webp")));

        org.junit.jupiter.api.Assertions.assertEquals(before, countFiles(),
                "a rejected upload must not leave a file behind");
    }

    @Test
    void pngUploadIsStoredOutsideTheWebRoot() throws Exception {
        var session = login("alice", "alice123456");
        byte[] png = pngBytes();
        MockMultipartFile payload = new MockMultipartFile("file", "avatar.png", "image/png", png);

        mvc.perform(multipart("/changeUserInfo")
                        .file(payload)
                        .session(session)
                        .with(csrf())
                        .param("user-name", "alice"))
                .andExpect(status().is3xxRedirection());

        try (var files = Files.list(UPLOAD_DIR)) {
            org.junit.jupiter.api.Assertions.assertTrue(
                    files.anyMatch(p -> p.getFileName().toString().matches("u\\d+-[0-9a-f]+\\.png")),
                    "expected a resized png avatar to be stored under the configured directory");
        }
    }

    @Test
    void profilePageShowsTheDefaultAvatarForAFreshAccount() throws Exception {
        String username = "avatartest" + System.nanoTime() % 100000;
        mvc.perform(post("/register")
                        .with(csrf())
                        .param("user-name", username)
                        .param("pwd", "secret123"))
                .andExpect(status().isOk());

        var session = login(username, "secret123");
        mvc.perform(get("/user").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/img/user-pic/default.jpg")));
    }

    private static long countFiles() throws Exception {
        if (!Files.isDirectory(UPLOAD_DIR)) {
            return 0;
        }
        try (var files = Files.list(UPLOAD_DIR)) {
            return files.count();
        }
    }

    private static byte[] pngBytes() throws Exception {
        BufferedImage image = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}