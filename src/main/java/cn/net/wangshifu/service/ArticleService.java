package cn.net.wangshifu.service;

import cn.net.wangshifu.dao.ArticleDao;
import cn.net.wangshifu.dao.CommentDao;
import cn.net.wangshifu.exception.ForbiddenException;
import cn.net.wangshifu.exception.NotFoundException;
import cn.net.wangshifu.exception.ValidationException;
import cn.net.wangshifu.model.Article;
import cn.net.wangshifu.model.Catalog;
import cn.net.wangshifu.model.User;
import cn.net.wangshifu.util.HtmlSanitizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Article rules that the 2018 version pushed into MySQL triggers
 * ({@code before_article_add}, {@code before_article_delete}) live here now,
 * inside the same transaction as the insert/delete.
 */
@Service
public class ArticleService {

    private static final int TITLE_MAX = 120;
    private static final int SUMMARY_MAX = 300;

    private final ArticleDao articleDao;
    private final CommentDao commentDao;
    private final CatalogService catalogService;
    private final UserService userService;
    private final HtmlSanitizer htmlSanitizer;

    public ArticleService(ArticleDao articleDao, CommentDao commentDao, CatalogService catalogService,
                          UserService userService, HtmlSanitizer htmlSanitizer) {
        this.articleDao = articleDao;
        this.commentDao = commentDao;
        this.catalogService = catalogService;
        this.userService = userService;
        this.htmlSanitizer = htmlSanitizer;
    }

    @Transactional
    public Article publish(int authorId, String title, String contentHtml, String summary, int catalogId) {
        String cleanTitle = title == null ? "" : title.trim();
        if (cleanTitle.isEmpty() || cleanTitle.length() > TITLE_MAX) {
            throw new ValidationException("标题需为 1-" + TITLE_MAX + " 个字符");
        }
        String cleanContent = htmlSanitizer.sanitize(contentHtml);
        if (cleanContent.isBlank()) {
            throw new ValidationException("正文不能为空");
        }
        User author = userService.getById(authorId);
        Catalog catalog = catalogService.getRequired(catalogId);

        Article article = new Article();
        article.setTitle(cleanTitle);
        article.setContent(cleanContent);
        article.setArticleAbstract(truncate(summary));
        article.setAuthorId(author.getId());
        article.setAuthorUsername(author.getUsername());
        article.setCatalogId(catalog.getId());
        article.setCatalogName(catalog.getName());
        articleDao.insertArticle(article);
        return article;
    }

    public Article getRequired(int articleId) {
        Article article = articleDao.selectArticleById(articleId);
        if (article == null) {
            throw new NotFoundException("文章不存在");
        }
        return article;
    }

    public List<Article> byAuthor(int authorId) {
        return articleDao.selectArticleByAuthorId(authorId);
    }

    public List<Article> all() {
        return articleDao.selectAllArticle();
    }

    public List<Article> byCatalog(int catalogId) {
        List<Integer> ids = catalogService.subtreeIds(catalogId);
        if (ids.isEmpty()) {
            return List.of();
        }
        return articleDao.selectArticleByCatalogIds(ids);
    }

    /**
     * Author or staff only. Comments are removed first: this replaces the
     * {@code before_article_delete} trigger.
     */
    @Transactional
    public void delete(int articleId, User actor) {
        Integer authorId = articleDao.selectAuthorId(articleId);
        if (authorId == null) {
            throw new NotFoundException("文章不存在");
        }
        if (authorId != actor.getId() && !userService.isAdminOrOwner(actor.getId())) {
            throw new ForbiddenException("只能删除自己的文章");
        }
        commentDao.deleteByArticleId(articleId);
        articleDao.deleteArticleById(articleId);
    }

    /** Admin action: wipe everything a disabled user ever wrote. */
    @Transactional
    public void deleteEverythingOf(int userId) {
        commentDao.deleteByUserId(userId);
        // other people may have commented on this author's articles, and those comments
        // would otherwise point at articles that no longer exist
        commentDao.deleteCommentsOnArticlesOf(userId);
        articleDao.deleteByAuthorId(userId);
    }

    private static String truncate(String summary) {
        String clean = summary == null ? "" : summary.trim();
        return clean.length() <= SUMMARY_MAX ? clean : clean.substring(0, SUMMARY_MAX);
    }
}