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
package com.vaadin.flow.component.grid.editor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Person;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.dom.Element;

import elemental.json.Json;
import elemental.json.JsonObject;

public class EditorRendererTest {

    private EditorRenderer<Person> renderer;
    private Editor<Person> editor;
    private Element container;
    private Element editorContainer;

    @SuppressWarnings("unchecked")
    @Before
    public void init() {
        editor = Mockito.mock(Editor.class);
        renderer = Mockito.spy(new EditorRenderer<>(editor, "col"));
        container = new Element("div");
        editorContainer = new Element("div");
        Mockito.when(renderer.createEditorContainer())
                .thenReturn(editorContainer);
    }

    @Test
    public void setComponentFunction_editorIsOpen_componentIsRendered() {
        Label label = new Label();
        renderer.setComponentFunction(item -> label);
        editorIsOpen_componentIsRendered(label);
    }

    @Test
    public void setBindingFunction_editorIsOpen_componentIsRendered() {
        TextField field = new TextField();
        Binder<Person> binder = new Binder<>(Person.class);
        renderer.setBindingFunction(item -> binder.bind(field, "name"));
        editorIsOpen_componentIsRendered(field);
    }

    @Test
    public void setStaticBinding_editorIsOpen_componentIsRendered() {
        TextField field = new TextField();
        Binder<Person> binder = new Binder<>(Person.class);
        renderer.setStaticBinding(binder.bind(field, "name"));
        editorIsOpen_componentIsRendered(field);
    }

    private void editorIsOpen_componentIsRendered(Component component) {
        Mockito.when(editor.isOpen()).thenReturn(true);
        Mockito.when(renderer.getComponentNodeId(component)).thenReturn(42);

        Person item = new Person("Special Person", 42);

        renderer.render(container, null);
        renderer.refreshData(item);
        JsonObject object = Json.createObject();
        renderer.generateData(item, object);

        Assert.assertEquals(42, (int) object.getNumber("_col_editor"));
        Mockito.verify(renderer, Mockito.times(1))
                .getComponentNodeId(component);

        Assert.assertEquals(1, editorContainer.getChildCount());
        Assert.assertEquals(component,
                editorContainer.getChild(0).getComponent().get());
    }

    @Test
    public void setComponentFunction_editorIsClosed_nothingIsRendered() {
        renderer.setComponentFunction(item -> new Label());
        editorIsClosed_nothingIsRendered();
    }

    @Test
    public void setBindingFunction_editorIsClosed_nothingIsRendered() {
        TextField field = new TextField();
        Binder<Person> binder = new Binder<>(Person.class);
        renderer.setBindingFunction(item -> binder.bind(field, "name"));
        editorIsClosed_nothingIsRendered();
    }

    @Test
    public void setStaticBinding_editorIsClosed_nothingIsRendered() {
        TextField field = new TextField();
        Binder<Person> binder = new Binder<>(Person.class);
        renderer.setStaticBinding(binder.bind(field, "name"));
        editorIsClosed_nothingIsRendered();
    }

    private void editorIsClosed_nothingIsRendered() {
        Mockito.when(editor.isOpen()).thenReturn(false);

        Person item = new Person("Special Person", 42);

        renderer.render(container, null);
        renderer.refreshData(item);
        JsonObject object = Json.createObject();
        renderer.generateData(item, object);

        Assert.assertFalse(object.hasKey("_col_editor"));
        Assert.assertEquals(0, editorContainer.getChildCount());
    }

    @Test
    public void setComponentFunction_functionReturnsNull_emptyIsRendered() {
        renderer.setComponentFunction(item -> null);
        functionReturnsNull_emptyIsRendered();
    }

    @Test
    public void setBindingFunction_functionReturnsNull_emptyIsRendered() {
        renderer.setBindingFunction(item -> null);
        functionReturnsNull_emptyIsRendered();
    }

    @Test
    public void setStaticBinding_setNull_emptyIsRendered() {
        renderer.setStaticBinding(null);
        functionReturnsNull_emptyIsRendered();
    }

    private void functionReturnsNull_emptyIsRendered() {
        Mockito.when(editor.isOpen()).thenReturn(true);

        Person item = new Person("Special Person", 42);

        renderer.render(container, null);
        renderer.refreshData(item);
        JsonObject object = Json.createObject();
        renderer.generateData(item, object);

        Assert.assertTrue(object.hasKey("_col_editor"));
        Assert.assertEquals(1, editorContainer.getChildCount());
        Assert.assertNull(editorContainer.getChild(0).getProperty("innerHTML"));
    }
}
