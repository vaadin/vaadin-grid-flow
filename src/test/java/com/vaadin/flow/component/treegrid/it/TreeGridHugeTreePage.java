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

import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.NoTheme;

@Route("treegrid-huge-tree")
@NoTheme
public class TreeGridHugeTreePage extends Div {

    private TreeGrid<String> treeGrid;
    private TreeDataProvider<String> inMemoryDataProvider;

    public TreeGridHugeTreePage() {
        initializeDataProvider();
        treeGrid = new TreeGrid<>();
        treeGrid.setDataProvider(inMemoryDataProvider);
        treeGrid.setWidth("100%");
        treeGrid.addHierarchyColumn(String::toString).setHeader("String")
                .setId("string");
        treeGrid.addColumn((i) -> "--").setHeader("Nothing");
        treeGrid.setId("testComponent");

        NativeButton expand = new NativeButton("Expand Granddad 1");
        expand.addClickListener(event -> treeGrid.expand("Granddad 1"));
        NativeButton collapse = new NativeButton("Collapse Granddad 1");
        collapse.addClickListener(event -> treeGrid.collapse("Granddad 1"));

        add(treeGrid, expand, collapse);
    }

    private void initializeDataProvider() {
        TreeData<String> data = new TreeData<>();
        for (int i = 0; i < 3; i++) {
            String granddad = "Granddad " + i;
            data.addItem(null, granddad);
            for (int j = 0; j < 3; j++) {
                String dad = "Dad " + i + "/" + j;
                data.addItem(granddad, dad);
                for (int k = 0; k < 300; k++) {
                    String son = "Son " + i + "/" + j + "/" + k;
                    data.addItem(dad, son);
                }
            }
        }
        inMemoryDataProvider = new TreeDataProvider<>(data);
    }

}
