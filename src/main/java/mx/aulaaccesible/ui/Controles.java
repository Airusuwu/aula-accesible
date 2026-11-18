package mx.aulaaccesible.ui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

final class Controles {
    private Controles() {}

    static Label texto(String texto, String estilo) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add(estilo);
        etiqueta.setWrapText(true);
        etiqueta.setMinWidth(0);
        return etiqueta;
    }

    static Button boton(String texto, String estilo, Runnable accion) {
        Button boton = new Button(texto);
        boton.getStyleClass().add(estilo);
        boton.setOnAction(evento -> accion.run());
        return boton;
    }

    static VBox caja(double espacio, String estilo, Node... contenido) {
        VBox caja = new VBox(espacio, contenido);
        caja.getStyleClass().add(estilo);
        return caja;
    }

    static Node espacio() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    static HBox fila(String estilo, Node... nodos) {
        HBox fila = new HBox(12, nodos);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add(estilo);
        return fila;
    }

    static ScrollPane desplazar(Node contenido) {
        ScrollPane panel = new ScrollPane(contenido);
        panel.setFitToWidth(true);
        panel.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        panel.setPannable(false);
        return panel;
    }

    static TextField campo(String texto, String ejemplo, int limite) {
        TextField entrada = new TextField(texto);
        entrada.setPromptText(ejemplo);
        entrada.setTextFormatter(new TextFormatter<>(cambio ->
                cambio.getControlNewText().length() <= limite ? cambio : null));
        return entrada;
    }

    static VBox campoConEtiqueta(String nombre, TextField campo) {
        Label etiqueta = texto(nombre, "etiqueta-campo");
        etiqueta.setLabelFor(campo);
        return caja(4, "campo-con-etiqueta", etiqueta, campo);
    }

    static void enfocar(Node nodo) {
        Platform.runLater(nodo::requestFocus);
    }

    static TitledPane desplegable(String titulo, Node contenido) {
        var panel = new TitledPane(titulo, contenido);
        panel.setExpanded(false);
        panel.setAnimated(false);
        return panel;
    }
}
