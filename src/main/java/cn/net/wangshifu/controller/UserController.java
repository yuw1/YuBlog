package cn.net.wangshifu.controller;

import cn.net.wangshifu.dao.UserDao;
import cn.net.wangshifu.model.User;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class UserController {
    public static void main(String[] args) {
        ApplicationContext context = null;
        context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
        UserDao userDao=(UserDao) context.getBean("userDao");
        User user=new User();
        //添加两条数据
        user.setNickname("WangYu1");
        user.setPassword("wangyu3322");
        userDao.addUser(user);
        System.out.println("添加成功");
    }
}
