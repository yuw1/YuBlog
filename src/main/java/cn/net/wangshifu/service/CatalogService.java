package cn.net.wangshifu.service;

import cn.net.wangshifu.dao.ArticleDao;
import cn.net.wangshifu.dao.CatalogDao;
import cn.net.wangshifu.exception.NotFoundException;
import cn.net.wangshifu.exception.ValidationException;
import cn.net.wangshifu.model.Catalog;
import cn.net.wangshifu.util.CatalogUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CatalogService {

    private final CatalogDao catalogDao;
    private final ArticleDao articleDao;

    public CatalogService(CatalogDao catalogDao, ArticleDao articleDao) {
        this.catalogDao = catalogDao;
        this.articleDao = articleDao;
    }

    public List<Catalog> all() {
        return catalogDao.selectAll();
    }

    /** Whole tree, rooted at the first top level catalog. */
    public Catalog tree() {
        return CatalogUtil.toTree(all());
    }

    public Catalog getRequired(int id) {
        Catalog catalog = catalogDao.selectById(id);
        if (catalog == null) {
            throw new NotFoundException("分类不存在");
        }
        return catalog;
    }

    /** Id of the root catalog (smallest id), or 0 when no catalog exists yet. */
    public int rootCatalogId() {
        Catalog root = tree();
        return root.getId();
    }

    public List<Catalog> route(int catalogId) {
        return CatalogUtil.route(all(), catalogId);
    }

    public List<Integer> subtreeIds(int catalogId) {
        return CatalogUtil.subtreeIds(tree(), catalogId);
    }

    @Transactional
    public void add(int parentId, String name) {
        String clean = normalizeName(name);
        int parent = parentId;
        if (parent <= CatalogUtil.NO_CATALOG && catalogDao.selectAll().size() > 0) {
            parent = rootCatalogId();
        }
        if (parent > CatalogUtil.NO_CATALOG) {
            getRequired(parent);
        }
        catalogDao.insertCatalog(clean, parent);
    }

    /** Renaming a catalog also refreshes the denormalised name on its articles. */
    @Transactional
    public void rename(int id, String name) {
        String clean = normalizeName(name);
        getRequired(id);
        catalogDao.renameCatalog(id, clean);
        articleDao.reassignCatalog(id, id, clean);
    }

    @Transactional
    public void delete(int id) {
        Catalog catalog = getRequired(id);
        if (catalogDao.countChildren(id) > 0) {
            throw new ValidationException("请先删除子分类");
        }
        if (articleDao.countByCatalogId(id) > 0) {
            Catalog parent = catalogDao.selectById(catalog.getParentId());
            if (parent == null) {
                throw new ValidationException("请先移走该分类下的文章");
            }
            articleDao.reassignCatalog(id, parent.getId(), parent.getName());
        }
        catalogDao.deleteCatalog(id);
    }

    private static String normalizeName(String name) {
        String clean = name == null ? "" : name.trim();
        if (clean.isEmpty() || clean.length() > 50) {
            throw new ValidationException("分类名需为 1-50 个字符");
        }
        return clean;
    }
}