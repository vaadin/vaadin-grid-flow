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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.AbstractRow.AbstractCell;
import com.vaadin.flow.component.grid.FooterRow.FooterCell;

/**
 * @author Vaadin Ltd.
 */
public class FooterRow extends AbstractRow<FooterCell> {

    public static class FooterCell extends AbstractCell {

        FooterCell(AbstractColumn<?> column) {
            super(column);
        }

        @Override
        public void setText(String text) {
            getColumn().renderHeader(text);
        }

        @Override
        public void setComponent(Component component) {
            getColumn().renderHeader(component);
        }

    }

    public FooterRow(ColumnLayer layer) {
        super(layer, FooterCell::new);
    }
}
