package com.vaadin.flow.component.treegrid.demo.entity;

public class Webpage {
    private String name;
    private String url;
    private Webpage parent;

    public Webpage(String name, String url, Webpage parent) {
        this.name = name;
        this.url = url;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Webpage getParent() {
        return parent;
    }

    public void setParent(Webpage parent) {
        this.parent = parent;
    }

}
