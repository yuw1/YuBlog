package cn.net.wangshifu.dao;

import cn.net.wangshifu.model.AdminApply;

import java.util.List;

public interface AdminApplyDao {

    List<AdminApply> selectAdminApply();

    void addAdminApply(int userId);

    int hadApplyAdmin(int userId);

    void deleteAdminApply(int userId);
}
