package cn.net.wangshifu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ArticleController {

    @RequestMapping("/article")
    public String article() {
        return "article.html";
    }
}
