package cn.net.wangshifu.controller;

import cn.net.wangshifu.exception.ValidationException;
import cn.net.wangshifu.model.User;
import cn.net.wangshifu.service.BlogInfoService;
import cn.net.wangshifu.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final UserService userService;
    private final BlogInfoService blogInfoService;

    public LoginController(UserService userService, BlogInfoService blogInfoService) {
        this.userService = userService;
        this.blogInfoService = blogInfoService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "notlogin", required = false) String notlogin,
                        @RequestParam(value = "relogin", required = false) String relogin,
                        HttpServletRequest request,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", lastAuthenticationMessage(request));
        }
        if (logout != null) {
            model.addAttribute("msg", "你已经成功退出");
        }
        if (notlogin != null) {
            model.addAttribute("msg", "请先登录账号");
        }
        if (relogin != null) {
            model.addAttribute("msg", "密码修改成功，请重新登录");
        }
        model.addAttribute("blogLoginSlogen", blogInfoService.get("blog.login.slogen"));
        model.addAttribute("blogLoginMinorSlogen", blogInfoService.get("blog.login.minor_slogen"));
        model.addAttribute("blogLoginSencondMinorSlogen", blogInfoService.get("blog.login.second_minor_slogen"));
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        addRegisterSlogans(model);
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam("user-name") String username,
                           @RequestParam("pwd") String password,
                           Model model) {
        try {
            User user = userService.register(username, password);
            model.addAttribute("id", user.getId());
            model.addAttribute("username", user.getUsername());
            return "register_success";
        } catch (ValidationException e) {
            model.addAttribute("error", e.getMessage());
            addRegisterSlogans(model);
            return "register";
        }
    }

    private void addRegisterSlogans(Model model) {
        model.addAttribute("blogRegisterSlogen", blogInfoService.get("blog.register.slogen"));
        model.addAttribute("blogRegisterMinorSlogen", blogInfoService.get("blog.register.minor_slogen"));
        model.addAttribute("blogRegisterSencondMinorSlogen", blogInfoService.get("blog.register.second_minor_slogen"));
    }

    /**
     * Spring Security stores the failure on the session; the 2018 code read it without a null check
     * and threw a NullPointerException whenever the attribute was missing.
     */
    private static String lastAuthenticationMessage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object exception = session == null ? null : session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        if (exception instanceof DisabledException) {
            return "该账号已被禁用，请联系管理员";
        }
        return "用户名或密码错误";
    }
}