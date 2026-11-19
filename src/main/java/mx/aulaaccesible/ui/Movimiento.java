package mx.aulaaccesible.ui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.util.List;
import java.util.function.IntConsumer;

/**
 * Animaciones breves de la interfaz. Todas respetan la preferencia «Reducir movimiento»:
 * si está activa, el nodo queda directamente en su estado final y no se anima nada.
 */
final class Movimiento {
    private static final Interpolator SALIDA = Interpolator.SPLINE(0.23, 1, 0.32, 1);

    private Movimiento() {}

    static boolean reducido() { return Preferencias.reducirMovimiento().get(); }

    /** Entrada de una pantalla completa: aparece con un fundido y sube unos píxeles. */
    static void entrar(Node nodo) {
        entrar(nodo, 0, 12, 200);
    }

    /** Entrada lateral al cambiar de criterio: {@code direccion} 1 = viene de la derecha, -1 = de la izquierda. */
    static void entrarLateral(Node nodo, int direccion) {
        entrar(nodo, 10 * Integer.signum(direccion), 0, 160);
    }

    private static void entrar(Node nodo, double desdeX, double desdeY, double milisegundos) {
        if (reducido()) {
            restaurar(nodo);
            return;
        }
        var duracion = Duration.millis(milisegundos);
        nodo.setOpacity(0);
        nodo.setTranslateX(desdeX);
        nodo.setTranslateY(desdeY);
        var fundido = new FadeTransition(duracion, nodo);
        fundido.setToValue(1);
        fundido.setInterpolator(SALIDA);
        var desplazamiento = new TranslateTransition(duracion, nodo);
        desplazamiento.setToX(0);
        desplazamiento.setToY(0);
        desplazamiento.setInterpolator(SALIDA);
        var conjunto = new ParallelTransition(fundido, desplazamiento);
        // Si la animación se interrumpe, el nodo no debe quedar semitransparente.
        conjunto.setOnFinished(evento -> restaurar(nodo));
        conjunto.play();
    }

    private static void restaurar(Node nodo) {
        nodo.setOpacity(1);
        nodo.setTranslateX(0);
        nodo.setTranslateY(0);
    }

    /** Lleva la barra de progreso al nuevo valor con una transición suave. */
    static void progreso(ProgressBar barra, double valor) {
        if (reducido() || barra.getScene() == null) {
            barra.setProgress(valor);
            return;
        }
        DoubleProperty propiedad = barra.progressProperty();
        new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(propiedad, valor, SALIDA))).play();
    }

    /** Pequeño rebote del icono de estado cuando se responde un criterio. */
    static void rebotar(Node nodo) {
        if (reducido()) {
            return;
        }
        var crece = new ScaleTransition(Duration.millis(75), nodo);
        crece.setToX(1.15);
        crece.setToY(1.15);
        var vuelve = new ScaleTransition(Duration.millis(75), nodo);
        vuelve.setToX(1);
        vuelve.setToY(1);
        var secuencia = new SequentialTransition(crece, vuelve);
        secuencia.setOnFinished(evento -> {
            nodo.setScaleX(1);
            nodo.setScaleY(1);
        });
        secuencia.play();
    }

    /** Entrada escalonada de varias tarjetas (40 ms entre una y otra). */
    static void escalonar(List<? extends Node> nodos) {
        if (reducido()) {
            nodos.forEach(Movimiento::restaurar);
            return;
        }
        int posicion = 0;
        for (Node nodo : nodos) {
            nodo.setOpacity(0);
            var pausa = new PauseTransition(Duration.millis(40L * posicion++));
            pausa.setOnFinished(evento -> entrar(nodo, 0, 10, 220));
            pausa.play();
        }
    }

    /**
     * Cuenta de 0 a {@code total} en el texto de un rótulo. El texto accesible ya tiene el valor final
     * desde el inicio, así que un lector de pantalla nunca lee un número intermedio.
     */
    static void contar(Labeled rotulo, int total, IntConsumer alCambiar) {
        rotulo.setAccessibleText(String.valueOf(total));
        if (reducido() || total == 0) {
            alCambiar.accept(total);
            return;
        }
        var cuenta = new Timeline();
        cuenta.getKeyFrames().add(new KeyFrame(Duration.millis(600), new KeyValue(
                new javafx.beans.property.SimpleDoubleProperty() {
                    @Override public void set(double valor) {
                        super.set(valor);
                        alCambiar.accept((int) Math.round(valor));
                    }
                }, (double) total, SALIDA)));
        alCambiar.accept(0);
        cuenta.setOnFinished(evento -> alCambiar.accept(total));
        cuenta.play();
    }

    /**
     * Cambio de escena dentro de un ejemplo: el contenido se desvanece, {@code cambio} lo sustituye y vuelve a
     * aparecer (100 ms + 100 ms). Con «Reducir movimiento» el contenido cambia sin transición.
     */
    static void cambiarEscena(Node nodo, Runnable cambio) {
        if (reducido() || nodo.getScene() == null) {
            cambio.run();
            return;
        }
        var sale = new FadeTransition(Duration.millis(100), nodo);
        sale.setToValue(0);
        sale.setOnFinished(evento -> {
            cambio.run();
            var entra = new FadeTransition(Duration.millis(100), nodo);
            entra.setToValue(1);
            entra.setOnFinished(fin -> nodo.setOpacity(1));
            entra.play();
        });
        sale.play();
    }

    /**
     * Reloj que repite {@code tic} cada {@code segundos} hasta que se detenga (cuenta regresiva, carrusel). No es
     * una animación visual, así que no depende de «Reducir movimiento»: es justo el contenido que se evalúa.
     * Quien lo crea debe llamar a {@code play()} y a {@code stop()}.
     */
    static Timeline reloj(double segundos, Runnable tic) {
        var reloj = new Timeline(new KeyFrame(Duration.seconds(segundos), evento -> tic.run()));
        reloj.setCycleCount(Timeline.INDEFINITE);
        return reloj;
    }

    /** Espera {@code segundos} y ejecuta {@code alTerminar}. Quien la crea debe llamar a {@code play()}. */
    static PauseTransition esperar(double segundos, Runnable alTerminar) {
        var pausa = new PauseTransition(Duration.seconds(segundos));
        pausa.setOnFinished(evento -> alTerminar.run());
        return pausa;
    }

    /** Hace que un control se hunda un poco mientras se presiona. */
    static void presionable(Node nodo) {
        nodo.addEventHandler(MouseEvent.MOUSE_PRESSED, evento -> escalar(nodo, 0.97));
        nodo.addEventHandler(MouseEvent.MOUSE_RELEASED, evento -> escalar(nodo, 1));
    }

    private static void escalar(Node nodo, double valor) {
        if (reducido()) {
            return;
        }
        var escala = new ScaleTransition(Duration.millis(100), nodo);
        escala.setToX(valor);
        escala.setToY(valor);
        escala.play();
    }
}
