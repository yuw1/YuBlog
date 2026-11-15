package cn.net.wangshifu.dao;

import cn.net.wangshifu.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserDao {

    User selectUserById(int id);

    User selectUserByUsername(String username);

    List<User> selectUsersByIds(@Param("ids") List<Integer> ids);

    List<User> selectDisabledUsers();

    void insertUser(User user);

    void updateProfile(@Param("id") int id, @Param("username") String username, @Param("userPic") String userPic);

    void updatePassword(@Param("id") int id, @Param("passwordHash") String passwordHash);

    void updateEnabled(@Param("id") int id, @Param("enabled") boolean enabled);

    int countByUsername(String username);
}