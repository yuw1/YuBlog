package cn.net.wangshifu;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 走一遍真实用户的操作路径：发文、评论、改资料、改密码、退出、被封禁、申请管理员。
 *
 * <p>每个用例都用随机用户名，互不影响；数据库是整个测试类共享的，所以不能依赖执行顺序。
 */
class UserJourneyTest extends AbstractWebTest {

    private String uniqueName() {
        return "u" + Long.toString(System.nanoTime() % 1_000_000_000L);
    }

    private MockHttpSession registerAndLogin(String username) throws Exception {
        mvc.perform(post("/register").with(csrf())
                        .param("user-name", username)
                        .param("pwd", "secret123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(username)));
        return login(username, "secret123");
    }

    private int publish(MockHttpSession session, String title, String body) throws Exception {
        String location = mvc.perform(post("/publish").session(session).with(csrf())
                        .param("title-text", title)
                        .param("content-text", body)
                        .param("article-abstract", "摘要")
                        .param("article-catalog-id", "2"))
                .andExpect(status().is3xxRedirection())
                .andReturn().getResponse().getRedirectedUrl();
        return Integer.parseInt(location.replaceAll(".*articleId=(\\d+).*", "$1"));
    }

    @Test
    void commentShowsUpOnTheArticleAndCanBeDeletedByItsAuthor() throws Exception {
        String author = uniqueName();
        var authorSession = registerAndLogin(author);
        int articleId = publish(authorSession, "评论测试 " + author, "<p>正文</p>");

        String text = "这是 " + author + " 的评论";
        mvc.perform(post("/comment").session(authorSession).with(csrf())
                        .param("article_id", String.valueOf(articleId))
                        .param("comment_text", text))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/article?articleId=" + articleId).session(authorSession))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(text)))
                .andExpect(content().string(containsString(author)));

        // 作者能在自己的个人页删掉这条评论
        String page = mvc.perform(get("/user?page=comment").session(authorSession))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String commentId = page.replaceAll("(?s).*?name=\"comment-id\" value=\"(\\d+)\".*", "$1");

        mvc.perform(post("/delete-comment-by-id").session(authorSession).with(csrf())
                        .param("comment-id", commentId))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/article?articleId=" + articleId).session(authorSession))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(text))));
    }

    @Test
    void renamingAUserUpdatesTheirArticlesAndComments() throws Exception {
        String original = uniqueName();
        var session = registerAndLogin(original);
        int articleId = publish(session, "改名前的文章", "<p>x</p>");
        mvc.perform(post("/comment").session(session).with(csrf())
                        .param("article_id", String.valueOf(articleId))
                        .param("comment_text", "改名前的评论"))
                .andExpect(status().is3xxRedirection());

        String renamed = uniqueName();
        mvc.perform(post("/changeUserInfo").session(session).with(csrf())
                        .param("user-name", renamed))
                .andExpect(status().is3xxRedirection());

        // 文章列表、评论列表用的都是冗余字段，必须一起更新
        mvc.perform(get("/blog").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("改名前的文章")))
                .andExpect(content().string(containsString(renamed)))
                .andExpect(content().string(not(containsString(original))));

        mvc.perform(get("/article?articleId=" + articleId).session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(renamed)))
                .andExpect(content().string(not(containsString(original))));
    }

    @Test
    void changingThePasswordForcesANewLogin() throws Exception {
        String username = uniqueName();
        var session = registerAndLogin(username);

        mvc.perform(post("/changePassword").session(session).with(csrf())
                        .param("pwd", "newsecret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?relogin"));

        // 旧密码失效，新密码可用
        mvc.perform(post("/login").with(csrf())
                        .param("user-name", username).param("pwd", "secret123"))
                .andExpect(redirectedUrl("/login?error"));

        login(username, "newsecret123");
    }

    @Test
    void logoutEndsTheSession() throws Exception {
        String username = uniqueName();
        var session = registerAndLogin(username);

        mvc.perform(post("/logout").session(session).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));

        mvc.perform(get("/")).andExpect(status().is3xxRedirection());
    }

    @Test
    void disabledUserCannotLogInUntilEnabledAgain() throws Exception {
        String username = uniqueName();
        registerAndLogin(username);
        int userId = userIdOf(username);

        var admin = login("admin", "admin123456");
        mvc.perform(post("/admin-disable-user").session(admin).with(csrf())
                        .param("user-id", String.valueOf(userId)))
                .andExpect(status().is3xxRedirection());

        mvc.perform(post("/login").with(csrf())
                        .param("user-name", username).param("pwd", "secret123"))
                .andExpect(redirectedUrl("/login?error"));

        mvc.perform(post("/admin-enable-user").session(admin).with(csrf())
                        .param("user-id", String.valueOf(userId)))
                .andExpect(status().is3xxRedirection());

        login(username, "secret123");
    }

    @Test
    void applyingForAdminReturnsJsonAndTheOwnerCanApproveIt() throws Exception {
        String username = uniqueName();
        var session = registerAndLogin(username);
        int userId = userIdOf(username);

        mvc.perform(post("/applyToAdmin").session(session).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("success"));

        // 重复申请只是刷新时间，不会报错
        mvc.perform(post("/applyToAdmin").session(session).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("success"));

        mvc.perform(get("/user?page=user").session(session))
                .andExpect(content().string(containsString("已申请")));

        var owner = login("owner", "owner123456");
        mvc.perform(get("/owner?page=admin-request").session(owner))
                .andExpect(content().string(containsString(username)));

        mvc.perform(post("/owner-passAdminApply").session(owner).with(csrf())
                        .param("user-id", String.valueOf(userId))
                        .param("passAdminApply", "true"))
                .andExpect(status().is3xxRedirection());

        // 权限是登录时加载的，批下来之后要重新登录才生效
        var promoted = login(username, "secret123");
        mvc.perform(get("/admin").session(promoted)).andExpect(status().isOk());
    }

    @Test
    void duplicateUsernameIsRejectedWithAMessage() throws Exception {
        String username = uniqueName();
        registerAndLogin(username);

        mvc.perform(post("/register").with(csrf())
                        .param("user-name", username)
                        .param("pwd", "secret123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("用户名已被占用")));
    }

    @Test
    void clearingAUsersContentAlsoRemovesCommentsOthersLeftOnTheirArticles() throws Exception {
        String author = uniqueName();
        String commenter = uniqueName();
        var authorSession = registerAndLogin(author);
        var commenterSession = registerAndLogin(commenter);

        int articleId = publish(authorSession, "会被清空的文章 " + author, "<p>x</p>");
        String commentText = "别人留的评论 " + commenter;
        mvc.perform(post("/comment").session(commenterSession).with(csrf())
                        .param("article_id", String.valueOf(articleId))
                        .param("comment_text", commentText))
                .andExpect(status().is3xxRedirection());

        var admin = login("admin", "admin123456");
        mvc.perform(post("/admin-clear-all-article-comment").session(admin).with(csrf())
                        .param("user-id", String.valueOf(userIdOf(author))))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/admin?page=comment").session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString(commentText))));
    }

    @Test
    void formErrorsComeBackToThePageInsteadOfAnErrorScreen() throws Exception {
        String username = uniqueName();
        var session = registerAndLogin(username);

        // 改名撞上已存在的账号：回到个人页并给出提示，而不是 400 页面
        mvc.perform(post("/changeUserInfo").session(session).with(csrf())
                        .param("user-name", "owner"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user?page=user"));
        mvc.perform(get("/user?page=user").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("用户名已被占用")));

        // 正文为空：退回编辑页，标题草稿还在
        mvc.perform(post("/publish").session(session).with(csrf())
                        .param("title-text", "草稿标题")
                        .param("content-text", "   ")
                        .param("article-abstract", "摘要")
                        .param("article-catalog-id", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/write-article"));
        mvc.perform(get("/write-article").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("正文不能为空")))
                .andExpect(content().string(containsString("草稿标题")));
    }

    /** Looks the id up through the public profile page, so the test does not need a DAO. */
    private int userIdOf(String username) throws Exception {
        var admin = login("admin", "admin123456");
        String html = mvc.perform(get("/admin?page=user").session(admin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        // rows are: <td class="mono">ID</td> ... <td>username</td>
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("<td class=\"mono\">(\\d+)</td>\\s*<td>" + username + "</td>")
                .matcher(html);
        if (!m.find()) {
            throw new IllegalStateException("user " + username + " not found in admin list");
        }
        return Integer.parseInt(m.group(1));
    }
}