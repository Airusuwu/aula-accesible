package mx.aulaaccesible.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

import static mx.aulaaccesible.ui.Controles.caja;
import static mx.aulaaccesible.ui.Controles.texto;
import static mx.aulaaccesible.ui.Piezas.boton;
import static mx.aulaaccesible.ui.Piezas.escenario;
import static mx.aulaaccesible.ui.Piezas.etiqueta;

/** Ejemplos del área «Contenido multimedia». */
final class DemosMultimedia {
    private static final List<String> LECCION = List.of("leccion-1", "leccion-2", "leccion-3");

    private DemosMultimedia() {}

    static Demo alternativasImagenes(boolean cumple) {
        var voz = new Piezas.Voz();
        var mensaje = new Piezas.Mensaje();
        var grafica = caja(6, "demo-grafica", barra("Aprobaron", 0.6), barra("No aprobaron", 0.4));
        String clip = cumple ? "alt-bueno" : "alt-malo";
        var escuchar = boton("Escuchar con lector", () -> {
            voz.decir(clip);
            mensaje.neutro("El lector dice: «" + Sonidos.texto(clip) + "»");
        });
        escuchar.setGraphic(Iconos.icono("mdi2v-volume-high", "icono-boton"));
        escuchar.setContentDisplay(ContentDisplay.LEFT);
        var escenario = escenario(etiqueta("Resultados del examen"), grafica, escuchar, mensaje.nodo());
        return Demo.de(escenario, cumple
                ? "Pulsa «Escuchar con lector»: la imagen tiene un texto alternativo que cuenta lo que muestra."
                : "Pulsa «Escuchar con lector»: la imagen solo tiene el nombre del archivo y no dice nada útil.",
                escuchar).conSalida(voz::detener);
    }

    private static HBox barra(String nombre, double proporcion) {
        var relleno = new Region();
        relleno.getStyleClass().add("demo-barra");
        relleno.setMinWidth(150 * proporcion);
        relleno.setPrefWidth(150 * proporcion);
        relleno.setMaxWidth(150 * proporcion);
        var etiqueta = texto(nombre, "demo-texto");
        etiqueta.setMinWidth(100);
        var fila = new HBox(8, etiqueta, relleno);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    static Demo subtitulosVideos(boolean cumple) {
        var reproductor = new Reproductor(LECCION, List.of(escena("mdi2p-pizza")), true, false);
        var escenario = escenario(reproductor.vista());
        if (!cumple) {
            return Demo.de(escenario, "Reproduce el video: tiene voz, pero no hay forma de ver lo que dice.",
                    reproductor.boton()).conSalida(reproductor::detener);
        }
        Button cc = boton("Activar subtítulos", () -> { });
        cc.setGraphic(Iconos.icono("mdi2c-closed-caption-outline", "icono-boton"));
        cc.setContentDisplay(ContentDisplay.LEFT);
        cc.setOnAction(evento -> {
            reproductor.subtitulos(!reproductor.subtitulos());
            cc.setText(reproductor.subtitulos() ? "Quitar subtítulos" : "Activar subtítulos");
        });
        reproductor.controles().getChildren().add(cc);
        return Demo.de(escenario, "Reproduce el video y activa los subtítulos: el texto sigue lo que se dice.",
                reproductor.boton(), cc).conSalida(reproductor::detener);
    }

    static Demo transcripcionAudios(boolean cumple) {
        var reproductor = new Reproductor(LECCION, List.of(), false, false);
        var escenario = escenario(etiqueta("Audio de la clase"), reproductor.vista());
        if (!cumple) {
            return Demo.de(escenario, "Reproduce el audio: es la única forma de conocer la explicación.",
                    reproductor.boton()).conSalida(reproductor::detener);
        }
        var transcripcion = texto(String.join(" ", LECCION.stream().map(Sonidos::texto).toList()), "demo-transcripcion");
        transcripcion.setVisible(false);
        transcripcion.managedProperty().bind(transcripcion.visibleProperty());
        Button ver = boton("Ver transcripción", () -> { });
        ver.setGraphic(Iconos.icono("mdi2t-text-box-outline", "icono-boton"));
        ver.setContentDisplay(ContentDisplay.LEFT);
        ver.setOnAction(evento -> {
            transcripcion.setVisible(!transcripcion.isVisible());
            ver.setText(transcripcion.isVisible() ? "Ocultar transcripción" : "Ver transcripción");
        });
        reproductor.controles().getChildren().add(ver);
        escenario.getChildren().add(transcripcion);
        return Demo.de(escenario, "Reproduce el audio o abre la transcripción: puedes leer lo mismo que se dice.",
                reproductor.boton(), ver).conSalida(reproductor::detener);
    }

    static Demo alternativaVisualVideo(boolean cumple) {
        var clips = cumple ? List.of("agua-1", "agua-2", "agua-3", "agua-4")
                : List.of("vago-1", "vago-2", "vago-3", "vago-4");
        var escenas = List.of(escena("mdi2w-weather-sunny"), escena("mdi2c-cloud-outline"),
                escena("mdi2w-weather-pouring"), escena("mdi2w-waves"));
        var reproductor = new Reproductor(clips, escenas, true, true);
        var escenario = escenario(etiqueta("Video: el ciclo del agua"), reproductor.vista());
        return Demo.de(escenario, cumple
                ? "Reproduce el video y mira la imagen: la narración describe cada cosa que aparece, "
                        + "así no depende de verla."
                : "Reproduce el video y mira la imagen: la narración dice «miren esto» y no explica qué se ve. "
                        + "Quien no ve la pantalla se queda sin la información.",
                reproductor.boton()).conSalida(reproductor::detener);
    }

    private static Node escena(String icono) {
        var contenedor = new VBox(Iconos.icono(icono, "icono-demo-video"));
        contenedor.setAlignment(Pos.CENTER);
        return contenedor;
    }
}
