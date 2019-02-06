package com.vaadin.flow.component.treegrid.it;

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;

@TestPath("treegrid-component-renderer")
public class TreeGridComponentRendererIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        open();
        setupTreeGrid();
    }

    @Test
    public void treegridComponentRenderer_expandCollapseExpand_renderersShows() {
        getTreeGrid().expandWithClick(0);

        assertCellTexts(0, 0, "Granddad 0");
        assertCellTexts(1, 0, "Dad 0/0");
        assertCellTexts(2, 0, "Dad 0/1");
        assertCellTexts(3, 0, "Dad 0/2");
        assertCellTexts(4, 0, "Granddad 1");
        assertCellTexts(5, 0, "Granddad 2");
        assertAllRowsHasTextField();

        getTreeGrid().collapseWithClick(0);
        waitForRowCount(3);

        assertCellTexts(0, 0, "Granddad 0");
        assertCellTexts(1, 0, "Granddad 1");
        assertCellTexts(2, 0, "Granddad 2");
        assertAllRowsHasTextField();

        getTreeGrid().expandWithClick(0);
        waitForRowCount(6);

        assertCellTexts(0, 0, "Granddad 0");
        assertCellTexts(1, 0, "Dad 0/0");
        assertCellTexts(2, 0, "Dad 0/1");
        assertCellTexts(3, 0, "Dad 0/2");
        assertCellTexts(4, 0, "Granddad 1");
        assertCellTexts(5, 0, "Granddad 2");
        assertAllRowsHasTextField();
    }

    private void waitForRowCount(int count) {
        waitUntil(webDriver -> getTreeGrid().getRowCount() == count, 2000);
    }

    private void assertAllRowsHasTextField() {
        waitUntil(webDriver -> {
            for (int i = 0; i < getTreeGrid().getRowCount(); i++) {
                if (!getTreeGrid().hasComponentRenderer(i, 1,
                        By.tagName("vaadin-text-field"))) {
                    return false;
                }
            }
            return true;
        }, 2000);
    }
}
