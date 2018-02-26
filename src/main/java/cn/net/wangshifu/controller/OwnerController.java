package cn.net.wangshifu.controller;


import cn.net.wangshifu.bo.AdminApplyBo;
import cn.net.wangshifu.bo.UserRoleBo;
import cn.net.wangshifu.dao.UserDao;
import cn.net.wangshifu.model.AdminApply;
import cn.net.wangshifu.model.User;
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
public class OwnerController {

    @RequestMapping(value = "/owner", method = RequestMethod.GET)
    public ModelAndView owner(@RequestParam("page") String page) {
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

            if (page.equals("admin-request")) {
                List<AdminApply> adminApplies = AdminApplyBo.selectAdminApply(context);
                model.addObject("adminApplies", adminApplies);
                model.setViewName("owner_admin_request.html");
            } else if (page.equals("admin-list")) {
                List<Integer> adminIds = UserRoleBo.selectAllAdmin(context);

                if (adminIds.size() != 0) {
                    List<User> admins = userDao.selectUsersByIds(adminIds);
                    model.addObject("admins", admins);
                }
                model.setViewName("owner_admin_list.html");
            }


        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }

    @RequestMapping(value = "/owner-deleteAdminRole", method = RequestMethod.POST)
    public ModelAndView deleteAdminRole(
            @RequestParam("deleteAdminRole") String deleteAdminRole,
            @RequestParam("user-id") String adminId
    ) {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            model.addObject("id", userDetail.getUsername());
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
            if (deleteAdminRole.equals("true")) {
                UserRoleBo.deleteAdminRole(context, Integer.parseInt(adminId));
            }
            model.setViewName("redirect:/owner?page=admin-list");

        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }

    @RequestMapping(value = "/owner-passAdminApply", method = RequestMethod.POST)
    public ModelAndView passAdminApply(
            @RequestParam("passAdminApply") String passAdminApply,
            @RequestParam("user-id") String requestUserId
    ) {
        ModelAndView model = new ModelAndView();
        //检查用户是否已经登录
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof AnonymousAuthenticationToken)) {
            UserDetails userDetail = (UserDetails) auth.getPrincipal();
            model.addObject("id", userDetail.getUsername());
            ApplicationContext context = null;
            context = new ClassPathXmlApplicationContext("ApplicationContext.xml");

            if (passAdminApply.equals("true")) {
                AdminApplyBo.deleteAdminApply(context, Integer.parseInt(requestUserId));
                UserRoleBo.addAdminRole(context, Integer.parseInt(requestUserId));
            } else if (passAdminApply.equals("false")) {
                AdminApplyBo.deleteAdminApply(context, Integer.parseInt(requestUserId));
            }
            model.setViewName("redirect:/owner?page=admin-request");

        } else {
            model.setViewName("redirect:/login?notlogin");
        }
        return model;
    }

}
