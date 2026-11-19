package mx.aulaaccesible.ui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static mx.aulaaccesible.ui.Controles.boton;
import static mx.aulaaccesible.ui.Controles.caja;
import static mx.aulaaccesible.ui.Controles.texto;

/**
 * Caja de pruebas de un {@link Demo}.
 *
 * <ul>
 *   <li><b>Inactiva:</b> el ejemplo se ve pero no recibe ratón ni foco; en la guía solo hay una parada de Tab, el
 *       botón «Probar ejemplo». Un clic sobre el ejemplo también lo activa.</li>
 *   <li><b>Activa:</b> Tab y Mayús + Tab recorren solo el orden del ejemplo y terminan en «Salir del ejemplo»;
 *       las flechas no mueven el foco de la interfaz (cada ejemplo decide qué hacen en {@link Demo#flechas()}). Esc o «Salir del ejemplo» desactivan la caja y devuelven el
 *       foco a «Probar ejemplo». Si el foco se va a otro sitio (por ejemplo, con un clic fuera) también se
 *       desactiva, así el ejemplo nunca es una trampa de teclado.</li>
 * </ul>
 */
final class Ejemplo {
    /** Marca (en {@code getProperties()}) de un nodo que el ejemplo debe dejar fuera del teclado a propósito. */
    static final String SIN_TECLADO = "sin-teclado";

    private final Demo demo;
    private final VBox caja;
    private final Button accion;
    private final Label instruccion;
    private final Set<Node> alcanzables = new LinkedHashSet<>();
    private boolean activo;
    private boolean enTransicion;

    private Ejemplo(Demo demo) {
        this.demo = demo;
        this.accion = boton("Probar ejemplo", "mdi2p-play", "secundario", this::alternar);
        this.instruccion = texto(demo.instruccion(), "instruccion-ejemplo");
        instruccion.setVisible(false);
        instruccion.managedProperty().bind(instruccion.visibleProperty());
        this.caja = caja(8, "ejemplo", demo.vista(), accion, instruccion);

        recoger(demo.vista(), alcanzables);
        alcanzables.forEach(nodo -> nodo.setFocusTraversable(false));
        demo.vista().setMouseTransparent(true);

        caja.addEventFilter(MouseEvent.MOUSE_CLICKED, evento -> {
            if (!activo && !esDescendiente(accion, evento.getTarget())) {
                activar();
            }
        });
        // Un filtro (se ejecuta antes que el control): los botones y casillas de JavaFX mueven el foco con Tab y con
        // las flechas por su cuenta y consumirían el evento antes de que lo viera cualquier manejador.
        caja.addEventFilter(KeyEvent.KEY_PRESSED, evento -> {
            if (!activo || evento.isControlDown() || evento.isAltDown() || evento.isMetaDown()) {
                return;
            }
            switch (evento.getCode()) {
                case TAB -> {
                    evento.consume();
                    moverFoco(evento.isShiftDown() ? -1 : 1);
                }
                case UP, DOWN, LEFT, RIGHT -> {
                    // Un campo de texto o un deslizador usan las flechas para lo suyo (cursor, valor).
                    if (!(evento.getTarget() instanceof TextInputControl) && !(evento.getTarget() instanceof Slider)) {
                        evento.consume();
                        demo.flechas().accept(evento.getCode());
                    }
                }
                default -> { }
            }
        });
        // Un manejador (no un filtro): los controles del ejemplo que usan Esc (un menú) lo consumen antes.
        caja.addEventHandler(KeyEvent.KEY_PRESSED, evento -> {
            if (activo && !evento.isConsumed() && evento.getCode() == KeyCode.ESCAPE) {
                evento.consume();
                desactivar(true, true);
            }
        });
        ChangeListener<Node> alCambiarFoco = (obs, antes, dueno) -> revisarFoco(dueno);
        caja.sceneProperty().addListener((obs, antes, escena) -> {
            if (antes != null) {
                antes.focusOwnerProperty().removeListener(alCambiarFoco);
            }
            if (escena != null) {
                escena.focusOwnerProperty().addListener(alCambiarFoco);
            } else if (activo) {
                desactivar(false, false);
            }
        });
    }

    /**
     * Si el foco pasa a algo que no es del ejemplo (por ejemplo, con un clic fuera), el ejemplo se desactiva. Se
     * mira el nodo con foco de la escena y no el foco de la ventana, así cambiar de aplicación no lo cierra. Se
     * comprueba después porque un ejemplo que cambia de paso mueve el foco en el mismo instante.
     */
    private void revisarFoco(Node dueno) {
        if (activo && !enTransicion && !esDescendiente(caja, dueno)) {
            Platform.runLater(() -> {
                var escena = caja.getScene();
                if (activo && !enTransicion && escena != null && !esDescendiente(caja, escena.getFocusOwner())) {
                    desactivar(false, true);
                }
            });
        }
    }

    /** Envuelve el ejemplo en su caja de pruebas: lista para colocar dentro de una ficha. */
    static VBox envolver(Demo demo) {
        return new Ejemplo(demo).caja;
    }

    private void alternar() {
        if (activo) {
            desactivar(true, true);
        } else {
            activar();
        }
    }

    private void activar() {
        enTransicion = true;
        activo = true;
        caja.getStyleClass().add("ejemplo-activo");
        demo.vista().setMouseTransparent(false);
        alcanzables.forEach(nodo -> nodo.setFocusTraversable(true));
        presentarAccion();
        instruccion.setVisible(true);
        Movimiento.entrar(instruccion);
        Sonidos.interfaz("ejemplo-entrar");
        demo.alEntrar().run();
        var ciclo = ciclo();
        ciclo.get(0).requestFocus();
        Platform.runLater(() -> enTransicion = false);
    }

    private void desactivar(boolean devolverFoco, boolean conSonido) {
        if (!activo) {
            return;
        }
        activo = false;
        caja.getStyleClass().remove("ejemplo-activo");
        demo.alSalir().run();
        demo.vista().setMouseTransparent(true);
        // Se vuelve a recorrer: el ejemplo pudo crear controles nuevos mientras estaba activo.
        recoger(demo.vista(), alcanzables);
        alcanzables.forEach(nodo -> nodo.setFocusTraversable(false));
        presentarAccion();
        instruccion.setVisible(false);
        if (conSonido) {
            Sonidos.interfaz("ejemplo-salir");
        }
        if (devolverFoco) {
            accion.requestFocus();
        }
    }

    private void presentarAccion() {
        accion.setText(activo ? "Salir del ejemplo" : "Probar ejemplo");
        accion.setGraphic(Iconos.icono(activo ? "mdi2e-exit-to-app" : "mdi2p-play", "icono-boton"));
        accion.setContentDisplay(ContentDisplay.LEFT);
    }

    /** Controles del ejemplo en el orden de Tab; termina en el botón de salida salvo que el ejemplo atrape el foco. */
    private List<Node> ciclo() {
        var lista = new ArrayList<>(demo.orden().get());
        if (lista.isEmpty() || !demo.atrapa().getAsBoolean()) {
            lista.add(accion);
        }
        return lista;
    }

    private void moverFoco(int paso) {
        var lista = ciclo();
        Node dueno = caja.getScene() == null ? null : caja.getScene().getFocusOwner();
        int actual = -1;
        for (int i = 0; i < lista.size() && actual < 0; i++) {
            if (esDescendiente(lista.get(i), dueno)) {
                actual = i;
            }
        }
        int siguiente = actual < 0 ? (paso > 0 ? 0 : lista.size() - 1) : Math.floorMod(actual + paso, lista.size());
        lista.get(siguiente).requestFocus();
    }

    /** Cierto si {@code nodo} es {@code candidato} o uno de sus ancestros. */
    private static boolean esDescendiente(Node nodo, Object candidato) {
        for (Object actual = candidato; actual instanceof Node actualNodo; actual = actualNodo.getParent()) {
            if (actual == nodo) {
                return true;
            }
        }
        return false;
    }

    /** Reúne los controles que Tab alcanzaría, salvo los que el ejemplo deja sin teclado a propósito. */
    private static void recoger(Node nodo, Set<Node> destino) {
        if (nodo.isFocusTraversable() && !nodo.getProperties().containsKey(SIN_TECLADO)) {
            destino.add(nodo);
        }
        if (nodo instanceof Parent padre) {
            padre.getChildrenUnmodifiable().forEach(hijo -> recoger(hijo, destino));
        }
    }
}
