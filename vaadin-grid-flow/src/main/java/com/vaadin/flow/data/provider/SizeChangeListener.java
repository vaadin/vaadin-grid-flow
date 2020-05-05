package com.vaadin.flow.data.provider;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public interface SizeChangeListener {

    class SizeChangeEvent<T extends Component> extends ComponentEvent<T> {
        private int size;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source
         *         the source component
         */
        public SizeChangeEvent(T source, int size) {
            super(source, false);
            this.size = size;
        }

        public int getSize() {
            return size;
        }
    }

    void sizeChanged(SizeChangeEvent event);

}
