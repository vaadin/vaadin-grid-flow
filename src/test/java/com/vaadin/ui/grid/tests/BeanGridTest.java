/*
 * Copyright 2000-2017 Vaadin Ltd.
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
package com.vaadin.ui.grid.tests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.grid.Grid;
import com.vaadin.ui.grid.Grid.Column;

public class BeanGridTest {

    Grid<Person> grid;

    @Before
    public void init() {
        grid = new Grid<>(Person.class);
    }

    @Test
    public void beanGrid_columnsForPropertiesAddedWithCorrectKeys() {
        String[] expectedKeys = new String[] { "born", "name", "friend" };
        Object[] keys = grid.getColumns().stream().map(Column::getKey)
                .toArray();

        Assert.assertArrayEquals("Unexpected columns or column-keys",
                expectedKeys, keys);
    }

    @Test
    public void addColumnForSubProperty_columnAddedWithCorrectKey() {
        addColumnAndTestKey("friend.name");
        addColumnAndTestKey("friend.friend.friend");
        addColumnAndTestKey("friend.friend.friend.born");
    }

    private void addColumnAndTestKey(String property) {
        grid.addColumn(property);
        Assert.assertNotNull("Column for sub-property not found by key",
                grid.getColumnByKey(property));
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateColumnsForSameProperty_throws() {
        grid.addColumn("name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateColumnsForSameSubProperty_throws() {
        grid.addColumn("friend.name");
        grid.addColumn("friend.name");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addColumnForNonExistingProperty_throws() {
        grid.addColumn("foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addColumnForNonExistingSubProperty_throws() {
        grid.addColumn("friend.foobar");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void addPropertyColumnForGridWithoutPropertySet_throws() {
        Grid<Person> nonBeanGrid = new Grid<Person>();
        nonBeanGrid.addColumn("friend.name");
    }

}
