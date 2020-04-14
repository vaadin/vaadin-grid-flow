package com.vaadin.flow.component.treegrid.demo;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.grid.demo.GridDemo.Person;
import com.vaadin.flow.component.grid.demo.PersonData;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.component.treegrid.demo.data.DepartmentData;
import com.vaadin.flow.component.treegrid.demo.data.ProductData;
import com.vaadin.flow.component.treegrid.demo.entity.Account;
import com.vaadin.flow.component.treegrid.demo.entity.Department;
import com.vaadin.flow.component.treegrid.demo.entity.Product;
import com.vaadin.flow.component.treegrid.demo.service.AccountService;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.AbstractHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.renderer.NumberRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Route("vaadin-tree-grid")
@JsModule("@vaadin/flow-frontend/grid-demo-styles.js")
@HtmlImport("grid-demo-styles.html")
public class TreeGridDemo extends DemoView {
    /**
     * Shared between all read-only demos
     */
    private static final DepartmentData departmentData = new DepartmentData();

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
        createSingleSelect();
        createMultiSelect();
        createProgrammaticSelect();
        createGridWithSortableColumns();
        createGridWithTextFieldFilters();
        createHeaderAndFooter();
        createGridUsingComponent();
        createGridUsingComponentFilters();
        createFormattingText();
        createHtmlTemplateRenderer();
        createGridWithItemDetails();
        createItemDetailsOpenedProgrammatically();
        createContextMenu();
        createDynamicContextMenu();
        createClickListener();
        createDoubleClickListener();
        createBufferedEditor();
        createNotBufferedEditor();
        createDropLocations();
        createDragDropFilters();
    }

    private void createBasicTreeGridUsage() {
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

    private void createSingleSelect() {
        Div messageDiv = new Div();
        // begin-source-example
        // source-example-heading: TreeGrid single selection
        TreeGrid<Department> treeGrid = new TreeGrid<>();
        treeGrid.setItems(departmentData.getRootDepartments(),
                departmentData::getChildDepartments);

        treeGrid.addHierarchyColumn(Department::getName)
                .setHeader("Department Name");
        treeGrid.addColumn(Department::getManager).setHeader("Manager");

        treeGrid.asSingleSelect().addValueChangeListener(event -> {
            String message = String.format("Selection changed from %s to %s",
                    event.getOldValue(), event.getValue());
            messageDiv.setText(message);
        });
        // end-source-example
        addCard("Selection", "TreeGrid single selection", treeGrid, messageDiv);
    }

    private void createMultiSelect() {
        Div messageDiv = new Div();
        // begin-source-example
        // source-example-heading: TreeGrid multi selection
        TreeGrid<Department> treeGrid = new TreeGrid<>();
        treeGrid.setItems(departmentData.getRootDepartments(),
                departmentData::getChildDepartments);

        treeGrid.addHierarchyColumn(Department::getName)
                .setHeader("Department Name");
        treeGrid.addColumn(Department::getManager).setHeader("Manager");

        // set multi selection in TreeGrid
        treeGrid.setSelectionMode(SelectionMode.MULTI);
        treeGrid.asMultiSelect().addValueChangeListener(event -> {
            String message = String.format("Selection changed from %s to %s",
                    event.getOldValue(), event.getValue());
            messageDiv.setText(message);
        });

        // pre-select items
        treeGrid.asMultiSelect().select(departmentData.getDepartments().get(0),
                departmentData.getDepartments().get(7));
        // end-source-example
        addCard("Selection", "TreeGrid multi selection", treeGrid, messageDiv);
    }

    private void createProgrammaticSelect() {
        // begin-source-example
        // source-example-heading: TreeGrid with programmatic selection
        H3 firstHeader = new H3("TreeGrid with single select");
        TreeGrid<Department> firstTreeGrid = new TreeGrid<>();
        firstTreeGrid.setItems(departmentData.getRootDepartments(),
                departmentData::getChildDepartments);

        H3 secondHeader = new H3("TreeGrid with multi select");
        TreeGrid<Department> secondTreeGrid = new TreeGrid<>();
        secondTreeGrid.setItems(departmentData.getRootDepartments(),
                departmentData::getChildDepartments);
        secondTreeGrid.setSelectionMode(SelectionMode.MULTI);

        TextField filterField = new TextField();
        filterField.setValueChangeMode(ValueChangeMode.EAGER);
        filterField.addValueChangeListener(event -> {
            Optional<Department> foundDepartment = departmentData
                    .getDepartments().stream()
                    .filter(department -> department.getName().toLowerCase()
                            .startsWith(event.getValue().toLowerCase()))
                    .findFirst();

            firstTreeGrid.asSingleSelect()
                    .setValue(foundDepartment.orElse(null));

            secondTreeGrid.getSelectionModel().deselectAll();
            Set<Department> foundDepartments = departmentData.getDepartments()
                    .stream()
                    .filter(department -> department.getName().toLowerCase()
                            .startsWith(event.getValue().toLowerCase()))
                    .collect(Collectors.toSet());
            secondTreeGrid.asMultiSelect().setValue(foundDepartments);
        });

        firstTreeGrid.addHierarchyColumn(Department::getName).setHeader("Name");
        firstTreeGrid.addColumn(Department::getManager).setHeader("Manager");

        secondTreeGrid.addColumn(Department::getName).setHeader("Name");
        secondTreeGrid.addColumn(Department::getManager).setHeader("Manager");

        NativeButton deselectBtn = new NativeButton("Deselect all");
        deselectBtn.addClickListener(
                event -> secondTreeGrid.asMultiSelect().deselectAll());
        NativeButton selectAllBtn = new NativeButton("Select all");
        selectAllBtn.addClickListener(
                event -> ((GridMultiSelectionModel<Department>) secondTreeGrid
                        .getSelectionModel()).selectAll());
        // end-source-example
        addCard("Selection", "TreeGrid with programmatic selection",
                filterField, firstHeader, firstTreeGrid, secondHeader,
                secondTreeGrid, selectAllBtn, deselectBtn);
    }

    private void createGridWithSortableColumns() {
        Div messageDiv = new Div();
        ProductData productData = new ProductData();
        // begin-source-example
        // source-example-heading: TreeGrid with sortable columns
        TreeGrid<Product> treeGrid = new TreeGrid<>();
        treeGrid.setItems(productData.getRootItems(), productData::getChildren);
        treeGrid.setSelectionMode(SelectionMode.NONE);

        treeGrid.addHierarchyColumn(Product::getName).setHeader("Product Name");

        // addColumn is not Comparable so it uses toString method to sort the
        // column.
        treeGrid.addColumn(TemplateRenderer.<Product> of(
                "<div>[[item.country]]<br><small>[[item.code]]</small></div>")

                .withProperty("country", product -> product.getCountry())
                .withProperty("code", product -> product.getCode()), "country",
                "code").setHeader("Country of origin");

        Checkbox multiSort = new Checkbox("Multiple column sorting enabled");
        multiSort.addValueChangeListener(
                event -> treeGrid.setMultiSort(event.getValue()));

        // you can set the sort order from server-side with the grid.sort method
        NativeButton invertAllSortings = new NativeButton(
                "Invert all sort directions", event -> {
                    List<GridSortOrder<Product>> newList = treeGrid
                            .getSortOrder().stream()
                            .map(order -> new GridSortOrder<>(order.getSorted(),
                                    order.getDirection().getOpposite()))
                            .collect(Collectors.toList());
                    treeGrid.sort(newList);
                });

        NativeButton resetAllSortings = new NativeButton("Reset all sortings",
                event -> treeGrid.sort(null));
        // end-source-example
        addCard("Sorting", "TreeGrid with sortable columns", treeGrid,
                multiSort, messageDiv, invertAllSortings, resetAllSortings);
    }

    private void createGridWithTextFieldFilters() {
        // begin-source-example
        // source-example-heading: Using text fields for filtering root items
        TreeGrid<Department> treeGrid = new TreeGrid<>();
        treeGrid.setItems(departmentData.getRootDepartments(),
                departmentData::getChildDepartments);
        treeGrid.setSelectionMode(SelectionMode.NONE);

        Grid.Column<Department> departmentNameColumn = treeGrid
                .addHierarchyColumn(Department::getName)
                .setHeader("Department Name");
        treeGrid.addColumn(Department::getManager).setHeader("Manager");

        HeaderRow filterRow = treeGrid.appendHeaderRow();
        // filtering
        TextField departmentNameField = new TextField();
        departmentNameField.addValueChangeListener(event -> {
            treeGrid.setItems(
                    departmentData.addFilter(departmentNameField.getValue()),
                    departmentData::getChildDepartments);
            treeGrid.getDataProvider().refreshAll();
        });

        departmentNameField.setValueChangeMode(ValueChangeMode.EAGER);

        filterRow.getCell(departmentNameColumn)
                .setComponent(departmentNameField);
        departmentNameField.setSizeFull();
        departmentNameField.setPlaceholder("Filter by department root name");
        // end-source-example
        addCard("Filtering", "Using text fields for filtering root items",
                treeGrid);
    }

    private void createHeaderAndFooter() {
        // begin-source-example
        // source-example-heading: Header and footer texts
        TreeGrid<Department> treeGrid = new TreeGrid<>();
        treeGrid.setItems(departmentData.getRootDepartments(),
                departmentData::getChildDepartments);
        treeGrid.addHierarchyColumn(Department::getName)
                .setHeader("Department Name")
                .setFooter("Total: " + departmentData.getDepartments().size()
                        + " department");
        treeGrid.addColumn(Department::getManager).setHeader("Manager");
        // end-source-example
        addCard("Header and footer", "Header and footer texts", treeGrid);
    }

    private TreeGrid<Department> createGridUsingComponent() {
        // begin-source-example
        // source-example-heading: Using components
        TreeGrid<Department> treeGrid = new TreeGrid<>();
        treeGrid.setItems(departmentData.getRootDepartments(),
                departmentData::getChildDepartments);
        treeGrid.setSelectionMode(SelectionMode.NONE);

        treeGrid.addHierarchyColumn(Department::getName)
                .setHeader("Department Name");
        treeGrid.addColumn(Department::getManager).setHeader("Manager");

        // Or you can use an ordinary function to setup the component
        treeGrid.addComponentColumn(
                item -> createRemoveButton(treeGrid, departmentData, item))
                .setHeader("Actions");

        treeGrid.setSelectionMode(SelectionMode.NONE);
        return treeGrid;
    }

    private Button createRemoveButton(TreeGrid<Department> treeGrid,
            DepartmentData departmentData, Department department) {

        Button button = new Button("Remove", clickEvent -> {
            departmentData.removeDepartment(department);
            treeGrid.setItems(departmentData.getRootDepartments(),
                    departmentData::getChildDepartments);

        });
        return button;
    }

    // end-source-example
    private void createGridUsingComponentFilters() {
        TreeGrid<Department> treeGrid = createGridUsingComponent();
        addCard("Components", "Using components", treeGrid);
    }

    private void createFormattingText() {
        ProductData productData = new ProductData();
        // begin-source-example
        // source-example-heading: Formatting text
        String str = "2016-03-04 11:30:40";
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(str, formatter);

        LocalDate localDate = LocalDate.parse(str, formatter);

        TreeGrid<Product> treeGrid = new TreeGrid<>();
        treeGrid.setItems(productData.getRootItems(), productData::getChildren);
        treeGrid.setSelectionMode(SelectionMode.NONE);

        treeGrid.addHierarchyColumn(Product::getName).setHeader("Name")
                .setWidth("40px");
        // NumberRenderer to render numbers in general
        treeGrid.addColumn(new NumberRenderer<>(Product::getPrice, "$ %(,.2f",
                Locale.US, "$ 0.00")).setHeader("Price");

        // LocalDateTimeRenderer for date and time
        treeGrid.addColumn(new LocalDateTimeRenderer<>(Product::getPurchaseDate,
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT,
                        FormatStyle.MEDIUM)))
                .setHeader("Purchase Date and Time").setFlexGrow(2);
        // end-source-example
        addCard("Formatting contents", "Formatting text", treeGrid);
    }

    private void createHtmlTemplateRenderer() {
        ProductData productData = new ProductData();
        // begin-source-example
        // source-example-heading: TreeGrid with HTML template renderer
        TreeGrid<Product> treeGrid = new TreeGrid<>();
        treeGrid.setItems(productData.getRootItems(), productData::getChildren);
        treeGrid.addHierarchyColumn(Product::getName)
                .setHeader("Product category").setFlexGrow(2);
        NumberFormat moneyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        String str = "2016-03-04 11:30:40";
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss");

        // You can also set complex objects directly. Internal properties of the
        // bean are accessible in the template.

        treeGrid.addColumn(TemplateRenderer.<Product> of(
                "<div style='visibility:[[item.visibility]]'>Name: [[item.name]],Price: [[item.price]] <br> purchased on: <small>[[item.purchasedate]]</small></div>")
                .withProperty("name", Product::getName)
                // NumberRenderer to render numbers in general
                .withProperty("price",
                        product -> product.getPrice() == null ? null
                                : moneyFormat.format(product.getPrice()))
                .withProperty("visibility",
                        product -> product.getParent() == null ? "hidden" : "")
                .withProperty("purchasedate",
                        product -> formatter.format(product.getPurchaseDate())))
                .setFlexGrow(6);

        treeGrid.addColumn(TemplateRenderer.<Product> of(
                "<div style='visibility:[[item.visibility]]'>Country of origin: <small>[[item.country]]<small> <br>, with code: [[item.code]]</small> </div>")
                .withProperty("visibility",
                        product -> product.getParent() == null ? "hidden" : "")
                .withProperty("code", Product::getCode)
                .withProperty("country", Product::getCountry)).setFlexGrow(6);
        // end-source-example
        addCard("Formatting Contents", "TreeGrid with HTML template renderer",
                treeGrid);
    }

    private void createGridWithItemDetails() {
        ProductData productData = new ProductData();
        // begin-source-example
        // source-example-heading: TreeGrid with item details
        H3 header = new H3("Clicking on a row will show more details");
        TreeGrid<Product> treeGrid = new TreeGrid<>();
        treeGrid.setItems(productData.getRootItems(), productData::getChildren);

        treeGrid.addHierarchyColumn(Product::getName).setHeader("Name");

        treeGrid.addColumn(new LocalDateTimeRenderer<>(Product::getPurchaseDate,
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT,
                        FormatStyle.MEDIUM)))
                .setHeader("Purchase Date and Time").setFlexGrow(2);
        treeGrid.setSelectionMode(SelectionMode.NONE);

        // You can use any renderer for the item details. By default, the
        // details are opened and closed by clicking the rows.
        treeGrid.setItemDetailsRenderer(TemplateRenderer.<Product> of(
                "<div style='border: 1px solid gray; padding: 10px; width: 100%; box-sizing: border-box;'>"
                        + "<div>Product name is: <b>[[item.name]]</b></div>"
                        + "<div>Product price is: <b>[[item.price]]</b></div>"
                        + "<div>Country of origin: <b>[[item.country]]</b></div>"
                        + "</div>")
                .withProperty("name", Product::getName)
                .withProperty("price", Product::getPrice)
                .withProperty("country", Product::getCountry)
                .withEventHandler("handleClick", product -> {
                    treeGrid.getDataProvider().refreshItem(product);
                }));

        // end-source-example
        addCard("Product details", "TreeGrid with item details", header,
                treeGrid);
    }

    private void createItemDetailsOpenedProgrammatically() {
        ProductData productData = new ProductData();
        // begin-source-example
        // source-example-heading: Open details programmatically
        // Disable the default way of opening item details:
        H3 header = new H3("Clicking on buttons will show more details");
        TreeGrid<Product> treeGrid = new TreeGrid<>();
        treeGrid.setItems(productData.getRootItems(), productData::getChildren);

        treeGrid.addHierarchyColumn(Product::getName).setHeader("Name");

        treeGrid.addColumn(new LocalDateTimeRenderer<>(Product::getPurchaseDate,
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT,
                        FormatStyle.MEDIUM)))
                .setHeader("Purchase Date and Time").setFlexGrow(2);
        treeGrid.setSelectionMode(SelectionMode.NONE);

        // You can use any renderer for the item details. By default, the
        // details are opened and closed by clicking the rows.
        treeGrid.setItemDetailsRenderer(TemplateRenderer.<Product> of(
                "<div class='custom-details' style ='border: 1px solid gray; padding: 10px; width: 100%; box-sizing: border-box; visibility:[[item.visibility]]'>"
                        + "<div>Product name is: <b>[[item.name]]</b></div>"
                        + "<div>Product price is: <b>[[item.price]]</b></div>"
                        + "<div>Country of origin: <b>[[item.country]]</b></div>"
                        + "</div>")
                .withProperty("name", Product::getName)
                .withProperty("price", Product::getPrice)
                .withProperty("country", Product::getCountry)
                .withProperty("visibility",
                        product -> product.getParent() == null ? "hidden" : "")
                // This is now how we open the details
                .withEventHandler("handleClick", product -> {
                    treeGrid.getDataProvider().refreshItem(product);
                }));

        // Disable the default way of opening item details:
        treeGrid.setDetailsVisibleOnClick(false);

        treeGrid.addComponentColumn(
                product -> product.getParent() == null ? new Span()
                        : new NativeButton("Details",
                                event -> treeGrid.setDetailsVisible(product,
                                        !treeGrid.isDetailsVisible(product))));

        // end-source-example
        addCard("Product details", "Open details programmatically", header,
                treeGrid);
    }

    private void createContextMenu() {
        // begin-source-example
        // source-example-heading: Using ContextMenu with TreeGrid
        TreeGrid<Department> treeGrid = new TreeGrid<>();

        AbstractHierarchicalDataProvider<Department, Object> dataProvider = createDataProvider(
                departmentData);
        treeGrid.setDataProvider(dataProvider);
        treeGrid.addHierarchyColumn(Department::getName)
                .setHeader("Department Name");
        treeGrid.addColumn(Department::getManager).setHeader("Manager");

        GridContextMenu<Department> contextMenu = new GridContextMenu<>(
                treeGrid);
        GridMenuItem<Department> insert = contextMenu.addItem("Insert");

        insert.getSubMenu().addItem("Add a department before", event -> {
            Optional<Department> department = event.getItem();
            if (!department.isPresent()) {
                return;
            }

            departmentData.insertNewBefore(department.get());
            dataProvider.refreshAll();
        });
        insert.getSubMenu().add(new Hr());
        insert.getSubMenu().addItem("Add a Department after", event -> {
            Optional<Department> department = event.getItem();
            if (!department.isPresent()) {
                // no selected row
                return;
            }
            departmentData.insertNewAfter(department.get());
            dataProvider.refreshAll();
        });

        contextMenu.addItem("Remove", event -> {
            event.getItem().ifPresent(department -> {
                departmentData.removeDepartment(department);
                dataProvider.refreshAll();
            });
        });

        // end-source-example
        addCard("Context Menu", "Using ContextMenu with TreeGrid", treeGrid,
                contextMenu);
    }

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

    private void createDynamicContextMenu() {
        // begin-source-example
        // source-example-heading: Dynamic context menu
        TreeGrid<Department> treeGrid = new TreeGrid<>();
        AbstractHierarchicalDataProvider<Department, Object> dataProvider = createDataProvider(
                departmentData);
        treeGrid.setDataProvider(dataProvider);
        treeGrid.addHierarchyColumn(Department::getName)
                .setHeader("Department Name");
        treeGrid.addColumn(Department::getManager).setHeader("Manager");
        GridContextMenu<Department> contextMenu = new GridContextMenu<>(
                treeGrid);
        contextMenu.setDynamicContentHandler(department -> {
            if (department == null) {
                // do not show the context menu when a row is not clicked
                return false;
            }
            contextMenu.removeAll();
            contextMenu.addItem("Name: " + department.getName());
            contextMenu.addItem("Manager: " + department.getManager());
            return true; // show the context menu
        });
        // end-source-example
        addCard("Context Menu", "Dynamic context menu", treeGrid, contextMenu);
    }

    private void createClickListener() {
        FormLayout formLayout = new FormLayout();
        Label name = new Label();
        Label manager = new Label();
        Label column = new Label();

        // begin-source-example
        // source-example-heading: Item click listener
        TreeGrid<Department> treeGrid = new TreeGrid<>();
        treeGrid.setItems(departmentData.getRootDepartments(),
                departmentData::getChildDepartments);
        treeGrid.setSelectionMode(SelectionMode.NONE);

        treeGrid.addHierarchyColumn(Department::getName)
                .setHeader("Department Name");
        treeGrid.addColumn(Department::getManager).setHeader("Manager");

        // Disable selection: will receive only click events instead
        treeGrid.setSelectionMode(SelectionMode.NONE);

        formLayout.add(name, manager);
        formLayout.addFormItem(name, "Name");
        formLayout.addFormItem(manager, "Manager");

        treeGrid.addItemClickListener(event -> {
            name.setText(event.getItem().getName());
            manager.setText(event.getItem().getManager());
            column.setText(event.getColumn().getKey());
        });

        // end-source-example
        addCard("Click Listeners", "Item click listener", treeGrid, formLayout);
    }

    private void createDoubleClickListener() {
        Div message = new Div();
        FormLayout formLayout = new FormLayout();
        Label name = new Label();
        Label manager = new Label();

        // begin-source-example
        // source-example-heading: Item double click listener
        TreeGrid<Department> treeGrid = new TreeGrid<>();
        treeGrid.setItems(departmentData.getRootDepartments(),
                departmentData::getChildDepartments);
        treeGrid.setSelectionMode(SelectionMode.NONE);

        treeGrid.addHierarchyColumn(Department::getName)
                .setHeader("Department Name");
        treeGrid.addColumn(Department::getManager).setHeader("Manager");

        formLayout.add(name, manager);
        formLayout.addFormItem(name, "Name");
        formLayout.addFormItem(manager, "Manager");

        treeGrid.addItemDoubleClickListener(event -> {
            name.setText(event.getItem().getName());
            manager.setText(event.getItem().getManager());
        });

        // end-source-example
        message.addClickListener(event -> message.setText(""));
        addCard("Click Listeners", "Item double click listener", treeGrid,
                formLayout);
    }

    private void createBufferedEditor() {
        ProductData productData = new ProductData();
        Div message = new Div();
        // begin-source-example
        // source-example-heading: Editor in buffered mode
        TreeGrid<Product> treeGrid = new TreeGrid<>();
        treeGrid.setItems(productData.getRootItems(), productData::getChildren);
        treeGrid.setSelectionMode(SelectionMode.NONE);

        Grid.Column<Product> nameColumn = treeGrid
                .addHierarchyColumn(Product::getName).setHeader("Product name");
        Grid.Column<Product> priceColumn = treeGrid.addColumn(Product::getPrice)
                .setHeader("Price");

        Binder<Product> binder = new Binder<>(Product.class);
        Editor<Product> editor = treeGrid.getEditor();
        editor.setBinder(binder);
        editor.setBuffered(true);

        Div validationStatus = new Div();

        TextField nameField = new TextField();
        binder.forField(nameField)
                .withValidator(new StringLengthValidator(
                        "Name length must be between 3 and 50.", 3, 50))
                .withStatusLabel(validationStatus).bind("name");
        nameColumn.setEditorComponent(nameField);

        TextField priceField = new TextField();
        binder.forField(priceField)
                .withConverter(
                        new StringToDoubleConverter("Price must be a number."))
                .withStatusLabel(validationStatus).bind("price");
        priceColumn.setEditorComponent(priceField);

        Collection<Button> editButtons = Collections
                .newSetFromMap(new WeakHashMap<>());

        Grid.Column<Product> editorColumn = treeGrid
                .addComponentColumn(product -> {
                    Button edit = new Button("Edit");
                    edit.setVisible(product.getParent() != null);
                    edit.addClassName("edit");
                    edit.addClickListener(e -> {
                        editor.editItem(product);
                        nameField.focus();
                    });
                    edit.setEnabled(!editor.isOpen());
                    editButtons.add(edit);
                    return edit;
                });

        editor.addOpenListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));
        editor.addCloseListener(e -> editButtons.stream()
                .forEach(button -> button.setEnabled(!editor.isOpen())));

        Button save = new Button("Save", e -> editor.save());
        save.addClassName("save");

        Button cancel = new Button("Cancel", e -> editor.cancel());
        cancel.addClassName("cancel");

        // Add a keypress listener that listens for an escape key up event.
        // Note! some browsers return key as Escape and some as Esc
        treeGrid.getElement()
                .addEventListener("keyup", event -> editor.cancel())
                .setFilter("event.key === 'Escape' || event.key === 'Esc'");

        Div buttons = new Div(save, cancel);
        editorColumn.setEditorComponent(buttons);

        editor.addSaveListener(event -> message.setText(
                event.getItem().getName() + ", " + event.getItem().getPrice()));

        // end-source-example
        addCard("Grid editor", "Editor in buffered mode", message,
                validationStatus, treeGrid);
    }

    private void createNotBufferedEditor() {
        ProductData productData = new ProductData();
        Div message = new Div();
        message.setId("not-buffered-editor-msg");

        // begin-source-example
        // source-example-heading: Editor in not buffered mode
        TreeGrid<Product> treeGrid = new TreeGrid<>();
        treeGrid.setItems(productData.getRootItems(), productData::getChildren);
        treeGrid.setSelectionMode(SelectionMode.NONE);

        Grid.Column<Product> nameColumn = treeGrid
                .addHierarchyColumn(Product::getName).setHeader("Product name");
        Grid.Column<Product> priceColumn = treeGrid.addColumn(Product::getPrice)
                .setHeader("Price");

        Binder<Product> binder = new Binder<>(Product.class);
        treeGrid.getEditor().setBinder(binder);

        TextField nameField = new TextField();
        TextField priceField = new TextField();
        // Close the editor in case of backward between components
        nameField.getElement()
                .addEventListener("keydown",
                        event -> treeGrid.getEditor().cancel())
                .setFilter("event.key === 'Tab' && event.shiftKey");

        binder.forField(nameField)
                .withValidator(new StringLengthValidator(
                        "Name length must be between 3 and 50.", 3, 50))
                .bind("name");
        nameColumn.setEditorComponent(nameField);

        priceField.getElement()
                .addEventListener("keydown",
                        event -> treeGrid.getEditor().cancel())
                .setFilter("event.key === 'Tab'");
        binder.forField(priceField)
                .withConverter(
                        new StringToDoubleConverter("Price must be a number."))
                .bind("price");
        priceColumn.setEditorComponent(priceField);

        treeGrid.addItemDoubleClickListener(event -> {
            treeGrid.getEditor().editItem(event.getItem());
            nameField.focus();
        });

        treeGrid.getEditor().addCloseListener(event -> {
            if (binder.getBean() != null) {
                message.setText(binder.getBean().getName() + ", "
                        + binder.getBean().getPrice());
            }
        });

        // end-source-example
        addCard("Grid Editor", "Editor in not buffered mode", message,
                treeGrid);
    }

    public class PersonService {
        private PersonData personData = new PersonData();

        public List<Person> fetch(int offset, int limit) {
            return personData.getPersons().subList(offset, offset + limit);
        }

        public int count() {
            return personData.getPersons().size();
        }

        public List<Person> fetchAll() {
            return personData.getPersons();
        }
    }

    private Person draggedItem;

    private void createDropLocations() {
        PersonService personService = new PersonService();
        // begin-source-example
        // source-example-heading: Drop location

        TreeGrid<Person> treeGrid = new TreeGrid<>();
        Grid<Person> grid = new Grid<>(Person.class);
        grid.setSelectionMode(SelectionMode.NONE);
        grid.setItems(personService.fetch(0, 50));

        grid.addDragStartListener(event -> {
            draggedItem = event.getDraggedItems().get(0);
            /*
             * This enables dropping in between or on top of the existing grid
             * rows.
             */
            treeGrid.setDropMode(GridDropMode.ON_TOP_OR_BETWEEN);
        });

        grid.addDragEndListener(event -> {
            draggedItem = null;
            treeGrid.setDropMode(null);
        });
        grid.setColumns("firstName", "lastName", "phoneNumber");
        grid.setRowsDraggable(true);

        TreeData<Person> td = new TreeData<>();
        td.addItems(null, personService.fetch(51, 2));

        treeGrid.setDataProvider(new TreeDataProvider<Person>(td));
        treeGrid.addHierarchyColumn(Person::getFirstName)
                .setHeader("firstName");
        treeGrid.addColumn(Person::getLastName).setHeader("lastName");
        treeGrid.addColumn(Person::getPhoneNumber).setHeader("phoneNumber");
        treeGrid.setSelectionMode(SelectionMode.NONE);
        treeGrid.addDropListener(event -> {
            // Remove the items from the source grid
            @SuppressWarnings("unchecked")
            ListDataProvider<Person> sourceDataProvider = (ListDataProvider<Person>) grid
                    .getDataProvider();
            Collection<Person> sourceItems = sourceDataProvider.getItems();
            sourceItems.remove(draggedItem);
            grid.setItems(sourceItems);

            // Add the item to target grid
            Person dropOverItem = event.getDropTargetItem().get();
            if (GridDropLocation.ON_TOP == event.getDropLocation()) {
                td.addItem(dropOverItem, draggedItem);
            } else {
                Person parent = td.getParent(dropOverItem);
                td.addItem(parent, draggedItem);
                List<Person> siblings = td.getChildren(parent);
                int dropIndex = siblings.indexOf(dropOverItem)
                        + (event.getDropLocation() == GridDropLocation.BELOW ? 1
                                : 0);
                td.moveAfterSibling(draggedItem,
                        dropIndex > 0 ? siblings.get(dropIndex - 1) : null);
            }
            treeGrid.getDataProvider().refreshAll();
        });

        // end-source-example

        HorizontalLayout hl = new HorizontalLayout(grid, treeGrid);

        addCard("Drag and Drop", "Drop location", hl);
    }

    private void createDragDropFilters() {
        // begin-source-example
        // source-example-heading: Drag and drop filters
        PersonService personService = new PersonService();

        TreeGrid<Person> grid = new TreeGrid<>();
        TreeData<Person> td = new TreeData<>();

        // Disallow dragging supervisors
        grid.setDragFilter(person -> td.getParent(person) != null);

        grid.setDropFilter(person ->
        // Only support dropping on top of supervisors
        td.getRootItems().contains(person)
                // Don't allow more than 4 subordinates
                && td.getChildren(person).size() < 4
                // Disallow dropping on own supervisor
                && !td.getChildren(person).contains(draggedItem));

        grid.addHierarchyColumn(Person::getFirstName).setHeader("First name");
        grid.addColumn(Person::getLastName).setHeader("Last name");
        grid.addColumn(Person::getPhoneNumber).setHeader("Phone");
        grid.setRowsDraggable(true);
        grid.setSelectionMode(Grid.SelectionMode.NONE);

        td.addItems(null, personService.fetch(0, 3));
        td.addItems(td.getRootItems().get(0), personService.fetch(3, 3));
        td.addItems(td.getRootItems().get(1), personService.fetch(6, 2));

        grid.setDataProvider(new TreeDataProvider<Person>(td));
        grid.expand(td.getRootItems());

        grid.addDragStartListener(event -> {
            draggedItem = event.getDraggedItems().get(0);
            grid.setDropMode(GridDropMode.ON_TOP);

            // Refresh all related items to get the drop filter run for them
            //
            // For flat grids, dataProvider.refreshAll() does the job well but
            // for a TreeGrid with nodes expanded, it's more efficient to
            // refresh the items individually
            td.getRootItems().forEach(supervisor -> {
                grid.getDataProvider().refreshItem(supervisor);

                td.getChildren(supervisor).forEach(subordinate -> grid
                        .getDataProvider().refreshItem(subordinate));
            });

        });

        grid.addDragEndListener(event -> {
            draggedItem = null;
            grid.setDropMode(null);
        });

        grid.addDropListener(event -> {
            event.getDropTargetItem().ifPresent(supervisor -> {
                // Remove the item from it's previous supervisor's subordinates
                // list
                td.removeItem(draggedItem);

                // Close empty parents
                td.getRootItems().forEach(root -> {
                    if (td.getChildren(root).isEmpty()) {
                        grid.collapse(root);
                    }
                });

                // Add the item to the target supervisor's subordinates list
                td.addItem(supervisor, draggedItem);

                grid.getDataProvider().refreshAll();
            });
        });

        // end-source-example
        VerticalLayout vl = new VerticalLayout(new Label("Supervisors"), grid);

        addCard("Drag and Drop", "Drag and drop filters", vl);
    }
}
