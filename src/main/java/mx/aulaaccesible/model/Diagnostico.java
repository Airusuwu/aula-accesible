package mx.aulaaccesible.model;

import java.util.List;

public record Diagnostico(String estado, Conteos conteos, List<Pendiente> pendientes,
                          List<Recomendacion> recomendaciones, List<Problema> problemas) {
    public record Conteos(int favorables, int barreras, int pendientes, int noAplican) {}
    public record Pendiente(String criterio, String motivo) {}
    public record Recomendacion(String criterio, String prioridad, String accion) {}
    public record Evidencia(String criterio, String pregunta, Valor valor) {}
    public record Problema(String id, int regla, String titulo, List<Evidencia> evidencias) {}

    public String tituloEstado() {
        return switch (estado) {
            case "requiere_atencion_prioritaria" -> "Requiere atención prioritaria";
            case "requiere_mejoras" -> "Requiere mejoras";
            case "informacion_insuficiente" -> "Información insuficiente";
            case "sin_barreras_reportadas" -> "Sin barreras reportadas";
            case "sin_criterios_aplicables" -> "Sin criterios aplicables";
            default -> throw new IllegalStateException("Estado desconocido: " + estado);
        };
    }
}
