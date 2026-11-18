package mx.aulaaccesible.informe;

import mx.aulaaccesible.model.Criterio;
import mx.aulaaccesible.model.Diagnostico;
import mx.aulaaccesible.model.Evaluacion;
import mx.aulaaccesible.model.Respuesta;
import mx.aulaaccesible.model.Valor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Datos de la evaluación que corresponden al diagnóstico recibido de Prolog. */
public final class InformeEvaluacion {
    public static final String ALCANCE = "Este diagnóstico es orientativo y se basa en las respuestas de esta evaluación.";
    public static final String REQUIERE_VERIFICACION = "Requiere verificación";
    public static final String SIN_RESPONDER = "Sin responder";

    private final String software;
    private final String seccion;
    private final Diagnostico diagnostico;
    private final Map<String, Criterio> criterios;
    private final Map<String, Respuesta> respuestas;
    private final List<Criterio> favorables;
    private final Map<String, Diagnostico.Recomendacion> recomendaciones;

    public InformeEvaluacion(Evaluacion evaluacion, List<Criterio> catalogo, Diagnostico diagnostico) {
        software = evaluacion.software();
        seccion = evaluacion.seccion();
        this.diagnostico = diagnostico;
        criterios = catalogo.stream().collect(Collectors.toUnmodifiableMap(Criterio::id, Function.identity()));
        respuestas = evaluacion.respuestas().stream()
                .collect(Collectors.toUnmodifiableMap(Respuesta::criterio, Function.identity()));
        recomendaciones = diagnostico.recomendaciones().stream()
                .collect(Collectors.toUnmodifiableMap(Diagnostico.Recomendacion::criterio, Function.identity()));
        favorables = catalogo.stream()
                .filter(c -> respuestas.containsKey(c.id()) && respuestas.get(c.id()).valor() == Valor.SI)
                .toList();
        if (favorables.size() != diagnostico.conteos().favorables()) {
            throw new IllegalStateException("El conteo de favorables no coincide con las respuestas. Genera el diagnóstico de nuevo.");
        }
    }

    public String software() { return software; }
    public String seccion() { return seccion; }
    public Diagnostico diagnostico() { return diagnostico; }
    public List<Criterio> favorables() { return favorables; }
    public Criterio criterio(String id) { return criterios.get(id); }

    public String ubicacion(String id) {
        var respuesta = respuestas.get(id);
        return respuesta == null ? "" : respuesta.ubicacion();
    }

    public String accion(String id) {
        var recomendacion = recomendaciones.get(id);
        return recomendacion == null ? criterio(id).recomendacion() : recomendacion.accion();
    }

    public Map<String, List<Diagnostico.Recomendacion>> recomendacionesPorPrioridad() {
        return diagnostico.recomendaciones().stream().collect(Collectors.groupingBy(
                Diagnostico.Recomendacion::prioridad, LinkedHashMap::new, Collectors.toList()));
    }

    public Map<String, List<Diagnostico.Pendiente>> pendientesPorEstado() {
        return diagnostico.pendientes().stream().collect(Collectors.groupingBy(
                pendiente -> "desconocido".equals(pendiente.motivo()) ? REQUIERE_VERIFICACION : SIN_RESPONDER,
                LinkedHashMap::new, Collectors.toList()));
    }

    public String resumen() {
        var c = diagnostico.conteos();
        return cantidad(c.barreras(), "barrera reportada", "barreras reportadas") + " · "
                + cantidad(c.pendientes(), "pendiente", "pendientes") + " · "
                + cantidad(c.favorables(), "favorable", "favorables");
    }

    public String resumenNoAplica() {
        return cantidad(diagnostico.conteos().noAplican(), "criterio marcado como «No aplica»",
                "criterios marcados como «No aplica»");
    }

    public String explicacion() {
        return switch (diagnostico.estado()) {
            case "requiere_atencion_prioritaria" -> "Se reportaron barreras de prioridad alta.";
            case "requiere_mejoras" -> "Se reportaron barreras que conviene corregir.";
            case "informacion_insuficiente" -> "Quedan criterios por responder o verificar.";
            case "sin_barreras_reportadas" -> "Las respuestas de los criterios aplicables fueron favorables.";
            case "sin_criterios_aplicables" -> "Los criterios se marcaron como «No aplica».";
            default -> throw new IllegalStateException("Estado desconocido: " + diagnostico.estado());
        };
    }

    private static String cantidad(int numero, String singular, String plural) {
        return numero + " " + (numero == 1 ? singular : plural);
    }
}
