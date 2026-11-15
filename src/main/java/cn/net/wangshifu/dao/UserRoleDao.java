package cn.net.wangshifu.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserRoleDao {

    void insertRole(@Param("userId") int userId, @Param("role") String role);

    void deleteRole(@Param("userId") int userId, @Param("role") String role);

    List<String> selectRolesByUserId(int userId);

    /** Users that have ROLE_ADMIN but not ROLE_OWNER. */
    List<Integer> selectAdminUserIds();

    /** Users that only have ROLE_USER. */
    List<Integer> selectCommonUserIds();
}