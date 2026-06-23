package com.brh.reisewarnungaktuell.controller.dao;

import com.brh.reisewarnungaktuell.controller.Action;
import com.brh.reisewarnungaktuell.model.TravelWarning;
import com.brh.reisewarnungaktuell.model.TravelWarningPreview;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Ruft Reisewarnungsdaten von der Auswärtiges-Amt-API ab und speichert sie lokal im Cache.
 * Ermöglicht Offline-Zugriff und asynchrone Verarbeitung via Callbacks.
 * 
 * @see TravelWarningDAO
 * @see APIRequest
 */
public class OnlineDAO implements TravelWarningDAO {
    private static final Logger LOGGER = Logger.getLogger(OnlineDAO.class.getName());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String OFFLINE_CACHE_PATH = "offlineCache.json";
    private static final String API_BASE_URL = "https://www.auswaertiges-amt.de/opendata/travelwarning";

    private final List<TravelWarning> offlineCacheList;

    /**
     * Konstruktor für OnlineDAO. Initialisiert die Cache-Liste.
     */
    public OnlineDAO(){
        this.offlineCacheList = new ArrayList<>();
    }

    /**
     * Ruft eine Liste aller Reisewarnung-Vorschauen von der API ab.
     * 
     * @param callback Wird mit der Liste oder leer aufgerufen
     */
    @Override
    public void requestWarningPreviews( Action< ArrayList<TravelWarningPreview> > callback ){
        APIRequest request = new APIRequest();
        request.sendRequest(API_BASE_URL, json -> handleWarningPreviewResponse(json, callback));
    }

    /**
     * Ruft eine Reisewarnung anhand ihrer Land-ID ab und speichert sie im Cache.
     * 
     * @param id Länder-ID (z.B. "AF" für Afghanistan)
     * @param callback Wird mit der Reisewarnung oder leer aufgerufen
     */
    @Override
    public void requestWarningById(String id, Action<TravelWarning>  callback){
        APIRequest request = new APIRequest();
        String url = API_BASE_URL + "/" + id;
        request.sendRequest(url,
                jsonOptional -> handleWarningResponse(jsonOptional, callback));
    }

    /**
     * Verarbeitet die API-Antwort für Reisewarnung-Vorschauen und parsed die JSON-Daten.
     * 
     * @param json JSON-Antwort
     * @param callback Wird mit den geparsten Vorschauen aufgerufen
     */
    private void handleWarningPreviewResponse( String json, Action< ArrayList<TravelWarningPreview> > callback ) {

        if(json == null || json.isBlank()){
            LOGGER.log(Level.WARNING, "API antwortet mit leerer oder ungültiger Reisewarungsvorschau");
            return;
        }

        try{ //Daten sind verwendbar
            JsonNode root = OBJECT_MAPPER.readTree( json );
            JsonNode response = root.get("response");

            if( response == null ){
                LOGGER.log(Level.WARNING, "Ungültiges Feld response");
                return;
            }

            JsonNode contentList = response.get("contentList");
            if(contentList == null || !contentList.isArray()){
                LOGGER.log(Level.WARNING, "contentList ist null oder kein Array");
                return;
            }

            ArrayList<TravelWarningPreview> previews = new ArrayList<>();
            for( JsonNode idNode : contentList ){
                String id = idNode.asText();
                JsonNode preview = response.get(id);

                if(preview == null){
                    LOGGER.log(Level.INFO,
                            "Preview für id: "+id+" nicht gefunden, wird übersprungen");
                    continue;
                }

                //ToDo: Titel, Ländername holen und ein Record v. Typ
                //ToDo: TravelWarning Preview erzeugen
                String title = preview.get("title").asText();
                String countryName = preview.get("countryName").asText();

                TravelWarningPreview warning =
                            new TravelWarningPreview(id, title, countryName);
                previews.add(warning);
            }

            LOGGER.log(Level.INFO, "Reisewarnungs-Vorschau geladen");
            callback.invoke(previews);
        }
        catch( JsonProcessingException e ){
            LOGGER.log( Level.SEVERE, "Fehler beim Parsen der Reisewarnungsvorschau: " + e.getMessage());
        }
    }

    /**
     * Verarbeitet die API-Antwort für eine einzelne Reisewarnung und speichert sie im Cache.
     * 
     * @param json JSON-Antwort
     * @param callback Wird mit der geparsten Reisewarnung aufgerufen
     */
    private void handleWarningResponse(String json, Action<TravelWarning> callback) {
        if ( json == null || json.isBlank() ) {
            LOGGER.log(Level.WARNING, "API antwortete mit leerer Reisewarnung");
            return;
        }

        try {
            JsonNode rootNode = OBJECT_MAPPER.readTree(json);
            JsonNode responseNode = rootNode.get("response");

            if (responseNode == null) {
                LOGGER.log(Level.WARNING,"API Antwort hat kein 'response' Feld");
                return;
            }

            JsonNode contentList = responseNode.get("contentList");

            if (contentList == null || !contentList.isArray() || contentList.isEmpty() ){
                LOGGER.log(Level.WARNING,"'contentList' ist null, kein Array oder leer");
                return;
            }

            String id = contentList.get(0).asText();
            if ( id.isEmpty() ) {
                LOGGER.log(Level.WARNING, "ID aus contentList ist leer");
                return;
            }

            JsonNode warningNode = responseNode.get(id);
            if ( warningNode == null ){
                LOGGER.log(Level.WARNING, "Reisewarnung Node für ID '" + id + "' nicht gefunden");
                return;
            }

            String countryName = warningNode.get("countryName").asText();
            String title = warningNode.get("title").asText();
            String content = warningNode.get("content").asText();

            TravelWarning warning = new TravelWarning( id, countryName, title, content);
            LOGGER.log(Level.INFO, "Reisewarnung für '" + countryName + "' erfolgreich geladen");
            callback.invoke( warning );
            cacheWarnings( warning );

        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim Parsen der Reisewarnung: " + e.getMessage(), e);
        }
    }

    /**
     * Speichert eine Reisewarnung im In-Memory- und Datei-Cache.
     * 
     * @param warning zu speichernde Reisewarnung
     */
    private void cacheWarnings( TravelWarning warning ){
        //ToDo: warning der cachelist zufügen
        if(warning == null){
            LOGGER.warning("Ungültige Reisewarnung wurde nicht gespeichert");
            return;
        }
        offlineCacheList.add(warning);
        saveOfflineCache();
    }

    /**
     * Speichert die gecachte Reisewarnungsliste als JSON in offlineCache.json.
     * Nutzt UTF-8 Encoding und ObjectMapper für die Serialisierung.
     * 
     * @throws RuntimeException bei IO-Fehler
     */
    private void saveOfflineCache() {
        //ToDo cachelist in einem Zug mit Framework Jackson speichern
        //alternativ erst in json umwandeln, dann speichern
        //Pfad ist final Var schon gegeben
        try {
            OBJECT_MAPPER.writeValue(new File(OFFLINE_CACHE_PATH), offlineCacheList);
            LOGGER.log(Level.INFO, "Offline-Cache erfolgreich gespeichert mit " + offlineCacheList.size() + " Einträgen");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim Speichern des Offline-Cache: " + e.getMessage(), e);
              throw new RuntimeException(e);
        }
    }
}
