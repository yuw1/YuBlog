package cn.net.wangshifu.bo;

import cn.net.wangshifu.dao.ArticleDao;
import cn.net.wangshifu.dao.CommentDao;
import cn.net.wangshifu.model.Article;
import cn.net.wangshifu.model.Comment;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class CommentBo {
    public static List<Comment> selectAllComment(ApplicationContext context) {
        CommentDao commentDao = (CommentDao) context.getBean("commentDao");
        List<Comment> comments = commentDao.selectAllComment();
        return comments;
    }
}
