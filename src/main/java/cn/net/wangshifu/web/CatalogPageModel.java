package cn.net.wangshifu.web;

import cn.net.wangshifu.service.ArticleService;
import cn.net.wangshifu.service.BlogInfoService;
import cn.net.wangshifu.service.CatalogService;
import cn.net.wangshifu.util.CatalogUtil;
import cn.net.wangshifu.model.Catalog;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.util.List;

/**
 * Fills the model for the two catalog driven pages (home and blog list).
 *
 * <p>{@code IndexController} and {@code BlogController} used to be 80 % identical; the shared part
 * lives here now.
 */
@Component
public class CatalogPageModel {

    private final CatalogService catalogService;
    private final ArticleService articleService;
    private final BlogInfoService blogInfoService;

    public CatalogPageModel(CatalogService catalogService, ArticleService articleService,
                            BlogInfoService blogInfoService) {
        this.catalogService = catalogService;
        this.articleService = articleService;
        this.blogInfoService = blogInfoService;
    }

    /** @return the catalog that was actually selected (falls back to the root catalog) */
    public int fill(Model model, Integer requestedCatalogId) {
        List<Catalog> all = catalogService.all();
        int selected = requestedCatalogId == null ? catalogService.rootCatalogId() : requestedCatalogId;
        if (selected == CatalogUtil.NO_CATALOG) {
            selected = catalogService.rootCatalogId();
        }
        model.addAttribute("catalogId", selected);
        model.addAttribute("catalogRoot", CatalogUtil.toTree(all));
        model.addAttribute("route", CatalogUtil.route(all, selected));
        model.addAttribute("articles", articleService.byCatalog(selected));
        return selected;
    }

    public void addSlogans(Model model) {
        model.addAttribute("blogSlogen", blogInfoService.get("blog.slogen"));
        model.addAttribute("blogMinorSlogen", blogInfoService.get("blog.minor_slogen"));
    }
}