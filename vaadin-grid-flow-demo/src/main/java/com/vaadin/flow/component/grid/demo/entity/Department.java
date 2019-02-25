package com.vaadin.flow.component.grid.demo.entity;

public class Department {

    private int id;
    private String name;
    private String description;
    private Department parent;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Department getParent() {
        return parent;
    }

    public void setParent(Department parent) {
        this.parent = parent;
    }


    @Override
    public String toString() {
        return name;
    }

    public Department(int id, String name, Department department, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.parent = department;
    }
}
