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

import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.dom.DomListenerRegistration;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.internal.StateNode;

public class GridContextMenuTest {

    @Test(expected = IllegalArgumentException.class)
    public void setNonGridTargetForGridContextMenu_throws() {
        GridContextMenu<Object> gridContextMenu = new GridContextMenu<>();
        gridContextMenu.setTarget(new NativeButton());
    }

    @Test
    public void setTarget_nullTarget_connectorIsRemovedFromPreviousTarget() {
        Grid grid = Mockito.mock(Grid.class);
        Element element = Mockito.mock(Element.class);
        StateNode node = Mockito.mock(StateNode.class);
        Mockito.when(grid.getElement()).thenReturn(element);
        Mockito.when(element.getNode()).thenReturn(node);

        DomListenerRegistration registration = Mockito
                .mock(DomListenerRegistration.class);

        Mockito.when(
                element.addEventListener(Mockito.anyString(), Mockito.any()))
                .thenReturn(registration);

        GridContextMenu gridContextMenu = new GridContextMenu<>();
        gridContextMenu.setTarget(grid);

        gridContextMenu.setTarget(null);

        Mockito.verify(registration).remove();
        Mockito.verify(element)
                .callFunction("$contextMenuConnector.removeConnector");
    }

}
