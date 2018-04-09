package com.vaadin.flow.component.grid.it;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.testutil.AbstractComponentIT;
import com.vaadin.flow.testutil.TestPath;

@TestPath("grid-on-flex-layout")
public class GridOnFlexLayoutIT extends AbstractComponentIT {

    @Test
    public void gridOccupies100PercentOfThePage() {
        open();
        getDriver().manage().window().setSize(new Dimension(1000, 1000));

        WebElement grid = findElement(By.id("full-size-grid"));
        Dimension dimension = grid.getSize();
        Assert.assertEquals("The width of the grid should be 1000", 1000,
                dimension.getWidth());
        Assert.assertEquals("The height of the grid should be 1000", 1000,
                dimension.getHeight());

        getDriver().manage().window().setSize(new Dimension(500, 500));
        dimension = grid.getSize();
        Assert.assertEquals("The width of the grid should be 500", 500,
                dimension.getWidth());
        Assert.assertEquals("The height of the grid should be 500", 500,
                dimension.getHeight());
    }

}
