package mx.aulaaccesible.ui;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import mx.aulaaccesible.model.Criterio;
import mx.aulaaccesible.model.Evaluacion;
import mx.aulaaccesible.model.Valor;

import java.util.List;
import java.util.function.BiConsumer;

import static mx.aulaaccesible.ui.Controles.*;

final class Cuestionario {
    private static final PseudoClass COMPACTO = PseudoClass.getPseudoClass("compacto");
    private final Label titulo;
    private final CheckBox motivo;
    private final ScrollPane scroll;
    private final Label errorMotivo = texto("Confirma este motivo para usar «No aplica».", "error-campo");
    private final BorderPane vista;

    Cuestionario(Evaluacion evaluacion, List<List<Criterio>> secciones, int indiceSeccion, int indicePregunta,
                 BiConsumer<Integer, Integer> navegar, Runnable inicio, Runnable evaluar) {
        var seccion = secciones.get(indiceSeccion);
        var criterio = seccion.get(indicePregunta);
        var guardada = evaluacion.respuesta(criterio.id());
        int total = secciones.stream().mapToInt(List::size).sum();
        var progreso = texto("", "texto-progreso");
        var barra = new ProgressBar();
        barra.setMaxWidth(Double.MAX_VALUE);
        Runnable actualizarProgreso = () -> {
            long contestadas = evaluacion.respuestas().stream()
                    .filter(r -> r.valor() != null && (r.valor() != Valor.NO_APLICA || !r.motivo().isBlank()))
                    .count();
            progreso.setText(contestadas + " de " + total + " respondidos");
            barra.setProgress((double) contestadas / total);
            barra.setAccessibleText(progreso.getText());
        };
        actualizarProgreso.run();

        var menu = fila("areas-cuestionario");
        for (int i = 0; i < secciones.size(); i++) {
            int destino = i;
            var primerCriterio = secciones.get(i).get(0);
            String nombre = primerCriterio.nombreArea();
            boolean actual = i == indiceSeccion;
            var opcion = boton(nombreCorto(primerCriterio), actual ? "seccion-activa" : "seccion",
                    () -> {
                        if (!actual) {
                            navegar.accept(destino, 0);
                        }
                    });
            opcion.setAccessibleText(opcion.getText() + (nombre.equals(opcion.getText()) ? "" : ", " + nombre)
                    + (actual ? ", área actual" : ""));
            opcion.setMinWidth(0);
            opcion.setPrefWidth(0);
            opcion.setMaxWidth(Double.MAX_VALUE);
            opcion.setWrapText(true);
            if (i == secciones.size() - 1) {
                opcion.getStyleClass().add("ultima-area");
            }
            HBox.setHgrow(opcion, Priority.ALWAYS);
            menu.getChildren().add(opcion);
        }
        var segmentos = fila("segmentos-progreso");
        segmentos.setMouseTransparent(true);
        for (int i = 0; i < secciones.size(); i++) {
            var segmento = new Region();
            segmento.getStyleClass().add("segmento-progreso");
            if (i == secciones.size() - 1) {
                segmento.getStyleClass().add("ultimo-segmento");
            }
            segmento.setPrefWidth(0);
            HBox.setHgrow(segmento, Priority.ALWAYS);
            segmentos.getChildren().add(segmento);
        }
        var cabecera = caja(12, "cabecera-cuestionario", menu,
                caja(8, "progreso-cuestionario",
                        fila("etiqueta-progreso", texto("Progreso de la evaluación", "subtitulo"), espacio(), progreso),
                        new StackPane(barra, segmentos)));

        var elegir = new ChoiceBox<String>();
        seccion.stream().map(Criterio::titulo).forEach(elegir.getItems()::add);
        elegir.getSelectionModel().select(indicePregunta);
        elegir.setMaxWidth(Double.MAX_VALUE);
        elegir.setOnAction(evento -> {
            int destino = elegir.getSelectionModel().getSelectedIndex();
            if (destino >= 0 && destino != indicePregunta) {
                navegar.accept(indiceSeccion, destino);
            }
        });
        var etiquetaElegir = texto("Ir a un criterio", "etiqueta-campo");
        etiquetaElegir.setLabelFor(elegir);
        var selector = caja(4, "selector-criterio", etiquetaElegir, elegir);

        var opciones = new ToggleGroup();
        var filaOpciones = new FlowPane(8, 8);
        filaOpciones.getStyleClass().add("respuestas");
        for (Valor valor : Valor.values()) {
            if (valor != Valor.NO_APLICA || criterio.permiteNoAplica()) {
                var radio = new RadioButton(valor.toString());
                radio.setToggleGroup(opciones);
                radio.setUserData(valor);
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
                    (int) ((ancho + espacio) / (192 + espacio))));
            if (filaOpciones.getChildren().size() == 4 && columnas == 3) {
                columnas = 2;
            }
            double anchoOpcion = Math.max(192, Math.floor((ancho - espacio * (columnas - 1)) / columnas));
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
            actualizarProgreso.run();
        };

        opciones.selectedToggleProperty().addListener((obs, anterior, elegido) -> {
            if (elegido == null || elegido.getUserData() != Valor.NO_APLICA) {
                motivo.setSelected(false);
            }
            guardar.run();
        });
        ubicacion.textProperty().addListener((obs, anterior, valor) -> {
            anotacion.setText(valor.isBlank() ? "Añadir ubicación" : "Ubicación anotada");
            guardar.run();
        });
        motivo.selectedProperty().addListener((obs, anterior, valor) -> guardar.run());
        confirmacion.setVisible(guardada != null && guardada.valor() == Valor.NO_APLICA);

        var limpiar = boton("Limpiar respuesta", "enlace", () -> {
            opciones.selectToggle(null);
            filaOpciones.getChildren().get(0).requestFocus();
        });
        limpiar.visibleProperty().bind(opciones.selectedToggleProperty().isNotNull());
        limpiar.managedProperty().bind(limpiar.visibleProperty());
        String prioridad = criterio.prioridad();
        titulo = texto(criterio.pregunta(), "titulo-pregunta");
        titulo.setFocusTraversable(true);
        var contexto = criterio.nombreArea() + " · " + (indicePregunta + 1) + " de " + seccion.size();
        var tarjeta = caja(16, "tarjeta-pregunta",
                fila("cabecera-criterio", texto(contexto, "contexto-criterio"),
                        espacio(), texto("Prioridad " + prioridad, "ayuda")),
                titulo, filaOpciones, confirmacion);
        var guia = GuiasCriterios.para(criterio.id());
        if (guia != null) {
            tarjeta.getChildren().add(panelGuia(guia));
        }
        tarjeta.getChildren().addAll(anotacion, limpiar);
        scroll = desplazar(caja(16, "contenido-quiz", tarjeta,
                texto("Si omites un criterio, quedará pendiente en el diagnóstico.", "ayuda"), selector,
                boton("Volver al inicio", "enlace", inicio)));

        boolean primero = indiceSeccion == 0 && indicePregunta == 0;
        boolean ultimo = indiceSeccion == secciones.size() - 1 && indicePregunta == seccion.size() - 1;
        var anterior = boton("Anterior", "secundario", () -> {
            if (indicePregunta > 0) {
                navegar.accept(indiceSeccion, indicePregunta - 1);
            } else {
                navegar.accept(indiceSeccion - 1, secciones.get(indiceSeccion - 1).size() - 1);
            }
        });
        anterior.setDisable(primero);
        var diagnostico = boton(ultimo ? "Ver diagnóstico" : "Ver diagnóstico parcial",
                ultimo ? "primario" : "enlace", evaluar);
        var pie = ultimo ? fila("pie-acciones", anterior, espacio(), diagnostico)
                : fila("pie-acciones", diagnostico, espacio(), anterior);
        if (!ultimo) {
            pie.getChildren().add(boton("Siguiente", "primario", () -> {
                if (indicePregunta + 1 < seccion.size()) {
                    navegar.accept(indiceSeccion, indicePregunta + 1);
                } else {
                    navegar.accept(indiceSeccion + 1, 0);
                }
            }));
        }

        vista = new BorderPane();
        vista.getStyleClass().add("vista-cuestionario");
        vista.heightProperty().addListener((obs, altoAnterior, alto) ->
                vista.pseudoClassStateChanged(COMPACTO, alto.doubleValue() < 440));
        vista.setTop(cabecera);
        vista.setCenter(scroll);
        vista.setBottom(pie);
    }

    private static String nombreCorto(Criterio criterio) {
        return switch (criterio.area()) {
            case "navegacion_teclado" -> "Teclado";
            case "contenido_multimedia" -> "Multimedia";
            case "formularios_evaluaciones" -> "Formularios";
            case "organizacion_comprension" -> "Organización";
            default -> criterio.nombreArea();
        };
    }

    private static TitledPane panelGuia(GuiasCriterios.Guia guia) {
        var pasos = caja(4, "pasos-guia");
        for (int i = 0; i < guia.pasos().size(); i++) {
            var paso = texto((i + 1) + ". " + guia.pasos().get(i), "paso-guia");
            pasos.getChildren().add(paso);
        }
        var condicion = texto("Marca Sí " + guia.condicion(), "condicion-guia");
        condicion.setMaxWidth(Double.MAX_VALUE);
        var contenido = caja(12, "contenido-guia", pasos, condicion);
        var panel = desplegable("Cómo evaluar este criterio", contenido);
        panel.getStyleClass().add("guia-criterio");
        return panel;
    }

    BorderPane vista() { return vista; }

    void enfocarTitulo() { enfocar(titulo); }

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
