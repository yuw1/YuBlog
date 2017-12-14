package cn.net.wangshifu.dao;

import cn.net.wangshifu.model.User;

public interface UserDao {
    User getUser(User user);
    void addUser(User user);
    void updateUser(User user);
    void deleteUser(int id);
}
