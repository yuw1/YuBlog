package cn.net.wangshifu;


import cn.net.wangshifu.dao.AdminApplyDao;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class test {
    public static void main(String[] args) {
        ApplicationContext context = null;
        context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
        String userId = "10028";
        AdminApplyDao adminApplyDao = (AdminApplyDao) context.getBean("adminApplyDao");
        adminApplyDao.addAdminApply(Integer.parseInt(userId));
    }
}
