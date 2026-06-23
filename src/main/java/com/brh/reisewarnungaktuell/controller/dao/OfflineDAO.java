package com.brh.reisewarnungaktuell.controller.dao;

import com.brh.reisewarnungaktuell.controller.Action;
import com.brh.reisewarnungaktuell.model.TravelWarningPreview;
import com.brh.reisewarnungaktuell.model.TravelWarning;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Lädt gecachte Reisewarnungsdaten aus offlineCache.json.
 * Wird bei fehlender Internetverbindung verwendet.
 * 
 * @see OnlineDAO
 * @see TravelWarningDAO
 */
public class OfflineDAO implements TravelWarningDAO{
    private static final Logger LOGGER = Logger.getLogger(OfflineDAO.class.getName());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final String OFFLINE_CACHE_PATH = "offlineCache.json";

    /**
     * Lädt alle gecachten Reisewarnung-Vorschauen aus dem Offline-Cache.
     * 
     * @param callback Wird mit der Liste der Vorschauen oder leer aufgerufen
     */
    @Override
    public void requestWarningPreviews( Action< ArrayList<TravelWarningPreview> > callback){
        Optional<String> jsonOptional = loadJsonFromOfflineCache();

        if(jsonOptional.isEmpty()){
            LOGGER.log(Level.INFO, "Offline-Cache nicht gefunden oder leer");
            return;
        }

        String json = jsonOptional.get();
        TypeReference<List<TravelWarningPreview>> reference = new TypeReference<>() {
        };
        try {
            List<TravelWarningPreview> warnings = OBJECT_MAPPER.readValue(json, reference);
            if(warnings != null){
                LOGGER.info("Erfolgreich " + warnings.size() + " Reisewarnungen aus dem Cache geladen");
                callback.invoke( new ArrayList<>(warnings) );
            }
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim Parsen des Offline-Cache: " + e.getMessage(), e);
        }
    }

    /**
     * Sucht eine spezifische Reisewarnung anhand ihrer ID im Offline-Cache.
     * 
     * @param id Eindeutige ID der gesuchten Reisewarnung
     * @param callback Wird mit der gefundenen Warnung oder leer aufgerufen
     */
    @Override
    public void requestWarningById(String id, Action<TravelWarning> callback){
        Optional<String> jsonOptional = loadJsonFromOfflineCache();

        if(jsonOptional.isEmpty()){
            LOGGER.info("Offline-Cache nicht gefunden oder leer");
            return;
        }

        String json = jsonOptional.get();
        TypeReference<List<TravelWarning>> reference = new TypeReference<>() {
        };

        try {
            List<TravelWarning> warnings = OBJECT_MAPPER.readValue(json, reference);
            if(warnings != null) {
                Optional<TravelWarning> foundWarning = warnings.stream()
                        .filter(warning -> warning.id().equals(id))
                        .findFirst();

                if(foundWarning.isPresent()) {
                    LOGGER.info("Reisewarnung mit ID '" + id + "' aus dem Cache geladen");
                    callback.invoke( foundWarning.get() );
                } else {
                    LOGGER.warning("Reisewarnung mit ID '" + id + "' nicht im Cache gefunden");
                }
            }
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Fehler beim Parsen des Offline-Cache: " + e.getMessage(), e);
        }
    }

    /**
     * Lädt die JSON-Daten aus der Offline-Cache-Datei.
     * 
     * @return Optional mit dem JSON-String, wenn die Datei existiert, ansonsten leer
     */
    private Optional<String> loadJsonFromOfflineCache(){
        Path path = Paths.get(OFFLINE_CACHE_PATH);
        if(Files.exists(path) && Files.isRegularFile(path)){
            try {
                String json = Files.readString(path);
                return Optional.of(json);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Fehler beim Lesen der Offline-Cache-Datei: " + e.getMessage(), e);
                return Optional.empty();
            }
        }
        else{
            return Optional.empty();
        }
    }
}
