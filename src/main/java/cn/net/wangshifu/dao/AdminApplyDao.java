package cn.net.wangshifu.dao;

import cn.net.wangshifu.model.AdminApply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminApplyDao {

    List<AdminApply> selectAll();

    int countByUserId(int userId);

    void insertApply(@Param("userId") int userId, @Param("userName") String userName);

    void touchApply(int userId);

    void deleteByUserId(int userId);
}