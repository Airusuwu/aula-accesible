package mx.aulaaccesible.ui;

import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mx.aulaaccesible.model.Criterio;
import mx.aulaaccesible.model.Diagnostico;
import mx.aulaaccesible.model.Evaluacion;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mx.aulaaccesible.ui.Controles.*;

final class Resultados {
    private static final PseudoClass COMPACTO = PseudoClass.getPseudoClass("compacto");
    private final Evaluacion evaluacion;
    private final Map<String, Criterio> criterios;
    private final Diagnostico diagnostico;
    private final Label titulo;
    private final BorderPane vista;

    Resultados(Evaluacion evaluacion, List<Criterio> catalogo, Diagnostico diagnostico,
               Runnable editar, Runnable nueva, Runnable inicio) {
        this.evaluacion = evaluacion;
        this.diagnostico = diagnostico;
        criterios = catalogo.stream().collect(Collectors.toMap(Criterio::id, Function.identity()));

        titulo = texto(diagnostico.tituloEstado(), "titulo");
        titulo.setFocusTraversable(true);
        String contexto = Stream.of(evaluacion.software(), evaluacion.seccion())
                .filter(s -> !s.isBlank()).collect(Collectors.joining(" · "));
        var resumen = caja(3, "resumen", texto("DIAGNÓSTICO DE ACCESIBILIDAD", "sobrelinea"), titulo);
        resumen.getStyleClass().add(switch (diagnostico.estado()) {
            case "requiere_atencion_prioritaria", "requiere_mejoras" -> "resumen-barrera";
            case "informacion_insuficiente" -> "resumen-pendiente";
            case "sin_barreras_reportadas" -> "resumen-favorable";
            default -> "resumen-neutro";
        });
        if (!contexto.isBlank()) {
            var nombre = texto(contexto, "contexto");
            nombre.setWrapText(false);
            nombre.setTooltip(new Tooltip(contexto));
            resumen.getChildren().add(nombre);
        }

        var conteos = diagnostico.conteos();
        var metricas = fila("metricas", metrica(conteos.favorables(), "Favorables", "favorable"),
                metrica(conteos.barreras(), "Barreras", "barrera"), metrica(conteos.pendientes(), "Pendientes", "pendiente"),
                metrica(conteos.noAplican(), "No aplican", "neutra"));
        var encabezado = caja(10, "encabezado-resultados", resumen, metricas);
        var problemas = pestana("Problemas combinados", diagnostico.problemas().size(), problemas());
        var recomendaciones = pestana("Recomendaciones", conteos.barreras(), recomendaciones());
        var pendientes = pestana("Pendientes", conteos.pendientes(), pendientes());
        var pestanas = new TabPane(problemas, recomendaciones, pendientes);
        pestanas.getStyleClass().add("pestanas-diagnostico");
        pestanas.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        if (diagnostico.problemas().isEmpty()) {
            if (!diagnostico.recomendaciones().isEmpty()) {
                pestanas.getSelectionModel().select(recomendaciones);
            } else if (!diagnostico.pendientes().isEmpty()) {
                pestanas.getSelectionModel().select(pendientes);
            }
        }

        HBox pie = fila("pie-acciones",
                boton("Volver al inicio", "enlace", inicio),
                boton("Nueva evaluación", "secundario", nueva), espacio(),
                boton("Editar respuestas", "primario", editar));
        vista = new BorderPane();
        vista.getStyleClass().add("vista-resultados");
        vista.heightProperty().addListener((obs, anterior, alto) ->
                vista.pseudoClassStateChanged(COMPACTO, alto.doubleValue() < 470));
        vista.setTop(encabezado);
        vista.setCenter(pestanas);
        vista.setBottom(pie);
    }

    BorderPane vista() { return vista; }

    void enfocarTitulo() { enfocar(titulo); }

    private VBox metrica(int cantidad, String etiqueta, String estilo) {
        VBox tarjeta = caja(2, "metrica", texto(String.valueOf(cantidad), "numero"), texto(etiqueta, "etiqueta-campo"));
        tarjeta.getStyleClass().add(estilo);
        tarjeta.setMinWidth(0);
        tarjeta.setPrefWidth(0);
        tarjeta.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(tarjeta, Priority.ALWAYS);
        return tarjeta;
    }

    private VBox problemas() {
        VBox panel = caja(9, "lista-resultados");
        if (diagnostico.problemas().isEmpty()) {
            panel.getChildren().add(vacio("Las respuestas no activaron problemas combinados."));
        }
        for (var problema : diagnostico.problemas()) {
            VBox detalle = caja(9, "detalle", texto("Regla " + problema.regla() + " · Respuestas que la activaron", "ayuda"));
            for (var evidencia : problema.evidencias()) {
                var criterio = criterios.get(evidencia.criterio());
                var item = caja(4, "hallazgo",
                        texto(criterio.titulo() + " · Respuesta: " + evidencia.valor(), "subtitulo"),
                        texto(evidencia.pregunta(), "texto"));
                agregarUbicacion(item, criterio.id());
                detalle.getChildren().add(item);
            }
            panel.getChildren().add(desplegable(problema.titulo(), detalle));
        }
        return panel;
    }

    private VBox pendientes() {
        VBox panel = caja(8, "lista-resultados");
        if (diagnostico.pendientes().isEmpty()) {
            panel.getChildren().add(vacio("No hay criterios pendientes."));
            return panel;
        }
        for (var pendiente : diagnostico.pendientes()) {
            var criterio = criterios.get(pendiente.criterio());
            String motivo = pendiente.motivo().equals("desconocido") ? "Requiere verificación" : "Sin responder";
            var item = caja(4, "hallazgo", texto(criterio.titulo(), "subtitulo"),
                    texto(motivo, "estado-pendiente"), texto(criterio.pregunta(), "texto"));
            agregarUbicacion(item, criterio.id());
            panel.getChildren().add(item);
        }
        return panel;
    }

    private VBox recomendaciones() {
        VBox panel = caja(8, "lista-resultados");
        if (diagnostico.recomendaciones().isEmpty()) {
            panel.getChildren().add(vacio("No hay recomendaciones por barreras reportadas."));
        }
        for (var recomendacion : diagnostico.recomendaciones()) {
            var criterio = criterios.get(recomendacion.criterio());
            String prioridad = recomendacion.prioridad();
            var item = caja(5, "hallazgo",
                    fila("fila-titulo", texto(criterio.titulo(), "subtitulo"), espacio(),
                            texto("Prioridad " + prioridad, "prioridad-" + prioridad)),
                    texto(recomendacion.accion(), "texto"));
            agregarUbicacion(item, criterio.id());
            panel.getChildren().add(item);
        }
        return panel;
    }

    private static Tab pestana(String titulo, int cantidad, VBox panel) {
        panel.getChildren().add(texto("Diagnóstico orientativo basado en las respuestas de esta evaluación.", "ayuda"));
        return new Tab(titulo + " (" + cantidad + ")", desplazar(panel));
    }

    private static Node vacio(String mensaje) {
        return caja(0, "hallazgo", texto(mensaje, "texto"));
    }

    private void agregarUbicacion(VBox item, String id) {
        var respuesta = evaluacion.respuesta(id);
        if (respuesta != null && !respuesta.ubicacion().isBlank()) {
            item.getChildren().add(texto("Ubicación: " + respuesta.ubicacion(), "ubicacion"));
        }
    }
}
