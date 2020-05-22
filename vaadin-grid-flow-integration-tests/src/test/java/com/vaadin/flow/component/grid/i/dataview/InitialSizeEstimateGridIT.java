/*
 * Copyright 2000-2020 Vaadin Ltd.
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

package com.vaadin.flow.component.grid.i.dataview;

import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;

@TestPath("initial-size-estimate")
public class InitialSizeEstimateGridIT extends AbstractUndefinedSizeGridIT {

    @Test
    public void initialSizeEstimateGrid_scrollingPastEstimate_keepsScrolling() {
        int initialEstimate = 300;
        open(initialEstimate);
        verifyRows(initialEstimate);

        grid.scrollToRow(initialEstimate);

        verifyRows(initialEstimate + 200);
    }

    @Test
    public void initialSizeEstimateGrid_reachesEndBeforeEstimate_sizeChanges() {
        int initialEstimate = 500;
        open(initialEstimate);
        verifyRows(initialEstimate);

        int undefinedSizeBackendSize = 333;
        setUndefinedSizeBackendSize(undefinedSizeBackendSize);

        grid.scrollToRow(400);

        verifyRows(undefinedSizeBackendSize);

        Assert.assertEquals("Incorrect last row visible",
                undefinedSizeBackendSize - 1, grid.getLastVisibleRowIndex());
    }

    @Test
    public void initialSizeEstimateGrid_switchesToDefinedSize_sizeChanges() {
        int initialEstimate = 500;
        open(initialEstimate);
        verifyRows(initialEstimate);

        setUndefinedSizeBackendSize(1000);
        verifyRows(initialEstimate);

        setDefinedSizeCallback();
        verifyRows(1000);
    }

}
