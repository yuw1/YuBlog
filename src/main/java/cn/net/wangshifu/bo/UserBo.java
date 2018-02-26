package cn.net.wangshifu.bo;

import cn.net.wangshifu.dao.UserDao;
import cn.net.wangshifu.dao.UserRoleDao;
import cn.net.wangshifu.model.User;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class UserBo {

    public static List<User> selectAllCommonUser(ApplicationContext context) {
        UserRoleDao userRoleDao = (UserRoleDao) context.getBean("userRoleDao");
        List<Integer> commonUserIds = userRoleDao.selectAllCommonUserId();

        List<User> commonUsers = null;
        if (commonUserIds.size() != 0) {
            UserDao userDao = (UserDao) context.getBean("userDao");
            commonUsers = userDao.selectUsersByIds(commonUserIds);
        }
        return commonUsers;
    }

    public static List<User> selectDisableUser(ApplicationContext context) {
        UserDao userDao = (UserDao) context.getBean("userDao");
        List<User> disableUsers = userDao.selectDisableUser();
        return disableUsers;
    }

}
