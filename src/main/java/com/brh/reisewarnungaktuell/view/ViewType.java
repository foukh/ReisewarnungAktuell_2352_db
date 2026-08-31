package com.brh.reisewarnungaktuell.view;

public enum ViewType {
    SEARCH("search-view.fxml", 1720, 400),
    SITE("site-view.fxml", 900, 600),
    SUPPORT("support-view.fxml", 1600, 400);

    private final String path;
    private final double width;
    private final double height;

    ViewType(String path, double width, double height) {
        this.path = path;
        this.width = width;
        this.height = height;
    }

    public String getPath() {
        return path;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }
}