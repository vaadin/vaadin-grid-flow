package com.vaadin.flow.component.grid;

public enum DropMode {
    BETWEEN("between"), ON_TOP("on-top"), ON_TOP_OR_BETWEEN(
            "on-top-or-between"), ON_GRID("on-grid");

    private final String clientName;

    DropMode(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }
}
