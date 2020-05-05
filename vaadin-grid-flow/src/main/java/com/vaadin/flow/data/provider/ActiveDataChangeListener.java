package com.vaadin.flow.data.provider;

import java.util.stream.Stream;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;

public interface ActiveDataChangeListener {

    class ActiveDataChangeEvent<T, C extends Component> extends ComponentEvent<C> {
        private Stream<T> activeData;

        /**
         * Creates a new event using the given source and indicator whether the
         * event originated from the client side or the server side.
         *
         * @param source
         *         the source component
         */
        public ActiveDataChangeEvent(C source, Stream<T> activeData) {
            super(source, false);
            this.activeData = activeData;
        }

        public Stream<T> getActiveData() {
            return activeData;
        }
    }
    void activeDataChangeEvent(ActiveDataChangeEvent event);
}
