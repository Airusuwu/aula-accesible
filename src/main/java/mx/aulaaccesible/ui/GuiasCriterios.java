package mx.aulaaccesible.ui;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

final class GuiasCriterios {
    record Guia(List<String> pasos, String condicion) {}

    private static final Map<String, Guia> POR_CRITERIO = Map.ofEntries(
            entry("teclado_completo", guia("si puedes completar las acciones con el teclado.",
                    "Abre la sección y aparta el ratón.",
                    "Usa Tab y las flechas para llegar a los controles. Prueba activarlos con Intro o Espacio.")),
            entry("foco_visible", guia("si el foco se distingue en todos los controles.",
                    "Abre la sección que estás evaluando.",
                    "Presiona Tab para recorrer botones, enlaces y campos.",
                    "Observa si cada control muestra un borde u otra señal visible.")),
            entry("orden_foco", guia("si el foco sigue una secuencia que permite usar la sección.",
                    "Recorre la sección con Tab y vuelve con Mayús + Tab.",
                    "Observa si el foco sigue el orden de lectura y de las acciones.")),
            entry("sin_trampas_teclado", guia("si puedes salir de cada componente sin usar el ratón.",
                    "Abre los menús, diálogos y controles interactivos con el teclado.",
                    "Comprueba que puedas cerrarlos o continuar con Tab, Mayús + Tab o Esc, según el control.")),
            entry("contraste_legible", guia("si la herramienta confirma un contraste suficiente en los textos comprobados.",
                    "Identifica el color de un texto y el fondo sobre el que aparece.",
                    "Mide el contraste con una herramienta y revisa también los textos pequeños y de ayuda.")),
            entry("informacion_sin_color", guia("si toda la información importante tiene una señal adicional al color.",
                    "Busca mensajes de error, indicadores y elementos que cambien de color.",
                    "Comprueba que puedas entenderlos por su texto, forma, icono o patrón.")),
            entry("ampliacion_texto", guia("si al aumentar el texto puedes leer y usar todo el contenido.",
                    "Aumenta el tamaño del texto o el zoom desde la aplicación o el sistema.",
                    "Recorre la sección y comprueba que los textos y controles sigan disponibles.")),
            entry("redistribucion_contenido", guia("si la vista estrecha mantiene legibles y utilizables los elementos.",
                    "Reduce el ancho de la ventana o abre la sección en una pantalla estrecha.",
                    "Revisa si se superponen elementos o se cortan textos y acciones.")),
            entry("alternativas_imagenes", guia("si cada imagen informativa tiene una alternativa textual equivalente.",
                    "Localiza imágenes que comuniquen información necesaria.",
                    "Comprueba con un lector de pantalla o en la descripción de la imagen si existe un texto equivalente.")),
            entry("subtitulos_videos", guia("si los videos con voz tienen subtítulos comprensibles y sincronizados.",
                    "Abre un video con voz y busca el control de subtítulos.",
                    "Actívalos y comprueba que el texto corresponda a lo que se escucha.")),
            entry("transcripcion_audios", guia("si puedes acceder a una transcripción equivalente de cada audio.",
                    "Busca una transcripción junto a cada recurso de solo audio.",
                    "Compara un fragmento del texto con lo que se escucha.")),
            entry("alternativa_visual_video", guia("si la información visual importante también se describe.",
                    "Identifica lo que solo se comunica mediante imágenes o acciones en el video.",
                    "Busca esa información en la narración, una pista de descripción o un texto equivalente.")),
            entry("etiquetas_campos", guia("si cada campo tiene una etiqueta asociada que aclara su propósito.",
                    "Recorre los campos y comprueba que cada uno tenga un nombre visible.",
                    "Comprueba con un lector de pantalla o en la configuración del formulario que el nombre esté asociado.")),
            entry("instrucciones_campos", guia("si los requisitos de captura se conocen antes de llenar el formulario.",
                    "Revisa las indicaciones antes de escribir en los campos.",
                    "Busca avisos sobre campos obligatorios, formatos y requisitos que afecten la captura.")),
            entry("errores_explicados", guia("si el mensaje dice qué corregir y cómo hacerlo.",
                    "En una prueba, deja un campo obligatorio vacío o escribe un valor con formato incorrecto.",
                    "Comprueba que el mensaje identifique el campo y explique cómo solucionar el error.")),
            entry("revision_envio", guia("si puedes revisar y corregir antes de la entrega definitiva.",
                    "Llena una actividad de prueba hasta llegar al paso de entrega.",
                    "Busca una forma de revisar y cambiar las respuestas antes de confirmar.")),
            entry("encabezados_estructurados", guia("si los encabezados reflejan una jerarquía coherente.",
                    "Identifica el título principal y los títulos de las subsecciones.",
                    "Comprueba su estructura con un lector de pantalla o el esquema del documento.")),
            entry("enlaces_descriptivos", guia("si el texto de cada enlace permite anticipar su destino o función.",
                    "Lee los enlaces y revisa qué abre o hace cada uno.",
                    "Busca enlaces con textos ambiguos como «Aquí» o «Más» y comprueba si su nombre los distingue.")),
            entry("instrucciones_claras", guia("si las instrucciones permiten saber qué hacer y en qué orden.",
                    "Lee las instrucciones de una actividad antes de comenzarla.",
                    "Sigue los pasos y comprueba si queda claro qué se espera en cada uno.")),
            entry("navegacion_consistente", guia("si los controles repetidos mantienen su nombre, ubicación y función.",
                    "Compara los menús y los controles de navegación en varias pantallas de la sección.",
                    "Comprueba si las mismas acciones se encuentran y se identifican de forma consistente.")),
            entry("tiempo_ajustable", guia("si puedes ampliar o desactivar el límite cuando corresponda.",
                    "Identifica si la actividad tiene un límite de tiempo.",
                    "Revisa sus ajustes y avisos para ver si puedes ampliar o desactivar el límite.")),
            entry("control_movimiento", guia("si encuentras un control para pausar los elementos en movimiento.",
                    "Busca contenido que se anime, desplace o cambie de forma automática.",
                    "Identifica el control para pausarlo y comprueba que detiene el movimiento.")),
            entry("control_audio", guia("si puedes detener o controlar por separado el audio automático.",
                    "Abre la sección y comprueba si se reproduce audio sin pedirlo.",
                    "Prueba detenerlo o ajustar su volumen sin cambiar el volumen general del equipo.")),
            entry("alternativa_arrastre", guia("si puedes completar la actividad sin arrastrar.",
                    "Abre una actividad que pida arrastrar elementos.",
                    "Busca una alternativa con teclado, botones o menús y prueba completar la misma acción."))
    );

    private GuiasCriterios() {}

    static Guia para(String id) {
        return POR_CRITERIO.get(id);
    }

    private static Guia guia(String condicion, String... pasos) {
        return new Guia(List.of(pasos), condicion);
    }
}
