package cn.net.wangshifu.bo;

import cn.net.wangshifu.dao.AdminApplyDao;
import cn.net.wangshifu.model.AdminApply;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class AdminApplyBo {

    public static boolean hadApplyAdmin(ApplicationContext context, int userId) {
        AdminApplyDao adminApplyDao = (AdminApplyDao) context.getBean("adminApplyDao");
        int count = adminApplyDao.hadApplyAdmin(userId);
        if (count > 0) {
            return true;
        } else {
            return false;
        }
    }

    public static List<AdminApply> selectAdminApply(ApplicationContext context) {
        AdminApplyDao adminApplyDao = (AdminApplyDao) context.getBean("adminApplyDao");
        List<AdminApply> adminApplies = adminApplyDao.selectAdminApply();
        return adminApplies;
    }

    public static void deleteAdminApply(ApplicationContext context, int userId) {
        AdminApplyDao adminApplyDao = (AdminApplyDao) context.getBean("adminApplyDao");
        adminApplyDao.deleteAdminApply(userId);
    }
}
