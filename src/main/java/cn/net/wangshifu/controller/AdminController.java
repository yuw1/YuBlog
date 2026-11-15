package cn.net.wangshifu.controller;

import cn.net.wangshifu.model.User;
import cn.net.wangshifu.service.AdminApplyService;
import cn.net.wangshifu.service.ArticleService;
import cn.net.wangshifu.service.CatalogService;
import cn.net.wangshifu.service.CommentService;
import cn.net.wangshifu.service.CurrentUser;
import cn.net.wangshifu.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Admin console. Access control lives in {@code SecurityConfig}; the controller no longer repeats
 * a "are you logged in" check in every method.
 */
@Controller
public class AdminController {

    private final UserService userService;
    private final ArticleService articleService;
    private final CommentService commentService;
    private final CatalogService catalogService;
    private final AdminApplyService adminApplyService;
    private final CurrentUser currentUser;

    public AdminController(UserService userService, ArticleService articleService, CommentService commentService,
                           CatalogService catalogService, AdminApplyService adminApplyService,
                           CurrentUser currentUser) {
        this.userService = userService;
        this.articleService = articleService;
        this.commentService = commentService;
        this.catalogService = catalogService;
        this.adminApplyService = adminApplyService;
        this.currentUser = currentUser;
    }

    @GetMapping("/admin")
    public String admin(@RequestParam(value = "page", required = false) String page, Model model) {
        String requested = page == null ? "user" : page;
        return switch (requested) {
            case "article" -> {
                model.addAttribute("articles", articleService.all());
                yield "admin_article";
            }
            case "comment" -> {
                model.addAttribute("comments", commentService.all());
                yield "admin_comment";
            }
            case "user-disable" -> {
                model.addAttribute("disableUsers", userService.listDisabledUsers());
                yield "admin_user_disable";
            }
            case "catalog" -> {
                // one list, so the template can tell "has children" from the same instances
                var catalogs = catalogService.all();
                model.addAttribute("catalogs", catalogs);
                model.addAttribute("catalogRoot", cn.net.wangshifu.util.CatalogUtil.toTree(catalogs));
                yield "admin_catalog";
            }
            default -> {
                model.addAttribute("commonUsers", userService.listCommonUsers());
                yield "admin_user";
            }
        };
    }

    @PostMapping("/applyToAdmin")
    @ResponseBody
    public Map<String, String> applyToAdmin() {
        adminApplyService.apply(currentUser.require().getId());
        return Map.of("state", "success");
    }

    @PostMapping("/admin-disable-user")
    public String disableUser(@RequestParam("user-id") int userId) {
        userService.setEnabled(userId, false);
        return "redirect:/admin?page=user";
    }

    @PostMapping("/admin-enable-user")
    public String enableUser(@RequestParam("user-id") int userId) {
        userService.setEnabled(userId, true);
        return "redirect:/admin?page=user-disable";
    }

    @PostMapping("/admin-clear-all-article-comment")
    public String clearAll(@RequestParam("user-id") int userId) {
        articleService.deleteEverythingOf(userId);
        return "redirect:/admin?page=user-disable";
    }

    @PostMapping("/admin-delete-article-by-id")
    public String deleteArticle(@RequestParam("article-id") int articleId) {
        User admin = currentUser.require();
        articleService.delete(articleId, admin);
        return "redirect:/admin?page=article";
    }

    @PostMapping("/admin-delete-comment-by-id")
    public String deleteComment(@RequestParam("comment-id") int commentId) {
        User admin = currentUser.require();
        commentService.delete(commentId, admin);
        return "redirect:/admin?page=comment";
    }

    @PostMapping("/admin-add-catalog")
    public String addCatalog(@RequestParam("parent-catalog-id") int parentCatalogId,
                             @RequestParam("sub-catalog-name") String name) {
        catalogService.add(parentCatalogId, name);
        return "redirect:/admin?page=catalog";
    }

    @PostMapping("/admin-rename-catalog")
    public String renameCatalog(@RequestParam("catalog-id") int catalogId,
                                @RequestParam("catalog-name") String name) {
        catalogService.rename(catalogId, name);
        return "redirect:/admin?page=catalog";
    }

    @PostMapping("/admin-delete-catalog-by-id")
    public String deleteCatalog(@RequestParam("catalog-id") int catalogId) {
        catalogService.delete(catalogId);
        return "redirect:/admin?page=catalog";
    }
}