package cn.net.wangshifu.service;

import cn.net.wangshifu.dao.CommentDao;
import cn.net.wangshifu.exception.ForbiddenException;
import cn.net.wangshifu.exception.NotFoundException;
import cn.net.wangshifu.exception.ValidationException;
import cn.net.wangshifu.model.Article;
import cn.net.wangshifu.model.Comment;
import cn.net.wangshifu.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Replaces the {@code comment_add} trigger: title, author name, commenter name and avatar
 * are filled in here instead of by the database.
 */
@Service
public class CommentService {

    private static final int TEXT_MAX = 2000;

    private final CommentDao commentDao;
    private final ArticleService articleService;
    private final UserService userService;

    public CommentService(CommentDao commentDao, ArticleService articleService, UserService userService) {
        this.commentDao = commentDao;
        this.articleService = articleService;
        this.userService = userService;
    }

    @Transactional
    public Comment add(int articleId, int userId, String text) {
        String clean = text == null ? "" : text.trim();
        if (clean.isEmpty()) {
            throw new ValidationException("评论不能为空");
        }
        if (clean.length() > TEXT_MAX) {
            throw new ValidationException("评论不能超过 " + TEXT_MAX + " 个字符");
        }
        Article article = articleService.getRequired(articleId);
        User user = userService.getById(userId);

        Comment comment = new Comment();
        comment.setComment(clean);
        comment.setArticleId(article.getId());
        comment.setArticleTitle(article.getTitle());
        comment.setAuthor(article.getAuthorUsername());
        comment.setUserId(user.getId());
        comment.setUsername(user.getUsername());
        comment.setUserPic(user.getUserPic());
        commentDao.insertComment(comment);
        return comment;
    }

    public List<Comment> byArticle(int articleId) {
        return commentDao.selectByArticleId(articleId);
    }

    public List<Comment> byUser(int userId) {
        return commentDao.selectByUserId(userId);
    }

    public List<Comment> all() {
        return commentDao.selectAll();
    }

    @Transactional
    public void delete(int commentId, User actor) {
        Integer ownerId = commentDao.selectUserId(commentId);
        if (ownerId == null) {
            throw new NotFoundException("评论不存在");
        }
        if (ownerId != actor.getId() && !userService.isAdminOrOwner(actor.getId())) {
            throw new ForbiddenException("只能删除自己的评论");
        }
        commentDao.deleteCommentById(commentId);
    }
}