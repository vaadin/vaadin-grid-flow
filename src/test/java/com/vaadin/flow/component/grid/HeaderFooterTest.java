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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.Lists;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.dom.Element;

public class HeaderFooterTest {

    private static final Predicate<Element> isColumn = element -> "vaadin-grid-column"
            .equals(element.getTag());
    private static final Predicate<Element> isColumnGroup = element -> "vaadin-grid-column-group"
            .equals(element.getTag());
    private static final Predicate<Element> isTemplate = element -> "template"
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
        addColumns();
    }

    private void addColumns() {
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
        List<List<Element>> layers = getColumnLayersAndAssertCount(1);
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
        List<List<Element>> layers = getColumnLayersAndAssertCount(1);
        Assert.assertTrue("Columns should have headers but no footers",
                isHeaderRow(layers.get(0)) && !isFooterRow(layers.get(0)));
    }

    @Test
    public void appendHeaderRow_headerLayerAdded() {
        grid.appendHeaderRow();
        List<List<Element>> layers = getColumnLayersAndAssertCount(1);
        Assert.assertTrue("Columns should have headers but no footers",
                isHeaderRow(layers.get(0)) && !isFooterRow(layers.get(0)));
    }

    @Test
    public void addHeaderAndFooterRow_oneLayerWithBothTemplatesAdded() {
        grid.appendHeaderRow();
        grid.appendFooterRow();
        assertOneLayerWithHeaderAndFooter();

        init();
        grid.prependHeaderRow();
        grid.prependFooterRow();
        assertOneLayerWithHeaderAndFooter();
    }

    private void assertOneLayerWithHeaderAndFooter() {
        List<List<Element>> layers = getColumnLayersAndAssertCount(1);
        Assert.assertTrue("Columns should have headers but no footers",
                isHeaderRow(layers.get(0)) && isFooterRow(layers.get(0)));
    }

    @Test
    public void appendHeaderRows_firstOnTop() {
        HeaderRow first = grid.appendHeaderRow();
        HeaderRow second = grid.appendHeaderRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(first, layers.get(1));
        assertRowWrapsLayer(second, layers.get(0));
    }

    @Test
    public void prependHeaderRows_firstOnBottom() {
        HeaderRow first = grid.prependHeaderRow();
        HeaderRow second = grid.prependHeaderRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(first, layers.get(0));
        assertRowWrapsLayer(second, layers.get(1));
    }

    @Test
    public void appendFooterRows_firstOnTop() {
        FooterRow first = grid.appendFooterRow();
        FooterRow second = grid.appendFooterRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(first, layers.get(0));
        assertRowWrapsLayer(second, layers.get(1));
    }

    @Test
    public void prependFooterRows_firstOnBottom() {
        FooterRow first = grid.prependFooterRow();
        FooterRow second = grid.prependFooterRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(first, layers.get(1));
        assertRowWrapsLayer(second, layers.get(0));
    }

    @Test
    public void addHeaderRows_addFooterRows_footersOnLowerLayer() {
        HeaderRow h1 = grid.prependHeaderRow();
        HeaderRow h2 = grid.prependHeaderRow();

        FooterRow f1 = grid.appendFooterRow();
        FooterRow f2 = grid.appendFooterRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(3);

        // bottom layer is used by both headers and footers
        assertRowWrapsLayer(h1, layers.get(0));
        assertRowWrapsLayer(f1, layers.get(0));

        assertRowWrapsLayer(f2, layers.get(1));
        assertRowWrapsLayer(h2, layers.get(2));
    }

    @Test
    public void addFooterRows_addHeaderRows_footersOnLowerLayer() {
        FooterRow f1 = grid.appendFooterRow();
        FooterRow f2 = grid.appendFooterRow();

        HeaderRow h1 = grid.prependHeaderRow();
        HeaderRow h2 = grid.prependHeaderRow();

        List<List<Element>> layers = getColumnLayersAndAssertCount(3);

        // bottom layer is used by both headers and footers
        assertRowWrapsLayer(h1, layers.get(0));
        assertRowWrapsLayer(f1, layers.get(0));

        assertRowWrapsLayer(f2, layers.get(1));
        assertRowWrapsLayer(h2, layers.get(2));
    }

    @Test
    public void addHeaderAndFooterRows_addColumns_rowsUpdatedToWrapCorrectElements() {
        grid = new Grid<>();

        HeaderRow h1 = grid.prependHeaderRow();
        HeaderRow h2 = grid.prependHeaderRow();

        FooterRow f1 = grid.appendFooterRow();
        FooterRow f2 = grid.appendFooterRow();

        addColumns();

        List<List<Element>> layers = getColumnLayersAndAssertCount(3);

        // bottom layer is used by both headers and footers
        assertRowWrapsLayer(h1, layers.get(0));
        assertRowWrapsLayer(f1, layers.get(0));

        assertRowWrapsLayer(f2, layers.get(1));
        assertRowWrapsLayer(h2, layers.get(2));
    }

    @Test
    public void joinTwoFirstHeaderCells() {
        HeaderRow bottom = grid.prependHeaderRow();
        HeaderRow top = grid.prependHeaderRow();
        HeaderCell lastCell = top.getCells().get(2);
        HeaderCell joined = top.join(firstColumn, secondColumn);

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(bottom, layers.get(0));
        assertRowWrapsLayer(top, layers.get(1));

        Assert.assertEquals(
                "HeaderRow should have two cells after joining two of three cells",
                2, top.getCells().size());
        Assert.assertEquals(
                "The joined cell should be the first cell of the row after joining two first cells",
                joined, top.getCells().get(0));
        Assert.assertEquals(
                "The last cell should not be affected after joining two first cells",
                lastCell, top.getCells().get(1));
    }

    @Test
    public void joinTwoLastHeaderCells() {
        HeaderRow bottom = grid.prependHeaderRow();
        HeaderRow top = grid.prependHeaderRow();
        HeaderCell firstCell = top.getCells().get(0);
        HeaderCell joined = top.join(secondColumn, thirdColumn);

        List<List<Element>> layers = getColumnLayersAndAssertCount(2);
        assertRowWrapsLayer(bottom, layers.get(0));
        assertRowWrapsLayer(top, layers.get(1));

        Assert.assertEquals(
                "HeaderRow should have two cells after joining two of three cells",
                2, top.getCells().size());
        Assert.assertEquals(
                "The joined cell should be the last cell of the row after joining two last cells",
                joined, top.getCells().get(1));
        Assert.assertEquals(
                "The first cell should not be affected after joining two last cells",
                firstCell, top.getCells().get(0));
    }

    @Test
    public void joinNonAdjacentHeaderCells_throws() {
        grid.prependHeaderRow();
        HeaderRow top = grid.prependHeaderRow();
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("not adjacent");
        top.join(firstColumn, thirdColumn);
    }

    @Test
    public void getHeaderRows_orderFromTopToBottom() {
        HeaderRow row1 = grid.prependHeaderRow();
        assertHeaderRowOrder(row1);

        HeaderRow row2 = grid.prependHeaderRow();
        assertHeaderRowOrder(row2, row1);

        HeaderRow row3 = grid.appendHeaderRow();
        assertHeaderRowOrder(row2, row1, row3);

        grid.appendFooterRow();
        grid.prependFooterRow();

        assertHeaderRowOrder(row2, row1, row3);
    }

    @Test
    public void getCellByColumn_returnsCorrectCell() {
        AbstractRow<?> row = grid.appendFooterRow();
        assertGettingCellsByColumns(row);
        row = grid.prependFooterRow();
        assertGettingCellsByColumns(row);

        row = grid.appendHeaderRow();
        assertGettingCellsByColumns(row);
        row = grid.prependHeaderRow();
        assertGettingCellsByColumns(row);
    }

    private void assertGettingCellsByColumns(AbstractRow<?> row) {
        IntStream.range(0, grid.getColumns().size()).forEach(i -> {
            Assert.assertSame("getCell(Column) returned unexpected cell",
                    row.getCells().get(i),
                    row.getCell(grid.getColumns().get(i)));
        });
    }

    @Test
    public void getJoinedCellByColumn_worksWithEachChildColumn() {
        grid.prependHeaderRow();
        HeaderRow topRow = grid.prependHeaderRow();
        HeaderCell joined = topRow.join(secondColumn, thirdColumn);

        Assert.assertSame(
                "Joined header cell was not found by its child column", joined,
                topRow.getCell(secondColumn));
        Assert.assertSame(
                "Joined header cell was not found by its child column", joined,
                topRow.getCell(thirdColumn));
        Assert.assertSame(
                "getCell(Column) returned unexpected cell after joining other cells",
                topRow.getCells().get(0), topRow.getCell(firstColumn));
    }

    @Test
    public void getCellByColumnNotBelongingToGrid_throws() {
        HeaderRow row = grid.prependHeaderRow();
        Column<?> mockColumn = new Column<>(new Grid<String>(), "",
                TemplateRenderer.of(""));

        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Cannot find a cell");
        row.getCell(mockColumn);
    }

    @Test
    public void getFooterRows_orderFromTopToBottom() {
        FooterRow row1 = grid.prependFooterRow();
        assertFooterRowOrder(row1);

        FooterRow row2 = grid.prependFooterRow();
        assertFooterRowOrder(row2, row1);

        FooterRow row3 = grid.appendFooterRow();
        assertFooterRowOrder(row2, row1, row3);

        grid.appendHeaderRow();
        grid.prependHeaderRow();

        assertFooterRowOrder(row2, row1, row3);
    }

    private void assertHeaderRowOrder(HeaderRow... rows) {
        Assert.assertEquals("Grid returned unexpected amount of header rows",
                rows.length, grid.getHeaderRows().size());
        IntStream.range(0, rows.length).forEach(i -> {
            Assert.assertSame(
                    "Grid did no return expected header rows in order from top to bottom",
                    rows[i], grid.getHeaderRows().get(i));
        });
    }

    private void assertFooterRowOrder(FooterRow... rows) {
        Assert.assertEquals("Grid returned unexpected amount of footer rows",
                rows.length, grid.getFooterRows().size());
        IntStream.range(0, rows.length).forEach(i -> {
            Assert.assertSame(
                    "Grid did no return expected footer rows in order from top to bottom",
                    rows[i], grid.getFooterRows().get(i));
        });
    }

    private void assertRowWrapsLayer(AbstractRow<?> row, List<Element> layer) {
        List<Element> cellWrappedElements = row.getCells().stream()
                .map(cell -> cell.getColumn().getElement())
                .collect(Collectors.toList());

        Assert.assertEquals(
                "The row contains unexpected amount of column elements",
                layer.size(), cellWrappedElements.size());

        IntStream.range(0, layer.size()).forEach(i -> {
            Assert.assertEquals(
                    "The row is not referring to expected column elements",
                    layer.get(i), cellWrappedElements.get(i));
        });
    }

    private List<List<Element>> getColumnLayersAndAssertCount(
            int expectedAmountOfLayers) {
        List<List<Element>> layers = getColumnLayers();
        assertLayerCount(layers, expectedAmountOfLayers);
        return layers;
    }

    private void assertLayerCount(List<List<Element>> layers,
            int expectedCount) {
        Assert.assertEquals("Unexpected amount of column layers", expectedCount,
                layers.size());
    }

    /**
     * Gets all the layers of column-elements in the column-hierarchy of the
     * Grid. The order is from in-most to out-most. So the first layer consists
     * of the vaadin-grid-column elements, and the second layer consists of
     * vaadin-grid-column-group elements that are their parents, and so on.
     * 
     * @see ColumnLayer
     */
    private List<List<Element>> getColumnLayers() {
        List<List<Element>> layers = new ArrayList<List<Element>>();
        List<Element> children = grid.getElement().getChildren()
                .collect(Collectors.toList());
        while (children.stream().anyMatch(isColumnGroup)) {
            children = children.stream().filter(isTemplate.negate())
                    .collect(Collectors.toList());
            if (!children.stream().allMatch(isColumnGroup)) {
                throw new IllegalStateException(
                        "All column-children on the same hierarchy level "
                                + "should be either vaadin-grid-columns or "
                                + "vaadin-grid-column-groups. "
                                + "All of the tags on this layer are:\n"
                                + children.stream().map(Element::getTag)
                                        .reduce("", (a, b) -> (a + " " + b)));
            }
            layers.add(children);
            children = children.stream()
                    .flatMap(element -> element.getChildren())
                    .collect(Collectors.toList());
        }
        if (children.stream().anyMatch(isColumn)) {
            children = children.stream().filter(isTemplate.negate())
                    .collect(Collectors.toList());
            if (!children.stream().allMatch(isColumn)) {
                throw new IllegalStateException(
                        "All column-children on the same hierarchy level "
                                + "should be either vaadin-grid-columns or "
                                + "vaadin-grid-column-groups. "
                                + "All of the tags on this layer are:\n"
                                + children.stream().map(Element::getTag)
                                        .reduce("", (a, b) -> (a + " " + b)));
            }
            layers.add(children);
        } else if (layers.size() > 0) {
            throw new IllegalStateException(
                    "If there are vaadin-grid-column-groups, there should "
                            + "also be vaadin-grid-columns inside them");
        }
        // reverse to have the same order as in the implementation code:
        // from inner-most to out-most
        return Lists.reverse(layers);
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
        return element.getChildren().filter(isTemplate)
                .filter(template -> template.getClassList().contains(className))
                .findFirst();
    }
}
