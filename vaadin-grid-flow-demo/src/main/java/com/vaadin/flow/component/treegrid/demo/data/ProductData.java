package com.vaadin.flow.component.treegrid.demo.data;

import com.vaadin.flow.component.treegrid.demo.entity.Product;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProductData {
    private static final List<Product> PRODUCT_LIST = createList();

    private static List<Product> createList() {
        List<Product> productList = new ArrayList<>();

        String str = "2016-03-04 11:30:40";
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(str, formatter);
        productList.add(new Product(1, "Living room", null, null, dateTime,
                null, null));
        productList.add(new Product(11, "Television", 30000d,
                productList.get(0), dateTime, "1001", "Korea"));
        productList.add(new Product(12, "Sofa", 15000d, productList.get(0),
                dateTime, "1002", "Finland"));
        productList.add(new Product(13, "Chair", 100d, productList.get(0),
                dateTime, "1003", "Finland"));
        productList.add(
                new Product(2, "Kitchen", null, null, dateTime, null, null));
        productList.add(new Product(21, "Refrigerator", 2000d,
                productList.get(4), dateTime, "2001", "USA"));
        productList.add(new Product(22, "Oven", 1000d, productList.get(4),
                dateTime, "2002", "Germany"));
        productList.add(new Product(23, "microwave", 200d, productList.get(4),
                dateTime, "2003", "Germany"));
        productList.add(new Product(24, "Kettle", 2000d, productList.get(4),
                dateTime, "2004", "France"));
        productList.add(new Product(25, "Toaster", 40d, productList.get(4),
                dateTime, "2005", "France"));
        productList.add(
                new Product(3, "Bedroom", null, null, dateTime, null, null));
        productList.add(new Product(31, "Bed", 1000d, productList.get(10),
                dateTime, "3001", "Finland"));
        productList.add(new Product(32, "lamp", 50d, productList.get(10),
                dateTime, "3002", "China"));
        productList.add(new Product(33, "Night stand", 50d, productList.get(10),
                dateTime, "3003", "Finland"));
        productList.add(new Product(34, "carpet", 100d, productList.get(10),
                dateTime, "3004", "Iran"));
        productList.add(
                new Product(4, "Bathroom", null, null, dateTime, null, null));
        productList.add(new Product(41, "Towel", 15d, productList.get(15),
                dateTime, "4001", "China"));
        productList.add(new Product(42, "Paper toilet", 10d,
                productList.get(15), dateTime, "4002", "China"));
        productList.add(new Product(43, "Shampoo", 27d, productList.get(15),
                dateTime, "4003", "Finland"));
        productList.add(new Product(44, "Soap", 7d, productList.get(15),
                dateTime, "4004", "Thailand"));
        return productList;
    }

    public List<Product> getAll() {
        return PRODUCT_LIST;
    }

    public List<Product> getRootItems() {
        return PRODUCT_LIST.stream()
                .filter(department -> department.getParent() == null)
                .collect(Collectors.toList());
    }

    public List<Product> getChildren(Product parent) {
        return PRODUCT_LIST.stream()
                .filter(product -> Objects.equals(product.getParent(), parent))
                .collect(Collectors.toList());
    }
}
