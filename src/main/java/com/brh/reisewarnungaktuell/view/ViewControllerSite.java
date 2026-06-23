package com.brh.reisewarnungaktuell.view;

import com.brh.reisewarnungaktuell.controller.MainController;
import com.brh.reisewarnungaktuell.model.TravelWarning;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.print.Doc;
import javax.swing.text.html.HTML;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Savepoint;
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
    @FXML
    private WebEngine engine;

    /**
     * Die aktuell in der WebView angezeigte Reisewarnung.
     * Wird benötigt, um sie später als HTML-Datei speichern zu können.
     */
    private TravelWarning currentWarning;

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

        this.currentWarning = warning;

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

    /**
     * Wird aufgerufen, wenn der Benutzer auf "Als Html-Webseite speichern" klickt.
     * Öffnet den System-Dialog zum Speichern von Dateien und schreibt den
     * HTML-Content der aktuell angezeigten Reisewarnung an den gewählten Pfad.
     *
     * @param event Das ActionEvent des Button-Klicks
     */
    @FXML
    private void onClickSaveAsHtml(ActionEvent event) {
        if (currentWarning == null) {
            LOGGER.warning("Kein Speichern möglich: keine Reisewarnung geladen");
            showErrorContent("Keine Reisewarnung zum Speichern vorhanden");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Reisewarnung als HTML-Webseite speichern");
        fileChooser.setInitialFileName(currentWarning.countryName() + ".html");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("HTML-Datei", "*.html")
        );

        Window ownerWindow = webView.getScene().getWindow();
        File targetFile = fileChooser.showSaveDialog(ownerWindow);

        if (targetFile == null) {
            LOGGER.info("Speichern als HTML wurde vom Benutzer abgebrochen");
            return;
        }

        try {
            Files.writeString(targetFile.toPath(), currentWarning.content(), StandardCharsets.UTF_8);
            LOGGER.info("Reisewarnung als HTML gespeichert unter: " + targetFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim Speichern der HTML-Datei: " + e.getMessage(), e);
            showErrorContent("Fehler beim Speichern der Datei");
        }
    }

    /**
     * Wird aufgerufen, wenn der Benutzer auf "Löschen" klickt.
     * Ersetzt den Inhalt der WebView durch den Text "gelöscht".
     *
     * @param event Das ActionEvent des Button-Klicks
     */
    @FXML
    private void onClickDelete(ActionEvent event) {
        engine.loadContent("gelöscht", "text/html");
        LOGGER.info("Anzeige der Reisewarnung wurde gelöscht");
    }
}
