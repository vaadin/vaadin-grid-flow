package com.vaadin.flow.component.treegrid.it;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.vaadin.flow.component.grid.testbench.TreeGridElement;
import com.vaadin.flow.testutil.AbstractComponentIT;

public class AbstractTreeGridIT extends AbstractComponentIT {

    protected TreeGridElement grid;

    public void before() {
        grid = $(TreeGridElement.class).first();
    }

    protected String id(String id) {
        return id.replace(" ", "");
    }

    protected boolean logContainsText(String txt) {
        String value = (String) executeScript("return arguments[0].value",
                findElement(By.id("log")));
        return value != null && value.contains(txt);
    }

    protected WebElement findElementByText(String text) {
        return findElement(By.id(id(text)));
    }

    protected void assertCellTexts(int startRowIndex, int cellIndex,
            String... cellTexts) {
        int index = startRowIndex;
        for (String cellText : cellTexts) {
            Assert.assertEquals(cellText,
                    grid.getCell(index, cellIndex).getText());
            index++;
        }
    }
}
