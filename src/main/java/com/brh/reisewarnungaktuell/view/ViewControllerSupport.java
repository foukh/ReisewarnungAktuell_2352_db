package com.brh.reisewarnungaktuell.view;

import com.brh.reisewarnungaktuell.controller.MainController;
import com.brh.reisewarnungaktuell.model.TravelWarningPreview;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Controller für die Supportansicht der Reisewarnungen.
 */

    public class ViewControllerSupport {
        @FXML
        private void onClickBackToSearch(){
            MainController.getInstance().requestViewChange(ViewType.SEARCH);


    }

}
