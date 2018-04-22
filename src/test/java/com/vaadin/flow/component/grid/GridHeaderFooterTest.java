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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.base.Predicate;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.dom.Element;

public class GridHeaderFooterTest {

    private static final Predicate<Element> isColumn = element -> "vaadin-grid-column"
            .equals(element.getTag());
    private static final Predicate<Element> isColumnGroup = element -> "vaadin-grid-column-group"
            .equals(element.getTag());

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
    public void initGrid_noHeaderFooterRows() {
        Assert.assertEquals("Grid should not have header rows initially", 0,
                grid.getHeaderRows().size());
        Assert.assertEquals("Grid should not have footer rows initially", 0,
                grid.getFooterRows().size());
    }

    @Test
    public void initGrid_noColumnGroups() {
        List<List<Element>> layers = getColumnLayers();
        Assert.assertTrue("Grid should not have column groups initially",
                layers.size() == 1);
    }

    @Test
    public void initGrid_noHeaderFooterTemplates() {
        List<List<Element>> layers = getColumnLayers();
        Assert.assertTrue("Grid should one layer of columns",
                layers.size() == 1);
        Assert.assertTrue(
                "Grid columns should not have header or "
                        + "footer templates initially",
                layers.get(0).stream().noneMatch(
                        element -> getHeaderTemplate(element).isPresent()
                                || getFooterTemplate(element).isPresent()));
    }

    @Test
    public void prependHeaderRow_headerLayerAdded() {
        grid.prependHeaderRow();
        List<List<Element>> layers = getColumnLayers();
        Assert.assertTrue("Grid should one layer of columns",
                layers.size() == 1);
        Assert.assertTrue("Columns should have headers but no footers",
                isHeaderRow(layers.get(0)) && !isFooterRow(layers.get(0)));
    }

    @Test
    public void appendHeaderRow_headerLayerAdded() {
        grid.appendHeaderRow();
        List<List<Element>> layers = getColumnLayers();
        Assert.assertTrue("Grid should one layer of columns",
                layers.size() == 1);
        Assert.assertTrue("Columns should have headers but no footers",
                isHeaderRow(layers.get(0)) && !isFooterRow(layers.get(0)));
    }

    private List<List<Element>> getColumnLayers() {
        List<List<Element>> layers = new ArrayList<List<Element>>();
        List<Element> children = grid.getElement().getChildren()
                .collect(Collectors.toList());
        while (children.stream().anyMatch(isColumnGroup)) {
            if (!children.stream().allMatch(isColumnGroup)) {
                throw new IllegalStateException(
                        "All column-children on the same hierarchy level "
                                + "should be either vaadin-grid-columns or "
                                + "vaadin-grid-column-groups");
            }
            layers.add(children);
            children = children.stream()
                    .flatMap(element -> element.getChildren())
                    .collect(Collectors.toList());
        }
        if (children.stream().anyMatch(isColumn)) {
            if (!children.stream().allMatch(isColumn)) {
                throw new IllegalStateException(
                        "All column-children on the same hierarchy level "
                                + "should be either vaadin-grid-columns or "
                                + "vaadin-grid-column-groups");
            }
            layers.add(children);
        } else if (layers.size() > 0) {
            throw new IllegalStateException(
                    "If there are vaadin-grid-column-groups, there should "
                            + "also be vaadin-grid-columns inside them");
        }
        return layers;
    }

    private boolean isHeaderRow(List<Element> layer) {
        return layer.stream()
                .allMatch(element -> getHeaderTemplate(element).isPresent());
    }

    private boolean isFooterRow(List<Element> layer) {
        return layer.stream()
                .allMatch(element -> getFooterTemplate(element).isPresent());
    }

    private Optional<Element> getHeaderTemplate(Element element) {
        return getTemplate(element, "header");
    }

    private Optional<Element> getFooterTemplate(Element element) {
        return getTemplate(element, "footer");
    }

    private Optional<Element> getTemplate(Element element, String className) {
        return element.getChildren()
                .filter(child -> child.getTag() == "template"
                        && child.getClassList().contains(className))
                .findFirst();
    }
}
