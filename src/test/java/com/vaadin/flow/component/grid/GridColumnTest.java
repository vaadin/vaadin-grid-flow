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
package com.vaadin.flow.component.grid;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.component.grid.Grid.Column;

public class GridColumnTest {

    Grid<String> grid;
    Column<String> firstColumn;
    Column<String> secondColumn;
    Column<String> thirdColumn;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void init() {
        grid = new Grid<>();
        firstColumn = grid.addColumn(str -> str);
        secondColumn = grid.addColumn(str -> str);
        thirdColumn = grid.addColumn(str -> str);
    }

    @Test
    public void setKey_getByKey() {
        firstColumn.setKey("foo");
        secondColumn.setKey("bar");
        Assert.assertEquals(firstColumn, grid.getColumnByKey("foo"));
        Assert.assertEquals(secondColumn, grid.getColumnByKey("bar"));
    }

    @Test(expected = IllegalStateException.class)
    public void changeKey_throws() {
        firstColumn.setKey("foo");
        firstColumn.setKey("bar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateKey_throws() {
        firstColumn.setKey("foo");
        secondColumn.setKey("foo");
    }

    @Test
    public void removeColumnByKey() {
        firstColumn.setKey("first");
        grid.removeColumnByKey("first");
        Assert.assertNull(grid.getColumnByKey("first"));
    }

    @Test
    public void removeColumnByNullKey_throws() {
        expectNullPointerException("columnKey should not be null");
        grid.removeColumnByKey(null);
    }

    @Test
    public void removeColumn() {
        firstColumn.setKey("first");
        grid.removeColumn(firstColumn);
        Assert.assertNull(grid.getColumnByKey("first"));
    }

    @Test
    public void removeNullColumn_throws() {
        expectNullPointerException("column should not be null");
        grid.removeColumn(null);
    }

    @Test
    public void removeInvalidColumnByKey_throws() {
        expectIllegalArgumentException(
                "The column with key 'wrong' is not part of this Grid");

        grid.removeColumnByKey("wrong");
    }

    @Test
    public void removeColumnByKeyTwice_throws() {
        expectIllegalArgumentException(
                "The column with key 'first' is not part of this Grid");

        firstColumn.setKey("first");
        grid.removeColumnByKey("first");
        grid.removeColumnByKey("first");
    }

    @Test
    public void removeInvalidColumn_throws() {
        expectIllegalArgumentException(
                "The column with key 'wrong' is not part of this Grid");

        Grid<String> grid2 = new Grid<>();
        Column<String> wrongColumn = grid2.addColumn(str -> str);
        wrongColumn.setKey("wrong");
        grid.removeColumn(wrongColumn);
    }

    @Test
    public void removeColumnTwice_throws() {
        expectIllegalArgumentException(
                "The column with key 'first' is not part of this Grid");

        firstColumn.setKey("first");
        grid.removeColumn(firstColumn);
        grid.removeColumn(firstColumn);
    }

    private void expectNullPointerException(String message) {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage(message);
    }

    private void expectIllegalArgumentException(String message) {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(message);
    }
}
