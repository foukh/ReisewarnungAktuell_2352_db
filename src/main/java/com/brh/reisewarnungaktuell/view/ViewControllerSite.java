package com.brh.reisewarnungaktuell.view;

import com.brh.reisewarnungaktuell.controller.MainController;
import com.brh.reisewarnungaktuell.model.TravelWarning;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.util.Observable;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Controller für die Detailansicht einer einzelnen Reisewarnung.
 * Zeigt den vollständigen Inhalt einer Reisewarnung in einer WebView an.
 */
public class ViewControllerSite {
    private static final Logger LOGGER = Logger.getLogger(ViewControllerSite.class.getName());

    @FXML
    private Label titleLabel;
    @FXML
    private WebView webView;
    private WebEngine engine;

    /**
     * Initialisiert die WebView und konfiguriert den WebEngine.
     * Wird automatisch von JavaFX nach dem Laden der FXML-Datei aufgerufen.
     */
    @FXML
    public void initialize(){
        engine = webView.getEngine();
        LOGGER.log(Level.INFO,"ViewControllerSite initialisiert");
    }

    /**
     * Zeigt den Content einer einzelnen Reisewarnung in der WebView an.
     * Lädt den HTML-Content der Warnung und zeigt ihn an.
     * 
     * @param warning TravelWarning-Objekt.
     *
     */
    public void showSiteContent(TravelWarning warning) {
        if ( warning == null ) {
            LOGGER.warning("Versuch, leere Reisewarnung anzuzeigen");
            showErrorContent("Keine Reisewarnung verfügbar");
            return;
        }

        Platform.runLater(() -> {
            try {
                titleLabel.setText(warning.title() + " - " + warning.countryName());
                engine.loadContent(warning.content(), "text/html");

                LOGGER.info("Reisewarnung für '" + warning.countryName() + "' wird angezeigt");

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Fehler beim Anzeigen der Reisewarnung: " + e.getMessage(), e);
                showErrorContent("Fehler beim Laden der Reisewarnung");
            }
        });
    }

    /**
     * Zeigt eine Fehlerseite in der WebView an.
     * 
     * @param errorMessage Die anzuzeigende Fehlermeldung
     */
    private void showErrorContent(String errorMessage) {

    }

    /**
     * Wird aufgerufen, wenn der Benutzer auf "Suche" klickt.
     * Wechselt zurück zur Suchansicht.
     * 
     * @param event Das ActionEvent des Button-Klicks
     */
    @FXML
    private void onSearchClicked(ActionEvent event) {
        MainController.getInstance().requestViewChange(ViewType.SEARCH);
    }
}
