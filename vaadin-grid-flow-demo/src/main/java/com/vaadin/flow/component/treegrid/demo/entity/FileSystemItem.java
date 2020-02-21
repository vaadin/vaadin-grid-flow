package com.vaadin.flow.component.treegrid.demo.entity;

import com.vaadin.flow.component.icon.VaadinIcon;

public class FileSystemItem {
    private VaadinIcon icon;
    private String name;
    private FileSystemItem parent;

    public FileSystemItem(VaadinIcon icon, String name, FileSystemItem parent) {
        this.icon = icon;
        this.name = name;
        this.parent = parent;
    }

    public VaadinIcon getIcon() {
        return icon;
    }

    public void setIcon(VaadinIcon icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FileSystemItem getParent() {
        return parent;
    }

    public void setParent(FileSystemItem parent) {
        this.parent = parent;
    }

}
