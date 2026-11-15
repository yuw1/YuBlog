package cn.net.wangshifu.model;

import java.io.Serializable;
import java.util.List;

/**
 * A node of the catalog tree. {@code parentId == 0} means "top level".
 */
public class Catalog implements Comparable<Catalog>, Serializable {
    private int id;
    private String name;
    private int parentId;
    private List<Catalog> subCatalogs;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public List<Catalog> getSubCatalogs() {
        return subCatalogs;
    }

    public void setSubCatalogs(List<Catalog> subCatalogs) {
        this.subCatalogs = subCatalogs;
    }

    @Override
    public int compareTo(Catalog that) {
        return Integer.compare(this.id, that.id);
    }

    @Override
    public String toString() {
        return "Catalog{id=" + id + ", name='" + name + "', parentId=" + parentId + '}';
    }
}