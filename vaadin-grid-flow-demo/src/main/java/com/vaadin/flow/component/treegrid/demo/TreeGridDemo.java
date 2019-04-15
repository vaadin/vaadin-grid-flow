package com.vaadin.flow.component.treegrid.demo;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.demo.GridDemo.Person;
import com.vaadin.flow.component.grid.demo.PeopleGenerator;
import com.vaadin.flow.component.treegrid.demo.data.DepartmentData;
import com.vaadin.flow.component.treegrid.demo.entity.Account;
import com.vaadin.flow.component.treegrid.demo.entity.Department;
import com.vaadin.flow.component.treegrid.demo.service.AccountService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Route("vaadin-tree-grid")
@HtmlImport("grid-demo-styles.html")
public class TreeGridDemo extends DemoView {

    /**
     * Example object.
     */
    public static class PersonWithLevel extends Person {

        private int level;

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }
    }

    public static final List<PersonWithLevel> rootItems = createRootItems();

    @Override
    protected void initView() {
        createBasicTreeGridUsage();
        createLazyLoadingTreeGridUsage();
    }

    private void createBasicTreeGridUsage() {
        DepartmentData departmentData = new DepartmentData();
        Map<PersonWithLevel, List<PersonWithLevel>> childMap = new HashMap<>();
        TextArea message = new TextArea("");
        message.setHeight("100px");
        message.setReadOnly(true);

        // begin-source-example
        // source-example-heading: TreeGrid Basics
        TreeGrid<Department> grid = new TreeGrid<>();

        grid.setItems(departmentData.getRootDepartments(), department ->
                departmentData.getChildDepartments(department));
        grid.addHierarchyColumn(Department::getName).setHeader("Hierarchy");
        grid.addColumn(Department::getDescription).setHeader("Description");

        grid.addExpandListener(event -> message.setValue(
                String.format("Expanded %s item(s)", event.getItems().size())
                        + "\n" + message.getValue()));
        grid.addCollapseListener(event -> message.setValue(
                String.format("Collapsed %s item(s)", event.getItems().size())
                        + "\n" + message.getValue()));

        // end-source-example
        grid.setId("treegridbasic");


        addCard("TreeGrid Basics", withTreeGridToggleButtons(departmentData.getRootDepartments().get(0), grid, message));
    }

    private <T> Component[] withTreeGridToggleButtons(T root,
                                                      TreeGrid<T> grid, Component... other) {
        NativeButton toggleFirstItem = new NativeButton("Toggle first item",
                evt -> {
                    if (grid.isExpanded(root)) {
                        grid.collapseRecursively(Collections.singleton(root), 0);
                    } else {
                        grid.expandRecursively(Collections.singleton(root), 0);
                    }
                });
        toggleFirstItem.setId("treegrid-toggle-first-item");
        Div div1 = new Div(toggleFirstItem);

        NativeButton toggleRecursivelyFirstItem = new NativeButton(
                "Toggle first item recursively", evt -> {
            if (grid.isExpanded(root)) {
                grid.collapseRecursively(Collections.singleton(root), 2);
            } else {
                grid.expandRecursively(Collections.singleton(root), 2);
            }
        });
        toggleFirstItem.setId("treegrid-toggle-first-item-recur");
        Div div3 = new Div(toggleRecursivelyFirstItem);

        return Stream.concat(Stream.of(grid, div1, div3),
                Stream.of(other)).toArray(Component[]::new);
    }

    // TreeGrid with lazy loading
    private void createLazyLoadingTreeGridUsage() {
        AccountService accountService = new AccountService();
        TextArea message = new TextArea("");
        message.setHeight("100px");
        message.setReadOnly(true);

        // begin-source-example
        // source-example-heading: TreeGrid with lazy loading
        TreeGrid<Account> grid = new TreeGrid<>();
        grid.addHierarchyColumn(Account::toString).setHeader("Hierarchy");
        grid.addColumn(Account::getCode).setHeader("Code");

        HierarchicalDataProvider dataProvider =
                new AbstractBackEndHierarchicalDataProvider<Account, Void>() {

                    @Override
                    public int getChildCount(HierarchicalQuery<Account, Void> query) {
                        return (int) accountService.getChildCount(query.getParent());
                    }

                    @Override
                    public boolean hasChildren(Account item) {
                        return accountService.hasChildren(item);
                    }

                    @Override
                    protected Stream<Account> fetchChildrenFromBackEnd(
                            HierarchicalQuery<Account, Void> query) {
                        return accountService.fetchChildren(query.getParent()).stream();
                    }
                };

        grid.setDataProvider(dataProvider);

        // end-source-example
        grid.setId("treegridlazy");

        addCard("TreeGrid", "TreeGrid with lazy loading", grid);
    }

    private static List<PersonWithLevel> getRootItems() {
        return rootItems;
    }

    private static List<PersonWithLevel> createRootItems() {
        return createSubItems(500, 0);
    }

    private static List<PersonWithLevel> createSubItems(int number, int level) {
        return new PeopleGenerator().generatePeopleWithLevels(number, level);
    }
}
