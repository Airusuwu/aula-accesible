package mx.aulaaccesible.ui;

import javafx.animation.Animation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;

import java.util.List;

import static mx.aulaaccesible.ui.Controles.caja;
import static mx.aulaaccesible.ui.Controles.texto;

/**
 * Reproductor de ejemplo: un «video» (escenas dibujadas con nodos que cambian frase a frase) o un audio, con
 * narración generada (los clips del catálogo de {@link Sonidos}) y subtítulos opcionales por frase. Todo se detiene
 * con {@link #detener()}, que el ejemplo llama al salir. Si no hay audio disponible, cada frase dura 3 segundos
 * para que el ejemplo siga funcionando solo con la imagen y los subtítulos.
 */
final class Reproductor {
    private final List<String> clips;
    private final List<Node> escenas;
    private final boolean conImagen;
    private final StackPane pantalla = new StackPane();
    private final Label subtitulo = texto("", "demo-subtitulo");
    private final Label estado = texto("Listo para reproducir.", "demo-texto");
    private final Button reproducir;
    private final VBox vista;
    private final FlowPane controles;
    private boolean subtitulosActivos;
    private int indice = -1;
    private boolean reproduciendo;
    private MediaPlayer jugador;
    private Animation respaldo;

    /**
     * @param clips            ids de los clips de voz, uno por frase
     * @param escenas          imágenes de la pantalla (una por frase; si hay una sola, no cambia); ignoradas en un audio
     * @param conImagen        {@code true} para un video, {@code false} para un audio
     * @param subtitulosActivos si los subtítulos están encendidos desde el inicio
     */
    Reproductor(List<String> clips, List<Node> escenas, boolean conImagen, boolean subtitulosActivos) {
        this.clips = clips;
        this.escenas = escenas;
        this.conImagen = conImagen;
        this.subtitulosActivos = subtitulosActivos;
        this.reproducir = Piezas.boton("Reproducir", this::alternar);
        presentarBoton();
        subtitulo.setVisible(false);
        subtitulo.managedProperty().bind(subtitulo.visibleProperty());
        subtitulo.setMaxWidth(Double.MAX_VALUE);

        vista = caja(8, "reproductor");
        if (conImagen) {
            pantalla.getStyleClass().add("demo-video");
            pantalla.setAlignment(Pos.CENTER);
            if (!escenas.isEmpty()) {
                pantalla.getChildren().setAll(escenas.get(0));
            }
            vista.getChildren().addAll(pantalla, subtitulo);
        } else {
            var fila = new HBox(10, Iconos.icono("mdi2m-music-note", "icono-demo"), estado);
            fila.getStyleClass().add("demo-audio");
            fila.setAlignment(Pos.CENTER_LEFT);
            vista.getChildren().addAll(fila, subtitulo);
        }
        controles = new FlowPane(8, 8, reproducir);
        vista.getChildren().add(controles);
    }

    Node vista() { return vista; }

    /** Fila de controles bajo el reproductor: empieza con «Reproducir» y el ejemplo puede añadir más. */
    FlowPane controles() { return controles; }

    Button boton() { return reproducir; }

    boolean subtitulos() { return subtitulosActivos; }

    void subtitulos(boolean activos) {
        subtitulosActivos = activos;
        actualizarSubtitulo();
    }

    boolean reproduciendo() { return reproduciendo; }

    /** Detiene la reproducción, libera el audio y deja el reproductor al principio. */
    void detener() {
        liberar();
        indice = -1;
        reproduciendo = false;
        subtitulo.setText("");
        actualizarSubtitulo();
        estado.setText("Listo para reproducir.");
        if (conImagen && !escenas.isEmpty()) {
            pantalla.getChildren().setAll(escenas.get(0));
        }
        presentarBoton();
    }

    private void alternar() {
        if (indice < 0) {
            reproducirFrase(0);
        } else if (reproduciendo) {
            reproduciendo = false;
            if (jugador != null) {
                jugador.pause();
            }
            if (respaldo != null) {
                respaldo.pause();
            }
            estado.setText("En pausa.");
            presentarBoton();
        } else {
            reproduciendo = true;
            if (jugador != null) {
                jugador.play();
            }
            if (respaldo != null) {
                respaldo.play();
            }
            estado.setText(textoEstado());
            presentarBoton();
        }
    }

    private void reproducirFrase(int frase) {
        liberar();
        indice = frase;
        reproduciendo = true;
        mostrarEscena(frase);
        subtitulo.setText(Sonidos.texto(clips.get(frase)));
        actualizarSubtitulo();
        estado.setText(textoEstado());
        presentarBoton();
        jugador = Sonidos.reproductor(clips.get(frase));
        if (jugador == null) {
            usarRespaldo();
            return;
        }
        var actual = jugador;
        actual.setOnEndOfMedia(this::siguiente);
        actual.setOnError(() -> {
            if (jugador == actual) {
                Sonidos.liberar(actual);
                jugador = null;
                usarRespaldo();
            }
        });
        actual.play();
    }

    private void usarRespaldo() {
        respaldo = Movimiento.esperar(3, this::siguiente);
        respaldo.play();
    }

    private void siguiente() {
        if (indice + 1 < clips.size()) {
            reproducirFrase(indice + 1);
        } else {
            detener();
        }
    }

    private void liberar() {
        Sonidos.liberar(jugador);
        jugador = null;
        if (respaldo != null) {
            respaldo.stop();
            respaldo = null;
        }
    }

    private void mostrarEscena(int frase) {
        if (!conImagen || escenas.isEmpty()) {
            return;
        }
        Node nueva = escenas.get(Math.min(frase, escenas.size() - 1));
        if (pantalla.getChildren().isEmpty() || pantalla.getChildren().get(0) != nueva) {
            Movimiento.cambiarEscena(pantalla, () -> pantalla.getChildren().setAll(nueva));
        }
    }

    private void actualizarSubtitulo() {
        subtitulo.setVisible(subtitulosActivos && indice >= 0 && !subtitulo.getText().isBlank());
    }

    private String textoEstado() {
        return "Reproduciendo, frase " + (indice + 1) + " de " + clips.size() + ".";
    }

    private void presentarBoton() {
        reproducir.setText(reproduciendo ? "Pausar" : "Reproducir");
        reproducir.setGraphic(Iconos.icono(reproduciendo ? "mdi2p-pause" : "mdi2p-play", "icono-boton"));
        reproducir.setContentDisplay(ContentDisplay.LEFT);
    }
}
