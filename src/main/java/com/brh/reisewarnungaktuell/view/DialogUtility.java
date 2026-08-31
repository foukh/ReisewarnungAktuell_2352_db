package com.brh.reisewarnungaktuell.view;

import javafx.scene.control.Alert;

/**
 * Bietet statische Hilfsmethoden zur Anzeige von Info- und Fehlermeldungen.
 */
public class DialogUtility {

    /**
     * Zeigt einen Info-Dialog mit dem übergebenen Text an.
     *
     * @param text Der anzuzeigende Informationstext
     */
    public static void showInfoDialog(String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }

    /**
     * Zeigt einen Fehler-Dialog mit dem übergebenen Text an.
     *
     * @param text Der anzuzeigende Fehlertext
     */
    public static void showErrorDialog(String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}