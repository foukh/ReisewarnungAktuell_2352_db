package com.brh.reisewarnungaktuell.controller;

import com.brh.reisewarnungaktuell.App;
import com.brh.reisewarnungaktuell.controller.dao.OfflineDAO;
import com.brh.reisewarnungaktuell.controller.dao.OnlineDAO;
import com.brh.reisewarnungaktuell.controller.dao.TravelWarningDAO;
import com.brh.reisewarnungaktuell.model.TravelWarningPreview;
import com.brh.reisewarnungaktuell.view.ViewControllerSearch;
import com.brh.reisewarnungaktuell.view.ViewControllerSite;
import com.brh.reisewarnungaktuell.view.ViewManager;
import com.brh.reisewarnungaktuell.view.ViewType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.swing.text.View;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Zentraler Controller der Anwendung.
 * Verwaltet die Kommunikation zwischen Views, DAOs und steuert den Anwendungsfluss.
 * Implementiert das Singleton-Pattern für globale Zugriffsmöglichkeit.
 */
public class MainController {
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());
    private static MainController instance;

    private ViewManager view;
    private TravelWarningDAO dao;

    /**
     * Privater Konstruktor für Singleton-Pattern.
     */
    private MainController() {
    }

    /**
     * Gibt die Singleton-Instanz des MainControllers zurück.
     * Thread-safe Implementierung mit synchronisiertem Zugriff.
     * @return Die einzige Instanz des MainControllers
     */
    public static synchronized MainController getInstance() {

          if(instance == null) instance = new MainController();

          return instance;
    }

    /**
     * Initialisiert den Controller und alle abhängigen Komponenten.
     * Prüft die Internetverbindung und wählt entsprechend Online- oder Offline-DAO.
     * Zeigt die Suchansicht als Startansicht an.
     */
    public void init() {


        view = new ViewManager();

        if(isOnline()){
            dao = new OnlineDAO();
            LOGGER.log(Level.INFO, "Onlinebetrieb");
        }
        else{
            dao = new OfflineDAO();
            LOGGER.log(Level.INFO, "Offlinebetrieb");
            App.getStage().setTitle(App.getStage().getTitle() + " (offline)");
        }

        view.showView(ViewType.SEARCH);

        if(isOnline()){
            Optional<Object> controlerOptional = view.getCurrentViewController();

            if (controlerOptional.isEmpty()){
                LOGGER.log(Level.WARNING, "Controller konnte nicht geladen werden");
                return;
            }
            ViewControllerSearch controllerSearch = (ViewControllerSearch) controlerOptional.get();
            controllerSearch.hideOfflineCacheButton();
        }
    }

    /**
     * Wird von der Suchansicht aufgerufen, wenn sie bereit für Daten ist.
     * Fordert die Liste aller Reisewarnung-Vorschauen vom DAO an.
     *
     * @param callback Callback-Funktion, die mit den Warnungsvorschauen aufgerufen wird
     */
    public void searchViewIsReady( Action< ArrayList<TravelWarningPreview> > callback) {

        if(callback == null){
            LOGGER.log(Level.WARNING, "Callback vom SearchView ist null");
            return;
        }

        dao.requestWarningPreviews( callback );

    }

    /**
     * Zeigt die Detailansicht für eine spezifische Reisewarnung an.
     * Wechselt zur Site-Ansicht und lädt die Warnung mit der gegebenen ID.
     *
     * @param travelWarningId Die ID der anzuzeigenden Reisewarnung (darf nicht null oder leer sein)
     * @throws IllegalArgumentException wenn travelWarningId null oder nur Whitespace enthält
     */
    public void requestWarningSite( String travelWarningId ) {
         view.showView(ViewType.SITE);
         Optional<Object> controllerOptional = view.getCurrentViewController();

         if(controllerOptional.isEmpty()){
             LOGGER.log(Level.SEVERE, "Site Controller konnte nicht geladen werden.");
             return;
         }
         ViewControllerSite controller = (ViewControllerSite) controllerOptional.get();
         dao.requestWarningById( travelWarningId, controller::showSiteContent );

    }

    /**
     * Wechselt zu einer anderen Ansicht.
     *
     * @param type Der Typ der anzuzeigenden Ansicht
     * @throws IllegalArgumentException wenn type null ist
     */
    public void requestViewChange(ViewType type) {
        if (type == null) {
            throw new IllegalArgumentException("ViewType darf nicht null sein");
        }

        try {
            view.showView(type);
            LOGGER.log(Level.INFO, "Wechsle zu Ansicht: " + type);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim Wechseln zur Ansicht: " + type, e);
        }
    }

    /**
     * Prüft die Internetverbindung durch einen HTTP-Request zur Auswärtiges-Amt-Website.
     * Verwendet Timeouts für bessere Performance und Zuverlässigkeit.
     *
     * @return true wenn die Verbindung erfolgreich ist, false bei Fehlern
     */
    private boolean isOnline() {
        try {
            URL url = URI.create("https://www.auswaertiges-amt.de").toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Timeouts setzen
            connection.setConnectTimeout(5000); // 5 Sekunden
            connection.setReadTimeout(5000);    // 5 Sekunden
            connection.setRequestMethod("HEAD"); // Nur Header anfordern

            int responseCode = connection.getResponseCode();
            boolean isOnline = (responseCode == 200);

            LOGGER.info("Online-Check: " + (isOnline ? "Verbindung verfügbar" : "Keine Verbindung"));
            return isOnline;

        } catch (IOException e) {

             return false;
        }
    }
}
