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
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.router.Route;

@Route("item-click-listener")
public class ItemClickListenerPage extends Div {

    public ItemClickListenerPage() {
        Div clickMsg = new Div();
        clickMsg.setId("clickMsg");

        Div dblClickMsg = new Div();
        dblClickMsg.setId("dblClickMsg");

        Div columnClickMsg = new Div();
        columnClickMsg.setId("columnClickMsg");

        Div columnDblClickMsg = new Div();
        columnDblClickMsg.setId("columnDblClickMsg");

        Grid<String> grid = new Grid<>();
        grid.setItems("foo", "bar");
        grid.addColumn(item -> item).setHeader("Name").setKey("Name");
        grid.addComponentColumn(item -> new Checkbox(item));

        grid.addItemClickListener(event -> {
            clickMsg.add(event.getItem());
            columnClickMsg.add(getColumnKeyFromEvent(event));
        });

        grid.addItemDoubleClickListener(event -> {
            dblClickMsg
                    .setText(String.valueOf(event.getClientY()));
            columnDblClickMsg.setText(getColumnKeyFromEvent(event));
        });

        grid.setItemDetailsRenderer(new ComponentRenderer<>((SerializableFunction<String, Span>) ItemClickListenerPage::getDetailsComponent));
        grid.setDetailsVisible("foo", false);
        grid.setDetailsVisible("bar", true);

        add(grid, clickMsg, dblClickMsg, columnClickMsg, columnDblClickMsg);
    }

    private static Span getDetailsComponent(String s) {
        Span result = new Span(s);
        result.setId("details-"+s);
        return result;
    }

    private static String getColumnKeyFromEvent(ItemClickEvent<?> event) {
        if (event.getColumn().isPresent()) {
            return event.getColumn().get().getKey();
        }
        else {
            return "(cannot get clicked column key!)";
        }
    }

}
