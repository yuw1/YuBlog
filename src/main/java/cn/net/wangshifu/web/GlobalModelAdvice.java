package cn.net.wangshifu.web;

import cn.net.wangshifu.model.User;
import cn.net.wangshifu.service.AdminApplyService;
import cn.net.wangshifu.service.BlogInfoService;
import cn.net.wangshifu.service.CurrentUser;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;

/**
 * Attributes every page needs (blog name, current user, avatar, pending admin application).
 *
 * <p>In 2018 each of the six page controllers repeated these five lookups, including a brand new
 * {@code ApplicationContext} per request. They now live in one place and run once per request.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final CurrentUser currentUser;
    private final AdminApplyService adminApplyService;
    private final BlogInfoService blogInfoService;

    public GlobalModelAdvice(CurrentUser currentUser, AdminApplyService adminApplyService,
                             BlogInfoService blogInfoService) {
        this.currentUser = currentUser;
        this.adminApplyService = adminApplyService;
        this.blogInfoService = blogInfoService;
    }

    @ModelAttribute
    public void commonAttributes(Model model) {
        model.addAttribute("blogName", blogInfoService.blogName());
        model.addAttribute("year", LocalDate.now().getYear());
        currentUser.find().ifPresent(user -> {
            model.addAttribute("username", user.getUsername());
            model.addAttribute("userId", user.getId());
            model.addAttribute("userPic", user.getUserPic());
            model.addAttribute("hadApplyAdmin", adminApplyService.hasApplied(user.getId()) ? "true" : "false");
        });
    }

    /** Kept for readability: {@code User} is looked up once per request and cached. */
    protected User currentUserOrNull() {
        return currentUser.find().orElse(null);
    }
}