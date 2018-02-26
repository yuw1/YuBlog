package cn.net.wangshifu.dao;

import cn.net.wangshifu.model.User;

import java.util.List;

public interface UserRoleDao {
    void addAdminRole(int userId);

    List<Integer> selectAllAdmin();

    void deleteAdminRole(int adminId);

    List<Integer> selectAllCommonUserId();
}
