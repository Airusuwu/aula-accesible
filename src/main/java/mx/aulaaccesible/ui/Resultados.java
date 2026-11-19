package mx.aulaaccesible.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.css.PseudoClass;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import mx.aulaaccesible.informe.ExportadorInformeHtml;
import mx.aulaaccesible.informe.InformeEvaluacion;
import mx.aulaaccesible.model.Valor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        var exportar = boton("Exportar informe", "mdi2e-export-variant", "primario", this::exportarInforme);
        exportar.setMinWidth(Region.USE_PREF_SIZE);
        var insigniaEstado = new StackPane(Iconos.estado(diagnostico.estado()));
        insigniaEstado.getStyleClass().addAll("insignia-estado", claseEstado(diagnostico.estado()));
        insigniaEstado.setAlignment(Pos.CENTER);
        var encabezado = fila("encabezado-resultados", insigniaEstado, veredicto, exportar);

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

        var metricas = fila("fila-metricas",
                tarjetaMetrica("Favorables", conteos.favorables(), Valor.SI, "metrica-si",
                        () -> pestanas.getSelectionModel().select(favorables)),
                tarjetaMetrica("Barreras", conteos.barreras(), Valor.NO, "metrica-no",
                        () -> pestanas.getSelectionModel().select(recomendaciones)),
                tarjetaMetrica("Pendientes", conteos.pendientes(), Valor.DESCONOCIDO, "metrica-pendiente",
                        () -> pestanas.getSelectionModel().select(pendientes)),
                tarjetaMetrica("No aplican", conteos.noAplican(), Valor.NO_APLICA, "metrica-no-aplica", null));
        Movimiento.escalonar(metricas.getChildren());

        var aviso = texto("Diagnóstico orientativo basado en tus respuestas.", "ayuda");
        aviso.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(aviso, Priority.ALWAYS);
        var pie = fila("pie-acciones", aviso);
        for (var accion : java.util.List.of(boton("Volver al inicio", "mdi2h-home-outline", "enlace", inicio),
                boton("Nueva evaluación", "mdi2p-plus-circle-outline", "enlace", nueva), boton("Editar respuestas", "mdi2p-pencil-outline", "secundario", editar))) {
            accion.setMinWidth(Region.USE_PREF_SIZE);
            pie.getChildren().add(accion);
        }
        estadoExportacion.setVisible(false);
        estadoExportacion.managedProperty().bind(estadoExportacion.visibleProperty());
        estadoExportacion.setFocusTraversable(true);
        vista = new BorderPane();
        vista.getStyleClass().add("vista-resultados");
        vista.heightProperty().addListener((obs, anterior, alto) -> {
            // Con la fila de métricas el encabezado ocupa más: se compacta en ventanas de menos de ~700 px.
            boolean compacto = alto.doubleValue() < 700;
            vista.pseudoClassStateChanged(COMPACTO, compacto);
            etiquetaDiagnostico.setVisible(!compacto);
        });
        vista.setTop(caja(0, "zona-superior-resultados", encabezado, metricas));
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
            var mosaico = new Mosaico();
            for (var recomendacion : recomendaciones) {
                String id = recomendacion.criterio();
                var accion = caja(4, "accion-recomendada",
                        texto("Acción recomendada", "etiqueta-recomendacion"),
                        texto(recomendacion.accion(), "texto"));
                var ficha = caja(8, "ficha-recomendacion", texto(informe.criterio(id).titulo(), "subtitulo"), accion);
                agregarUbicacion(ficha, id);
                mosaico.agregar(ficha);
            }
            grupo.getChildren().add(mosaico);
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
                var enlace = boton("Ir al criterio", "mdi2a-arrow-right", "enlace", () -> irAlCriterio.accept(criterio.id()));
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

    private static String claseEstado(String estado) {
        return switch (estado) {
            case "requiere_atencion_prioritaria" -> "estado-critico";
            case "requiere_mejoras" -> "estado-mejoras";
            case "sin_barreras_reportadas" -> "estado-ok";
            default -> "estado-info";
        };
    }

    /**
     * Tarjeta con el conteo de un tipo de respuesta. Con {@code alPulsar} es un botón que lleva a su pestaña;
     * sin él es solo informativa y no entra en el orden de tabulación.
     */
    private static Node tarjetaMetrica(String nombre, int cantidad, Valor valor, String estilo, Runnable alPulsar) {
        var numero = new Label(String.valueOf(cantidad));
        numero.getStyleClass().add("numero-metrica");
        Movimiento.contar(numero, cantidad, actual -> numero.setText(String.valueOf(actual)));
        // Rótulo sin ajuste de línea: con ajuste, dentro de un botón la altura de la tarjeta salía distinta.
        var rotulo = new Label(nombre);
        rotulo.getStyleClass().add("rotulo-metrica");
        var textos = new VBox(0, numero, rotulo);
        textos.setAlignment(Pos.CENTER_LEFT);
        var contenido = new HBox(14, Iconos.icono(Iconos.codigoValor(valor), "icono-metrica"), textos);
        contenido.setAlignment(Pos.CENTER_LEFT);
        Node tarjeta;
        if (alPulsar == null) {
            contenido.getStyleClass().addAll("tarjeta-metrica", estilo);
            tarjeta = contenido;
        } else {
            var boton = boton("", "tarjeta-metrica", alPulsar);
            boton.getStyleClass().add(estilo);
            boton.setGraphic(contenido);
            boton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            boton.setAlignment(Pos.CENTER_LEFT);
            boton.setAccessibleText(nombre + ": " + cantidad + ". Ver el detalle");
            tarjeta = boton;
        }
        HBox.setHgrow(tarjeta, Priority.ALWAYS);
        ((Region) tarjeta).setMaxWidth(Double.MAX_VALUE);
        ((Region) tarjeta).setMinWidth(0);
        ((Region) tarjeta).setPrefWidth(0);
        return tarjeta;
    }

    /**
     * Reparte las fichas en las columnas que caben (mínimo 380 px cada una). Usa un GridPane porque calcula la
     * altura de cada fila con el ancho real de su columna; un TilePane la medía con otro ancho y cortaba el texto.
     */
    private static final class Mosaico extends GridPane {
        private static final double ANCHO_MINIMO = 380;
        private final List<Node> fichas = new ArrayList<>();
        private int columnas;

        Mosaico() {
            getStyleClass().add("mosaico-recomendaciones");
            setHgap(16);
            setVgap(16);
            widthProperty().addListener((obs, anterior, nuevo) ->
                    organizar(Math.max(1, (int) ((nuevo.doubleValue() + getHgap()) / (ANCHO_MINIMO + getHgap())))));
        }

        void agregar(Node ficha) {
            fichas.add(ficha);
            organizar(Math.max(1, columnas));
        }

        private void organizar(int nuevasColumnas) {
            columnas = nuevasColumnas;
            getChildren().clear();
            getColumnConstraints().clear();
            for (int i = 0; i < columnas; i++) {
                var columna = new ColumnConstraints();
                columna.setPercentWidth(100.0 / columnas);
                getColumnConstraints().add(columna);
            }
            for (int i = 0; i < fichas.size(); i++) {
                add(fichas.get(i), i % columnas, i / columnas);
            }
        }
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
