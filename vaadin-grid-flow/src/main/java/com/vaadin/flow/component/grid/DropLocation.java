package com.vaadin.flow.component.grid;

public enum DropLocation {
    ON_TOP("on-top"), ABOVE("above"), BELOW("below"), EMPTY("empty");

    private final String clientName;

    private DropLocation(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }

}
