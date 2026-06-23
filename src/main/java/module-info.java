module com.brh.reisewarnungaktuell {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    requires javafx.base;
    requires javafx.web;
    requires javafx.graphics;
    requires java.net.http;
    requires java.logging;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires jdk.security.auth;
    requires java.sql;

    opens com.brh.reisewarnungaktuell to javafx.fxml;
    exports com.brh.reisewarnungaktuell;
    exports com.brh.reisewarnungaktuell.view;
    opens com.brh.reisewarnungaktuell.view to javafx.fxml;
    exports com.brh.reisewarnungaktuell.model;
    opens com.brh.reisewarnungaktuell.model to javafx.fxml;
    exports com.brh.reisewarnungaktuell.controller;
    opens com.brh.reisewarnungaktuell.controller to javafx.fxml;
    exports com.brh.reisewarnungaktuell.controller.dao;
    opens com.brh.reisewarnungaktuell.controller.dao to javafx.fxml;
}