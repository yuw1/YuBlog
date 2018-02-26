package cn.net.wangshifu.bo;

import cn.net.wangshifu.dao.UserRoleDao;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class UserRoleBo {
    public static void addAdminRole(ApplicationContext context, int userId) {
        UserRoleDao userRoleDao = (UserRoleDao) context.getBean("userRoleDao");
        userRoleDao.addAdminRole(userId);
    }

    public static List<Integer> selectAllAdmin(ApplicationContext context) {
        UserRoleDao userRoleDao = (UserRoleDao) context.getBean("userRoleDao");
        List<Integer> integers = userRoleDao.selectAllAdmin();
        return integers;
    }

    public static void deleteAdminRole(ApplicationContext context, int adminId) {
        UserRoleDao userRoleDao = (UserRoleDao) context.getBean("userRoleDao");
        userRoleDao.deleteAdminRole(adminId);
    }


}
