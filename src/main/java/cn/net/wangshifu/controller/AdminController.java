package cn.net.wangshifu.controller;

import cn.net.wangshifu.bo.AdminApplyBo;
import cn.net.wangshifu.bo.ArticleBo;
import cn.net.wangshifu.bo.CommentBo;
import cn.net.wangshifu.bo.UserBo;
import cn.net.wangshifu.dao.*;
import cn.net.wangshifu.model.Article;
import cn.net.wangshifu.model.Catalog;
import cn.net.wangshifu.model.Comment;
import cn.net.wangshifu.model.User;
import cn.net.wangshifu.util.CatalogUtil;
import com.alibaba.fastjson.JSON;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.net.wangshifu.util.PropertiesUtil.blogName;

@Controller
public class AdminController {

    @RequestMapping("/admin")
    public ModelAndView admin(
            @RequestParam(value = "page", required = false) String page
    ) {
        if (page == null) {
            page = "user";
        }

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

            if (AdminApplyBo.hadApplyAdmin(context, userId)) {
                model.addObject("hadApplyAdmin", "true");
            } else {
                model.addObject("hadApplyAdmin", "false");
            }

            model.addObject("blogName", blogName);

            if (page.equals("user")) {
                List<User> commonUsers = UserBo.selectAllCommonUser(context);
                model.addObject("commonUsers", commonUsers);
                model.setViewName("admin_user.html");
            } else if (page.equals("article")) {
                List<Article> articles = ArticleBo.selectAllArticle(context);
                model.addObject("articles", articles);
                model.setViewName("admin_article.html");
            } else if (page.equals("comment")) {
                List<Comment> comments = CommentBo.selectAllComment(context);
                model.addObject("comments", comments);
                model.setViewName("admin_comment.html");
            } else if (page.equals("user-disable")) {
                List<User> disableUsers = UserBo.selectDisableUser(context);
                model.addObject("disableUsers", disableUsers);
                model.setViewName("admin_user_disable.html");
            } else if (page.equals("catalog")) {
                CatalogDao catalogDao = (CatalogDao) context.getBean("catalogDao");
                List<Catalog> catalogs = catalogDao.selectAllCatalog();
                Catalog root = CatalogUtil.catalog2Tree(catalogs);

                model.addObject("catalogRoot", root);
                model.addObject("catalogs", catalogs);
                model.setViewName("admin_catalog.html");
            } else {
                model.setViewName("admin_user.html");
            }
        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }

    @RequestMapping(value = "/applyToAdmin", method = RequestMethod.POST)
    @ResponseBody
    public String applyToAdmin() {
        Map<String, String> mapper = new HashMap<String, String>();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
            String userId = userDetail.getUsername();
            AdminApplyDao adminApplyDao = (AdminApplyDao) context.getBean("adminApplyDao");
            adminApplyDao.addAdminApply(Integer.parseInt(userId));
            mapper.put("state", "success");
        } else {
            mapper.put("state", "fail");
        }
        return JSON.toJSONString(mapper);
    }

    @RequestMapping(value = "/admin-disable-user", method = RequestMethod.POST)
    public ModelAndView disableUser(
            @RequestParam(value = "user-id") String disableUserId
    ) {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
            String userId = userDetail.getUsername();
            UserDao userDao = (UserDao) context.getBean("userDao");
            userDao.disableUser(Integer.parseInt(disableUserId));
            model.setViewName("redirect:/admin?page=user");
        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }

    @RequestMapping(value = "/admin-enable-user", method = RequestMethod.POST)
    public ModelAndView enableUser(
            @RequestParam(value = "user-id") String enableUserId
    ) {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
            String userId = userDetail.getUsername();
            UserDao userDao = (UserDao) context.getBean("userDao");
            userDao.enableUser(Integer.parseInt(enableUserId));
            model.setViewName("redirect:/admin?page=user-disable");
        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }


    @RequestMapping(value = "/admin-clear-all-article-comment", method = RequestMethod.POST)
    public ModelAndView clearAllArticleComment(
            @RequestParam(value = "user-id") String clearInfoUserId
    ) {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
            String userId = userDetail.getUsername();
            CommentDao commentDao = (CommentDao) context.getBean("commentDao");
            commentDao.deleteAllCommentByUserId(Integer.parseInt(clearInfoUserId));

            ArticleDao articleDao = (ArticleDao) context.getBean("articleDao");
            articleDao.deleteAllArticleByAuthorId(Integer.parseInt(clearInfoUserId));

            model.setViewName("redirect:/admin?page=user-disable");
        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }


    @RequestMapping(value = "/admin-delete-article-by-id", method = RequestMethod.POST)
    public ModelAndView deleteArticleById(
            @RequestParam(value = "article-id") String articleId
    ) {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");

            ArticleDao articleDao = (ArticleDao) context.getBean("articleDao");
            articleDao.deleteArticleById(Integer.parseInt(articleId));

            model.setViewName("redirect:/admin?page=article");
        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }


    @RequestMapping(value = "/admin-delete-comment-by-id", method = RequestMethod.POST)
    public ModelAndView deleteCommentById(
            @RequestParam(value = "comment-id") String commentId
    ) {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
            CommentDao commentDao = (CommentDao) context.getBean("commentDao");
            commentDao.deleteCommentById(Integer.parseInt(commentId));

            model.setViewName("redirect:/admin?page=comment");
        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }


    @RequestMapping(value = "/admin-add-catalog", method = RequestMethod.POST)
    public ModelAndView addCatalog(
            @RequestParam(value = "parent-catalog-id") String parentCatalogId,
            @RequestParam(value = "sub-catalog-name") String subCatalogName
    ) {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
            CatalogDao catalogDao = (CatalogDao) context.getBean("catalogDao");
            catalogDao.addCatalog(subCatalogName, Integer.parseInt(parentCatalogId));

            model.setViewName("redirect:/admin?page=catalog");
        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }

    @RequestMapping(value = "/admin-rename-catalog", method = RequestMethod.POST)
    public ModelAndView renameCatalog(
            @RequestParam(value = "catalog-id") String catalogId,
            @RequestParam(value = "catalog-name") String catalogName
    ) {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
            CatalogDao catalogDao = (CatalogDao) context.getBean("catalogDao");
            catalogDao.renameCatalog(Integer.parseInt(catalogId), catalogName);

            model.setViewName("redirect:/admin?page=catalog");
        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }

    @RequestMapping(value = "/admin-delete-catalog-by-id", method = RequestMethod.POST)
    public ModelAndView deleteCatalogById(
            @RequestParam(value = "catalog-id") String catalogId
    ) {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
            CatalogDao catalogDao = (CatalogDao) context.getBean("catalogDao");
            Catalog catalog = catalogDao.selectCatalogById(Integer.parseInt(catalogId));
            Catalog parentCatalog = catalogDao.selectCatalogById(catalog.getParentId());
            ArticleDao articleDao = (ArticleDao) context.getBean("articleDao");
            articleDao.changeCatalog(catalog.getId(), parentCatalog.getId(), parentCatalog.getName());

            catalogDao.deleteCatalogById(Integer.parseInt(catalogId));

            model.setViewName("redirect:/admin?page=catalog");
        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }
}
