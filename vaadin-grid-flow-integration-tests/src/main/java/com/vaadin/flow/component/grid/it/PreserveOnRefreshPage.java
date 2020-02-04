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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.GridSortOrderBuilder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.bean.Person;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route("preserve-on-refresh")
@PreserveOnRefresh
public class PreserveOnRefreshPage extends Div {

    public PreserveOnRefreshPage() {
        Grid<Person> grid = new Grid<>();
        grid.setItems(new Person("foo", 20));
        grid.addComponentColumn(person -> new Span(person.getFirstName()))
                .setHeader(new Span("header")).setFooter(new Span("footer"));
        add(grid);
        TEST_GUI();
    }

    private List<TEST_DATA> createTestData() {
        List<TEST_DATA> data = new ArrayList<>();

        for(int i = 0; i < 99; i++) {
            data.add(new TEST_DATA("test" + i, i, (int)(Math.random() * 20)));
        }

        return data;
    }



    public void TEST_GUI() {
        Grid<TEST_DATA> grid = new Grid<>(TEST_DATA.class);
        setSizeFull();

        grid.setSizeFull();

        grid.setId("second-grid");
        grid.setItems(createTestData());
        // removes generated columns
        grid.removeAllColumns();

        Grid.Column<TEST_DATA> stringColumn = grid.addColumn(TEST_DATA::getStest).setSortable(true).setHeader("String");
        stringColumn.setId("string-column");
        Grid.Column<TEST_DATA> indexColumn = grid.addColumn(TEST_DATA::getSint).setSortable(true).setHeader("index");
        indexColumn.setId("index-column");
        Grid.Column<TEST_DATA> randomColumn = grid.addColumn(TEST_DATA::getRandom).setSortable(true).setHeader("random");
        randomColumn.setId("random-column");

        grid.setMultiSort(true);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        List<GridSortOrder<TEST_DATA>> sortByString = new GridSortOrderBuilder<TEST_DATA>()
                .thenAsc(stringColumn).build();
        NativeButton buttonString = new NativeButton("Sort by String", e -> {
            grid.sort(sortByString);
        });
        List<GridSortOrder<TEST_DATA>> sortByIndex = new GridSortOrderBuilder<TEST_DATA>()
                .thenAsc(indexColumn).build();
        NativeButton buttonIndex = new NativeButton("Sort by Index", e -> {
            grid.sort(sortByIndex);
        });
        List<GridSortOrder<TEST_DATA>> sortByRandom = new GridSortOrderBuilder<TEST_DATA>()
                .thenAsc(randomColumn).build();
        NativeButton buttonRandom = new NativeButton("Sort by Random", e -> {
            grid.sort(sortByRandom);
        });
        buttonString.setId("btn-sort-by-string");
        buttonIndex.setId("btn-sort-by-index");
        buttonRandom.setId("btn-sort-by-random");

        Button btRm = new Button("Remove", evt -> remove(grid));
        btRm.setId("btn-rm");
        Button btattach = new Button("Attach", evt -> add(grid));
        btattach.setId(("btn-attach"));

        add(btRm, btattach, buttonString, buttonIndex, buttonRandom, grid);
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
