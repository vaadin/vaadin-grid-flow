/*
 * Copyright 2000-2018 Vaadin Ltd.
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
package com.vaadin.flow.component.treegrid.it;

import org.junit.Test;
import org.openqa.selenium.By;

import com.vaadin.flow.testutil.TestPath;

@TestPath("treegrid-expand-all")
public class TreeGridExpandAllIT extends AbstractTreeGridIT {

    @Test
    public void addNewItemAfterCollapseAndExpand() {
        open();

        setupTreeGrid();

        findElement(By.id("collapse")).click();
        findElement(By.id("expand")).click();
        findElement(By.id("add-new")).click();

        assertCellTexts(6, 0, "New son");
    }
}
