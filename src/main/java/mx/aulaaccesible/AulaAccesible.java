package mx.aulaaccesible;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class AulaAccesible extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        var fxml = new FXMLLoader(AulaAccesible.class.getResource("ventana.fxml"));
        var pantalla = Screen.getPrimary().getVisualBounds();
        var scene = new Scene(fxml.load(),
                Math.min(1120, pantalla.getWidth() - 28),
                Math.min(735, pantalla.getHeight() - 45));
        scene.getStylesheets().add(AulaAccesible.class.getResource("aula.css").toExternalForm());
        stage.setTitle("Aula Accesible");
        stage.setMinWidth(760);
        stage.setMinHeight(445);
        stage.setScene(scene);
        stage.show();
    }
}
