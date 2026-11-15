package cn.net.wangshifu.dao;

import cn.net.wangshifu.model.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleDao {

    void insertArticle(Article article);

    Article selectArticleById(int id);

    List<Article> selectArticleByAuthorId(int authorId);

    List<Article> selectArticleByCatalogIds(@Param("catalogIds") List<Integer> catalogIds);

    List<Article> selectAllArticle();

    Integer selectAuthorId(int articleId);

    int countByCatalogId(int catalogId);

    void deleteArticleById(int articleId);

    void deleteByAuthorId(int authorId);

    /** Keeps the denormalised author name in sync when a user renames themselves. */
    void updateAuthorUsername(@Param("authorId") int authorId, @Param("username") String username);

    /** Used when a catalog is renamed or deleted. */
    void reassignCatalog(@Param("catalogId") int catalogId,
                         @Param("newCatalogId") int newCatalogId,
                         @Param("newCatalogName") String newCatalogName);
}