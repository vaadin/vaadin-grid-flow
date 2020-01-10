package com.vaadin.flow.component.treegrid.demo.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.vaadin.flow.component.treegrid.demo.entity.Webpage;

public class WebpageData {

    private static final List<Webpage> WEBPAGE_LIST = createWebpageList();

    private static List<Webpage> createWebpageList() {
        List<Webpage> webpageList = new ArrayList<>();

        webpageList.add(new Webpage("Vaadin", "https://vaadin.com", null));
        webpageList.add(new Webpage("New Project",
                "https://vaadin.com/start/latest", webpageList.get(0)));
        webpageList.add(new Webpage("UI Components",
                "https://vaadin.com/components", webpageList.get(0)));
        webpageList.add(new Webpage("Add-on Directory ",
                "https://vaadin.com/directory", webpageList.get(0)));
        webpageList.add(new Webpage("Spring Boot",
            "https://vaadin.com/start/latest", webpageList.get(1)));
        webpageList.add(new Webpage("CDI and Java EE",
            "https://vaadin.com/start/latest", webpageList.get(1)));
        webpageList.add(new Webpage("Plain Java Servlet",
            "https://vaadin.com/start/latest", webpageList.get(1)));
        webpageList.add(new Webpage("Button",
            "https://vaadin.com/components/vaadin-button", webpageList.get(2)));
        webpageList.add(new Webpage("Combo Box",
            "https://vaadin.com/components/vaadin-combo-box", webpageList.get(2)));
        webpageList.add(new Webpage("Date Picker",
            "https://vaadin.com/components/vaadin-date-picker", webpageList.get(2)));
        webpageList.add(new Webpage("Grid",
            "https://vaadin.com/components/vaadin-grid", webpageList.get(2)));
        webpageList.add(new Webpage("Text Field",
            "https://vaadin.com/components/vaadin-text-field", webpageList.get(2)));
        return webpageList;
    }

    public List<Webpage> getWebpages() {
        return WEBPAGE_LIST;
    }

    public List<Webpage> getRootPages() {
        return WEBPAGE_LIST.stream().filter(page -> page.getParent() == null)
                .collect(Collectors.toList());
    }

    public List<Webpage> getChildPages(Webpage parent) {
        return WEBPAGE_LIST.stream()
                .filter(page -> Objects.equals(page.getParent(), parent))
                .collect(Collectors.toList());
    }
}
