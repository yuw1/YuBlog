package cn.net.wangshifu.util;

import cn.net.wangshifu.model.Catalog;

import java.util.*;


/**
 * 注意对subCatalogs的操作
 */
public class CatalogUtil {

    public static Catalog catalog2Tree(List<Catalog> catalogs) {
        Collections.sort(catalogs);
        Iterator<Catalog> iterator = catalogs.iterator();
        Catalog root = iterator.next();
        root.setSubCatalogs(new ArrayList<Catalog>());
        Map<Integer, Catalog> mapper = new HashMap<Integer, Catalog>();
        mapper.put(root.getId(), root);
        while (iterator.hasNext()) {
            Catalog temp = iterator.next();
            mapper.put(temp.getId(), temp);
            Catalog parent = mapper.get(temp.getParentId());
            if (parent.getSubCatalogs() == null) {
                parent.setSubCatalogs(new ArrayList<Catalog>());
            }
            parent.getSubCatalogs().add(temp);
        }
        return root;
    }

    public static List<Catalog> getRouteFromRoot(List<Catalog> catalogs, int catalogId) {
        Collections.sort(catalogs);
        Iterator<Catalog> iterator = catalogs.iterator();
        Catalog root = iterator.next();
        Map<Integer, Catalog> mapper = new HashMap<Integer, Catalog>();
        mapper.put(root.getId(), root);
        while (iterator.hasNext()) {
            Catalog temp = iterator.next();
            mapper.put(temp.getId(), temp);
            Catalog parent = mapper.get(temp.getParentId());
        }

        List<Catalog> route = new ArrayList<Catalog>();
        while (mapper.containsKey(catalogId)) {
            Catalog temp = mapper.get(catalogId);
            route.add(temp);
            if (catalogId == temp.getParentId()) {
                break;
            }
            catalogId = temp.getParentId();
        }
        Collections.sort(route);
        return route;
    }

    public static List<Integer> findSubCatalogs(List<Catalog> catalogs, int catalogId) {
        Collections.sort(catalogs);
        Iterator<Catalog> iterator = catalogs.iterator();
        Catalog root = iterator.next();
        Map<Integer, Catalog> mapper = new HashMap<Integer, Catalog>();
        mapper.put(root.getId(), root);
        while (iterator.hasNext()) {
            Catalog temp = iterator.next();
            mapper.put(temp.getId(), temp);
        }
        List<Integer> catalogIds = new ArrayList<Integer>();
        List<Catalog> subCatalogFind = new ArrayList<Catalog>();
        catalogIds.add(catalogId);
        subCatalogFind.add(mapper.get(catalogId));
        while (subCatalogFind.size() != 0) {
            Catalog temp = subCatalogFind.get(0);
            subCatalogFind.remove(0);
            if (temp.getSubCatalogs() != null) {
                for (Catalog subTemp : temp.getSubCatalogs()) {
                    catalogIds.add(subTemp.getId());
                    subCatalogFind.add(subTemp);
                }
            }
        }
        return catalogIds;
    }
}
