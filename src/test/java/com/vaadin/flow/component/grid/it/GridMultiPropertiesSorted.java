package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.demo.DemoView;
import com.vaadin.flow.router.Route;

import java.util.Collections;

@Route("grid-multiproperty-memory-sort")
public class GridMultiPropertiesSorted extends DemoView {

    private Grid<FullNamePerson> grid;

    public static class FullNamePerson {
        private final String firstName;
        private final String lastName;
        private final Object nonSortabeStuff;

        public FullNamePerson(String firstName, String lastName, Object nonSortabeStuff) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.nonSortabeStuff = nonSortabeStuff;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getFullName() {
            return firstName + " " + lastName;
        }

        public Object getNonSortabeStuff() {
            return nonSortabeStuff;
        }
    }

    @Override
    protected void initView() {
        grid = new Grid<>(FullNamePerson.class);
        grid.setItems(
                new FullNamePerson("Ali", "Baba", null),
                new FullNamePerson("John", "Smith", new Object()),
                new FullNamePerson("Richard", "Smith", null),
                new FullNamePerson("Catherine", "Zeta-Jones", new Object()),
                new FullNamePerson("Basil", "Pupkin", null)
        );

        //Last name is second part, but has highest sort priority, also non-sortable values included
        grid.getColumnByKey("fullName")
                .setSortProperty("lastName", "nonSortabeStuff", "firstName");

        Button ascSortBtn = new Button("Full Name Asc", e -> setSort(SortDirection.ASCENDING));
        ascSortBtn.setId("ascSortBtn");
        Button descSortBtn = new Button("Full Name Asc", e -> setSort(SortDirection.DESCENDING));
        descSortBtn.setId("descSortBtn");
        add(grid, ascSortBtn, descSortBtn);

    }

    private void setSort(SortDirection direction) {
        Grid.Column<FullNamePerson> fullName = grid.getColumnByKey("fullName");
        grid.sort(Collections.singletonList(new GridSortOrder<>(fullName, direction)));
    }
}
