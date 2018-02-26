package cn.net.wangshifu.model;

import java.util.List;

public class Catalog implements Comparable {
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
    public String toString() {
        return "Catalog{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", parentId=" + parentId +
                ", subCatalogs=" + subCatalogs +
                '}';
    }

    public int compareTo(Object o) {
        Catalog that = (Catalog) o;
        return this.id - that.id;
    }
}
