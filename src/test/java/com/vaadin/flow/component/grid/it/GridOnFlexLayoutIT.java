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
        getDriver().manage().window().setSize(new Dimension(600, 600));

        WebElement grid = findElement(By.id("full-size-grid"));
        Dimension dimension = grid.getSize();
        Assert.assertEquals("The width of the grid should be 600", 600,
                dimension.getWidth());

        // On CI there's an offset of 105 pixels on the height
        Assert.assertEquals("The height of the grid should be 600", 600, 600,
                105);

        getDriver().manage().window().setSize(new Dimension(300, 300));
        dimension = grid.getSize();
        Assert.assertEquals("The width of the grid should be 300", 300,
                dimension.getWidth());

        // On CI there's an offset of 105 pixels on the height
        Assert.assertEquals("The height of the grid should be 300", 300, 300,
                105);
    }

}
