package cn.net.wangshifu.controller;

import cn.net.wangshifu.bo.AdminApplyBo;
import cn.net.wangshifu.dao.ArticleDao;
import cn.net.wangshifu.dao.CatalogDao;
import cn.net.wangshifu.dao.CommentDao;
import cn.net.wangshifu.dao.UserDao;
import cn.net.wangshifu.model.Article;
import cn.net.wangshifu.model.Catalog;
import cn.net.wangshifu.model.Comment;
import cn.net.wangshifu.model.User;
import cn.net.wangshifu.util.CatalogUtil;
import com.sun.org.apache.xpath.internal.operations.Mod;
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

import java.util.List;

import static cn.net.wangshifu.util.PropertiesUtil.blogName;

@Controller
public class ArticleController {

    @RequestMapping("/article")
    public ModelAndView article(@RequestParam("articleId") String articleId, @RequestParam(value = "msg", required = false) String msg) {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            model.addObject("id", userDetail.getUsername());
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
            UserDao userDao = (UserDao) context.getBean("userDao");

            int userId = Integer.parseInt(userDetail.getUsername());
            User user = userDao.selectUserById(userId);
            model.addObject("username", user.getUsername());
            model.addObject("userPic", user.getUserPic());

            ArticleDao articleDao = (ArticleDao) context.getBean("articleDao");
            Article article = articleDao.selectArticleById(Integer.parseInt(articleId));
            model.addObject("articleId", article.getId());
            model.addObject("title", article.getTitle());
            model.addObject("content", article.getContent());
            model.addObject("authorUsername", article.getAuthorUsername());
            model.addObject("authorId", article.getAuthorId());
            model.addObject("lastModified", article.getLastModified());

            CommentDao commentDao = (CommentDao) context.getBean("commentDao");
            List<Comment> comments = commentDao.selectCommentByArticleId(Integer.parseInt(articleId));
            model.addObject("comments", comments);

            if (msg != null) {
                model.addObject("msg", "评论发表成功");
            }


            if (AdminApplyBo.hadApplyAdmin(context, userId)) {
                model.addObject("hadApplyAdmin", "true");
            } else {
                model.addObject("hadApplyAdmin", "false");
            }

            model.addObject("blogName", blogName);

            model.setViewName("article.html");

        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }

    @RequestMapping(value = "write-article", method = RequestMethod.GET)
    public ModelAndView writeArticle() {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            model.addObject("id", userDetail.getUsername());
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
            UserDao userDao = (UserDao) context.getBean("userDao");

            int userId = Integer.parseInt(userDetail.getUsername());
            User user = userDao.selectUserById(userId);
            model.addObject("username", user.getUsername());
            model.addObject("userPic", user.getUserPic());

            CatalogDao catalogDao = (CatalogDao) context.getBean("catalogDao");
            List<Catalog> catalogs = catalogDao.selectAllCatalog();
            Catalog root = CatalogUtil.catalog2Tree(catalogs);

            model.addObject("catalogRoot", root);

            if (AdminApplyBo.hadApplyAdmin(context, userId)) {
                model.addObject("hadApplyAdmin", "true");
            } else {
                model.addObject("hadApplyAdmin", "false");
            }
            model.addObject("blogName", blogName);

            model.setViewName("write_article.html");
        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }

    @RequestMapping(value = "/publish", method = RequestMethod.POST)
    public ModelAndView publish(
            @RequestParam("title-text") String title,
            @RequestParam("content-text") String content,
            @RequestParam("article-abstract") String articleAbstract,
            @RequestParam("article-catalog-id") String catalogId
    ) {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            int authorId = Integer.parseInt(userDetail.getUsername());

            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");

            ArticleDao articleDao = (ArticleDao) context.getBean("articleDao");
            Article article = new Article();
            article.setTitle(title);
            article.setAuthorId(authorId);
            article.setArticleAbstract(articleAbstract);
            article.setContent(content);
            article.setCatalogId(Integer.parseInt(catalogId));
            articleDao.addArticle(article);
            int id = article.getId();
            model.addObject("msg", "文章发表成功");
            model.setViewName("redirect:/article?articleId=" + id);
        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }


    @RequestMapping(value = "/delete-article-by-id", method = RequestMethod.POST)
    public ModelAndView deleteArticleById(
            @RequestParam("article-id") String articleId
    ) {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            int userId = Integer.parseInt(userDetail.getUsername());
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
            ArticleDao articleDao = (ArticleDao) context.getBean("articleDao");
            int authorId = articleDao.selectUserIdByArticleId(Integer.parseInt(articleId));
            if (authorId == userId) {
                articleDao.deleteArticleById(Integer.parseInt(articleId));
            }

            model.setViewName("redirect:/user?page=article");
        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }

}
