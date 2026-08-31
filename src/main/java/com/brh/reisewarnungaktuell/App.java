package com.brh.reisewarnungaktuell;

import com.brh.reisewarnungaktuell.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class    App extends Application {

    private static Stage currentStage;

    @Override
    public void start(Stage stage) throws IOException {
         currentStage = stage;
         stage.setTitle("Reisewarnung Aktuell");
         MainController.getInstance().init();
         stage.show();
    }

    public static Stage getStage(){
        return currentStage;
    }
}
