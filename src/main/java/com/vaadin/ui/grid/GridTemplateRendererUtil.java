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
package com.vaadin.ui.grid;

import java.util.Map;

import com.vaadin.flow.dom.Element;
import com.vaadin.function.ValueProvider;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.grid.Grid.GridDataGenerator;
import com.vaadin.ui.renderers.ComponentTemplateRenderer;
import com.vaadin.ui.renderers.TemplateRenderer;
import com.vaadin.ui.renderers.TemplateRendererUtil;
import com.vaadin.util.JsonSerializer;

/**
 * Helper class with utility methods used internally by {@link Grid} to support
 * {@link TemplateRenderer}s inside cells, headers, footers and detail rows.
 * <p>
 * This class is not meant to be used outside the scope of the Grid.
 * 
 * @author Vaadin Ltd.
 */
class GridTemplateRendererUtil {

    private GridTemplateRendererUtil() {
    }

    static String getAppId() {
        String appId = UI.getCurrent().getSession().getService().getMainDivId(
                UI.getCurrent().getSession(), VaadinRequest.getCurrent());
        appId = appId.substring(0, appId.indexOf("-"));
        return appId;
    }

    static <T> void setupTemplateRenderer(TemplateRenderer<T> renderer,
            Element contentTemplate, Element templateDataHost,
            GridDataGenerator<T> dataGenerator,
            ValueProvider<String, ?> keyMapper) {

        renderer.getValueProviders()
                .forEach((key, provider) -> dataGenerator.addDataGenerator(
                        (item, jsonObject) -> jsonObject.put(key,
                                JsonSerializer.toJson(provider.apply(item)))));

        TemplateRendererUtil.registerEventHandlers(renderer, contentTemplate,
                templateDataHost, key -> (T) keyMapper.apply(key));
    }

    static <T> void setupHeaderOrFooterComponentRenderer(Component owner,
            ComponentTemplateRenderer<? extends Component, T> componentRenderer) {

        Component renderedComponent = componentRenderer.createComponent(null);
        owner.getElement().appendVirtualChild(renderedComponent.getElement());

        String appId = getAppId();

        componentRenderer.setTemplateAttribute("appid", appId);
        componentRenderer.setTemplateAttribute("nodeid", String
                .valueOf(renderedComponent.getElement().getNode().getId()));
    }

    static <T> void registerRenderedComponent(
            ComponentTemplateRenderer<? extends Component, T> componentRenderer,
            Map<String, Component> renderedComponents, Element container,
            String itemKey, Component component) {

        Element element = component.getElement();
        container.appendChild(element);

        if (renderedComponents != null) {
            renderedComponents.put(itemKey, component);
        }
    }

}
