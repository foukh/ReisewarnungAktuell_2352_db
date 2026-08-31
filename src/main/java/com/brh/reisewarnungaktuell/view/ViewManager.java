package com.brh.reisewarnungaktuell.view;

import com.brh.reisewarnungaktuell.App;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Verwaltet den View-Management und das Laden von FXML-Views.
 * Ist verantwortlich für das Wechseln zwischen verschiedenen Ansichten
 * und den Zugriff auf die entsprechenden Controller.
 */
public class ViewManager {
    private static final Logger LOGGER = Logger.getLogger(ViewManager.class.getName());
    private static final double WINDOW_WIDTH = 720.0;
    private static final double WINDOW_HEIGHT = 400.0;

    private FXMLLoader currentLoader;

    /**
     * Zeigt eine View mit Standardgröße an (ruft die parametrisierte Variante
     * mit den Default-Werten WINDOW_WIDTH/WINDOW_HEIGHT auf).
     *
     * @param type Der Typ der anzuzeigenden View (darf nicht null sein)
     */
    public void showView(ViewType type) {
        showView(type, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    /**
     * Zeigt eine View basierend auf dem ViewType an und setzt dabei eine
     * individuell festgelegte Fenstergröße.
     * Lädt die entsprechende FXML-Datei und setzt sie als Szene im Hauptfenster.
     *
     * @param type Der Typ der anzuzeigenden View (darf nicht null sein)
     * @param width Die gewünschte Breite des Fensters
     * @param height Die gewünschte Höhe des Fensters
     * @throws IllegalArgumentException wenn type null ist
     * @throws RuntimeException wenn die FXML-Datei nicht geladen werden kann
     */
    public void showView(ViewType type, double width, double height) {

        String viewName = type.getPath();

        try{

            currentLoader  = new FXMLLoader(App.class.getResource(viewName));
            Parent view = currentLoader.load();

            if(view == null){
                throw new NullPointerException("View ist ungültig, konnte nicht geladen werden");
            }

            //View ist gültig, es geht weiter...
            Scene scene = new Scene(view, type.getWidth(), type.getHeight());
            App.getStage().setScene(scene);

            LOGGER.info("View wurde erfolgreich geladen: "+ type);

        }catch( IOException e ){
            LOGGER.log( Level.SEVERE, "Fehler beim Laden der View " + viewName
                    + "Error: "+e.getMessage() );
            throw new RuntimeException("View konnte nicht geladen werden "
                    + viewName +": "+e.getMessage(), e);
        }
        catch( NullPointerException e){
            LOGGER.log(Level.SEVERE, "Resource nicht gefunden " + viewName, e);
            throw new RuntimeException("FXML-Resource nicht vorhanden : Error:"+ e.getMessage());
        }
        catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gibt den Controller der aktuell geladenen View zurück.
     *
     * @return Ein Optional mit dem Controller der aktuellen View,
     * oder leer wenn keine View geladen ist
     */
    public Optional<Object> getCurrentViewController() {
        if (currentLoader == null) {
            LOGGER.warning("Versuch, Controller zu bekommen ohne geladene View");
            return Optional.empty();
        }
        return Optional.ofNullable(currentLoader.getController());
    }
}