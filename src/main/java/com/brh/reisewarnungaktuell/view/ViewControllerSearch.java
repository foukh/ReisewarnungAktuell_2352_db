package com.brh.reisewarnungaktuell.view;

import com.brh.reisewarnungaktuell.controller.MainController;
import com.brh.reisewarnungaktuell.controller.dao.OfflineDAO;
import com.brh.reisewarnungaktuell.controller.dao.OnlineDAO;
import com.brh.reisewarnungaktuell.model.TravelWarningPreview;
import com.sun.security.auth.NTNumericCredential;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Controller für die Suchansicht der Reisewarnungen.
 * Zeigt eine Liste von Reisewarnung-Vorschauen in einem ScrollPane an.
 */
public class ViewControllerSearch {
    private static final Logger LOGGER = Logger.getLogger(ViewControllerSearch.class.getName());

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Button offlineCacheClearBtn;

    /**
     * Wird bei Instanziierung des Controllers aufgerufen.
     * Registriert sich beim MainController für Daten-Updates.
     */
    @FXML
    private void initialize(){
         MainController.getInstance().
                 searchViewIsReady(this::createWarningOverview);
    }

    /**
     * Erstellt die Übersicht der Reisewarnungen als scrollbare Liste von Buttons.
     * Jeder Button repräsentiert eine Reisewarnung und kann angeklickt werden,
     * um zur Detailansicht zu wechseln.
     * 
     * @param warnings Optional mit der Liste der TravelWarningPreview-Objekte.
     *                        Wenn leer, wird nichts angezeigt.
     */
    public void createWarningOverview( ArrayList<TravelWarningPreview> warnings) {
        System.out.println(warnings);

        if(warnings == null){
            LOGGER.log(Level.WARNING, "Leere Vorschauliste");
            return;
        }

        VBox vbox = new VBox();
        vbox.setSpacing( 5 );
        vbox.setStyle( "-fx-padding: 10;");

        for( var preview : warnings){
            var previewBtn = createWarningButton(preview);
            vbox.getChildren().add(previewBtn);
        }

        Platform.runLater( ()->
                {
                      LOGGER.info("Vorschau Reisewarnung aktualisiert");
                      scrollPane.setContent( vbox );
                }
        );






    }

    /**
     * Erstellt einen Button für eine einzelne Reisewarnung-Vorschau.
     * 
     * @param preview Die Reisewarnung-Vorschau
     * @return Konfigurierter Button mit Click-Handler
     */
    private Button createWarningButton( TravelWarningPreview preview) {
        Button btn = new Button( "🌍 "+preview.title() +
                " | " + preview.countryName() );

        btn.setPrefWidth(680);
        btn.setAlignment((Pos.CENTER_LEFT));
        btn.setTooltip( new Tooltip("Klicken um Details der Reisewarnung zu sehen"));

        btn.setOnAction(event->{
            MainController.getInstance().requestWarningSite(preview.id());
         });

        return btn;
    }
    @FXML
    private void onSupportClicked(ActionEvent actionEvent) { MainController.getInstance().requestViewChange(ViewType.SUPPORT);
    }

    public void hideOfflineCacheButton(){
        offlineCacheClearBtn.setVisible(false);
    }

    @FXML
    public void onClickCacheDeleter() {
        try {
            OfflineDAO.clearOfflineCache();
            DialogUtility.showInfoDialog("Offline-Cache wurde gelöscht.");
        } catch (RuntimeException e) {

        }
    }

    @FXML
    public void onClickClose() {
        Platform.exit();
    }
}
