package mx.aulaaccesible.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.Locale;

import static mx.aulaaccesible.ui.Controles.texto;
import static mx.aulaaccesible.ui.Piezas.boton;
import static mx.aulaaccesible.ui.Piezas.campo;
import static mx.aulaaccesible.ui.Piezas.escenario;
import static mx.aulaaccesible.ui.Piezas.etiqueta;

/** Ejemplos del área «Presentación visual». */
final class DemosVisual {
    private static final Locale ES_MX = Locale.forLanguageTag("es-MX");

    private DemosVisual() {}

    static Demo contrasteLegible(boolean cumple) {
        var mensaje = new Piezas.Mensaje();
        var muestra = texto("Entrega tu tarea antes del viernes.", cumple ? "demo-texto" : "demo-tenue");
        var escenario = escenario(etiqueta("Texto de muestra"), muestra);
        var medir = boton("Medir contraste", () -> {
            // Se mide el color real que el tema resolvió para el texto y para el fondo del ejemplo.
            double relacion = Contraste.relacion(Contraste.color(muestra.getTextFill()), Contraste.fondo(escenario));
            String cifra = String.format(ES_MX, "Contraste: %.1f a 1 (mínimo %.1f a 1). ", relacion,
                    Contraste.MINIMO_TEXTO);
            if (relacion >= Contraste.MINIMO_TEXTO) {
                mensaje.ok(cifra + "Cumple.");
            } else {
                mensaje.error(cifra + "No cumple: el texto casi no se distingue.");
            }
        });
        escenario.getChildren().addAll(medir, mensaje.nodo());
        return Demo.de(escenario, "Lee el texto y pulsa «Medir contraste» para ver la relación real entre el texto y su fondo.",
                medir);
    }

    static Demo informacionSinColor(boolean cumple) {
        var mensaje = new Piezas.Mensaje();
        var correo = campo("tu@correo.mx");
        var enviar = boton("Enviar", () -> {
            boolean valido = Piezas.correoValido(correo.getText().trim());
            correo.getStyleClass().removeAll("demo-error", "demo-valido");
            correo.getStyleClass().add(valido ? "demo-valido" : "demo-error");
            if (!cumple) {
                mensaje.ocultar();
            } else if (valido) {
                mensaje.ok("Correo válido.");
            } else {
                mensaje.error("Falta tu correo completo. Ejemplo: ana@escuela.mx");
            }
        });
        var escenario = escenario(etiqueta("Correo"), correo, enviar, mensaje.nodo());
        return Demo.de(escenario, cumple
                ? "Escribe algo sin @ y pulsa «Enviar»: el error se ve por el borde, el icono y el texto."
                : "Escribe algo sin @ y pulsa «Enviar»: solo cambia el color del borde. Si no distingues colores, "
                        + "no te enteras.", correo, enviar);
    }

    static Demo ampliacionTexto(boolean cumple) {
        int[] tamano = {16};
        var muestra = texto("Lee la lección completa antes de responder.", "demo-texto");
        if (!cumple) {
            muestra.setWrapText(false);
            muestra.setTextOverrun(OverrunStyle.ELLIPSIS);
            muestra.setMaxWidth(230);
        }
        var valor = new Label();
        valor.getStyleClass().add("demo-texto");
        Runnable aplicar = () -> {
            muestra.setStyle("-fx-font-size: " + tamano[0] + "px;");
            valor.setText("Tamaño: " + tamano[0] + " px");
        };
        aplicar.run();
        Button menos = boton("A−", () -> { tamano[0] = Math.max(12, tamano[0] - 4); aplicar.run(); });
        Button mas = boton("A+", () -> { tamano[0] = Math.min(32, tamano[0] + 4); aplicar.run(); });
        menos.setAccessibleText("Reducir el texto");
        mas.setAccessibleText("Ampliar el texto");
        var escenario = escenario(new HBox(8, menos, mas, valor), muestra);
        ((HBox) escenario.getChildren().get(0)).setAlignment(Pos.CENTER_LEFT);
        return Demo.de(escenario, cumple
                ? "Usa «A+» y «A−» (o las flechas) para cambiar el tamaño: el texto pasa a otra línea y sigue completo."
                : "Usa «A+» y «A−» (o las flechas): el texto se corta con «…» y pierdes parte del mensaje.",
                menos, mas).conFlechas(tecla -> {
                    if (tecla == KeyCode.UP || tecla == KeyCode.RIGHT) {
                        mas.fire();
                    } else {
                        menos.fire();
                    }
                });
    }

    static Demo redistribucionContenido(boolean cumple) {
        var slider = new Slider(140, 280, 280);
        slider.setBlockIncrement(20);
        slider.setMajorTickUnit(20);
        slider.setAccessibleText("Ancho de la ventana");
        var valor = texto("", "demo-texto");
        valor.textProperty().bind(slider.valueProperty().asString(ES_MX, "Ancho de la ventana: %.0f px"));

        var botones = new javafx.scene.Node[]{boton("Anterior", () -> { }), boton("Siguiente", () -> { }),
                boton("Enviar", () -> { })};
        Pane contenido = cumple ? new FlowPane(8, 8, botones) : new HBox(8, botones);
        var marco = new VBox(contenido);
        marco.getStyleClass().add("demo-marco-estrecho");
        marco.setAlignment(Pos.TOP_LEFT);
        marco.minWidthProperty().bind(slider.valueProperty());
        marco.prefWidthProperty().bind(slider.valueProperty());
        marco.maxWidthProperty().bind(slider.valueProperty());
        var recorte = new Rectangle();
        recorte.widthProperty().bind(marco.widthProperty());
        recorte.heightProperty().bind(marco.heightProperty());
        marco.setClip(recorte);
        var escenario = escenario(valor, slider, marco);
        return Demo.de(escenario, cumple
                ? "Mueve el deslizador con las flechas ← →: al estrecharse, los botones pasan a otra línea y todos se ven."
                : "Mueve el deslizador con las flechas ← →: al estrecharse, los botones se salen y quedan cortados.",
                slider);
    }
}
