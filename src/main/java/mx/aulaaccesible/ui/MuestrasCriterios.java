package mx.aulaaccesible.ui;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Map.entry;

/**
 * Registro de los ejemplos interactivos de «Así se cumple» y «Así no se cumple» de cada criterio. Cada ejemplo se
 * construye con un {@link Supplier} porque la guía existe en dos árboles de nodos (panel lateral y plegable) y un
 * nodo no puede estar en los dos. Las demos viven en una clase por área: {@link DemosTeclado},
 * {@link DemosVisual}, {@link DemosMultimedia}, {@link DemosFormularios}, {@link DemosOrganizacion} y
 * {@link DemosInteraccion}.
 */
final class MuestrasCriterios {
    record Muestra(Supplier<Demo> cumple, Supplier<Demo> noCumple) {}

    private static final Map<String, Muestra> POR_CRITERIO = Map.ofEntries(
            entry("teclado_completo", muestra(DemosTeclado::tecladoCompleto)),
            entry("foco_visible", muestra(DemosTeclado::focoVisible)),
            entry("orden_foco", muestra(DemosTeclado::ordenFoco)),
            entry("sin_trampas_teclado", muestra(DemosTeclado::sinTrampasTeclado)),
            entry("contraste_legible", muestra(DemosVisual::contrasteLegible)),
            entry("informacion_sin_color", muestra(DemosVisual::informacionSinColor)),
            entry("ampliacion_texto", muestra(DemosVisual::ampliacionTexto)),
            entry("redistribucion_contenido", muestra(DemosVisual::redistribucionContenido)),
            entry("alternativas_imagenes", muestra(DemosMultimedia::alternativasImagenes)),
            entry("subtitulos_videos", muestra(DemosMultimedia::subtitulosVideos)),
            entry("transcripcion_audios", muestra(DemosMultimedia::transcripcionAudios)),
            entry("alternativa_visual_video", muestra(DemosMultimedia::alternativaVisualVideo)),
            entry("etiquetas_campos", muestra(DemosFormularios::etiquetasCampos)),
            entry("instrucciones_campos", muestra(DemosFormularios::instruccionesCampos)),
            entry("errores_explicados", muestra(DemosFormularios::erroresExplicados)),
            entry("revision_envio", muestra(DemosFormularios::revisionEnvio)),
            entry("encabezados_estructurados", muestra(DemosOrganizacion::encabezadosEstructurados)),
            entry("enlaces_descriptivos", muestra(DemosOrganizacion::enlacesDescriptivos)),
            entry("instrucciones_claras", muestra(DemosOrganizacion::instruccionesClaras)),
            entry("navegacion_consistente", muestra(DemosOrganizacion::navegacionConsistente)),
            entry("tiempo_ajustable", muestra(DemosInteraccion::tiempoAjustable)),
            entry("control_movimiento", muestra(DemosInteraccion::controlMovimiento)),
            entry("control_audio", muestra(DemosInteraccion::controlAudio)),
            entry("alternativa_arrastre", muestra(DemosInteraccion::alternativaArrastre))
    );

    private MuestrasCriterios() {}

    /** Ejemplos del criterio, o {@code null} si no tiene. */
    static Muestra para(String id) {
        return POR_CRITERIO.get(id);
    }

    static Set<String> ids() {
        return POR_CRITERIO.keySet();
    }

    /** Una demo que recibe «cumple» y devuelve el ejemplo correspondiente. */
    private static Muestra muestra(Function<Boolean, Demo> demo) {
        return new Muestra(() -> demo.apply(true), () -> demo.apply(false));
    }
}
