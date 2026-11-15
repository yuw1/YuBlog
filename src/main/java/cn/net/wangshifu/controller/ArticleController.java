package cn.net.wangshifu.controller;

import cn.net.wangshifu.config.YuBlogProperties;
import cn.net.wangshifu.exception.NotFoundException;
import cn.net.wangshifu.exception.ValidationException;
import cn.net.wangshifu.model.Article;
import cn.net.wangshifu.model.Catalog;
import cn.net.wangshifu.model.User;
import cn.net.wangshifu.service.ArticleService;
import cn.net.wangshifu.service.CatalogService;
import cn.net.wangshifu.service.CommentService;
import cn.net.wangshifu.service.CurrentUser;
import cn.net.wangshifu.service.UserService;
import cn.net.wangshifu.util.CatalogUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ArticleController {

    private final ArticleService articleService;
    private final CommentService commentService;
    private final CatalogService catalogService;
    private final UserService userService;
    private final CurrentUser currentUser;
    private final YuBlogProperties properties;

    public ArticleController(ArticleService articleService, CommentService commentService,
                             CatalogService catalogService, UserService userService,
                             CurrentUser currentUser, YuBlogProperties properties) {
        this.articleService = articleService;
        this.commentService = commentService;
        this.catalogService = catalogService;
        this.userService = userService;
        this.currentUser = currentUser;
        this.properties = properties;
    }

    @GetMapping("/article")
    public String article(@RequestParam("articleId") int articleId,
                          @RequestParam(value = "msg", required = false) String msg,
                          Model model) {
        Article article = articleService.getRequired(articleId);
        User viewer = currentUser.require();
        User author = userService.findById(article.getAuthorId());

        model.addAttribute("articleId", article.getId());
        model.addAttribute("title", article.getTitle());
        model.addAttribute("content", article.getContent());
        model.addAttribute("authorId", article.getAuthorId());
        model.addAttribute("authorUsername", article.getAuthorUsername());
        model.addAttribute("authorPic", author != null ? author.getUserPic() : properties.getDefaultAvatar());
        model.addAttribute("catalogId", article.getCatalogId());
        model.addAttribute("catalogName", article.getCatalogName());
        model.addAttribute("lastModified", article.getLastModifiedText());
        model.addAttribute("isOwn", article.getAuthorId() == viewer.getId() ? "true" : "false");
        model.addAttribute("comments", commentService.byArticle(articleId));
        if (msg != null) {
            model.addAttribute("msg", msg);
        }
        return "article";
    }

    @GetMapping("/write-article")
    public String writeArticle(Model model) {
        Catalog root = catalogService.tree();
        int catalogId = catalogService.rootCatalogId();
        Catalog selected = catalogId == CatalogUtil.NO_CATALOG ? root : catalogService.getRequired(catalogId);
        model.addAttribute("catalogRoot", CatalogUtil.toTree(catalogService.all()));
        model.addAttribute("defaultCatalogId", catalogId);
        model.addAttribute("defaultCatalogName", selected.getName());
        return "write_article";
    }

    @PostMapping("/publish")
    public String publish(@RequestParam("title-text") String title,
                          @RequestParam("content-text") String content,
                          @RequestParam(value = "article-abstract", required = false) String summary,
                          @RequestParam("article-catalog-id") int catalogId,
                          RedirectAttributes redirect) {
        User author = currentUser.require();
        try {
            Article article = articleService.publish(author.getId(), title, content, summary, catalogId);
            return "redirect:/article?articleId=" + article.getId() + "&msg=文章发表成功";
        } catch (ValidationException | NotFoundException e) {
            // 别把一篇长文丢在错误页上：退回编辑页并给出原因
            redirect.addFlashAttribute("error", e.getMessage());
            redirect.addFlashAttribute("titleDraft", title);
            redirect.addFlashAttribute("abstractDraft", summary);
            redirect.addFlashAttribute("contentDraft", content);
            return "redirect:/write-article";
        }
    }

    @PostMapping("/delete-article-by-id")
    public String deleteArticle(@RequestParam("article-id") int articleId) {
        articleService.delete(articleId, currentUser.require());
        return "redirect:/user?page=article";
    }
}