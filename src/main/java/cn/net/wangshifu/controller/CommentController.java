package cn.net.wangshifu.controller;

import cn.net.wangshifu.dao.CommentDao;
import cn.net.wangshifu.dao.UserDao;
import cn.net.wangshifu.model.Comment;
import cn.net.wangshifu.model.User;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CommentController {
    public static void main(String[] args) {
        ApplicationContext context = null;
        context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
        CommentDao commentDao = (CommentDao) context.getBean("commentDao");
        Comment comment = new Comment();
        comment.setArticleId(12);
        comment.setComment("666");
        comment.setUserId(10002);
        commentDao.addComment(comment);
    }
}
