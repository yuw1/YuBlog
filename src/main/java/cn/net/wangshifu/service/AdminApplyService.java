package cn.net.wangshifu.service;

import cn.net.wangshifu.dao.AdminApplyDao;
import cn.net.wangshifu.dao.UserRoleDao;
import cn.net.wangshifu.exception.NotFoundException;
import cn.net.wangshifu.model.AdminApply;
import cn.net.wangshifu.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Replaces the {@code admin_apply_add} stored procedure.
 */
@Service
public class AdminApplyService {

    private final AdminApplyDao adminApplyDao;
    private final UserRoleDao userRoleDao;
    private final UserService userService;

    public AdminApplyService(AdminApplyDao adminApplyDao, UserRoleDao userRoleDao, UserService userService) {
        this.adminApplyDao = adminApplyDao;
        this.userRoleDao = userRoleDao;
        this.userService = userService;
    }

    public List<AdminApply> list() {
        return adminApplyDao.selectAll();
    }

    public boolean hasApplied(int userId) {
        return adminApplyDao.countByUserId(userId) > 0;
    }

    /** Idempotent: applying twice only refreshes {@code last_apply_time}. */
    @Transactional
    public void apply(int userId) {
        User user = userService.getById(userId);
        if (userService.hasRole(userId, UserService.ROLE_ADMIN)
                || userService.hasRole(userId, UserService.ROLE_OWNER)) {
            return;
        }
        if (hasApplied(userId)) {
            adminApplyDao.touchApply(userId);
        } else {
            adminApplyDao.insertApply(userId, user.getUsername());
        }
    }

    @Transactional
    public void approve(int userId) {
        if (!hasApplied(userId)) {
            throw new NotFoundException("该申请不存在");
        }
        adminApplyDao.deleteByUserId(userId);
        if (!userRoleDao.selectRolesByUserId(userId).contains(UserService.ROLE_ADMIN)) {
            userRoleDao.insertRole(userId, UserService.ROLE_ADMIN);
        }
    }

    @Transactional
    public void reject(int userId) {
        if (!hasApplied(userId)) {
            throw new NotFoundException("该申请不存在");
        }
        adminApplyDao.deleteByUserId(userId);
    }
}