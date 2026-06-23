package com.brh.reisewarnungaktuell.controller.dao;

import com.brh.reisewarnungaktuell.controller.Action;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * APIRequest führt asynchrone HTTP-GET-Anfragen aus und liefert die Antwort über Callbacks zurück.
 * Verwendet den modernen Java HTTP Client mit konfigurierbaren Timeouts und Logging.
 */
public class APIRequest {
    private static final Logger LOGGER = Logger.getLogger(APIRequest.class.getName());
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private static final String USER_AGENT = "ReisewarnungAktuell/1.0";

    /**
     * Sendet eine asynchrone HTTP-GET-Anfrage an die angegebene URL.
     * 
     * @param url Die vollständige URL für die Anfrage (muss gültig sein)
     * @param callback Wird mit dem Response-Body oder Optional.empty() bei Fehlern aufgerufen
     * @throws IllegalArgumentException wenn die URL null oder leer ist
     */
    public void sendRequest(String url, Action<String> callback) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL darf nicht null oder leer sein");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", USER_AGENT)
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            LOGGER.info("Sende HTTP-GET-Anfrage an: " + url);

            CompletableFuture<HttpResponse<String>> future = HTTP_CLIENT.sendAsync(
                    request, HttpResponse.BodyHandlers.ofString());

            future.thenAccept(response ->  handleResponse(response, callback))
                .exceptionally(throwable -> {
                    LOGGER.log(Level.SEVERE, "HTTP-Anfrage fehlgeschlagen: " + throwable.getMessage(), throwable);
                     return null;
                 });

        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Ungültige URL: " + url, e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unerwarteter Fehler beim Senden der Anfrage", e);
        }
    }




    /**
     * Verarbeitet die HTTP-Antwort und ruft den Callback mit dem Ergebnis auf.
     * 
     * @param response Die HTTP-Antwort vom Server
     * @param callback Der Callback, der mit dem Response-Body aufgerufen wird
     */
    private void handleResponse(HttpResponse<String> response, Action<String> callback) {
        if (response.statusCode() == 200) {
            String body = response.body();
            LOGGER.info("HTTP-Anfrage erfolgreich: " + body);
            callback.invoke(body);
        }

        else if (response.statusCode() == 300) {
            String body = response.body();
            LOGGER.info("HTTP-Anfrage Umleitung:" + body);
            callback.invoke(body);
        }

        else if (response.statusCode() == 400) {
            String body = response.body();
            LOGGER.info("HTTP-Anfrage Client Error" + body);
            callback.invoke(body);
        }
        else if (response.statusCode() == 500) {
            String body = response.body();
            LOGGER.info ("HTTP-Anfrage Server Error" + body);
            callback.invoke(body);
        }
        else if (response.statusCode() == 600) {
            String body = response.body();
            LOGGER.info("HTTP-Anfrage Fehlerhafter Bereich" + body);
            callback.invoke(body);
        }
        else {
            LOGGER.warning("HTTP-Anfrage fehlgeschlagen, Status: " + response.statusCode());
        }
    }
}
