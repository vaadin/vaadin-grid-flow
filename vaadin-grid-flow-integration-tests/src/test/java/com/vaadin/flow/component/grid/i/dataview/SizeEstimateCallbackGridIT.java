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

import java.math.BigDecimal;

import com.vaadin.flow.testutil.TestPath;
import org.junit.Assert;
import org.junit.Test;

import static com.vaadin.flow.component.grid.it.dataview.AbstractUndefinedSizeGridPage.DEFAULT_INITIAL_SIZE_ESTIMATE;

@TestPath("size-estimate-callback")
public class SizeEstimateCallbackGridIT extends AbstractUndefinedSizeGridIT {

    @Test
    public void estimateCallbackGrid_scrollingPastEstimate_fetchesNewEstimate() {
        open();

        // the callback returns initial size
        verifyRows(DEFAULT_INITIAL_SIZE_ESTIMATE);

        grid.scrollToRow(DEFAULT_INITIAL_SIZE_ESTIMATE - 30);

        // the callback should have returned new size as + 20%

        int newSize = DEFAULT_INITIAL_SIZE_ESTIMATE
                + new BigDecimal(DEFAULT_INITIAL_SIZE_ESTIMATE)
                        .divide(new BigDecimal(5)).intValue();

        /* TODO #1038 enable the rest of the test after unnecessary fetches from grid
           are fixed

        verifyRows(newSize);

        grid.scrollToRow(newSize);

        newSize = newSize
                + new BigDecimal(newSize).divide(new BigDecimal(5)).intValue();

        verifyRows(newSize);
         */
    }

    @Test
    public void estimateCallbackGrid_reachesEndBeforeEstimate_sizeChanges() {
        open(300);

        verifyRows(300);

        setNextSizeEstimate(500);

        grid.scrollToRow(240);

        verifyRows(500);

        setUndefinedSizeBackendSize(469);
        // make sure the next estimate is increased before reaching end
        setNextSizeEstimate(800);

        grid.scrollToRow(444);

        verifyRows(469);
    }

    @Test
    public void estimateCallbackGrid_newCallbackIsSet_newSizeEstimateIsApplied() {
        open(300);
        verifyRows(300);

        setNextSizeEstimate(500);
        grid.scrollToRow(240);
        final int expectedLastRowAfterScroll = 257;

        verifyRows(500);
        Assert.assertEquals(
                "Incorrect row visible after scroll",
                expectedLastRowAfterScroll, grid.getLastVisibleRowIndex());

        setNextSizeEstimate(600);
        // no change yet
        verifyRows(500);
        Assert.assertEquals(
                "Incorrect last row visible",
                expectedLastRowAfterScroll, grid.getLastVisibleRowIndex());

        // set the callback again
        setSizeEstimateCallback();

        verifyRows(600);
        Assert.assertEquals(
                "Last visible row should not change after estimated size change",
                expectedLastRowAfterScroll, grid.getLastVisibleRowIndex());

        setNextSizeEstimate(555);
        // no change yet
        verifyRows(600);
        // set the callback again
        setSizeEstimateCallback();
        Assert.assertEquals(
                "Last visible row should not change after estimated size change",
                expectedLastRowAfterScroll, grid.getLastVisibleRowIndex());

        verifyRows(555);
    }

    @Test
    public void estimateCallbackGridScrolledToEnd_newCallbackSet_newEstimateSizeNotApplied() {
        open(300);
        setUndefinedSizeBackendSize(200);
        verifyRows(300);

        setNextSizeEstimate(500);
        grid.scrollToRow(250);
        verifyRows(200);
        Assert.assertEquals(
                "Last visible row wrong",
                199, grid.getLastVisibleRowIndex());

        setSizeEstimateCallback();
        // since the end was reached, only a reset() to data provider will reset estimated size
        verifyRows(200);
        Assert.assertEquals(
                "Last visible row wrong",
                199, grid.getLastVisibleRowIndex());
    }

}
