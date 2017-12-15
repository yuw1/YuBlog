package cn.net.wangshifu.controller;

import cn.net.wangshifu.dao.ArticleDao;
import cn.net.wangshifu.model.Article;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ArticleController {
    public static void main(String[] args) {
        ApplicationContext context = null;
        context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
        ArticleDao articleDao = (ArticleDao) context.getBean("articleDao");
        Article article = new Article();
        article.setContent("如果你在触发器里面对刚刚插入的数据进行了 insert/update, 则出现这个问题。因为会造成循环的调用.\n" +
                "\n" +
                "create trigger test\n" +
                "before update on test\n" +
                "for each row\n" +
                "  update test set NEW.updateTime = NOW() where id=NEW.ID;\n" +
                "END\n" +
                "应该使用set操作，而不是在触发器里使用 update,比如\n" +
                "\n" +
                "create trigger test\n" +
                "before update on test\n" +
                "for each row\n" +
                "set NEW.updateTime = NOW();\n" +
                "END");
        article.setAuthorId(10008);
        articleDao.addArticle(article);
    }
}
