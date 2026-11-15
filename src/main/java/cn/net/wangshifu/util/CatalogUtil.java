package cn.net.wangshifu.util;

import cn.net.wangshifu.model.Catalog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalog tree helpers.
 *
 * <p>The data model keeps one top level catalog (the one with the smallest id) acting as the root
 * of the blog, exactly like the original table did. Unlike the 2018 version nothing here throws
 * when a parent id is missing or when the table is empty.
 */
public final class CatalogUtil {

    public static final int NO_CATALOG = 0;

    private CatalogUtil() {
    }

    /**
     * Builds the tree. Nodes whose parent does not exist are attached to the root, and an empty
     * table yields an empty in-memory root instead of a {@code NoSuchElementException}.
     */
    public static Catalog toTree(List<Catalog> flat) {
        Catalog root = new Catalog();
        root.setId(NO_CATALOG);
        root.setName("root");
        root.setParentId(NO_CATALOG);
        root.setSubCatalogs(new ArrayList<>());
        if (flat == null || flat.isEmpty()) {
            return root;
        }

        List<Catalog> sorted = new ArrayList<>(flat);
        sorted.sort(Comparator.comparingInt(Catalog::getId));

        Map<Integer, Catalog> byId = new HashMap<>();
        for (Catalog c : sorted) {
            c.setSubCatalogs(new ArrayList<>());
            byId.put(c.getId(), c);
        }

        Catalog realRoot = sorted.get(0);
        for (Catalog c : sorted) {
            if (c == realRoot) {
                continue;
            }
            Catalog parent = byId.get(c.getParentId());
            if (parent == null || parent == c) {
                parent = realRoot;
            }
            parent.getSubCatalogs().add(c);
        }
        return realRoot;
    }

    /** Path from the root down to {@code catalogId}; empty when the id is unknown. */
    public static List<Catalog> route(List<Catalog> flat, int catalogId) {
        Map<Integer, Catalog> byId = new HashMap<>();
        if (flat != null) {
            for (Catalog c : flat) {
                byId.put(c.getId(), c);
            }
        }
        List<Catalog> route = new ArrayList<>();
        int cursor = catalogId;
        int guard = 0;
        while (byId.containsKey(cursor) && guard++ < 100) {
            Catalog node = byId.get(cursor);
            route.add(0, node);
            if (node.getParentId() == cursor || !byId.containsKey(node.getParentId())) {
                break;
            }
            cursor = node.getParentId();
        }
        return route;
    }

    /** {@code catalogId} plus every descendant id. Unknown ids yield a single element list. */
    public static List<Integer> subtreeIds(Catalog treeRoot, int catalogId) {
        List<Integer> ids = new ArrayList<>();
        ids.add(catalogId);
        Catalog start = find(treeRoot, catalogId);
        if (start == null) {
            return ids;
        }
        List<Catalog> queue = new ArrayList<>();
        queue.add(start);
        while (!queue.isEmpty()) {
            Catalog node = queue.remove(0);
            if (node.getSubCatalogs() == null) {
                continue;
            }
            for (Catalog child : node.getSubCatalogs()) {
                ids.add(child.getId());
                queue.add(child);
            }
        }
        return ids;
    }

    private static Catalog find(Catalog node, int id) {
        if (node == null) {
            return null;
        }
        if (node.getId() == id) {
            return node;
        }
        if (node.getSubCatalogs() == null) {
            return null;
        }
        for (Catalog child : node.getSubCatalogs()) {
            Catalog found = find(child, id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}