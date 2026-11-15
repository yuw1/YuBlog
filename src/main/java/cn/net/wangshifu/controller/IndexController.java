package cn.net.wangshifu.controller;

import cn.net.wangshifu.web.CatalogPageModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IndexController {

    private final CatalogPageModel catalogPageModel;

    public IndexController(CatalogPageModel catalogPageModel) {
        this.catalogPageModel = catalogPageModel;
    }

    @GetMapping("/")
    public String index(@RequestParam(value = "catalogId", required = false) Integer catalogId, Model model) {
        catalogPageModel.fill(model, catalogId);
        catalogPageModel.addSlogans(model);
        return "index";
    }
}