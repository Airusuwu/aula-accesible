package mx.aulaaccesible.ui;

import javafx.scene.Node;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import mx.aulaaccesible.informe.ExportadorInformeHtml;
import mx.aulaaccesible.informe.InformeEvaluacion;

import java.io.IOException;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mx.aulaaccesible.ui.Controles.*;

final class Resultados {
    private static final PseudoClass COMPACTO = PseudoClass.getPseudoClass("compacto");
    private final InformeEvaluacion informe;
    private final Label titulo;
    private final Label estadoExportacion = texto("", "ayuda");
    private final BorderPane vista;

    Resultados(InformeEvaluacion informe, Consumer<String> irAlCriterio,
               Runnable editar, Runnable nueva, Runnable inicio) {
        this.informe = informe;
        var diagnostico = informe.diagnostico();
        var conteos = diagnostico.conteos();

        titulo = texto(diagnostico.tituloEstado(), "titulo");
        titulo.setFocusTraversable(true);
        var noAplica = texto(informe.resumenNoAplica(), "ayuda");
        noAplica.setMinWidth(Region.USE_PREF_SIZE);
        var metadatos = fila("metadatos-informe", noAplica);
        String contexto = Stream.of(informe.software(), informe.seccion())
                .filter(s -> !s.isBlank()).collect(Collectors.joining(" · "));
        if (!contexto.isBlank()) {
            var nombre = texto(contexto, "contexto");
            nombre.setWrapText(false);
            nombre.setTooltip(new Tooltip(contexto));
            HBox.setHgrow(nombre, Priority.ALWAYS);
            metadatos.getChildren().addAll(texto("·", "ayuda"), nombre);
        }
        var etiquetaDiagnostico = texto("Diagnóstico de accesibilidad", "contexto-criterio");
        etiquetaDiagnostico.managedProperty().bind(etiquetaDiagnostico.visibleProperty());
        var veredicto = caja(4, "veredicto-resultados", etiquetaDiagnostico,
                titulo, texto(informe.resumen(), "resumen-datos"),
                texto(informe.explicacion(), diagnostico.estado().equals("requiere_atencion_prioritaria")
                        ? "explicacion-prioritaria" : "texto"), metadatos);
        HBox.setHgrow(veredicto, Priority.ALWAYS);
        var exportar = boton("Exportar informe", "primario", this::exportarInforme);
        exportar.setMinWidth(Region.USE_PREF_SIZE);
        var encabezado = fila("encabezado-resultados", veredicto, exportar);

        var recomendaciones = pestana("Recomendaciones", conteos.barreras(), recomendaciones());
        var pendientes = pestana("Pendientes", conteos.pendientes(), pendientes(irAlCriterio));
        var favorables = pestana("Favorables", conteos.favorables(), favorables());
        var problemas = pestana("Problemas combinados", diagnostico.problemas().size(), problemas());
        var pestanas = new TabPane(recomendaciones, pendientes, favorables, problemas);
        pestanas.getStyleClass().add("pestanas-diagnostico");
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        if (conteos.barreras() == 0) {
            pestanas.getSelectionModel().select(conteos.pendientes() > 0 ? pendientes : favorables);
        }

        var aviso = texto("Diagnóstico orientativo basado en tus respuestas.", "ayuda");
        aviso.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(aviso, Priority.ALWAYS);
        var pie = fila("pie-acciones", aviso);
        for (var accion : java.util.List.of(boton("Volver al inicio", "enlace", inicio),
                boton("Nueva evaluación", "enlace", nueva), boton("Editar respuestas", "secundario", editar))) {
            accion.setMinWidth(Region.USE_PREF_SIZE);
            pie.getChildren().add(accion);
        }
        estadoExportacion.setVisible(false);
        estadoExportacion.managedProperty().bind(estadoExportacion.visibleProperty());
        estadoExportacion.setFocusTraversable(true);
        vista = new BorderPane();
        vista.getStyleClass().add("vista-resultados");
        vista.heightProperty().addListener((obs, anterior, alto) -> {
            boolean compacto = alto.doubleValue() < 470;
            vista.pseudoClassStateChanged(COMPACTO, compacto);
            etiquetaDiagnostico.setVisible(!compacto);
        });
        vista.setTop(encabezado);
        vista.setCenter(pestanas);
        vista.setBottom(caja(0, "pie-resultados", estadoExportacion, pie));
    }

    BorderPane vista() { return vista; }

    void enfocarTitulo() { enfocar(titulo); }

    private VBox recomendaciones() {
        VBox panel = caja(12, "lista-resultados");
        if (informe.diagnostico().recomendaciones().isEmpty()) {
            panel.getChildren().add(texto("No hay recomendaciones por barreras reportadas.", "texto"));
        }
        informe.recomendacionesPorPrioridad().forEach((prioridad, recomendaciones) -> {
            VBox grupo = grupo("Prioridad " + prioridad, recomendaciones.size());
            grupo.getStyleClass().add("grupo-recomendaciones");
            for (var recomendacion : recomendaciones) {
                String id = recomendacion.criterio();
                var accion = caja(4, "accion-recomendada",
                        texto("Acción recomendada", "etiqueta-recomendacion"),
                        texto(recomendacion.accion(), "texto"));
                var ficha = caja(8, "ficha-recomendacion", texto(informe.criterio(id).titulo(), "subtitulo"), accion);
                agregarUbicacion(ficha, id);
                grupo.getChildren().add(ficha);
            }
            panel.getChildren().add(grupo);
        });
        return panel;
    }

    private VBox pendientes(Consumer<String> irAlCriterio) {
        VBox panel = caja(12, "lista-resultados");
        if (informe.diagnostico().pendientes().isEmpty()) {
            panel.getChildren().add(texto("No hay criterios pendientes.", "texto"));
        }
        informe.pendientesPorEstado().forEach((estado, pendientes) -> {
            VBox grupo = grupo(estado, pendientes.size());
            for (var pendiente : pendientes) {
                var criterio = informe.criterio(pendiente.criterio());
                var enlace = boton("Ir al criterio", "enlace", () -> irAlCriterio.accept(criterio.id()));
                enlace.setAccessibleText("Ir al criterio " + criterio.titulo());
                var contenido = cajaCriterio(criterio.id(), criterio.pregunta());
                HBox.setHgrow(contenido, Priority.ALWAYS);
                grupo.getChildren().add(fila("fila-informe", contenido, enlace));
            }
            panel.getChildren().add(grupo);
        });
        return panel;
    }

    private VBox favorables() {
        VBox panel = caja(12, "lista-resultados");
        if (informe.favorables().isEmpty()) {
            panel.getChildren().add(texto("No se registraron respuestas favorables.", "texto"));
        } else {
            var grupo = grupo("Criterios con respuesta «Sí»", informe.favorables().size());
            for (var criterio : informe.favorables()) {
                grupo.getChildren().add(filaCriterio(criterio.id(), criterio.pregunta()));
            }
            panel.getChildren().add(grupo);
        }
        return panel;
    }

    private VBox problemas() {
        VBox panel = caja(8, "lista-resultados");
        if (informe.diagnostico().problemas().isEmpty()) {
            panel.getChildren().add(texto("Las respuestas no activaron problemas combinados.", "texto"));
        }
        for (var problema : informe.diagnostico().problemas()) {
            VBox detalle = caja(4, "detalle", texto("Respuestas que activaron el hallazgo · Regla "
                    + problema.regla(), "ayuda"));
            for (var evidencia : problema.evidencias()) {
                var criterio = informe.criterio(evidencia.criterio());
                var item = caja(4, "fila-informe", texto(criterio.titulo() + " · Respuesta: " + evidencia.valor(), "subtitulo"),
                        texto(evidencia.pregunta(), "texto"),
                        texto("Acción: " + informe.accion(criterio.id()), "texto"));
                agregarUbicacion(item, criterio.id());
                detalle.getChildren().add(item);
            }
            panel.getChildren().add(desplegable(problema.titulo(), detalle));
        }
        return panel;
    }

    private static VBox grupo(String nombre, int cantidad) {
        return caja(0, "grupo-informe", texto(nombre + " (" + cantidad + ")", "titulo-grupo"));
    }

    private Node filaCriterio(String id, String detalle) {
        return caja(0, "fila-informe", cajaCriterio(id, detalle));
    }

    private VBox cajaCriterio(String id, String detalle) {
        var fila = caja(4, "datos-criterio", texto(informe.criterio(id).titulo(), "subtitulo"), texto(detalle, "texto"));
        agregarUbicacion(fila, id);
        return fila;
    }

    private void agregarUbicacion(VBox fila, String id) {
        if (!informe.ubicacion(id).isBlank()) {
            fila.getChildren().add(texto("Ubicación: " + informe.ubicacion(id), "ubicacion"));
        }
    }

    private static Tab pestana(String titulo, int cantidad, VBox panel) {
        return new Tab(titulo + " (" + cantidad + ")", desplazar(panel));
    }

    private void exportarInforme() {
        var guardar = new FileChooser();
        guardar.setTitle("Guardar informe de accesibilidad");
        guardar.setInitialFileName("informe-aula-accesible.html");
        guardar.getExtensionFilters().add(new FileChooser.ExtensionFilter("Informe HTML", "*.html", "*.htm"));
        var archivo = guardar.showSaveDialog(vista.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        String nombre = archivo.getName().toLowerCase(Locale.ROOT);
        if (!nombre.endsWith(".html") && !nombre.endsWith(".htm")) {
            informar("Usa la extensión .html para guardar el informe.", true);
            return;
        }
        try {
            ExportadorInformeHtml.guardar(informe, archivo.toPath());
            informar("Informe guardado. Puedes abrirlo en el navegador para imprimirlo.", false);
            estadoExportacion.setTooltip(new Tooltip(archivo.getAbsolutePath()));
        } catch (IOException | SecurityException e) {
            informar("No se pudo guardar el informe. Elige otra carpeta y vuelve a intentar.", true);
        }
    }

    private void informar(String mensaje, boolean error) {
        estadoExportacion.setTooltip(null);
        estadoExportacion.setText(mensaje);
        estadoExportacion.getStyleClass().setAll("label", "estado-exportacion", error ? "error-campo" : "ayuda");
        estadoExportacion.setVisible(true);
        enfocar(estadoExportacion);
    }
}
