package cn.net.wangshifu;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityRulesTest extends AbstractWebTest {

    @Test
    void plainUserCannotOpenTheAdminConsole() throws Exception {
        var session = login("alice", "alice123456");
        mvc.perform(get("/admin").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanOpenTheAdminConsole() throws Exception {
        var session = login("admin", "admin123456");
        mvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("博客管理")));
    }

    @Test
    void ownerCanOpenOwnerConsoleButAdminCannot() throws Exception {
        var owner = login("owner", "owner123456");
        mvc.perform(get("/owner?page=admin-list").session(owner))
                .andExpect(status().isOk());

        var admin = login("admin", "admin123456");
        mvc.perform(get("/owner?page=admin-list").session(admin))
                .andExpect(status().isForbidden());
    }

    @Test
    void csrfIsRequiredForStateChangingRequests() throws Exception {
        var session = login("alice", "alice123456");
        mvc.perform(post("/comment")
                        .session(session)
                        .param("comment_text", "no csrf token here")
                        .param("article_id", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCannotDeleteSomeoneElsesArticle() throws Exception {
        var alice = login("alice", "alice123456");
        String body = mvc.perform(post("/publish")
                        .session(alice)
                        .with(csrf())
                        .param("title-text", "alice 的私有文章")
                        .param("content-text", "<p>hello</p>")
                        .param("article-abstract", "摘要")
                        .param("article-catalog-id", "2"))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();
        int articleId = Integer.parseInt(body.replaceAll(".*articleId=(\\d+).*", "$1"));

        var admin = login("admin", "admin123456");
        mvc.perform(post("/delete-article-by-id")
                        .session(admin)
                        .with(csrf())
                        .param("article-id", String.valueOf(articleId)))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/article?articleId=" + articleId).session(admin))
                .andExpect(status().isNotFound());
    }

    @Test
    void articleBodyIsSanitizedBeforeItIsRendered() throws Exception {
        var alice = login("alice", "alice123456");
        String location = mvc.perform(post("/publish")
                        .session(alice)
                        .with(csrf())
                        .param("title-text", "带脚本的文章")
                        .param("content-text", "<p>safe</p><script>alert('xss')</script><img src=x onerror=alert(1)>")
                        .param("article-abstract", "xss")
                        .param("article-catalog-id", "2"))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();

        mvc.perform(get(location).session(alice))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<p>safe</p>")))
                .andExpect(content().string(not(containsString("alert('xss')"))))
                .andExpect(content().string(not(containsString("onerror"))));
    }
}