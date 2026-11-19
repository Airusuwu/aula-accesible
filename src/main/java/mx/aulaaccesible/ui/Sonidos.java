package mx.aulaaccesible.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sonidos de la aplicación. Los .mp3 se generan una vez con {@code scripts/generar-sonidos.ps1} (ElevenLabs) a
 * partir de {@code sonidos/catalogo.json} y viajan dentro del jar: la app no necesita clave ni conexión.
 *
 * <p>Los sonidos de la interfaz respetan la preferencia «Sonidos». El audio de los ejemplos es contenido que la
 * persona inicia a propósito, así que suena aunque esa preferencia esté apagada. Si no hay dispositivo de audio,
 * todo falla en silencio: ningún sonido es nunca la única señal de nada.
 */
final class Sonidos {
    /** Una entrada del catálogo. En las voces {@code texto} es lo que se dice; en los efectos, su descripción. */
    record Entrada(String id, String tipo, String texto, Double duracion, Boolean bucle) {}

    private static final String RUTA = "/mx/aulaaccesible/sonidos/";
    private static final double VOLUMEN_INTERFAZ = 0.5;
    private static final Map<String, AudioClip> CLIPS = new HashMap<>();
    private static List<Entrada> catalogo;

    private Sonidos() {}

    /** Todas las entradas de {@code catalogo.json}. */
    static synchronized List<Entrada> catalogo() {
        if (catalogo == null) {
            try (InputStream entrada = Sonidos.class.getResourceAsStream(RUTA + "catalogo.json")) {
                catalogo = List.of(new ObjectMapper().readValue(entrada, Entrada[].class));
            } catch (IOException | RuntimeException e) {
                catalogo = List.of();
            }
        }
        return catalogo;
    }

    /** Texto de una voz, que sirve de subtítulo y de transcripción. Vacío si el id no existe. */
    static String texto(String id) {
        return catalogo().stream().filter(e -> e.id().equals(id)).map(Entrada::texto).findFirst().orElse("");
    }

    static URL recurso(String id) {
        return Sonidos.class.getResource(RUTA + id + ".mp3");
    }

    /** Sonido breve de la interfaz; no hace nada si «Sonidos» está apagado. */
    static void interfaz(String id) {
        if (Preferencias.sonidos().get()) {
            efecto(id);
        }
    }

    /** Efecto de un ejemplo (por ejemplo, «tiempo agotado»): contenido pedido a propósito, no depende de «Sonidos». */
    static void efecto(String id) {
        try {
            AudioClip clip = CLIPS.get(id);
            if (clip == null) {
                URL url = recurso(id);
                if (url == null) {
                    return;
                }
                clip = new AudioClip(url.toExternalForm());
                clip.setVolume(VOLUMEN_INTERFAZ);
                CLIPS.put(id, clip);
            }
            clip.play();
        } catch (RuntimeException e) {
            // Sin dispositivo de audio o con un códec no disponible: la interfaz sigue igual, sin sonido.
        }
    }

    /**
     * Reproductor de un clip de los ejemplos, en bucle si así lo marca el catálogo. Devuelve {@code null} si el
     * clip no existe o no se puede abrir. Quien lo pide debe llamar a {@code dispose()} al terminar.
     */
    static MediaPlayer reproductor(String id) {
        try {
            URL url = recurso(id);
            if (url == null) {
                return null;
            }
            var jugador = new MediaPlayer(new Media(url.toExternalForm()));
            jugador.setOnError(() -> { });
            boolean bucle = catalogo().stream().anyMatch(e -> e.id().equals(id) && Boolean.TRUE.equals(e.bucle()));
            if (bucle) {
                jugador.setCycleCount(MediaPlayer.INDEFINITE);
            }
            return jugador;
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** Detiene y libera un reproductor sin lanzar errores; acepta {@code null}. */
    static void liberar(MediaPlayer jugador) {
        if (jugador == null) {
            return;
        }
        try {
            jugador.stop();
            jugador.dispose();
        } catch (RuntimeException e) {
            // Ya estaba liberado.
        }
    }
}
