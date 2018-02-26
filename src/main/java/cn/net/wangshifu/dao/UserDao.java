package cn.net.wangshifu.dao;

import cn.net.wangshifu.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserDao {
    User selectUserById(int id);

    String addUser(User user);
    void updateUser(User user);

    void updateUserWithoutPic(User user);
    void deleteUser(int id);

    List<User> selectUsersByIds(List<Integer> userIds);

    List<User> selectDisableUser();

    void disableUser(int userId);

    void enableUser(int userId);

    void changePassword(@Param("userId") int userId, @Param("password") String password);
}
