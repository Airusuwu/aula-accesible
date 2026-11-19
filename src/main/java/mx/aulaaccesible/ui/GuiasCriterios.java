package mx.aulaaccesible.ui;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

final class GuiasCriterios {
    /** Pasos para evaluar, la condición para marcar «Sí» y un ejemplo que cumple y otro que no. */
    record Guia(List<String> pasos, String condicion, String cumple, String noCumple) {}

    private static final Map<String, Guia> POR_CRITERIO = Map.ofEntries(
            entry("teclado_completo", guia("si puedes completar las acciones con el teclado.",
                    "Al usar solo Tab, las flechas, Intro y Espacio puedes abrir lecciones, responder y enviar la actividad.",
                    "Hay botones o menús que solo responden al clic del ratón y no puedes llegar a ellos con Tab.",
                    "Abre la sección y aparta el ratón.",
                    "Usa Tab y las flechas para llegar a los controles. Prueba activarlos con Intro o Espacio.")),
            entry("foco_visible", guia("si el foco se distingue en todos los controles.",
                    "Al presionar Tab, el botón «Enviar» muestra un borde grueso de otro color.",
                    "Al presionar Tab no cambia nada visible y no sabes qué control está activo.",
                    "Abre la sección que estás evaluando.",
                    "Presiona Tab para recorrer botones, enlaces y campos.",
                    "Observa si cada control muestra un borde u otra señal visible.")),
            entry("orden_foco", guia("si el foco sigue una secuencia que permite usar la sección.",
                    "El foco pasa del nombre al correo y luego a la contraseña, igual que se leen en pantalla.",
                    "El foco salta del primer campo al último y después vuelve al segundo.",
                    "Recorre la sección con Tab y vuelve con Mayús + Tab.",
                    "Observa si el foco sigue el orden de lectura y de las acciones.")),
            entry("sin_trampas_teclado", guia("si puedes salir de cada componente sin usar el ratón.",
                    "Un cuadro de diálogo se cierra con Esc o con su botón «Cerrar» y el foco vuelve a donde estabas.",
                    "Al abrir un menú con el teclado el foco queda atrapado y solo el ratón permite salir.",
                    "Abre los menús, diálogos y controles interactivos con el teclado.",
                    "Comprueba que puedas cerrarlos o continuar con Tab, Mayús + Tab o Esc, según el control.")),
            entry("contraste_legible", guia("si la herramienta confirma un contraste suficiente en los textos comprobados.",
                    "El texto oscuro sobre fondo claro alcanza una relación de contraste de al menos 4.5 a 1.",
                    "El texto gris claro sobre fondo blanco casi no se distingue y la herramienta marca un contraste bajo.",
                    "Identifica el color de un texto y el fondo sobre el que aparece.",
                    "Mide el contraste con una herramienta y revisa también los textos pequeños y de ayuda.")),
            entry("informacion_sin_color", guia("si toda la información importante tiene una señal adicional al color.",
                    "El campo con error tiene borde rojo, un icono y el texto «Falta tu correo».",
                    "El único aviso de error es que el borde del campo se pone rojo.",
                    "Busca mensajes de error, indicadores y elementos que cambien de color.",
                    "Comprueba que puedas entenderlos por su texto, forma, icono o patrón.")),
            entry("ampliacion_texto", guia("si al aumentar el texto puedes leer y usar todo el contenido.",
                    "Con el texto al 200 % las líneas se acomodan en varias filas y todo sigue disponible.",
                    "Con el texto ampliado las frases se cortan con «…» o los botones quedan tapados.",
                    "Aumenta el tamaño del texto o el zoom desde la aplicación o el sistema.",
                    "Recorre la sección y comprueba que los textos y controles sigan disponibles.")),
            entry("redistribucion_contenido", guia("si la vista estrecha mantiene legibles y utilizables los elementos.",
                    "En una ventana estrecha los botones se apilan uno bajo otro y todo se lee completo.",
                    "En una ventana estrecha los botones se encimaron o se salen del borde y no se pueden usar.",
                    "Reduce el ancho de la ventana o abre la sección en una pantalla estrecha.",
                    "Revisa si se superponen elementos o se cortan textos y acciones.")),
            entry("alternativas_imagenes", guia("si cada imagen informativa tiene una alternativa textual equivalente.",
                    "Una gráfica tiene el texto alternativo «Gráfica: el 60 % del grupo aprobó el examen».",
                    "La gráfica solo tiene el nombre del archivo, «imagen1.png», como descripción.",
                    "Localiza imágenes que comuniquen información necesaria.",
                    "Comprueba con un lector de pantalla o en la descripción de la imagen si existe un texto equivalente.")),
            entry("subtitulos_videos", guia("si los videos con voz tienen subtítulos comprensibles y sincronizados.",
                    "El video ofrece subtítulos que muestran lo que dice la persona al mismo tiempo que habla.",
                    "El video tiene voz, pero no ofrece subtítulos o aparecen desfasados del audio.",
                    "Abre un video con voz y busca el control de subtítulos.",
                    "Actívalos y comprueba que el texto corresponda a lo que se escucha.")),
            entry("transcripcion_audios", guia("si puedes acceder a una transcripción equivalente de cada audio.",
                    "Junto al audio de la clase hay un enlace «Ver transcripción» con todo lo que se dice.",
                    "El audio es la única forma de conocer la explicación y no hay texto equivalente.",
                    "Busca una transcripción junto a cada recurso de solo audio.",
                    "Compara un fragmento del texto con lo que se escucha.")),
            entry("alternativa_visual_video", guia("si la información visual importante también se describe.",
                    "Cuando aparece un diagrama sin voz, la narración lo describe: «El agua sube del mar a las nubes».",
                    "El video muestra pasos solo con imágenes y nadie los explica con palabras ni con texto.",
                    "Identifica lo que solo se comunica mediante imágenes o acciones en el video.",
                    "Busca esa información en la narración, una pista de descripción o un texto equivalente.")),
            entry("etiquetas_campos", guia("si cada campo tiene una etiqueta asociada que aclara su propósito.",
                    "Sobre el campo se lee «Correo electrónico» y un lector de pantalla lo anuncia al entrar.",
                    "El campo solo muestra el texto de ejemplo «Escribe aquí», que desaparece al escribir.",
                    "Recorre los campos y comprueba que cada uno tenga un nombre visible.",
                    "Comprueba con un lector de pantalla o en la configuración del formulario que el nombre esté asociado.")),
            entry("instrucciones_campos", guia("si los requisitos de captura se conocen antes de llenar el formulario.",
                    "La etiqueta dice «Fecha (dd/mm/aaaa) · Obligatorio» antes de que escribas.",
                    "El campo solo dice «Fecha» y el formato se descubre al recibir un error.",
                    "Revisa las indicaciones antes de escribir en los campos.",
                    "Busca avisos sobre campos obligatorios, formatos y requisitos que afecten la captura.")),
            entry("errores_explicados", guia("si el mensaje dice qué corregir y cómo hacerlo.",
                    "El mensaje dice «Correo: falta la @. Ejemplo: ana@escuela.mx» junto al campo con error.",
                    "El mensaje solo dice «Error en el formulario» y no indica cuál campo ni qué cambiar.",
                    "En una prueba, deja un campo obligatorio vacío o escribe un valor con formato incorrecto.",
                    "Comprueba que el mensaje identifique el campo y explique cómo solucionar el error.")),
            entry("revision_envio", guia("si puedes revisar y corregir antes de la entrega definitiva.",
                    "Antes de entregar aparece un resumen con tus respuestas, un botón «Editar» y otro «Enviar definitivamente».",
                    "El botón «Enviar» entrega la actividad de inmediato, sin resumen ni forma de corregir.",
                    "Llena una actividad de prueba hasta llegar al paso de entrega.",
                    "Busca una forma de revisar y cambiar las respuestas antes de confirmar.")),
            entry("encabezados_estructurados", guia("si los encabezados reflejan una jerarquía coherente.",
                    "Hay un título principal, bajo él los temas y bajo cada tema sus apartados, en orden.",
                    "Los títulos son solo texto grande sin nivel, o se salta del nivel 1 al nivel 4.",
                    "Identifica el título principal y los títulos de las subsecciones.",
                    "Comprueba su estructura con un lector de pantalla o el esquema del documento.")),
            entry("enlaces_descriptivos", guia("si el texto de cada enlace permite anticipar su destino o función.",
                    "El enlace dice «Descargar guía de la unidad 2 (PDF)» y se entiende sin leer lo que lo rodea.",
                    "El enlace dice «Haz clic aquí» y fuera de contexto no se sabe qué abre.",
                    "Lee los enlaces y revisa qué abre o hace cada uno.",
                    "Busca enlaces con textos ambiguos como «Aquí» o «Más» y comprueba si su nombre los distingue.")),
            entry("instrucciones_claras", guia("si las instrucciones permiten saber qué hacer y en qué orden.",
                    "La actividad enumera los pasos: 1. Lee el texto. 2. Responde. 3. Envía.",
                    "Las indicaciones son un párrafo largo donde no se distingue qué se hace primero.",
                    "Lee las instrucciones de una actividad antes de comenzarla.",
                    "Sigue los pasos y comprueba si queda claro qué se espera en cada uno.")),
            entry("navegacion_consistente", guia("si los controles repetidos mantienen su nombre, ubicación y función.",
                    "En todas las pantallas el menú está arriba y lista «Inicio», «Lecciones» y «Ayuda» en el mismo orden.",
                    "En cada pantalla el menú cambia de lugar o de nombre: a veces «Ayuda» y a veces «Soporte».",
                    "Compara los menús y los controles de navegación en varias pantallas de la sección.",
                    "Comprueba si las mismas acciones se encuentran y se identifican de forma consistente.")),
            entry("tiempo_ajustable", guia("si puedes ampliar o desactivar el límite cuando corresponda.",
                    "El examen muestra el tiempo restante y un botón «Ampliar tiempo» o un ajuste previo.",
                    "El examen se cierra solo a los cinco minutos y no hay forma de ampliar el tiempo.",
                    "Identifica si la actividad tiene un límite de tiempo.",
                    "Revisa sus ajustes y avisos para ver si puedes ampliar o desactivar el límite.")),
            entry("control_movimiento", guia("si encuentras un control para pausar los elementos en movimiento.",
                    "El carrusel de imágenes cambia solo y tiene un botón «Pausar» a la vista.",
                    "El carrusel y las animaciones se mueven sin parar y no existe un control para detenerlos.",
                    "Busca contenido que se anime, desplace o cambie de forma automática.",
                    "Identifica el control para pausarlo y comprueba que detiene el movimiento.")),
            entry("control_audio", guia("si puedes detener o controlar por separado el audio automático.",
                    "Si suena un audio al abrir la sección, tiene su propio botón de pausa y de volumen.",
                    "Suena música al abrir la sección y solo se calla bajando el volumen de todo el equipo.",
                    "Abre la sección y comprueba si se reproduce audio sin pedirlo.",
                    "Prueba detenerlo o ajustar su volumen sin cambiar el volumen general del equipo.")),
            entry("alternativa_arrastre", guia("si puedes completar la actividad sin arrastrar.",
                    "Además de arrastrar la tarjeta, hay un botón «Mover a…» que hace lo mismo con el teclado.",
                    "Para ordenar los elementos solo se puede arrastrar con el ratón.",
                    "Abre una actividad que pida arrastrar elementos.",
                    "Busca una alternativa con teclado, botones o menús y prueba completar la misma acción."))
    );

    private GuiasCriterios() {}

    static Guia para(String id) {
        return POR_CRITERIO.get(id);
    }

    static Set<String> ids() {
        return POR_CRITERIO.keySet();
    }

    private static Guia guia(String condicion, String cumple, String noCumple, String... pasos) {
        return new Guia(List.of(pasos), condicion, cumple, noCumple);
    }
}
