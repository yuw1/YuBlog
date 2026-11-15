package cn.net.wangshifu;

import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The 2018 controllers called {@code Integer.parseInt} on raw request parameters and walked the
 * catalog tree without null checks, so these requests used to produce Tomcat 500 pages.
 */
class CatalogAndErrorHandlingTest extends AbstractWebTest {

    @Test
    void unknownCatalogIdStillRendersTheHomePage() throws Exception {
        var session = login("owner", "owner123456");
        mvc.perform(get("/?catalogId=99999").session(session))
                .andExpect(status().isOk());
    }

    @Test
    void nonNumericArticleIdIsABadRequest() throws Exception {
        var session = login("alice", "alice123456");
        mvc.perform(get("/article?articleId=abc").session(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingArticleIsNotFound() throws Exception {
        var session = login("alice", "alice123456");
        mvc.perform(get("/article?articleId=424242").session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCanAddRenameAndDeleteACatalog() throws Exception {
        var admin = login("admin", "admin123456");
        mvc.perform(post("/admin-add-catalog")
                        .session(admin).with(csrf())
                        .param("parent-catalog-id", "1")
                        .param("sub-catalog-name", "临时分类"))
                .andExpect(status().is3xxRedirection());

        mvc.perform(get("/admin?page=catalog").session(admin))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("临时分类")));
    }

    @Test
    void deletingACatalogWithChildrenIsRefused() throws Exception {
        var admin = login("admin", "admin123456");
        mvc.perform(post("/admin-delete-catalog-by-id")
                        .session(admin).with(csrf())
                        .param("catalog-id", "1"))
                .andExpect(status().isBadRequest());
    }
}