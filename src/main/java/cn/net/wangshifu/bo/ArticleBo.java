package cn.net.wangshifu.bo;

import cn.net.wangshifu.dao.ArticleDao;
import cn.net.wangshifu.model.Article;
import org.springframework.context.ApplicationContext;

import java.util.List;

public class ArticleBo {
    public static List<Article> selectAllArticle(ApplicationContext context) {
        ArticleDao articleDao = (ArticleDao) context.getBean("articleDao");
        List<Article> articles = articleDao.selectAllArticle();
        return articles;
    }

}
