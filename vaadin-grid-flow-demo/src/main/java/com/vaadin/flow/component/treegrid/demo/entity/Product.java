package com.vaadin.flow.component.treegrid.demo.entity;

import java.time.LocalDateTime;

public class Product {
    private Integer id;
    private String name;
    private Double price;
    private Product parent;
    private LocalDateTime purchaseDate;
    private String code;
    private String country;

    public Product(int id, String name, Double price, Product parent,
            LocalDateTime purchaseDate, String code, String country) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.parent = parent;
        this.purchaseDate = purchaseDate;
        this.code = code;
        this.country = country;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Product getParent() {
        return parent;
    }

    public void setParent(Product parent) {
        this.parent = parent;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return name;
    }
}
