package mx.aulaaccesible.ui;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import mx.aulaaccesible.model.Criterio;
import mx.aulaaccesible.model.Evaluacion;
import mx.aulaaccesible.model.Valor;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static mx.aulaaccesible.ui.Controles.*;

/**
 * Pantalla del cuestionario: barra lateral de áreas y criterios, la pregunta al centro y, si el ancho de la
 * ventana alcanza, la guía «Cómo evaluar este criterio» como panel fijo a la derecha. Se crea una vez por
 * evaluación y {@link #mostrar(int, int)} solo reemplaza la tarjeta central.
 */
final class Cuestionario {
    private static final PseudoClass COMPACTO = PseudoClass.getPseudoClass("compacto");
    /** Ancho de ventana desde el que la guía se muestra como panel lateral en vez de plegable. */
    private static final double ANCHO_PANEL_GUIA = 1360;
    private static final double ANCHO_OPCION = 192;
    /** Ancho de la guía plegable desde el que las dos fichas del ejemplo van lado a lado. */
    private static final double ANCHO_DOS_COLUMNAS = 640;

    private final Evaluacion evaluacion;
    private final List<List<Criterio>> secciones;
    private final Runnable evaluar;
    private final BiConsumer<Integer, Integer> alCambiarUbicacion;
    private final BarraCriterios barra;
    private final BorderPane vista = new BorderPane();
    private final StackPane zonaTarjeta = new StackPane();
    private final VBox panelGuia = caja(12, "panel-guia");
    private final ScrollPane desplazamientoGuia = desplazar(panelGuia);

    private int seccionActual = -1;
    private int criterioActual = -1;
    private boolean guiaLateral;

    // Elementos del criterio que se está mostrando.
    private Label titulo;
    private CheckBox motivo;
    private Label errorMotivo;
    private ScrollPane scroll;
    private VBox huecoGuia;
    private TitledPane guiaPlegable;
    private VBox contenidoGuia;

    Cuestionario(Evaluacion evaluacion, List<List<Criterio>> secciones, Runnable inicio, Runnable evaluar,
                 BiConsumer<Integer, Integer> alCambiarUbicacion) {
        this.evaluacion = evaluacion;
        this.secciones = secciones;
        this.evaluar = evaluar;
        this.alCambiarUbicacion = alCambiarUbicacion;
        this.barra = new BarraCriterios(evaluacion, secciones, (s, c) -> mostrar(s, c, false), inicio);

        desplazamientoGuia.setMinWidth(380);
        desplazamientoGuia.setPrefWidth(380);
        desplazamientoGuia.setMaxWidth(380);
        desplazamientoGuia.getStyleClass().add("zona-guia");
        desplazamientoGuia.setVisible(false);
        desplazamientoGuia.managedProperty().bind(desplazamientoGuia.visibleProperty());

        vista.getStyleClass().add("vista-cuestionario");
        // El orden en que se asignan define el orden de tabulación: lateral, pregunta, guía y pie.
        vista.setLeft(barra.vista());
        vista.setCenter(zonaTarjeta);
        vista.setRight(desplazamientoGuia);
        vista.heightProperty().addListener((obs, altoAnterior, alto) ->
                vista.pseudoClassStateChanged(COMPACTO, alto.doubleValue() < 440));
        vista.widthProperty().addListener((obs, anchoAnterior, ancho) -> {
            boolean lateral = ancho.doubleValue() >= ANCHO_PANEL_GUIA;
            if (lateral != guiaLateral) {
                guiaLateral = lateral;
                colocarGuia();
            }
        });
    }

    BorderPane vista() { return vista; }

    int seccion() { return seccionActual; }

    int criterio() { return criterioActual; }

    void enfocarTitulo() { enfocar(titulo); }

    /** Muestra un criterio y lleva el foco a su pregunta. */
    void mostrar(int indiceSeccion, int indicePregunta) {
        mostrar(indiceSeccion, indicePregunta, true);
    }

    /**
     * Muestra un criterio. La animación lateral indica si se avanza o se retrocede. Desde la barra lateral el
     * foco se queda en ella, para poder seguir recorriendo la lista con el teclado.
     */
    private void mostrar(int indiceSeccion, int indicePregunta, boolean moverFoco) {
        boolean primera = criterioActual < 0;
        if (indiceSeccion == seccionActual && indicePregunta == criterioActual) {
            if (moverFoco) {
                enfocarTitulo();
            }
            return;
        }
        int direccion = primera ? 0
                : Integer.compare(posicion(indiceSeccion, indicePregunta), posicion(seccionActual, criterioActual));
        seccionActual = indiceSeccion;
        criterioActual = indicePregunta;
        alCambiarUbicacion.accept(indiceSeccion, indicePregunta);

        construirTarjeta(indiceSeccion, indicePregunta);
        zonaTarjeta.getChildren().setAll(scroll);
        vista.setBottom(construirPie(indiceSeccion, indicePregunta));
        colocarGuia();
        barra.actualizar(indiceSeccion, indicePregunta);
        if (!primera) {
            Movimiento.entrarLateral(scroll, direccion);
        }
        if (moverFoco) {
            enfocarTitulo();
        }
    }

    private int posicion(int indiceSeccion, int indicePregunta) {
        int antes = 0;
        for (int i = 0; i < indiceSeccion; i++) {
            antes += secciones.get(i).size();
        }
        return antes + indicePregunta;
    }

    private void construirTarjeta(int indiceSeccion, int indicePregunta) {
        var seccion = secciones.get(indiceSeccion);
        var criterio = seccion.get(indicePregunta);
        var guardada = evaluacion.respuesta(criterio.id());

        var opciones = new ToggleGroup();
        var filaOpciones = new FlowPane(8, 8);
        filaOpciones.getStyleClass().add("respuestas");
        for (Valor valor : Valor.values()) {
            if (valor != Valor.NO_APLICA || criterio.permiteNoAplica()) {
                var radio = new RadioButton(valor.toString());
                radio.setToggleGroup(opciones);
                radio.setUserData(valor);
                radio.setGraphic(Iconos.valor(valor));
                radio.setContentDisplay(ContentDisplay.LEFT);
                radio.setGraphicTextGap(8);
                radio.setAccessibleText(valor + ", " + criterio.titulo());
                radio.setMaxWidth(Double.MAX_VALUE);
                radio.setSelected(guardada != null && guardada.valor() == valor);
                filaOpciones.getChildren().add(radio);
            }
        }
        filaOpciones.widthProperty().addListener((obs, anterior, nuevo) -> {
            double ancho = nuevo.doubleValue();
            double espacio = filaOpciones.getHgap();
            int columnas = Math.max(1, Math.min(filaOpciones.getChildren().size(),
                    (int) ((ancho + espacio) / (ANCHO_OPCION + espacio))));
            if (filaOpciones.getChildren().size() == 4 && columnas == 3) {
                columnas = 2;
            }
            double anchoOpcion = Math.max(ANCHO_OPCION, Math.floor((ancho - espacio * (columnas - 1)) / columnas));
            for (var opcion : filaOpciones.getChildren()) {
                ((RadioButton) opcion).setPrefWidth(anchoOpcion);
            }
        });

        var ubicacion = campo(guardada == null ? "" : guardada.ubicacion(), "Ej. Lección 2, botón Enviar", 350);
        var anotacion = desplegable(ubicacion.getText().isBlank() ? "Añadir ubicación" : "Ubicación anotada",
                caja(8, "detalle", campoConEtiqueta("Ubicación (opcional)", ubicacion)));
        anotacion.getStyleClass().add("anotacion");
        anotacion.setExpanded(guardada != null && !guardada.ubicacion().isEmpty());
        motivo = new CheckBox(criterio.motivoNoAplica());
        motivo.setWrapText(true);
        motivo.setAccessibleText(criterio.motivoNoAplica() + ", confirmar motivo de No aplica");
        motivo.setSelected(guardada != null && !guardada.motivo().isEmpty());
        errorMotivo = texto("Confirma este motivo para usar «No aplica».", "error-campo");
        var confirmacion = caja(4, "confirmacion-motivo", texto("Motivo de «No aplica»", "etiqueta-campo"),
                errorMotivo, motivo);
        errorMotivo.setVisible(false);
        errorMotivo.managedProperty().bind(errorMotivo.visibleProperty());
        confirmacion.managedProperty().bind(confirmacion.visibleProperty());

        Runnable guardar = () -> {
            var seleccion = opciones.getSelectedToggle();
            Valor valor = seleccion == null ? null : (Valor) seleccion.getUserData();
            boolean requiereMotivo = valor == Valor.NO_APLICA;
            confirmacion.setVisible(requiereMotivo);
            if (!requiereMotivo || motivo.isSelected()) {
                errorMotivo.setVisible(false);
                motivo.setAccessibleHelp(null);
            }
            evaluacion.cambiar(criterio, valor, ubicacion.getText(),
                    requiereMotivo && motivo.isSelected() ? criterio.motivoNoAplica() : "");
        };

        opciones.selectedToggleProperty().addListener((obs, anterior, elegido) -> {
            if (elegido == null || elegido.getUserData() != Valor.NO_APLICA) {
                motivo.setSelected(false);
            }
            guardar.run();
            barra.respondido(indiceSeccion, indicePregunta, true);
        });
        ubicacion.textProperty().addListener((obs, anterior, valor) -> {
            anotacion.setText(valor.isBlank() ? "Añadir ubicación" : "Ubicación anotada");
            guardar.run();
            barra.respondido(indiceSeccion, indicePregunta, false);
        });
        motivo.selectedProperty().addListener((obs, anterior, valor) -> {
            guardar.run();
            barra.respondido(indiceSeccion, indicePregunta, false);
        });
        confirmacion.setVisible(guardada != null && guardada.valor() == Valor.NO_APLICA);

        var limpiar = boton("Limpiar respuesta", "mdi2c-close-circle-outline", "enlace", () -> {
            opciones.selectToggle(null);
            filaOpciones.getChildren().get(0).requestFocus();
        });
        limpiar.visibleProperty().bind(opciones.selectedToggleProperty().isNotNull());
        limpiar.managedProperty().bind(limpiar.visibleProperty());

        titulo = texto(criterio.pregunta(), "titulo-pregunta");
        titulo.setFocusTraversable(true);
        var contexto = criterio.nombreArea() + " · " + (indicePregunta + 1) + " de " + seccion.size();
        var prioridad = fila("insignia-prioridad",
                Iconos.icono("mdi2f-flag-outline", "icono-boton"),
                texto("Prioridad " + criterio.prioridad(), "ayuda"));
        var tarjeta = caja(16, "tarjeta-pregunta",
                fila("cabecera-criterio", Iconos.area(criterio.area()), texto(contexto, "contexto-criterio"),
                        espacio(), prioridad),
                titulo, filaOpciones, confirmacion);
        huecoGuia = caja(0, "hueco-guia");
        tarjeta.getChildren().addAll(huecoGuia, anotacion, limpiar);
        prepararGuia(criterio);
        tarjeta.setMaxWidth(1100);

        var contenido = caja(16, "contenido-quiz", tarjeta,
                texto("Si omites un criterio, quedará pendiente en el diagnóstico.", "ayuda"));
        contenido.setAlignment(Pos.TOP_CENTER);
        scroll = desplazar(contenido);
    }

    /**
     * Prepara las dos presentaciones de la guía: el contenido del panel lateral y el panel plegable de la
     * tarjeta. Son dos árboles de nodos independientes, porque mover un mismo nodo entre un TitledPane y un
     * VBox deja la guía vacía; {@link #colocarGuia()} solo decide cuál de los dos está en la escena.
     */
    private void prepararGuia(Criterio criterio) {
        contenidoGuia = null;
        guiaPlegable = null;
        var guia = GuiasCriterios.para(criterio.id());
        if (guia == null) {
            return;
        }
        contenidoGuia = contenidoDeGuia(criterio.id(), guia, false);
        guiaPlegable = desplegable("Cómo evaluar este criterio", contenidoDeGuia(criterio.id(), guia, true));
        guiaPlegable.getStyleClass().add("guia-criterio");
    }

    /**
     * Pasos, ejemplo y condición para marcar «Sí». En el panel lateral las fichas del ejemplo van apiladas; en el
     * plegable, que ocupa todo el ancho de la tarjeta, se reparten en dos columnas si caben.
     */
    private static VBox contenidoDeGuia(String id, GuiasCriterios.Guia guia, boolean plegable) {
        var pasos = caja(4, "pasos-guia");
        for (int i = 0; i < guia.pasos().size(); i++) {
            pasos.getChildren().add(texto((i + 1) + ". " + guia.pasos().get(i), "paso-guia"));
        }
        var muestra = MuestrasCriterios.para(id);
        var cumple = fichaEjemplo(true, guia.cumple(), muestra == null ? null : muestra.cumple());
        var noCumple = fichaEjemplo(false, guia.noCumple(), muestra == null ? null : muestra.noCumple());
        Node fichas = plegable ? fichasEnColumnas(cumple, noCumple) : caja(12, "fichas-ejemplo", cumple, noCumple);
        var condicion = texto("Marca Sí " + guia.condicion(), "condicion-guia");
        condicion.setMaxWidth(Double.MAX_VALUE);
        return caja(12, "contenido-guia", pasos, texto("Ejemplo", "subtitulo-guia"), fichas, condicion);
    }

    /** Ficha «Así se cumple» o «Así no se cumple»: la forma del icono (✓ o ✗) distingue, no solo el color. */
    private static VBox fichaEjemplo(boolean cumple, String descripcion, Supplier<Node> demo) {
        var estado = cumple ? "estado-si" : "estado-no";
        var marca = new HBox(Iconos.valor(cumple ? Valor.SI : Valor.NO));
        marca.getStyleClass().addAll("marca-estado", estado);
        marca.setAlignment(Pos.CENTER);
        var encabezado = fila("encabezado-ejemplo", marca, texto(cumple ? "Así se cumple" : "Así no se cumple",
                "titulo-ejemplo"));
        var ficha = caja(8, "ficha-ejemplo", encabezado);
        ficha.getStyleClass().add(estado);
        if (demo != null) {
            ficha.getChildren().add(demo.get());
        }
        ficha.getChildren().add(texto(descripcion, "texto-ejemplo"));
        return ficha;
    }

    private static GridPane fichasEnColumnas(Node cumple, Node noCumple) {
        var rejilla = new GridPane();
        rejilla.setHgap(12);
        rejilla.setVgap(12);
        rejilla.widthProperty().addListener((obs, anterior, ancho) ->
                distribuirFichas(rejilla, cumple, noCumple, ancho.doubleValue() >= ANCHO_DOS_COLUMNAS ? 2 : 1));
        distribuirFichas(rejilla, cumple, noCumple, 1);
        return rejilla;
    }

    private static void distribuirFichas(GridPane rejilla, Node cumple, Node noCumple, int columnas) {
        if (rejilla.getColumnConstraints().size() == columnas) {
            return;
        }
        rejilla.getChildren().clear();
        rejilla.getColumnConstraints().clear();
        for (int i = 0; i < columnas; i++) {
            var columna = new ColumnConstraints();
            columna.setPercentWidth(100.0 / columnas);
            rejilla.getColumnConstraints().add(columna);
        }
        GridPane.setConstraints(cumple, 0, 0);
        GridPane.setConstraints(noCumple, columnas == 2 ? 1 : 0, columnas == 2 ? 0 : 1);
        rejilla.getChildren().addAll(cumple, noCumple);
    }

    /** Muestra la guía como panel fijo a la derecha o como plegable dentro de la tarjeta, según el ancho. */
    private void colocarGuia() {
        boolean hayGuia = contenidoGuia != null;
        boolean lateral = hayGuia && guiaLateral;
        desplazamientoGuia.setVisible(lateral);
        if (!hayGuia) {
            panelGuia.getChildren().clear();
            return;
        }
        if (lateral) {
            huecoGuia.getChildren().clear();
            var encabezado = fila("encabezado-guia", Iconos.icono("mdi2h-help-circle-outline", "icono-area"),
                    texto("Cómo evaluar este criterio", "subtitulo"));
            panelGuia.getChildren().setAll(encabezado, contenidoGuia);
        } else {
            panelGuia.getChildren().clear();
            huecoGuia.getChildren().setAll(guiaPlegable);
        }
    }

    private HBox construirPie(int indiceSeccion, int indicePregunta) {
        var seccion = secciones.get(indiceSeccion);
        boolean primero = indiceSeccion == 0 && indicePregunta == 0;
        boolean ultimo = indiceSeccion == secciones.size() - 1 && indicePregunta == seccion.size() - 1;
        var anterior = boton("Anterior", "mdi2a-arrow-left", "secundario", () -> {
            if (indicePregunta > 0) {
                mostrar(indiceSeccion, indicePregunta - 1);
            } else {
                mostrar(indiceSeccion - 1, secciones.get(indiceSeccion - 1).size() - 1);
            }
        });
        anterior.setDisable(primero);
        var diagnostico = boton(ultimo ? "Ver diagnóstico" : "Ver diagnóstico parcial", "mdi2c-chart-bar",
                ultimo ? "primario" : "enlace", evaluar);
        var pie = ultimo ? fila("pie-acciones", anterior, espacio(), diagnostico)
                : fila("pie-acciones", diagnostico, espacio(), anterior);
        if (!ultimo) {
            var siguiente = boton("Siguiente", "mdi2a-arrow-right", "primario", () -> {
                if (indicePregunta + 1 < seccion.size()) {
                    mostrar(indiceSeccion, indicePregunta + 1);
                } else {
                    mostrar(indiceSeccion + 1, 0);
                }
            });
            siguiente.setContentDisplay(ContentDisplay.RIGHT);
            pie.getChildren().add(siguiente);
        }
        return pie;
    }

    void mostrarErrorMotivo() {
        errorMotivo.setVisible(true);
        motivo.setAccessibleHelp(errorMotivo.getText());
        Platform.runLater(() -> {
            vista.applyCss();
            vista.layout();
            motivo.requestFocus();
            var contenido = scroll.getContent();
            var aviso = motivo.getParent();
            var cajaError = contenido.sceneToLocal(aviso.localToScene(aviso.getBoundsInLocal()));
            double altoVisible = scroll.getViewportBounds().getHeight();
            double desplazable = contenido.getLayoutBounds().getHeight() - altoVisible;
            if (desplazable > 0 && cajaError.getMaxY() + 8 > altoVisible) {
                scroll.setVvalue(Math.min(1, (cajaError.getMaxY() + 8 - altoVisible) / desplazable));
            }
        });
    }
}
