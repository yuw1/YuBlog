package cn.net.wangshifu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {

    @RequestMapping(value = "/403")
    public String err403() {
        return "403.html";
    }

    @RequestMapping(value = "*")
    public String err404() {
        return "404.html";
    }

}
