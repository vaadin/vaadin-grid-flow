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
package com.vaadin.flow.component.grid;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.StatusChangeEvent;

public class EditorImplTest {

    private Grid<String> grid;
    private TestEditor editor;

    private static class TestEditor extends EditorImpl<String> {

        private List<String> refreshedItems = new ArrayList<>();

        public TestEditor(Grid<String> grid) {
            super(grid, null);
        }

        @Override
        protected void refresh(String item) {
            refreshedItems.add(item);
        }
    }

    @Before
    public void setUp() {
        grid = new Grid<>();
        editor = new TestEditor(grid);

        editor.setBinder(new Binder<>());
        grid.getDataCommunicator().getKeyMapper().key("bar");
    }

    @Test(expected = IllegalStateException.class)
    public void editItem_itemIsNotKnown_throw() {
        editor.editItem("foo");
    }

    @Test(expected = IllegalStateException.class)
    public void editItem_noBinder_throw() {
        editor = new TestEditor(grid);
        editor.editItem("bar");
    }

    @Test(expected = IllegalStateException.class)
    public void editItem_editorIsBufferedAndOpen_throw() {
        grid.getDataCommunicator().getKeyMapper().key("foo");

        editor.setBuffered(true);
        editor.editItem("bar");

        editor.editItem("foo");
    }

    @Test
    public void editItem_itemIsKnown_binderStatusEventAndEditorOpenEvent() {
        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorOpenEvent<String>> openEventCapure = new AtomicReference<EditorOpenEvent<String>>();
        assertOpenEvents(statusEventCapture, openEventCapure);

        // In not buffered mode there is the bean in the binder
        Assert.assertEquals("bar", editor.getBinder().getBean());
    }

    @Test
    public void editItem_itemIsKnown_binderIsInBufferedMode_binderStatusEventAndEditorOpenEvent() {
        editor.setBuffered(true);

        AtomicReference<StatusChangeEvent> statusEventCapture = new AtomicReference<>();
        AtomicReference<EditorOpenEvent<String>> openEventCapure = new AtomicReference<EditorOpenEvent<String>>();
        assertOpenEvents(statusEventCapture, openEventCapure);

        // In not buffered mode there is no bean in the binder
        Assert.assertNull("bar", editor.getBinder().getBean());
    }

    private void assertOpenEvents(
            AtomicReference<StatusChangeEvent> statusEventCapture,
            AtomicReference<EditorOpenEvent<String>> openEventCapure) {
        editor.getBinder().addStatusChangeListener(
                event -> statusEventCapture.compareAndSet(null, event));

        editor.addOpenListener(
                event -> openEventCapure.compareAndSet(null, event));
        editor.editItem("bar");

        Assert.assertNotNull(statusEventCapture.get());
        Assert.assertNotNull(openEventCapure.get());

        Assert.assertEquals("bar", openEventCapure.get().getBean());
    }
}
