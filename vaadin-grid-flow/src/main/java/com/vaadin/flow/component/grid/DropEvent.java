package com.vaadin.flow.component.grid;

import java.util.Map;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public class DropEvent<T extends Component> extends ComponentEvent<T> {
    private final Map<String, String> data;
    // private final DragSource<?> dragSource;
    private final Component dragSourceComponent;
    // private final DropEffect dropEffect;

    /**
     * Creates a server side drop event.
     *
     * @param target
     *            Component that received the drop.
     * @param data
     *            Map containing all types and corresponding data from the
     *            {@code
     *         DataTransfer} object.
     * @param dropEffect
     *            the desired drop effect
     * @param dragSource
     *            Drag source extension of the component that initiated the drop
     *            event.-
     */
    public DropEvent(T target, Map<String, String> data,
            Component dragSourceComponent) {
        super(target, true);

        this.data = data;
        this.dragSourceComponent = dragSourceComponent;
        // this.dropEffect = dropEffect;
        // this.dragSource = dragSource;
        // this.dragSourceComponent = Optional.ofNullable(dragSource)
        // .map(DragSource::getDragSourceComponent).orElse(null);
    }

    /**
     * Get data from the {@code DataTransfer} object.
     *
     * @param type
     *            Data format, e.g. {@code text/plain} or {@code text/uri-list}.
     * @return Optional data for the given format if exists in the {@code
     * DataTransfer}, otherwise {@code Optional.empty()}.
     */
    public Optional<String> getDataTransferData(String type) {
        return Optional.ofNullable(data.get(type));
    }

    /**
     * Get data of any of the types {@code "text"}, {@code "Text"} or {@code
     * "text/plain"}.
     * <p>
     * IE 11 transfers data dropped from the desktop as {@code "Text"} while
     * most other browsers transfer textual data as {@code "text/plain"}.
     *
     * @return First existing data of types in order {@code "text"}, {@code
     * "Text"} or {@code "text/plain"}, or {@code null} if none of them exist.
     */
    public String getDataTransferText() {
        // Read data type "text"
        String text = data.get("text");

        // IE stores data dragged from the desktop as "Text"
        if (text == null) {
            text = data.get("Text");
        }

        // Browsers may store the key as "text/plain"
        if (text == null) {
            text = data.get("text/plain");
        }

        return text;
    }

    /**
     * Get all of the transfer data from the {@code DataTransfer} object. The
     * data can be iterated to find the most relevant data as it preserves the
     * order in which the data was set to the drag source element.
     *
     * @return Map of type/data pairs, containing all the data from the {@code
     * DataTransfer} object.
     */
    public Map<String, String> getDataTransferData() {
        return data;
    }

    /**
     * Get the desired dropEffect for the drop event.
     * <p>
     * <em>NOTE: Currently you cannot trust this to work on all browsers!
     * https://github.com/vaadin/framework/issues/9247 For Chrome & IE11 it is
     * never set and always returns {@link DropEffect#NONE} even though the drop
     * succeeded!</em>
     *
     * @return the drop effect
     */
    // public DropEffect getDropEffect() {
    // return dropEffect;
    // }

    /**
     * Returns the drag source component if the drag originated from a component
     * in the same UI as the drop target component, or an empty optional.
     *
     * @return Drag source component or an empty optional.
     */
    public Optional<Component> getDragSourceComponent() {
        return Optional.ofNullable(dragSourceComponent);
    }

    /**
     * Returns the extension of the drag source component if the drag originated
     * from a component in the same UI as the drop target component, or an empty
     * optional.
     *
     * @return Drag source extension or an empty optional
     */
    // public Optional<DragSource<? extends Component>> getDragSource() {
    // return Optional.ofNullable(dragSource);
    // }

    /**
     * Gets the server side drag data. This data can be set during the drag
     * start event on the server side and can be used to transfer data between
     * drag source and drop target when they are in the same UI.
     *
     * @return Optional server side drag data if set and the drag source and the
     *         drop target are in the same UI, otherwise empty {@code Optional}.
     * @see DragSourceExtension#setDragData(Object)
     */
    // public Optional<Object> getDragData() {
    // return getDragSource().map(DragSource::getDragData);
    // }

    /**
     * Returns the drop target component where the drop event occurred.
     *
     * @return Component on which a drag source was dropped.
     */
    public T getComponent() {
        return getSource();
    }
}