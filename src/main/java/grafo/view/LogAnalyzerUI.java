package grafo.view;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


/**
 * Questa classe permette di eseguire il codice per il process mining grazie ad un'interfaccia grafica
 * fornita grazie a JavaFX.
 *
 * @author Donici Ionut Bogdan
 */
public class LogAnalyzerUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Log Analyzer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
