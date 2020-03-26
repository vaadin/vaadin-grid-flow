package com.vaadin.flow.component.treegrid.demo;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.demo.GridDemo.Person;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.component.treegrid.demo.data.DepartmentData;
import com.vaadin.flow.component.treegrid.demo.entity.Account;
import com.vaadin.flow.component.treegrid.demo.entity.Department;
import com.vaadin.flow.component.treegrid.demo.service.AccountService;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.AbstractHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

import java.util.Collections;
import java.util.stream.Stream;

@Route("vaadin-tree-grid")
@JsModule("@vaadin/flow-frontend/grid-demo-styles.js")
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

    @Override
    protected void initView() {
        createBasicTreeGridUsage();
        createLazyLoadingTreeGridUsage();
        createDragAndDrop();
    }

    private void createBasicTreeGridUsage() {
        DepartmentData departmentData = new DepartmentData();
        TextArea message = new TextArea("");
        message.setHeight("100px");
        message.setReadOnly(true);

        // begin-source-example
        // source-example-heading: TreeGrid Basics
        TreeGrid<Department> grid = new TreeGrid<>();

        grid.setItems(departmentData.getRootDepartments(),
                departmentData::getChildDepartments);
        grid.addHierarchyColumn(Department::getName)
                .setHeader("Department Name");
        grid.addColumn(Department::getManager).setHeader("Manager");

        grid.addExpandListener(event -> message.setValue(
                String.format("Expanded %s item(s)", event.getItems().size())
                        + "\n" + message.getValue()));
        grid.addCollapseListener(event -> message.setValue(
                String.format("Collapsed %s item(s)", event.getItems().size())
                        + "\n" + message.getValue()));

        // end-source-example
        grid.setId("treegridbasic");

        addCard("TreeGrid Basics", withTreeGridToggleButtons(
                departmentData.getRootDepartments().get(0), grid, message));
    }

    private <T> Component[] withTreeGridToggleButtons(T root, TreeGrid<T> grid,
            Component... other) {
        NativeButton toggleFirstItem = new NativeButton("Toggle first item",
                evt -> {
                    if (grid.isExpanded(root)) {
                        grid.collapseRecursively(Collections.singleton(root),
                                0);
                    } else {
                        grid.expandRecursively(Collections.singleton(root), 0);
                    }
                });
        toggleFirstItem.setId("treegrid-toggle-first-item");
        Div div1 = new Div(toggleFirstItem);

        NativeButton toggleRecursivelyFirstItem = new NativeButton(
                "Toggle first item recursively", evt -> {
                    if (grid.isExpanded(root)) {
                        grid.collapseRecursively(Collections.singleton(root),
                                2);
                    } else {
                        grid.expandRecursively(Collections.singleton(root), 2);
                    }
                });
        toggleFirstItem.setId("treegrid-toggle-first-item-recur");
        Div div3 = new Div(toggleRecursivelyFirstItem);

        return Stream.concat(Stream.of(grid, div1, div3), Stream.of(other))
                .toArray(Component[]::new);
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
        grid.addHierarchyColumn(Account::toString).setHeader("Account Title");
        grid.addColumn(Account::getCode).setHeader("Code");

        HierarchicalDataProvider dataProvider = new AbstractBackEndHierarchicalDataProvider<Account, Void>() {

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

        addCard("TreeGrid with lazy loading", grid);
    }

    private void createDragAndDrop() {
        DepartmentData departmentData = new DepartmentData();
        TreeGrid<Department> treeGrid = createDnDTreeGrid(departmentData);

        addCard("Drag and drop", treeGrid);
    }

    // begin-source-example
    // source-example-heading: Drag and drop

    private Department draggedItem;

    private TreeGrid<Department> createDnDTreeGrid(
            DepartmentData departmentData) {
        TreeGrid<Department> treeGrid = new TreeGrid<>();

        treeGrid.setDataProvider(createDataProvider(departmentData));

        treeGrid.addHierarchyColumn(Department::getName)
                .setHeader("Department Name");
        treeGrid.addColumn(Department::getManager).setHeader("Manager");
        treeGrid.setRowsDraggable(true);
        treeGrid.setDropMode(GridDropMode.ON_TOP);

        // To keep the dragged item.
        treeGrid.addDragStartListener(
                event -> draggedItem = event.getDraggedItems().get(0));

        // To clear the dragged item when the dragged drop operation ends.
        treeGrid.addDragEndListener(event -> draggedItem = null);

        // To change the parent of the dragged item to item where it is dropped
        // on
        treeGrid.addDropListener(event -> {
            Department targetDepartment = event.getDropTargetItem().get();
            while (targetDepartment != null) {
                if (draggedItem.equals(targetDepartment)) {
                    Notification.show(
                            "You can't drag an item to itself or to its children!");
                    return;
                }
                targetDepartment = targetDepartment.getParent();
            }

            Department oldParent = draggedItem.getParent();
            draggedItem.setParent(event.getDropTargetItem().get());
            treeGrid.getDataProvider()
                    .refreshItem(event.getDropTargetItem().get(), true);

            if (oldParent == null) {
                treeGrid.getDataProvider().refreshAll();
            } else {
                treeGrid.getDataProvider().refreshItem(oldParent, true);
            }
        });
        return treeGrid;
    }
    // end-source-example

    private AbstractHierarchicalDataProvider<Department, Object> createDataProvider(
            DepartmentData departmentData) {
        return new AbstractHierarchicalDataProvider<Department, Object>() {
            @Override
            public int getChildCount(
                    HierarchicalQuery<Department, Object> query) {
                return departmentData.getChildCount(query.getParent());
            }

            @Override
            public Stream<Department> fetchChildren(
                    HierarchicalQuery<Department, Object> query) {
                return departmentData.getChildDepartments(query.getParent())
                        .stream();
            }

            @Override
            public boolean hasChildren(Department item) {
                return departmentData.hasChildren(item);
            }

            @Override
            public boolean isInMemory() {
                return false;
            }
        };
    }
}
