package de.uni_passau.fim.seibt.v8;

import de.uni_passau.fim.seibt.v8.controller.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class of the 'V8' application. Shows the JavaFX GUI of the program when its {@link #main(String[])} method is
 * called.
 */
public class V8 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(V8.class.getResource("V8.fxml"));
        Parent parent = fxmlLoader.load();

        Scene root = new Scene(parent);

        fxmlLoader.<Controller>getController().setStage(primaryStage);

        primaryStage.setScene(root);
        primaryStage.setTitle("V8");
        primaryStage.setResizable(true);

        primaryStage.show();
    }

    /**
     * Shows the JavaFX GUI of the program.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        launch(args);
    }
}
