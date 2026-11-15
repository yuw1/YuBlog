package cn.net.wangshifu;

import org.junit.jupiter.api.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowTest extends AbstractWebTest {

    @Test
    void anonymousIsRedirectedToLogin() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginPageRenders() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("用户名")));
    }

    @Test
    void seededOwnerCanLogInAndSeeHomePage() throws Exception {
        var session = login("owner", "owner123456");
        mvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("欢迎来到 YuBlog")));
    }

    @Test
    void wrongPasswordLandsOnErrorFlag() throws Exception {
        mvc.perform(post("/login")
                        .param("user-name", "owner")
                        .param("pwd", "wrong-password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void registrationCreatesAUserThatCanLogIn() throws Exception {
        String username = "tester" + System.nanoTime() % 100000;
        mvc.perform(post("/register")
                        .param("user-name", username)
                        .param("pwd", "secret123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(username)));

        var session = login(username, "secret123");
        mvc.perform(get("/").session(session)).andExpect(status().isOk());
    }

    @Test
    void shortPasswordIsRejected() throws Exception {
        mvc.perform(post("/register")
                        .param("user-name", "shortpwd" + System.nanoTime() % 100000)
                        .param("pwd", "123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("密码至少")));
    }
}