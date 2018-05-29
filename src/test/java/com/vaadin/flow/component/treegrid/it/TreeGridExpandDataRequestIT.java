package com.vaadin.flow.component.treegrid.it;

import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import static org.junit.Assert.assertFalse;

public class TreeGridExpandDataRequestIT extends AbstractTreeGridIT {

    @Before
    public void before() {
        getDriver().get(getRootURL() + "/" + TreeGridBasicFeaturesPage.VIEW);

        super.before();
        findElement(By.id("LoggingDataProvider")).click();

        clearLog();
    }

    private void clearLog() {
        findElement(By.id(id("Clear log"))).click();
    }

    @Test
    public void expand_node0_does_not_request_root_nodes() {
        grid.expandWithClick(0);
        assertFalse("Log should not contain request for root nodes.",
                logContainsText("Root node request: "));
    }

    @Test
    public void expand_node0_after_node1_does_not_request_children_of_node1() {
        grid.expandWithClick(1);
        assertFalse("Log should not contain request for root nodes.",
                logContainsText("Root node request: "));
        clearLog();
        grid.expandWithClick(0);
        assertFalse("Log should not contain request for children of '0 | 1'.",
                logContainsText("Children request: 0 | 1"));
        assertFalse("Log should not contain request for root nodes.",
                logContainsText("Root node request: "));
    }
}
