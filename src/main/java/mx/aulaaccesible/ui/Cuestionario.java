package mx.aulaaccesible.ui;

import javafx.application.Platform;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import mx.aulaaccesible.model.Criterio;
import mx.aulaaccesible.model.Evaluacion;
import mx.aulaaccesible.model.Valor;

import java.util.List;
import java.util.function.BiConsumer;

import static mx.aulaaccesible.ui.Controles.*;

final class Cuestionario {
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
            progreso.setText(contestadas + " de " + total + " respondidas");
            barra.setProgress((double) contestadas / total);
            barra.setAccessibleText(progreso.getText());
        };
        actualizarProgreso.run();

        VBox menu = caja(9, "menu", texto("Progreso de la evaluación", "subtitulo"), progreso,
                barra, texto("SECCIONES", "sobrelinea"));
        for (int i = 0; i < secciones.size(); i++) {
            int destino = i;
            String nombre = secciones.get(i).get(0).nombreArea();
            boolean actual = i == indiceSeccion;
            var opcion = boton((i + 1) + ". " + nombre, actual ? "seccion-activa" : "seccion",
                    () -> {
                        if (!actual) {
                            navegar.accept(destino, 0);
                        }
                    });
            opcion.setAccessibleText(opcion.getText() + (actual ? ", sección actual" : ""));
            opcion.setMaxWidth(Double.MAX_VALUE);
            opcion.setWrapText(true);
            menu.getChildren().add(opcion);
        }
        menu.getChildren().add(boton("Volver al inicio", "enlace", inicio));
        var panelMenu = desplazar(menu);
        panelMenu.getStyleClass().add("panel-menu");
        panelMenu.setPrefWidth(240);
        panelMenu.setMinWidth(195);

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
        var etiquetaElegir = texto("Ir a un criterio de esta sección", "etiqueta-campo");
        etiquetaElegir.setLabelFor(elegir);
        var selector = caja(4, "selector-criterio", etiquetaElegir, elegir);

        var encabezado = caja(5, "encabezado-quiz",
                texto("SECCIÓN " + (indiceSeccion + 1) + " DE " + secciones.size(), "sobrelinea"),
                texto(criterio.nombreArea(), "titulo-seccion"), selector);
        var opciones = new ToggleGroup();
        var filaOpciones = new FlowPane(9, 8);
        filaOpciones.getStyleClass().add("respuestas");
        for (Valor valor : Valor.values()) {
            if (valor != Valor.NO_APLICA || criterio.permiteNoAplica()) {
                var radio = new RadioButton(valor.toString());
                radio.setToggleGroup(opciones);
                radio.setUserData(valor);
                radio.setAccessibleText(valor + ", " + criterio.titulo());
                radio.setSelected(guardada != null && guardada.valor() == valor);
                filaOpciones.getChildren().add(radio);
            }
        }

        var ubicacion = campo(guardada == null ? "" : guardada.ubicacion(), "Ej. Lección 2, botón Enviar", 350);
        var anotacion = desplegable("Ubicación del hallazgo (opcional)", caja(8, "detalle",
                campoConEtiqueta("¿Dónde encontraste esto?", ubicacion)));
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
        ubicacion.textProperty().addListener((obs, anterior, valor) -> guardar.run());
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
        var tarjeta = caja(12, "tarjeta-pregunta",
                fila("cabecera-criterio", texto("CRITERIO " + criterio.orden() + " DE " + total, "sobrelinea"),
                        espacio(), texto("Prioridad " + prioridad, "prioridad-" + prioridad)),
                titulo, filaOpciones, confirmacion, limpiar, anotacion);
        scroll = desplazar(caja(12, "contenido-quiz", tarjeta,
                texto("Puedes omitir un criterio. Se mostrará como pendiente en el diagnóstico.", "ayuda")));

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
        var diagnostico = boton("Generar diagnóstico", ultimo ? "primario" : "secundario", evaluar);
        var pie = fila("pie-acciones", anterior, espacio(), diagnostico);
        if (!ultimo) {
            pie.getChildren().add(boton("Siguiente", "primario", () -> {
                if (indicePregunta + 1 < seccion.size()) {
                    navegar.accept(indiceSeccion, indicePregunta + 1);
                } else {
                    navegar.accept(indiceSeccion + 1, 0);
                }
            }));
        }

        var contenido = new BorderPane();
        contenido.setTop(encabezado);
        contenido.setCenter(scroll);
        contenido.setBottom(pie);
        vista = new BorderPane(contenido, null, null, null, panelMenu);
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
