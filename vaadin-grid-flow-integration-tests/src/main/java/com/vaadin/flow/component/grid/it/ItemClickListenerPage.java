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
package com.vaadin.flow.component.grid.it;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ItemClickEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("item-click-listener")
public class ItemClickListenerPage extends Div {

    public ItemClickListenerPage() {
        Div clickMsg = new Div();
        clickMsg.setId("clickMsg");

        Div dblClickMsg = new Div();
        dblClickMsg.setId("dblClickMsg");

        Div columnKeyMsg = new Div();
        columnKeyMsg.setId("columnClickMsg");

        Grid<String> grid = new Grid<>();
        grid.setItems("foo", "bar");
        grid.addColumn(item -> item).setHeader("Name").setKey("Name");
        grid.addComponentColumn(item -> new Checkbox(item));

        grid.addItemClickListener(event -> {
            clickMsg.add(event.getItem());
            columnKeyMsg.add(getColumnKeyFromEvent(event));
        });

        grid.addItemDoubleClickListener(event -> {
            dblClickMsg
                    .setText(String.valueOf(event.getClientY()));
            columnKeyMsg.setText(getColumnKeyFromEvent(event));
        });

        add(grid, clickMsg, dblClickMsg, columnKeyMsg);
    }

    private static String getColumnKeyFromEvent(ItemClickEvent<?> event) {
        if(event.getColumn().isPresent()) {
            return event.getColumn().get().getKey();
        }
        else {
            return "(cannot get clicked column key!)";
        }
    }

}
