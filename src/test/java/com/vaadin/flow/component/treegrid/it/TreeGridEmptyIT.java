package com.vaadin.flow.component.treegrid.it;

import org.junit.Assert;
import org.junit.Test;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("treegrid-empty")
public class TreeGridEmptyIT extends AbstractComponentIT {

    @Test
    public void empty_treegrid_initialized_correctly() {
        open();

        TreeGridElement grid = $(TreeGridElement.class).id("treegrid");

        Assert.assertTrue("Grid should be displayd", grid.isDisplayed());

        Assert.assertTrue("Grid should not have rows", grid.getRowCount() == 0);
    }
}
