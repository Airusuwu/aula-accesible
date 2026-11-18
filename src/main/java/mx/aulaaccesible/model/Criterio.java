package mx.aulaaccesible.model;

public record Criterio(String id, int orden, String area, String nombreArea,
                       String prioridad, String pregunta, String recomendacion, String motivoNoAplica) {
    public boolean permiteNoAplica() {
        return !motivoNoAplica.isBlank();
    }

    public String titulo() {
        return switch (id) {
            case "teclado_completo" -> "Operación con teclado";
            case "foco_visible" -> "Foco visible";
            case "orden_foco" -> "Orden del foco";
            case "sin_trampas_teclado" -> "Salida de controles con teclado";
            case "contraste_legible" -> "Contraste del texto";
            case "informacion_sin_color" -> "Información sin depender del color";
            case "ampliacion_texto" -> "Ampliación del texto";
            case "redistribucion_contenido" -> "Redistribución del contenido";
            case "alternativas_imagenes" -> "Alternativas para imágenes";
            case "subtitulos_videos" -> "Subtítulos en videos";
            case "transcripcion_audios" -> "Transcripción de audios";
            case "alternativa_arrastre" -> "Alternativa al arrastre";
            case "etiquetas_campos" -> "Etiquetas de campos";
            case "instrucciones_campos" -> "Instrucciones de captura";
            case "errores_explicados" -> "Explicación de errores";
            case "revision_envio" -> "Revisión antes del envío";
            case "encabezados_estructurados" -> "Estructura de encabezados";
            case "enlaces_descriptivos" -> "Enlaces descriptivos";
            case "instrucciones_claras" -> "Instrucciones claras";
            case "navegacion_consistente" -> "Navegación consistente";
            case "tiempo_ajustable" -> "Tiempo ajustable";
            case "control_movimiento" -> "Control del movimiento";
            case "control_audio" -> "Control del audio";
            case "alternativa_visual_video" -> "Descripción visual del video";
            default -> pregunta;
        };
    }
}
