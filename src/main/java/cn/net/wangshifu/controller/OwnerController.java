package cn.net.wangshifu.controller;

import cn.net.wangshifu.service.AdminApplyService;
import cn.net.wangshifu.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OwnerController {

    private final AdminApplyService adminApplyService;
    private final UserService userService;

    public OwnerController(AdminApplyService adminApplyService, UserService userService) {
        this.adminApplyService = adminApplyService;
        this.userService = userService;
    }

    @GetMapping("/owner")
    public String owner(@RequestParam(value = "page", required = false) String page, Model model) {
        if ("admin-list".equals(page)) {
            model.addAttribute("admins", userService.listAdmins());
            return "owner_admin_list";
        }
        model.addAttribute("adminApplies", adminApplyService.list());
        return "owner_admin_request";
    }

    @PostMapping("/owner-passAdminApply")
    public String passAdminApply(@RequestParam("passAdminApply") String pass,
                                 @RequestParam("user-id") int userId) {
        if ("true".equals(pass)) {
            adminApplyService.approve(userId);
        } else {
            adminApplyService.reject(userId);
        }
        return "redirect:/owner?page=admin-request";
    }

    @PostMapping("/owner-deleteAdminRole")
    public String deleteAdminRole(@RequestParam("deleteAdminRole") String confirm,
                                  @RequestParam("user-id") int userId) {
        if ("true".equals(confirm)) {
            userService.revokeRole(userId, UserService.ROLE_ADMIN);
        }
        return "redirect:/owner?page=admin-list";
    }
}