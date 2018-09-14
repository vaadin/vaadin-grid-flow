package com.vaadin.flow.component.grid;

import java.util.Optional;

import com.vaadin.flow.data.provider.DataGenerator;
import com.vaadin.flow.data.provider.DataKeyMapper;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;

/**
 * Renderer that sets the {@code path} property of a column to render its
 * content. It doesn't create a {@code <template>} element.
 * 
 * @author Vaadin Ltd.
 *
 * @param <T>
 *            the value type of the column
 */
public class ColumnPathRenderer<T> extends Renderer<T> {

    private final String columnId;
    private final ValueProvider<T, ?> valueProvider;

    /**
     * Creates a new renderer for the specified column.
     * 
     * @param columnId
     *            the internal id of the column
     * @param valueProvider
     *            the provider of the value to be rendered in the grid cells
     */
    public ColumnPathRenderer(String columnId,
            ValueProvider<T, ?> valueProvider) {
        this.columnId = columnId;
        this.valueProvider = valueProvider;
    }

    @Override
    public Rendering<T> render(Element container, DataKeyMapper<T> keyMapper) {

        container.setProperty("path", columnId);
        return new PathRendering();
    }

    private String formatValueToSendToTheClient(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }

    private class PathRendering implements Rendering<T> {

        @Override
        public Optional<DataGenerator<T>> getDataGenerator() {
            return Optional.of((item, jsonObject) -> jsonObject.put(columnId,
                    formatValueToSendToTheClient(valueProvider.apply(item))));
        }

        @Override
        public Element getTemplateElement() {
            return null;
        }

    }
}
