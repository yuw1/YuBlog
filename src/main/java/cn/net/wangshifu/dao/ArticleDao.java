package cn.net.wangshifu.dao;

import cn.net.wangshifu.model.Article;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ArticleDao {
    void addArticle(Article article);

    Article selectArticleById(int id);

    List<Article> selectArticleByAuthorId(int authorId);

    List<Article> selectArticleByCatalogId(List<Integer> catalogIds);

    List<Article> selectAllArticle();

    void deleteAllArticleByAuthorId(int authorId);

    void deleteArticleById(int articleId);

    void changeCatalog(@Param("catalogId") int catalogId, @Param("newCatalogId") int newCatalogId, @Param("newCatalogName") String newCatalogName);

    int selectUserIdByArticleId(@Param("articleId") int articleId);
}
