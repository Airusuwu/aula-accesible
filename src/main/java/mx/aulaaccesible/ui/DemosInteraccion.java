package mx.aulaaccesible.ui;

import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;

import java.util.ArrayList;
import java.util.List;

import static mx.aulaaccesible.ui.Controles.texto;
import static mx.aulaaccesible.ui.Piezas.boton;
import static mx.aulaaccesible.ui.Piezas.campo;
import static mx.aulaaccesible.ui.Piezas.escenario;
import static mx.aulaaccesible.ui.Piezas.etiqueta;
import static mx.aulaaccesible.ui.Piezas.nota;

/** Ejemplos del área «Interacción y control». */
final class DemosInteraccion {
    private static final int SEGUNDOS = 20;

    private DemosInteraccion() {}

    static Demo tiempoAjustable(boolean cumple) {
        var mensaje = new Piezas.Mensaje();
        var restante = new int[]{SEGUNDOS};
        var sinLimite = new boolean[]{false};
        var reloj = nota("");
        var respuesta = campo("Tu respuesta");
        var ampliar = boton("Ampliar tiempo", () -> { });
        var quitar = boton("Quitar límite", () -> { });
        var reiniciar = boton("Reiniciar", () -> { });
        Runnable actualizar = () -> reloj.setText(sinLimite[0] ? "Sin límite de tiempo."
                : String.format("Tiempo restante: 00:%02d", Math.max(0, restante[0])));
        Timeline[] temporizador = new Timeline[1];
        Runnable agotar = () -> {
            temporizador[0].stop();
            respuesta.setDisable(true);
            ampliar.setDisable(true);
            quitar.setDisable(true);
            Sonidos.efecto("demo-tiempo-agotado");
            mensaje.error("Tiempo agotado. La actividad se cerró y no guardó tu respuesta.");
        };
        temporizador[0] = Movimiento.reloj(1, () -> {
            if (sinLimite[0]) {
                return;
            }
            restante[0]--;
            actualizar.run();
            if (restante[0] <= 0) {
                agotar.run();
            }
        });
        Runnable empezar = () -> {
            restante[0] = SEGUNDOS;
            sinLimite[0] = false;
            respuesta.setDisable(false);
            respuesta.clear();
            ampliar.setDisable(false);
            quitar.setDisable(false);
            mensaje.ocultar();
            actualizar.run();
            temporizador[0].playFromStart();
        };
        ampliar.setOnAction(evento -> {
            restante[0] += 10;
            actualizar.run();
            mensaje.ok("Tiempo ampliado 10 segundos.");
        });
        quitar.setOnAction(evento -> {
            sinLimite[0] = true;
            actualizar.run();
            mensaje.ok("Límite de tiempo desactivado.");
        });
        reiniciar.setOnAction(evento -> {
            empezar.run();
            respuesta.requestFocus();
        });
        actualizar.run();
        var controles = new FlowPane(8, 8);
        if (cumple) {
            controles.getChildren().addAll(ampliar, quitar);
        }
        controles.getChildren().add(reiniciar);
        var escenario = escenario(etiqueta("¿Cuánto es 7 × 8?"), respuesta, reloj, controles, mensaje.nodo());
        var orden = new ArrayList<Node>(List.of(respuesta));
        if (cumple) {
            orden.addAll(List.of(ampliar, quitar));
        }
        orden.add(reiniciar);
        return Demo.de(escenario, cumple
                        ? "Mira bajar el tiempo y pulsa «Ampliar tiempo» o «Quitar límite» antes de que se acabe."
                        : "Mira bajar el tiempo: no hay forma de ampliarlo y, al llegar a cero, pierdes la actividad.",
                        orden.toArray(Node[]::new))
                .conEntrada(empezar).conSalida(() -> temporizador[0].stop());
    }

    private record Diapositiva(String icono, String texto) {}

    static Demo controlMovimiento(boolean cumple) {
        var diapositivas = List.of(new Diapositiva("mdi2w-weather-sunny", "Diapositiva 1: la mañana"),
                new Diapositiva("mdi2c-cloud-outline", "Diapositiva 2: la tarde"),
                new Diapositiva("mdi2w-weather-night", "Diapositiva 3: la noche"));
        var pantalla = new StackPane();
        pantalla.getStyleClass().add("demo-carrusel");
        var indice = new int[]{0};
        Runnable pintar = () -> {
            var actual = diapositivas.get(indice[0]);
            var contenido = new HBox(10, Iconos.icono(actual.icono(), "icono-demo-grande"),
                    texto(actual.texto(), "demo-texto"));
            contenido.setAlignment(Pos.CENTER_LEFT);
            pantalla.getChildren().setAll(contenido);
        };
        pintar.run();
        var reloj = Movimiento.reloj(2, () -> {
            indice[0] = (indice[0] + 1) % diapositivas.size();
            Movimiento.cambiarEscena(pantalla, pintar);
        });
        var escenario = escenario(etiqueta("Carrusel de la unidad"), pantalla);
        Runnable reiniciar = () -> {
            reloj.stop();
            indice[0] = 0;
            pintar.run();
        };
        if (!cumple) {
            return Demo.de(escenario, "Mira el carrusel: cambia solo, sin parar, y no hay ningún control para detenerlo.")
                    .conEntrada(reloj::play).conSalida(reiniciar);
        }
        Button pausar = boton("Pausar", () -> { });
        pausar.setGraphic(Iconos.icono("mdi2p-pause", "icono-boton"));
        pausar.setContentDisplay(ContentDisplay.LEFT);
        pausar.setOnAction(evento -> {
            boolean enMarcha = reloj.getStatus() == javafx.animation.Animation.Status.RUNNING;
            if (enMarcha) {
                reloj.pause();
            } else {
                reloj.play();
            }
            pausar.setText(enMarcha ? "Reanudar" : "Pausar");
            pausar.setGraphic(Iconos.icono(enMarcha ? "mdi2p-play" : "mdi2p-pause", "icono-boton"));
        });
        escenario.getChildren().add(pausar);
        return Demo.de(escenario, "Mira el carrusel y pulsa «Pausar» cuando quieras leer con calma.", pausar)
                .conEntrada(reloj::play)
                .conSalida(() -> {
                    reiniciar.run();
                    pausar.setText("Pausar");
                    pausar.setGraphic(Iconos.icono("mdi2p-pause", "icono-boton"));
                });
    }

    static Demo controlAudio(boolean cumple) {
        var jugador = new MediaPlayer[1];
        var mensaje = new Piezas.Mensaje();
        var escenario = escenario(etiqueta("Sección con música de fondo"), mensaje.nodo());
        Runnable arrancar = () -> {
            Sonidos.liberar(jugador[0]);
            jugador[0] = Sonidos.reproductor("demo-musica");
            if (jugador[0] != null) {
                jugador[0].setVolume(0.6);
                jugador[0].play();
            }
            mensaje.neutro("Está sonando música de fondo.");
        };
        Runnable parar = () -> {
            Sonidos.liberar(jugador[0]);
            jugador[0] = null;
        };
        if (!cumple) {
            return Demo.de(escenario, "Escucha: la música empieza sola y no hay forma de pausarla ni de bajarle el volumen.")
                    .conEntrada(arrancar).conSalida(parar);
        }
        Button pausa = boton("Pausar música", () -> { });
        pausa.setGraphic(Iconos.icono("mdi2p-pause", "icono-boton"));
        pausa.setContentDisplay(ContentDisplay.LEFT);
        var volumen = new Slider(0, 1, 0.6);
        volumen.setBlockIncrement(0.1);
        volumen.setAccessibleText("Volumen de la música");
        volumen.valueProperty().addListener((obs, antes, valor) -> {
            if (jugador[0] != null) {
                jugador[0].setVolume(valor.doubleValue());
            }
        });
        boolean[] enPausa = {false};
        pausa.setOnAction(evento -> {
            enPausa[0] = !enPausa[0];
            if (jugador[0] != null) {
                if (enPausa[0]) {
                    jugador[0].pause();
                } else {
                    jugador[0].play();
                }
            }
            pausa.setText(enPausa[0] ? "Reanudar música" : "Pausar música");
            pausa.setGraphic(Iconos.icono(enPausa[0] ? "mdi2p-play" : "mdi2p-pause", "icono-boton"));
            mensaje.neutro(enPausa[0] ? "Música en pausa." : "Está sonando música de fondo.");
        });
        escenario.getChildren().addAll(pausa, nota("Volumen"), volumen);
        return Demo.de(escenario, "Escucha la música y usa «Pausar música» o el volumen (flechas ← →) sin tocar el del equipo.",
                        pausa, volumen)
                .conEntrada(() -> {
                    enPausa[0] = false;
                    pausa.setText("Pausar música");
                    pausa.setGraphic(Iconos.icono("mdi2p-pause", "icono-boton"));
                    arrancar.run();
                })
                .conSalida(parar);
    }

    static Demo alternativaArrastre(boolean cumple) {
        var mensaje = new Piezas.Mensaje();
        var filas = new ArrayList<Label>();
        for (String paso : List.of("Hornear", "Servir", "Mezclar")) {
            var tarjeta = new Label(paso);
            tarjeta.setGraphic(Iconos.icono("mdi2d-drag-vertical", "icono-demo"));
            tarjeta.setGraphicTextGap(8);
            tarjeta.getStyleClass().add("demo-tarjeta");
            tarjeta.setMaxWidth(Double.MAX_VALUE);
            filas.add(tarjeta);
        }
        var lista = new Piezas.ListaLector(filas, indice -> { });
        Runnable revisar = () -> {
            var textos = filas.stream().map(Label::getText).toList();
            if (textos.equals(List.of("Mezclar", "Hornear", "Servir"))) {
                mensaje.ok("¡Orden correcto!");
            } else {
                mensaje.neutro("Ordena los pasos de la receta: mezclar, hornear y servir.");
            }
        };
        java.util.function.BiConsumer<Integer, Integer> mover = (desde, hasta) -> {
            if (desde.equals(hasta) || hasta < 0 || hasta >= filas.size()) {
                return;
            }
            var movida = filas.remove((int) desde);
            filas.add(hasta, movida);
            lista.vista().getChildren().setAll(filas);
            lista.elegir(hasta);
            revisar.run();
        };
        for (var tarjeta : filas) {
            tarjeta.setOnDragDetected(evento -> {
                var tablero = tarjeta.startDragAndDrop(TransferMode.MOVE);
                var contenido = new ClipboardContent();
                contenido.putString(String.valueOf(filas.indexOf(tarjeta)));
                tablero.setContent(contenido);
                evento.consume();
            });
            tarjeta.setOnDragOver(evento -> {
                if (evento.getDragboard().hasString()) {
                    evento.acceptTransferModes(TransferMode.MOVE);
                }
                evento.consume();
            });
            tarjeta.setOnDragDropped(evento -> {
                boolean hecho = evento.getDragboard().hasString();
                if (hecho) {
                    mover.accept(Integer.parseInt(evento.getDragboard().getString()), filas.indexOf(tarjeta));
                }
                evento.setDropCompleted(hecho);
                evento.consume();
            });
        }
        revisar.run();
        var escenario = escenario(etiqueta("Ordena los pasos de la receta"), lista.vista());
        if (!cumple) {
            Piezas.sinTeclado(lista.vista());
            escenario.getChildren().add(mensaje.nodo());
            return Demo.de(escenario, "Arrastra las tarjetas con el ratón para ordenarlas. Con el teclado no hay "
                    + "ninguna forma de hacerlo.");
        }
        var subir = boton("Subir", () -> mover.accept(lista.indice(), lista.indice() - 1));
        var bajar = boton("Bajar", () -> mover.accept(lista.indice(), lista.indice() + 1));
        subir.setGraphic(Iconos.icono("mdi2a-arrow-up", "icono-boton"));
        bajar.setGraphic(Iconos.icono("mdi2a-arrow-down", "icono-boton"));
        subir.setContentDisplay(ContentDisplay.LEFT);
        bajar.setContentDisplay(ContentDisplay.LEFT);
        escenario.getChildren().addAll(new FlowPane(8, 8, subir, bajar), mensaje.nodo());
        return Demo.de(escenario, "Arrastra una tarjeta, o elige una con ↑ ↓ y muévela con «Subir» y «Bajar»: "
                + "las dos formas funcionan.", lista.vista(), subir, bajar).conFlechas(lista::flecha);
    }
}
