package cn.net.wangshifu.controller;

import cn.net.wangshifu.bo.AdminApplyBo;
import cn.net.wangshifu.dao.ArticleDao;
import cn.net.wangshifu.dao.CommentDao;
import cn.net.wangshifu.dao.UserDao;
import cn.net.wangshifu.model.Article;
import cn.net.wangshifu.model.Comment;
import cn.net.wangshifu.model.User;
import cn.net.wangshifu.util.CatalogUtil;
import cn.net.wangshifu.dao.CatalogDao;
import cn.net.wangshifu.model.Catalog;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static cn.net.wangshifu.util.PropertiesUtil.blogName;


@Controller
public class UserController {

    @RequestMapping(value = "user", method = RequestMethod.GET)
    public ModelAndView userInfo(
            @RequestParam(value = "userId", required = false) String showUserId,
            @RequestParam(value = "page", required = false) String page
    ) {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            int userId = Integer.parseInt(userDetail.getUsername());
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
            UserDao userDao = (UserDao) context.getBean("userDao");
            User user = userDao.selectUserById(Integer.parseInt(userDetail.getUsername()));
            model.addObject("username", user.getUsername());
            model.addObject("userPic", user.getUserPic());

            if (showUserId == null) {
                showUserId = String.valueOf(userId);
            }

            if (Integer.parseInt(showUserId) == userId) {
                model.addObject("isOwn", "true");
            } else {
                model.addObject("isOwn", "false");
            }

            user = userDao.selectUserById(Integer.parseInt(showUserId));
            model.addObject("showUserId", user.getId());
            model.addObject("showUsername", user.getUsername());
            model.addObject("showUserPic", user.getUserPic());

            ArticleDao articleDao = (ArticleDao) context.getBean("articleDao");
            List<Article> articles = articleDao.selectArticleByAuthorId(Integer.parseInt(showUserId));
            model.addObject("articles", articles);

            CommentDao commentDao = (CommentDao) context.getBean("commentDao");
            List<Comment> comments = commentDao.selectCommentByCommenterId(Integer.parseInt(showUserId));


            model.addObject("comments", comments);

            if (AdminApplyBo.hadApplyAdmin(context, userId)) {
                model.addObject("hadApplyAdmin", "true");
            } else {
                model.addObject("hadApplyAdmin", "false");
            }
            model.addObject("blogName", blogName);

            if (page == null) {
                page = "user";
            }

            if (page.equals("user")) {
                model.addObject("page", "user");
            } else if (page.equals("article")) {
                model.addObject("page", "article");
            } else if (page.equals("comment")) {
                model.addObject("page", "comment");
            } else {
                model.addObject("page", "user");
            }

            model.setViewName("user_info.html");
        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }


    @RequestMapping(value = "/changeUserInfo", method = RequestMethod.POST)
    public ModelAndView changeUserInfo(
            HttpServletRequest request,
            @RequestParam("user-id") String userId,
            @RequestParam("user-name") String userName,
            @RequestParam(value = "file", required = false) MultipartFile userPic
    ) throws IOException {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            int loginUserId = Integer.parseInt(userDetail.getUsername());

            if (loginUserId == Integer.parseInt(userId)) {
                ApplicationContext context = null;
                context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
                UserDao userDao = (UserDao) context.getBean("userDao");
                User user = new User();
                user.setId(Integer.parseInt(userId));
                user.setUsername(userName);

                //如果文件不为空，写入上传路径
                if (!userPic.isEmpty()) {
                    User userOld = null;
                    userOld = userDao.selectUserById(Integer.parseInt(userId));
                    String[] oldPic = userOld.getUserPic().split("/");
                    String oldPicName = oldPic[oldPic.length - 1];

                    //上传文件路径
                    String path = request.getServletContext().getRealPath("/img/user-pic");

                    File oldFilePath = new File(path, oldPicName);

                    System.gc();
                    System.out.println(oldFilePath.delete());

                    //上传文件名
                    String filename = userPic.getOriginalFilename();
                    String[] filenameRegex = filename.split("\\.");
                    filename = userId + "." + filenameRegex[filenameRegex.length - 1];
                    File filepath = new File(path, filename);
                    //判断路径是否存在，如果不存在就创建一个
                    if (!filepath.getParentFile().exists()) {
                        filepath.getParentFile().mkdirs();
                    }
                    //将上传文件保存到一个目标文件当中
                    userPic.transferTo(new File(path + File.separator + filename));
                    Thumbnails.of(path + File.separator + filename)
                            .size(150, 150)
                            .toFile(path + File.separator + filename);
                    Thumbnails.of(path + File.separator + filename).sourceRegion(Positions.CENTER, 150, 150).size(150, 150).keepAspectRatio(false)
                            .toFile(path + File.separator + filename);
                    user.setUserPic("/img/user-pic" + File.separator + filename);

                    userDao.updateUser(user);

                    userPic.getInputStream().close();

                } else {
                    userDao.updateUserWithoutPic(user);
                }
            }

            model.setViewName("redirect:/user");
        } else {
            model.setViewName("redirect:/login?notlogin");
        }

        return model;
    }

    @RequestMapping(value = "/changePassword", method = RequestMethod.POST)
    public ModelAndView changePassword(
            @RequestParam("pwd") String password
    ) throws IOException {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            int loginUserId = Integer.parseInt(userDetail.getUsername());

            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
            UserDao userDao = (UserDao) context.getBean("userDao");
            userDao.changePassword(loginUserId, password);


            model.setViewName("redirect:/login?relogin");
        } else {
            model.setViewName("redirect:/login?notlogin");
        }

        return model;
    }
}
