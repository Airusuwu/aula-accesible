package mx.aulaaccesible.ui;

import javafx.scene.Node;
import javafx.scene.input.KeyCode;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Un ejemplo interactivo de «Así se cumple» o «Así no se cumple». {@link Ejemplo#envolver(Demo)} lo convierte en una
 * caja de pruebas: mientras está activa, Tab y las flechas se quedan dentro y no mueven el foco de la interfaz.
 *
 * @param vista       lo que se dibuja; los nodos con foco deben crearse sin ratón activo hasta que la caja se active
 * @param orden       controles en el orden en que Tab los recorre dentro del ejemplo. Es un {@link Supplier} porque
 *                    puede cambiar al interactuar (menús que se abren, pasos que se completan)
 * @param instruccion qué probar, visible mientras el ejemplo está activo
 * @param atrapa      si devuelve {@code true}, Tab da vueltas solo entre {@code orden} y no llega al botón «Salir
 *                    del ejemplo»: simula una trampa de teclado (solo Esc saca de ahí)
 * @param alEntrar    se ejecuta al activar la caja (arrancar audio, relojes…)
 * @param alSalir     se ejecuta al salir o al quitarse de la pantalla (detener audio, relojes…)
 * @param flechas     qué hace cada flecha (↑ ↓ ← →) dentro del ejemplo. Los botones y otros controles de JavaFX
 *                    mueven el foco con las flechas por su cuenta, así que {@link Ejemplo} las intercepta y las
 *                    entrega aquí; los campos de texto y los deslizadores conservan su comportamiento natural
 */
record Demo(Node vista, Supplier<List<Node>> orden, String instruccion, BooleanSupplier atrapa,
            Runnable alEntrar, Runnable alSalir, Consumer<KeyCode> flechas) {
    private static final Runnable NADA = () -> { };
    private static final Consumer<KeyCode> SIN_FLECHAS = tecla -> { };

    /** Demo con un orden de Tab fijo. */
    static Demo de(Node vista, String instruccion, Node... orden) {
        List<Node> fijo = List.of(orden);
        return new Demo(vista, () -> fijo, instruccion, () -> false, NADA, NADA, SIN_FLECHAS);
    }

    /** Demo cuyo orden de Tab depende de su estado. */
    static Demo dinamico(Node vista, String instruccion, Supplier<List<Node>> orden) {
        return new Demo(vista, orden, instruccion, () -> false, NADA, NADA, SIN_FLECHAS);
    }

    Demo conTrampa(BooleanSupplier atrapa) {
        return new Demo(vista, orden, instruccion, atrapa, alEntrar, alSalir, flechas);
    }

    Demo conEntrada(Runnable alEntrar) {
        return new Demo(vista, orden, instruccion, atrapa, alEntrar, alSalir, flechas);
    }

    Demo conSalida(Runnable alSalir) {
        return new Demo(vista, orden, instruccion, atrapa, alEntrar, alSalir, flechas);
    }

    Demo conFlechas(Consumer<KeyCode> flechas) {
        return new Demo(vista, orden, instruccion, atrapa, alEntrar, alSalir, flechas);
    }
}
