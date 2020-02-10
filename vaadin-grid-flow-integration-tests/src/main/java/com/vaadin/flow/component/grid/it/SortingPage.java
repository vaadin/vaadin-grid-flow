/*
 * Copyright 2000-2019 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.grid.it;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@Route("sorting")
@Theme(Lumo.class)
public class SortingPage extends Div {
    private TEST_GUI gui1;

    public SortingPage() {
        Grid<Person> grid = new Grid<>();
        grid.setId("sorting-grid");
        grid.setItems(new Person("B", 20), new Person("A", 30));
        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name");
        Column<Person> ageColumn = grid.addColumn(Person::getAge)
                .setHeader("Age");
        List<GridSortOrder<Person>> sortByName = new GridSortOrderBuilder<Person>()
                .thenAsc(nameColumn).build();
        grid.sort(sortByName);

        NativeButton button = new NativeButton("Sort by age", e -> {
            List<GridSortOrder<Person>> sortByAge = new GridSortOrderBuilder<Person>()
                    .thenAsc(ageColumn).build();
            grid.sort(sortByAge);
        });
        button.setId("sort-by-age");
        add(button, grid);

        // second grid for multi sort
//        Grid<Person> secondGrid = new Grid<>();
//        secondGrid.setId("sorting-grid-second");
//        secondGrid.setMultiSort(true);
//        secondGrid.setItems(new Person("B", 20), new Person("A", 30));
//        Column<Person> nameColumnSecondGrid = secondGrid.addColumn(Person::getFirstName)
//                .setHeader("Name").setSortable(true);
//        Column<Person> ageColumnSecondGrid = secondGrid.addColumn(Person::getAge)
//                .setHeader("Age").setSortable(true);
////
////        List<GridSortOrder<Person>> sortByNameSecondGrid = new GridSortOrderBuilder<Person>()
////                .thenAsc(nameColumnSecondGrid).build();
////        grid.sort(sortByNameSecondGrid);
//        NativeButton btnSortByNameSecondGrid = new NativeButton("Sort by name", e -> {
//            List<GridSortOrder<Person>> sortByNameSecondGridBuild = new GridSortOrderBuilder<Person>()
//                    .thenAsc(nameColumnSecondGrid).build();
//            secondGrid.sort(sortByNameSecondGridBuild);
//        });
//
//        secondGrid.setMultiSort(true);
//        List<GridSortOrder<Person>> sortByAgeSecondGrid = new GridSortOrderBuilder<Person>()
//                .thenAsc(ageColumnSecondGrid).build();
//        grid.sort(sortByAgeSecondGrid);
//        NativeButton btnSortByAgeSecondGrid = new NativeButton("Sort by age", e -> {
//            List<GridSortOrder<Person>> sortByAgeSecondGridBuild = new GridSortOrderBuilder<Person>()
//                    .thenAsc(ageColumnSecondGrid).build();
//            secondGrid.sort(sortByAgeSecondGridBuild);
//        });
//
//        btnSortByAgeSecondGrid.setId("sort-by-age-second-grid");
//        btnSortByNameSecondGrid.setId("sort-by-name-second-grid");
//
//        Button btnDeattachSecondGrid = new Button("De-Attach", evt -> remove(secondGrid));
//        btnDeattachSecondGrid.setId("btn-rm");
//        Button btnAttachSecondGrid = new Button("Attach", evt -> add(secondGrid));
//        btnAttachSecondGrid.setId(("btn-attach"));
//        add(btnSortByAgeSecondGrid, btnSortByNameSecondGrid, btnDeattachSecondGrid, btnAttachSecondGrid, secondGrid);

        setSizeFull();
        gui1 = new TEST_GUI();
        Button btnReset = new Button("rs", evt -> gui1.getGrid().sort(null));
        Button btRm = new Button("rm", evt -> remove(gui1));
        Button btattach = new Button("attach",
                evt -> add(gui1));
        add(btRm, btattach,btnReset);
    }

    private List<TEST_DATA> createTestData() {
        List<TEST_DATA> data = new ArrayList<>();

        for(int i=0; i<9; i++) {
            data.add(new TEST_DATA("test" + i, i, (int)(Math.random() * 100)));
        }

        return data;
    }

    public class TEST_GUI extends FlexLayout {
        Grid<TEST_DATA> grid = new Grid<>(TEST_DATA.class);
        public TEST_GUI() {
            setSizeFull();


            grid.setSizeFull();

            grid.setItems(createTestData());
            // removes generated columns
            grid.removeAllColumns();

            Grid.Column<TEST_DATA> string = grid.addColumn(TEST_DATA::getStest).setSortable(true).setHeader("String");
            Grid.Column<TEST_DATA> index = grid.addColumn(TEST_DATA::getSint).setSortable(true).setHeader("index");
            grid.addColumn(TEST_DATA::getRandom).setSortable(true).setHeader("random");

            grid.setMultiSort(true);
            grid.setSelectionMode(Grid.SelectionMode.MULTI);

            List<GridSortOrder<TEST_DATA>> sortByNameSecondGrid = new GridSortOrderBuilder<TEST_DATA>()
                    .thenAsc(string).build();
            grid.sort(sortByNameSecondGrid);

            List<GridSortOrder<TEST_DATA>> sortByNameSecondGrid2 = new GridSortOrderBuilder<TEST_DATA>()
                    .thenAsc(index).build();
            grid.sort(sortByNameSecondGrid2);

            grid.addSortListener(e -> {
                System.out.println(e.getSortOrder().size());
            });

            add(grid);
        }
        public Grid<TEST_DATA> getGrid() {
            return this.grid;
        }
    }

    public class TEST_DATA {
        public String stest;
        public int sint;
        public int random;

        public TEST_DATA(String stest, int sint, int random) {
            super();
            this.stest = stest;
            this.sint = sint;
            this.random = random;
        }
        public String getStest() {
            return stest;
        }
        public void setStest(String stest) {
            this.stest = stest;
        }
        public int getSint() {
            return sint;
        }
        public void setSint(int sint) {
            this.sint = sint;
        }
        public int getRandom() {
            return random;
        }
        public void setRandom(int random) {
            this.random = random;
        }
    }

}

