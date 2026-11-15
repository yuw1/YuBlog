package cn.net.wangshifu.controller;

import cn.net.wangshifu.exception.ValidationException;
import cn.net.wangshifu.model.User;
import cn.net.wangshifu.service.ArticleService;
import cn.net.wangshifu.service.AvatarStorage;
import cn.net.wangshifu.service.CommentService;
import cn.net.wangshifu.service.CurrentUser;
import cn.net.wangshifu.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class UserController {

    private final UserService userService;
    private final ArticleService articleService;
    private final CommentService commentService;
    private final AvatarStorage avatarStorage;
    private final CurrentUser currentUser;

    public UserController(UserService userService, ArticleService articleService,
                          CommentService commentService, AvatarStorage avatarStorage,
                          CurrentUser currentUser) {
        this.userService = userService;
        this.articleService = articleService;
        this.commentService = commentService;
        this.avatarStorage = avatarStorage;
        this.currentUser = currentUser;
    }

    @GetMapping("/user")
    public String userInfo(@RequestParam(value = "userId", required = false) Integer userId,
                           @RequestParam(value = "page", required = false) String page,
                           Model model) {
        User viewer = currentUser.require();
        User shown = userId == null ? viewer : userService.getById(userId);

        model.addAttribute("showUserId", shown.getId());
        model.addAttribute("showUsername", shown.getUsername());
        model.addAttribute("showUserPic", shown.getUserPic());
        model.addAttribute("isOwn", shown.getId() == viewer.getId() ? "true" : "false");
        model.addAttribute("articles", articleService.byAuthor(shown.getId()));
        model.addAttribute("comments", commentService.byUser(shown.getId()));
        model.addAttribute("page", normalizePage(page));
        return "user_info";
    }

    /**
     * The owner of the profile is taken from the session, never from the {@code user-id} field,
     * so this endpoint cannot be used to overwrite somebody else's profile.
     */
    @PostMapping("/changeUserInfo")
    public String changeUserInfo(@RequestParam("user-name") String userName,
                                 @RequestParam(value = "file", required = false) MultipartFile file,
                                 RedirectAttributes redirect) {
        User me = currentUser.require();
        try {
            String newAvatar = null;
            if (file != null && !file.isEmpty()) {
                String previous = me.getUserPic();
                newAvatar = avatarStorage.store(file, me.getId());
                avatarStorage.deleteQuietly(previous);
            }
            userService.updateProfile(me.getId(), userName, newAvatar);
            redirect.addFlashAttribute("ok", "个人信息已保存");
        } catch (ValidationException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/user?page=user";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam("pwd") String password, HttpServletRequest request,
                                 RedirectAttributes redirect) {
        User me = currentUser.require();
        try {
            userService.changePassword(me.getId(), password);
        } catch (ValidationException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/user?page=user";
        }
        // changing the password invalidates the current session
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/login?relogin";
    }

    private static String normalizePage(String page) {
        List<String> known = List.of("user", "article", "comment", "change-pwd");
        return page != null && known.contains(page) ? page : "user";
    }
}