package com.brh.reisewarnungaktuell.view;

public enum ViewType {
    SEARCH("search-view.fxml"),
    SITE("site-view.fxml"),
    SUPPORT("support-view.fxml");

    private final String path;
    ViewType(String path) {
        this.path = path;
    }
    public String getPath() {
        return path;
    }
}
