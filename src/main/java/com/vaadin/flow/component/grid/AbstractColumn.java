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
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.Rendering;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.internal.HtmlUtils;

/**
 * Base class with common implementation for different types of columns used
 * inside a {@link Grid}.
 *
 * @author Vaadin Ltd.
 * @param <T>
 *            the subclass type
 */
public class AbstractColumn<T extends AbstractColumn<T>> extends Component
        implements ColumnBase<T>, HasStyle {

    protected final Grid<?> grid;

    /**
     * Base constructor with the destination Grid.
     *
     * @param grid
     *            the grid that is the owner of this column
     */
    public AbstractColumn(Grid<?> grid) {
        this.grid = grid;
    }

    /**
     * Gets the owner of this column.
     *
     * @return the grid which owns this column
     */
    public Grid<?> getGrid() {
        return grid;
    }

    /**
     * Hides or shows the column. By default columns are visible before
     * explicitly hiding them.
     *
     * @param visible
     *            {@code false} to hide the column, {@code true} to show
     */
    @Override
    public void setVisible(boolean visible) {
        getElement().setProperty("hidden", !visible);
    }

    /**
     * Returns whether this column is visible. Default is {@code true}.
     *
     * @return {@code false} if the column is currently hidden, {@code true}
     *         otherwise
     */
    @Override
    @Synchronize("hidden-changed")
    public boolean isVisible() {
        return !getElement().getProperty("hidden", false);
    }

    @Override
    public T setHeader(String labelText) {
        return setHeader(TemplateRenderer.of(HtmlUtils.escape(labelText)));
    }

    @Override
    public T setFooter(String labelText) {
        return setFooter(TemplateRenderer.of(HtmlUtils.escape(labelText)));
    }

    @Override
    public T setHeader(Component headerComponent) {
        return setHeader(new ComponentRenderer<>(() -> headerComponent));
    }

    @Override
    public T setFooter(Component footerComponent) {
        return setFooter(new ComponentRenderer<>(() -> footerComponent));
    }

    protected T setHeader(Renderer<?> renderer) {
        Rendering<?> rendering = renderer.render(getElement(), null);
        rendering.getTemplateElement().get().setAttribute("class", "header");
        return (T) this;
    }

    protected T setFooter(Renderer<?> renderer) {
        Rendering<?> rendering = renderer.render(getElement(), null);
        rendering.getTemplateElement().get().setAttribute("class", "footer");
        return (T) this;
    }
}
