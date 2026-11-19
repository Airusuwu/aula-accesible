package mx.aulaaccesible.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;

import java.util.List;
import java.util.function.IntConsumer;

import static mx.aulaaccesible.ui.Controles.caja;
import static mx.aulaaccesible.ui.Controles.texto;

/** Bloques comunes de las demos de los ejemplos: escenario, botones, campos, mensajes y listas de lector. */
final class Piezas {
    private Piezas() {}

    /** Marco de un ejemplo. */
    static VBox escenario(Node... contenido) {
        return caja(10, "muestra-demo", contenido);
    }

    static Button boton(String nombre, Runnable accion) {
        return Controles.boton(nombre, "demo-boton", accion);
    }

    /** Deja el nodo fuera del recorrido con Tab, a propósito: así se enseña una barrera de teclado. */
    static <T extends Node> T sinTeclado(T nodo) {
        nodo.getProperties().put(Ejemplo.SIN_TECLADO, Boolean.TRUE);
        nodo.setFocusTraversable(false);
        return nodo;
    }

    static TextField campo(String marcador) {
        var campo = new TextField();
        campo.setPromptText(marcador);
        campo.getStyleClass().add("demo-entrada");
        return campo;
    }

    static Label etiqueta(String contenido) {
        return texto(contenido, "demo-etiqueta");
    }

    static Label nota(String contenido) {
        return texto(contenido, "demo-texto");
    }

    /** Correo mínimo: algo, una arroba, algo, un punto y algo. */
    static boolean correoValido(String correo) {
        return correo.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    }

    /** Mensaje con icono y texto que aparece al interactuar: de éxito o de error. */
    static final class Mensaje {
        private final HBox fila = new HBox(6);
        private final Label contenido = texto("", "demo-texto");

        Mensaje() {
            fila.setAlignment(Pos.TOP_LEFT);
            fila.setVisible(false);
            fila.managedProperty().bind(fila.visibleProperty());
        }

        Node nodo() { return fila; }

        void ok(String texto) { mostrar(texto, "mdi2c-check-circle", "icono-demo-ok", "demo-ok-texto"); }

        void error(String texto) { mostrar(texto, "mdi2a-alert-circle", "icono-demo-error", "demo-error-texto"); }

        void neutro(String texto) { mostrar(texto, "mdi2i-information-outline", "icono-demo", "demo-texto"); }

        void ocultar() { fila.setVisible(false); }

        private void mostrar(String texto, String icono, String estiloIcono, String estiloTexto) {
            contenido.setText(texto);
            contenido.getStyleClass().setAll("label", estiloTexto);
            fila.getChildren().setAll(Iconos.icono(icono, estiloIcono), contenido);
            fila.setVisible(true);
        }
    }

    /**
     * Lista que se recorre con las flechas ↑ ↓ (como una lista de encabezados o de enlaces en un lector de
     * pantalla). Es una sola parada de Tab; la fila actual se resalta y se avisa con {@code alCambiar}.
     */
    static final class ListaLector {
        private final VBox vista;
        private final List<? extends Node> filas;
        private final IntConsumer alCambiar;
        private int indice;

        ListaLector(List<? extends Node> filas, IntConsumer alCambiar) {
            this.filas = filas;
            this.alCambiar = alCambiar;
            this.vista = caja(4, "lista-lector");
            vista.getChildren().addAll(filas);
            vista.setFocusTraversable(true);
            vista.addEventHandler(KeyEvent.KEY_PRESSED, evento -> {
                switch (evento.getCode()) {
                    case DOWN -> { mover(1); evento.consume(); }
                    case UP -> { mover(-1); evento.consume(); }
                    case HOME -> { elegir(0); evento.consume(); }
                    case END -> { elegir(filas.size() - 1); evento.consume(); }
                    default -> { }
                }
            });
            for (Node fila : filas) {
                // El índice se calcula al hacer clic: la lista puede haberse reordenado.
                fila.addEventHandler(MouseEvent.MOUSE_PRESSED, evento -> {
                    vista.requestFocus();
                    elegir(filas.indexOf(fila));
                });
            }
            marcar();
        }

        VBox vista() { return vista; }

        int indice() { return indice; }

        void mover(int paso) { elegir(Math.max(0, Math.min(filas.size() - 1, indice + paso))); }

        /** ↑ ↓ cambian la fila actual; para pasarlas desde {@link Demo#conFlechas(java.util.function.Consumer)}. */
        void flecha(KeyCode tecla) {
            if (tecla == KeyCode.DOWN) {
                mover(1);
            } else if (tecla == KeyCode.UP) {
                mover(-1);
            }
        }

        void elegir(int nuevo) {
            indice = nuevo;
            marcar();
            alCambiar.accept(indice);
        }

        private void marcar() {
            for (int i = 0; i < filas.size(); i++) {
                var estilos = filas.get(i).getStyleClass();
                estilos.remove("demo-actual");
                if (i == indice) {
                    estilos.add("demo-actual");
                }
            }
        }
    }

    /** Voz del «lector de pantalla» de un ejemplo: reproduce un clip a la vez y se detiene al salir. */
    static final class Voz {
        private MediaPlayer jugador;

        void decir(String clip) {
            detener();
            var actual = Sonidos.reproductor(clip);
            if (actual == null) {
                return;
            }
            jugador = actual;
            actual.setOnEndOfMedia(() -> {
                if (jugador == actual) {
                    detener();
                }
            });
            actual.play();
        }

        void detener() {
            Sonidos.liberar(jugador);
            jugador = null;
        }
    }

    /** Asigna una tecla a una acción dentro de un nodo; consume el evento. */
    static void alPulsar(Node nodo, KeyCode tecla, Runnable accion) {
        nodo.addEventHandler(KeyEvent.KEY_PRESSED, evento -> {
            if (evento.getCode() == tecla) {
                accion.run();
                evento.consume();
            }
        });
    }
}
