package com.scentstation.simulator;

import com.scentstation.simulator.config.SimulatorConfig;
import com.scentstation.simulator.ui.SimulatorController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SimulatorApp extends Application {
    private SimulatorController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        String[] rawArgs = getParameters().getRaw().toArray(new String[0]);
        SimulatorConfig config = SimulatorConfig.loadDefault(rawArgs);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/simulator-view.fxml"));
        Parent root = loader.load();

        this.controller = loader.getController();
        this.controller.initializeWithConfig(config);

        Scene scene = new Scene(root, 1180, 760);
        scene.getStylesheets().add(getClass().getResource("/css/simulator.css").toExternalForm());

        primaryStage.setTitle("ScentStation IoT Device Simulator — " + config.getMachineId());
        primaryStage.setMinWidth(980);
        primaryStage.setMinHeight(640);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            if (controller != null) {
                controller.shutdown();
            }
        });
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
