package cn.net.wangshifu.controller;

import cn.net.wangshifu.dao.CommentDao;
import cn.net.wangshifu.model.Comment;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class CommentController {

    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public ModelAndView addComment(
            @RequestParam("comment_text") String commentText,
            @RequestParam("article_id") String articleId
    ) {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            int userId = Integer.parseInt(userDetail.getUsername());
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
            CommentDao commentDao = (CommentDao) context.getBean("commentDao");
            Comment comment = new Comment();
            comment.setArticleId(Integer.parseInt(articleId));
            comment.setComment(commentText);
            comment.setUserId(userId);
            commentDao.addComment(comment);
            model.addObject("msg", "评论发表成功");

            model.setViewName("redirect:/article?articleId=" + articleId);
        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }


    @RequestMapping(value = "/delete-comment-by-id", method = RequestMethod.POST)
    public ModelAndView deleteCommentById(
            @RequestParam("comment-id") String commentId
    ) {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            int userId = Integer.parseInt(userDetail.getUsername());
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
            CommentDao commentDao = (CommentDao) context.getBean("commentDao");
            int commentAuthorId = commentDao.selectUserIdByCommentId(Integer.parseInt(commentId));
            if (commentAuthorId == userId) {
                commentDao.deleteCommentById(Integer.parseInt(commentId));
            }

            model.setViewName("redirect:/user?page=comment");
        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }

}
