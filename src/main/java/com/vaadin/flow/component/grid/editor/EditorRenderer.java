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

import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.binder.Binder.Binding;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.internal.ExecutionContext;

import elemental.json.JsonObject;

/**
 * Renderer and DataGenerator used by {@link Column} to control the state of the
 * editor components and bindings.
 * <p>
 * Components are created during the {@link #generateData(Object, JsonObject)}
 * calls, and the proper data is sent to the client-side to be rendered.
 * 
 * @author Vaadin Ltd.
 *
 * @param <T>
 *            the type of the object being processed
 * 
 * @see Column#setEditorBinding(SerializableFunction)
 * @see Column#setEditorBinding(Binding)
 * @see Column#setEditorComponent(Component)
 * @see Column#setEditorComponent(SerializableFunction)
 */
public class EditorRenderer<T> extends Renderer<T> implements DataGenerator<T> {

    private Editor<T> editor;
    private String columnInternalId;
    private Element editorContainer;

    private SerializableFunction<T, ? extends Component> componentFunction;
    private SerializableFunction<T, Binding<T, ?>> bindingFunction;

    private Binding<T, ?> binding;
    private Binding<T, ?> staticBinding;
    private Component component;
    private String originalTemplate;

    // the flow-component-renderer needs something to load when the component is
    // null
    private Component emptyComponent;

    /**
     * Creates a new Data generator for a specific column.
     * 
     * @param editor
     *            the Grid's editor
     * @param columnInternalId
     *            the internal Id of the column that uses this data generator
     * @param editorContainer
     *            the container where all created editor components are appended
     *            to
     */
    public EditorRenderer(Editor<T> editor, String columnInternalId) {
        this.editor = editor;
        this.columnInternalId = columnInternalId;
    }

    /**
     * Sets the function that creates components to be used as editors for the
     * column. Using this method overrides whatever was set by
     * {@link #setBindingFunction(SerializableFunction)} and
     * {@link #setStaticBinding(Binding)}.
     * 
     * @param componentFunction
     *            the function that generates editor components
     */
    public void setComponentFunction(
            SerializableFunction<T, ? extends Component> componentFunction) {
        this.componentFunction = componentFunction;
        this.bindingFunction = null;
        this.staticBinding = null;
    }

    /**
     * Sets the function that creates bindings to be used as editors for the
     * column. Using this method overrides whatever was set by
     * {@link #setComponentFunction(SerializableFunction)} and
     * {@link #setStaticBinding(Binding)}.
     * 
     * @param bindingFunction
     *            the function that generates bindings
     */
    public void setBindingFunction(
            SerializableFunction<T, Binding<T, ?>> bindingFunction) {
        this.bindingFunction = bindingFunction;
        this.componentFunction = null;
        this.staticBinding = null;
    }

    /**
     * Sets the static binding that creates the editor for the column. Using
     * this method overrides whatever was set by
     * {@link #setComponentFunction(SerializableFunction)} and
     * {@link #setBindingFunction(SerializableFunction)}.
     * 
     * @param staticBinding
     *            the binding
     */
    public void setStaticBinding(Binding<T, ?> staticBinding) {
        this.staticBinding = staticBinding;
        this.bindingFunction = null;
        this.componentFunction = null;
    }

    @Override
    public void generateData(T item, JsonObject jsonObject) {
        if (editor.isOpen()) {
            if (component != null) {
                int nodeId = component.getElement().getNode().getId();
                jsonObject.put("_" + columnInternalId + "_editor", nodeId);
            }
        }
    }

    private void buildComponent(T item) {
        if (staticBinding != null || bindingFunction != null) {
            if (staticBinding != null) {
                // static bindings should never be unbound
                binding = staticBinding;
            } else {
                setBinding(bindingFunction.apply(item), item);
            }
            if (binding != null) {
                HasValue<?, ?> field = binding.getField();
                if (field != null && !(field instanceof Component)) {
                    throw new IllegalArgumentException(
                            "Binding target must be a component. It was "
                                    + field);
                }
                setComponent((Component) field);
            } else {
                setComponent(null);
            }
        } else if (componentFunction != null) {
            setComponent(componentFunction.apply(item));
        }
    }

    private void setComponent(Component newComponent) {
        if (component != null) {
            if (component.equals(newComponent)) {
                return;
            }
            if (component.getElement().getParent().equals(editorContainer)) {
                editorContainer.removeChild(component.getElement());
            }
        }

        if (newComponent == null) {
            newComponent = getOrCreateEmptyComponent();
        }

        // the component needs to be attached in order to have a nodeId
        editorContainer.appendChild(newComponent.getElement());
        component = newComponent;
    }

    private Component getOrCreateEmptyComponent() {
        if (emptyComponent == null) {
            emptyComponent = new Span();
        }
        return emptyComponent;
    }

    private void setBinding(Binding<T, ?> newBinding, T item) {
        if (binding != null && !binding.equals(newBinding)) {
            // Removes the old binding and the associated listeners
            binding.unbind();
        }
        binding = newBinding;
    }

    @Override
    public void refreshData(T item) {
        if (editor.isOpen()) {
            buildComponent(item);
        }
    }

    @Override
    public Rendering<T> render(Element container, DataKeyMapper<T> keyMapper,
            Element contentTemplate) {

        /*
         * The virtual container is needed as the parent of all editor
         * components. Editor components need a parent in order to have a proper
         * nodeId, and the nodeId is needed by the <flow-component-renderer> in
         * the client-side.
         */
        editorContainer = ElementFactory.createDiv();
        container.appendVirtualChild(editorContainer);

        /*
         * This is needed because ComponentRenderers don't set the innerHTML of
         * the <template> elements in advance, only before the client response.
         */
        runBeforeClientResponse(container, context -> {
            if (originalTemplate == null) {
                originalTemplate = contentTemplate.getProperty("innerHTML");
            }
            String appId = context.getUI().getInternals().getAppId();
            String editorTemplate = String.format(
                    "<flow-component-renderer appid='%s' nodeid='[[item._%s_editor]]'></flow-component-renderer>",
                    appId, columnInternalId);

            contentTemplate.setProperty("innerHTML", String.format(
            //@formatter:off
            "<template is='dom-if' if='[[item._editing]]' restamp>%s</template>" +
            "<template is='dom-if' if='[[!item._editing]]' restamp>%s</template>",
            //@formatter:on
                    editorTemplate, originalTemplate));
        });

        return new EditorRendering(contentTemplate);
    }

    private void runBeforeClientResponse(Element container,
            SerializableConsumer<ExecutionContext> execution) {
        container.getNode()
                .runWhenAttached(ui -> ui.getInternals().getStateTree()
                        .beforeClientResponse(container.getNode(), execution));
    }

    private class EditorRendering implements Rendering<T> {

        private final Element contentTemplate;

        public EditorRendering(Element contentTemplate) {
            this.contentTemplate = contentTemplate;
        }

        @Override
        public Optional<DataGenerator<T>> getDataGenerator() {
            return Optional.of(EditorRenderer.this);
        }

        @Override
        public Element getTemplateElement() {
            return contentTemplate;
        }
    }
}
