package cn.net.wangshifu.dao;

import cn.net.wangshifu.model.Catalog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CatalogDao {

    List<Catalog> selectAll();

    Catalog selectById(int id);

    void insertCatalog(@Param("name") String name, @Param("parentId") int parentId);

    void renameCatalog(@Param("id") int id, @Param("name") String name);

    void deleteCatalog(int id);

    int countChildren(int id);

    int countArticles(int id);
}