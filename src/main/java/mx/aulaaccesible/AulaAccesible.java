package mx.aulaaccesible;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import mx.aulaaccesible.ui.Preferencias;
import mx.aulaaccesible.ui.Tema;

import java.io.IOException;
import java.util.regex.Pattern;

public final class AulaAccesible extends Application {
    private static final Pattern TAMANO = Pattern.compile("(\\d{3,5})x(\\d{3,5})");

    @Override
    public void start(Stage stage) throws Exception {
        cargarFuente("AtkinsonHyperlegible-Regular.ttf");
        cargarFuente("AtkinsonHyperlegible-Bold.ttf");
        var fxml = new FXMLLoader(AulaAccesible.class.getResource("ventana.fxml"));
        var scene = new Scene(fxml.load(), 1280, 800);
        scene.getStylesheets().add(AulaAccesible.class.getResource("aula.css").toExternalForm());
        Tema.aplicar(scene, Preferencias.temaOscuro().get());
        Preferencias.temaOscuro().addListener((obs, anterior, oscuro) -> Tema.aplicar(scene, oscuro));
        stage.setTitle("Aula Accesible");
        stage.setMinWidth(1000);
        stage.setMinHeight(600);
        stage.setScene(scene);
        // AULA_VENTANA=1366x768 abre sin maximizar con ese tamaño, para revisar cómo se adapta la interfaz.
        var tamano = TAMANO.matcher(System.getenv().getOrDefault("AULA_VENTANA", ""));
        if (tamano.matches()) {
            stage.setWidth(Double.parseDouble(tamano.group(1)));
            stage.setHeight(Double.parseDouble(tamano.group(2)));
            stage.centerOnScreen();
        } else {
            stage.setMaximized(true);
        }
        stage.show();
    }

    private static void cargarFuente(String archivo) throws IOException {
        try (var fuente = AulaAccesible.class.getResourceAsStream("fuentes/" + archivo)) {
            if (fuente != null) {
                Font.loadFont(fuente, 16);
            }
        }
    }
}
