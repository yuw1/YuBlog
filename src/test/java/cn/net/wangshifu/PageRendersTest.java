package cn.net.wangshifu;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Renders every page as a real user would see it.
 *
 * <p>The 2018 version had no such safety net, so a broken template only showed up when somebody
 * opened the page. This test walks the whole navigation as owner (the widest role).
 */
class PageRendersTest extends AbstractWebTest {

    @Test
    void everyOwnerPageRenders() throws Exception {
        var session = login("owner", "owner123456");

        String[] pages = {
                "/",
                "/blog",
                "/blog?catalogId=1",
                "/blog?catalogId=2",
                "/write-article",
                "/user",
                "/user?page=user",
                "/user?page=article",
                "/user?page=comment",
                "/article?articleId=1",
                "/admin?page=user",
                "/admin?page=article",
                "/admin?page=comment",
                "/admin?page=user-disable",
                "/admin?page=catalog",
                "/owner?page=admin-request",
                "/owner?page=admin-list",
        };

        for (String page : pages) {
            mvc.perform(get(page).session(session))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("写文章")));
        }
    }

    @Test
    void publicPagesRender() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("登录")));
        mvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("注册")));
    }

    @Test
    void errorPagesRender() throws Exception {
        // everything but /login and /register needs a session, so probe as a logged in user
        var owner = login("owner", "owner123456");
        mvc.perform(get("/definitely-not-here").session(owner))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("404")));

        // AccessDeniedHandler forwards to /403; MockMvc does not execute the forward,
        // so only the status is asserted here (the body is checked by the live smoke test).
        var alice = login("alice", "alice123456");
        mvc.perform(get("/admin").session(alice))
                .andExpect(status().isForbidden());
    }

    @Test
    void catalogTreeIsRenderedOnTheBlogPage() throws Exception {
        var session = login("alice", "alice123456");
        mvc.perform(get("/blog").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("技术")))
                .andExpect(content().string(containsString("生活")));
    }
}