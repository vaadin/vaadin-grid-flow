package com.vaadin.flow.component.grid.editor;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.Binder.Binding;
import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.SerializableFunction;

import elemental.json.JsonObject;

public class EditorDataGenerator<T> implements DataGenerator<T> {

    private Editor<T> editor;
    private String columnInternalId;
    private Element container;

    private SerializableFunction<T, ? extends Component> componentFunction;
    private SerializableFunction<T, Binding<T, ?>> bindingFunction;

    private Binding<T, ?> binding;
    private Component component;

    public EditorDataGenerator(Editor<T> editor, String columnInternalId,
            Element container) {
        this.editor = editor;
        this.columnInternalId = columnInternalId;
        this.container = container;
    }

    public void setComponentFunction(
            SerializableFunction<T, ? extends Component> componentFunction) {
        this.componentFunction = componentFunction;
    }

    public void setBindingFunction(
            SerializableFunction<T, Binding<T, ?>> bindingFunction) {
        this.bindingFunction = bindingFunction;
    }

    @Override
    public void generateData(T item, JsonObject jsonObject) {
        if (component != null && editor.isOpen()) {
            int nodeId = component.getElement().getNode().getId();
            jsonObject.put("_" + columnInternalId + "_editor", nodeId);
        }
    }

    private void setComponent(Component newComponent) {
        if (component != null) {
            if (component.equals(newComponent)) {
                return;
            }
            if (component.getElement().getParent().equals(container)) {
                container.removeChild(component.getElement());
            }
        }

        if (newComponent != null) {
            container.appendChild(newComponent.getElement());
        }
        component = newComponent;
    }

    private void setBinding(Binding<T, ?> newBinding) {
        if (binding != null && !binding.equals(newBinding)) {
            binding.unbind();
        }
        binding = newBinding;
    }

    @Override
    public void refreshData(T item) {
        if (!editor.isOpen()) {
            return;
        }
        if (bindingFunction != null) {
            setBinding(bindingFunction.apply(item));
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
}
