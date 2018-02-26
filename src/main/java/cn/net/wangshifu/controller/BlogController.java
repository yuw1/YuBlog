package cn.net.wangshifu.controller;

import cn.net.wangshifu.bo.AdminApplyBo;
import cn.net.wangshifu.dao.ArticleDao;
import cn.net.wangshifu.dao.CatalogDao;
import cn.net.wangshifu.dao.UserDao;
import cn.net.wangshifu.model.Article;
import cn.net.wangshifu.model.Catalog;
import cn.net.wangshifu.model.User;
import cn.net.wangshifu.util.CatalogUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.ResourceBundle;


@Controller
public class BlogController {

    @RequestMapping("/blog")
    public ModelAndView blog(
            @RequestParam(value = "catalogId", required = false) String catalogId
    ) {
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

            if (catalogId == null) {
                catalogId = "0";
            }

            List<Catalog> route = CatalogUtil.getRouteFromRoot(catalogs, Integer.parseInt(catalogId));
            model.addObject("route", route);

            ArticleDao articleDao = (ArticleDao) context.getBean("articleDao");
            List<Integer> catalogIds = CatalogUtil.findSubCatalogs(catalogs, Integer.parseInt(catalogId));
            List<Article> articles = articleDao.selectArticleByCatalogId(catalogIds);
            model.addObject("articles", articles);

            if (AdminApplyBo.hadApplyAdmin(context, userId)) {
                model.addObject("hadApplyAdmin", "true");
            } else {
                model.addObject("hadApplyAdmin", "false");
            }

            ResourceBundle rb = ResourceBundle.getBundle("bloginfo".trim());
            String blogName = rb.getString("blog.name");
            model.addObject("blogName", blogName);

            model.setViewName("blog.html");
        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }
}
