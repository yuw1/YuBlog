package cn.net.wangshifu.dao;

import cn.net.wangshifu.model.Catalog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CatalogDao {
    List<Catalog> selectAllCatalog();

    void addCatalog(@Param("subCatalogName") String subCatalogName, @Param("parentCatalogId") int parentCatalogId);

    void renameCatalog(@Param("catalogId") int catalogId, @Param("catalogName") String catalogName);

    void deleteCatalogById(@Param("catalogId") int catalogId);

    Catalog selectCatalogById(@Param("catalogId") int catalogId);
}
