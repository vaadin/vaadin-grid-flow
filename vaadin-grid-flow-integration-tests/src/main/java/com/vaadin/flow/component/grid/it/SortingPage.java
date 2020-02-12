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
    private TestGui gui1;

    public SortingPage() {
        Grid<Person> grid = new Grid<>();
        grid.setId("sorting-grid");
        grid.setItems(new Person("B", 20), new Person("A", 30));
        Column<Person> nameColumn = grid.addColumn(Person::getFirstName)
                .setHeader("Name");
        Column<Person> ageColumn = grid.addColumn(Person::getAge)
                .setHeader("Age");
        add(grid);

        List<GridSortOrder<Person>> sortByName = new GridSortOrderBuilder<Person>()
                .thenAsc(nameColumn).build();
        grid.sort(sortByName);

        NativeButton button = new NativeButton("Sort by age", e -> {
            List<GridSortOrder<Person>> sortByAge = new GridSortOrderBuilder<Person>()
                    .thenAsc(ageColumn).build();
            grid.sort(sortByAge);
        });
        button.setId("sort-by-age");
        add(button);

        setSizeFull();
        gui1 = new TestGui();
        Button btnReset = new Button("reset", evt -> gui1.getGrid().sort(null));
        btnReset.setId("btn-reset");
        Button btRm = new Button("deattach", evt -> remove(gui1));
        btRm.setId("btn-deattach");
        Button btattach = new Button("attach",
                evt -> add(gui1));
        btattach.setId("btn-attach");
        add(btRm, btattach,btnReset);
    }

    public class TestGui extends FlexLayout {
        Grid<TestData> grid = new Grid<>(TestData.class);
        public TestGui() {
            setSizeFull();
            grid.setSizeFull();
            grid.setItems(createTestData());
            grid.setId("second-grid");
            // removes generated columns
            grid.removeAllColumns();
            Grid.Column<TestData> string = grid.addColumn(TestData::getStest).setSortable(true).setHeader("String");
            Grid.Column<TestData> index = grid.addColumn(TestData::getSint).setSortable(true).setHeader("index");
            grid.addColumn(TestData::getRandom).setSortable(true).setHeader("random");
            grid.setMultiSort(true);
            grid.setSelectionMode(Grid.SelectionMode.MULTI);
            List<GridSortOrder<TestData>> sortByNameSecondGrid = new GridSortOrderBuilder<TestData>()
                    .thenAsc(string).build();
            grid.sort(sortByNameSecondGrid);
            List<GridSortOrder<TestData>> sortByNameSecondGrid2 = new GridSortOrderBuilder<TestData>()
                    .thenAsc(index).build();
            grid.sort(sortByNameSecondGrid2);

            add(grid);
        }
        public Grid<TestData> getGrid() {
            return this.grid;
        }
        private List<TestData> createTestData() {
            List<TestData> data = new ArrayList<>();
            for(int i=0; i<9; i++) {
                data.add(new TestData("test" + i, i, 3 * 100));
            }
            return data;
        }
    }

    public class TestData {
        private String stest;
        private int sint;
        private int random;
        public TestData(String stest, int sint, int random) {
            super();
            this.stest = stest;
            this.sint = sint;
            this.random = random;
        }
        public String getStest() {
            return stest;
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

