package mx.aulaaccesible.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Map.entry;
import static mx.aulaaccesible.ui.Controles.caja;
import static mx.aulaaccesible.ui.Controles.fila;
import static mx.aulaaccesible.ui.Controles.texto;

/**
 * Mini demos visuales de «Así se cumple» y «Así no se cumple». Son ilustraciones estáticas hechas con etiquetas y
 * contenedores: no reciben foco ni ratón, no se animan y no tienen significado propio para un lector de pantalla,
 * porque el texto de la ficha ya describe el ejemplo. Cada demo se construye con un {@link Supplier} porque la guía
 * existe en dos árboles de nodos (panel lateral y plegable) y un nodo no puede estar en los dos.
 */
final class MuestrasCriterios {
    record Muestra(Supplier<Node> cumple, Supplier<Node> noCumple) {}

    private static final double ANCHO_ESTRECHO = 200;

    private static final Map<String, Muestra> POR_CRITERIO = Map.ofEntries(
            entry("foco_visible", muestra(
                    () -> demo(boton("Enviar", true)),
                    () -> demo(boton("Enviar", false)))),
            entry("orden_foco", muestra(
                    () -> demo(paso(1, "Nombre"), paso(2, "Correo"), paso(3, "Contraseña")),
                    () -> demo(paso(1, "Nombre"), paso(3, "Correo"), paso(2, "Contraseña")))),
            entry("sin_trampas_teclado", muestra(
                    () -> demo(dialogo(true)),
                    () -> demo(dialogo(false)))),
            entry("contraste_legible", muestra(
                    () -> demo(texto("Entrega tu tarea antes del viernes.", "demo-texto")),
                    () -> demo(texto("Entrega tu tarea antes del viernes.", "demo-tenue")))),
            entry("informacion_sin_color", muestra(
                    () -> demo(campoConError(true)),
                    () -> demo(campoConError(false)))),
            entry("ampliacion_texto", muestra(
                    () -> demo(texto("Lee la lección completa antes de responder.", "demo-grande")),
                    () -> demo(recortado("Lee la lección completa antes de responder.")))),
            entry("redistribucion_contenido", muestra(
                    () -> demo(marcoEstrecho(new VBox(6, boton("Anterior", false), boton("Siguiente", false),
                            boton("Enviar", false)))),
                    () -> demo(marcoEstrecho(new HBox(6, boton("Anterior", false), boton("Siguiente", false),
                            boton("Enviar", false)))))),
            entry("alternativas_imagenes", muestra(
                    () -> demo(imagen("Gráfica: el 60 % del grupo aprobó el examen")),
                    () -> demo(imagen("imagen1.png")))),
            entry("subtitulos_videos", muestra(
                    () -> demo(video(true)),
                    () -> demo(video(false)))),
            entry("transcripcion_audios", muestra(
                    () -> demo(audio(true), enlace("Ver transcripción")),
                    () -> demo(audio(true)))),
            entry("etiquetas_campos", muestra(
                    () -> demo(texto("Correo electrónico", "demo-etiqueta"), campo("ana@escuela.mx", false)),
                    () -> demo(campo("Escribe aquí", true)))),
            entry("instrucciones_campos", muestra(
                    () -> demo(texto("Fecha (dd/mm/aaaa) · Obligatorio", "demo-etiqueta"), campo("", false)),
                    () -> demo(texto("Fecha", "demo-etiqueta"), campo("", false)))),
            entry("errores_explicados", muestra(
                    () -> demo(aviso("Correo: falta la @. Ejemplo: ana@escuela.mx")),
                    () -> demo(aviso("Error en el formulario")))),
            entry("revision_envio", muestra(
                    () -> demo(texto("Pregunta 1: Verdadero · Pregunta 2: Falso", "demo-texto"),
                            new HBox(8, boton("Editar", false), boton("Enviar definitivamente", false))),
                    () -> demo(boton("Enviar", false)))),
            entry("encabezados_estructurados", muestra(
                    () -> demo(esquema("Unidad 2", 0), esquema("Tema: Fracciones", 1), esquema("Ejemplos", 2)),
                    () -> demo(esquema("Unidad 2", 0), esquema("Ejemplos", 3), esquema("Tema: Fracciones", 0)))),
            entry("enlaces_descriptivos", muestra(
                    () -> demo(enlace("Descargar guía de la unidad 2 (PDF)")),
                    () -> demo(enlace("Haz clic aquí")))),
            entry("instrucciones_claras", muestra(
                    () -> demo(texto("1. Lee el texto.", "demo-texto"), texto("2. Responde las preguntas.", "demo-texto"),
                            texto("3. Envía la actividad.", "demo-texto")),
                    () -> demo(texto("Lee el texto y luego responde las preguntas, revisa tus respuestas y después "
                            + "envía la actividad cuando termines todo lo anterior.", "demo-texto")))),
            entry("navegacion_consistente", muestra(
                    () -> demo(menu("Inicio", "Lecciones", "Ayuda"), menu("Inicio", "Lecciones", "Ayuda")),
                    () -> demo(menu("Inicio", "Lecciones", "Ayuda"), menu("Lecciones", "Soporte", "Inicio")))),
            entry("tiempo_ajustable", muestra(
                    () -> demo(reloj(), boton("Ampliar tiempo", false)),
                    () -> demo(reloj()))),
            entry("control_movimiento", muestra(
                    () -> demo(carrusel(), boton("Pausar", false)),
                    () -> demo(carrusel()))),
            entry("control_audio", muestra(
                    () -> demo(audio(true)),
                    () -> demo(audio(false)))),
            entry("alternativa_arrastre", muestra(
                    () -> demo(tarjetaArrastrable(), boton("Mover a…", false)),
                    () -> demo(tarjetaArrastrable())))
    );

    private MuestrasCriterios() {}

    static Muestra para(String id) {
        return POR_CRITERIO.get(id);
    }

    static Set<String> ids() {
        return POR_CRITERIO.keySet();
    }

    private static Muestra muestra(Supplier<Node> cumple, Supplier<Node> noCumple) {
        return new Muestra(cumple, noCumple);
    }

    /** Contenedor de una demo: solo ilustra, así que ignora el ratón y nunca entra en el recorrido con Tab. */
    private static VBox demo(Node... contenido) {
        var caja = caja(8, "muestra-demo", contenido);
        caja.setMouseTransparent(true);
        caja.setFocusTraversable(false);
        return caja;
    }

    private static Label boton(String nombre, boolean conFoco) {
        var etiqueta = new Label(nombre);
        etiqueta.getStyleClass().add("demo-boton");
        if (conFoco) {
            etiqueta.getStyleClass().add("demo-foco");
        }
        etiqueta.setMinWidth(Region.USE_PREF_SIZE);
        return etiqueta;
    }

    private static Label campo(String contenido, boolean marcador) {
        var etiqueta = new Label(contenido.isEmpty() ? " " : contenido);
        etiqueta.getStyleClass().addAll("demo-campo", marcador ? "demo-marcador" : "demo-texto");
        etiqueta.setMaxWidth(Double.MAX_VALUE);
        return etiqueta;
    }

    private static HBox paso(int numero, String nombre) {
        var insignia = new Label(String.valueOf(numero));
        insignia.getStyleClass().add("demo-numero");
        insignia.setMinWidth(Region.USE_PREF_SIZE);
        var fila = new HBox(8, insignia, campo(nombre, false));
        fila.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(fila.getChildren().get(1), javafx.scene.layout.Priority.ALWAYS);
        return fila;
    }

    private static VBox dialogo(boolean conSalida) {
        var contenido = caja(8, "demo-dialogo", texto("Elige un tema", "demo-etiqueta"), campo("Fracciones", false));
        if (conSalida) {
            contenido.getChildren().add(boton("Cerrar (Esc)", false));
        }
        return contenido;
    }

    private static Node campoConError(boolean conSenal) {
        var campo = campo("ana@escuela", false);
        campo.getStyleClass().add("demo-error");
        if (!conSenal) {
            return campo;
        }
        var aviso = fila("demo-aviso", Iconos.icono("mdi2a-alert-circle", "icono-demo-error"),
                texto("Falta tu correo", "demo-error-texto"));
        return caja(6, "demo-grupo", campo, aviso);
    }

    /** Texto grande cortado con «…»: es el defecto que se quiere ilustrar. */
    private static Label recortado(String contenido) {
        var etiqueta = new Label(contenido);
        etiqueta.getStyleClass().add("demo-grande");
        etiqueta.setWrapText(false);
        etiqueta.setTextOverrun(OverrunStyle.ELLIPSIS);
        etiqueta.setMaxWidth(ANCHO_ESTRECHO);
        etiqueta.setMinWidth(0);
        return etiqueta;
    }

    /** Marco de 200 px de ancho que recorta lo que se sale, para mostrar si el contenido se adapta o no. */
    private static Node marcoEstrecho(javafx.scene.layout.Pane contenido) {
        contenido.getStyleClass().add("demo-marco-estrecho");
        contenido.setMinWidth(ANCHO_ESTRECHO);
        contenido.setPrefWidth(ANCHO_ESTRECHO);
        contenido.setMaxWidth(ANCHO_ESTRECHO);
        var recorte = new Rectangle();
        recorte.widthProperty().bind(contenido.widthProperty());
        recorte.heightProperty().bind(contenido.heightProperty());
        contenido.setClip(recorte);
        return contenido;
    }

    private static HBox imagen(String alternativa) {
        var icono = Iconos.icono("mdi2i-image-outline", "icono-demo");
        var marco = new HBox(icono);
        marco.getStyleClass().add("demo-imagen");
        marco.setAlignment(Pos.CENTER);
        marco.setMinWidth(Region.USE_PREF_SIZE);
        var texto = texto("Texto alternativo: " + alternativa, "demo-texto");
        var fila = new HBox(10, marco, texto);
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private static VBox video(boolean conSubtitulos) {
        var pantalla = new HBox(Iconos.icono("mdi2p-play-circle-outline", "icono-demo-video"));
        pantalla.getStyleClass().add("demo-video");
        pantalla.setAlignment(Pos.CENTER);
        var contenido = caja(0, "demo-grupo", pantalla);
        if (conSubtitulos) {
            var subtitulo = new Label("Hoy veremos las fracciones.");
            subtitulo.getStyleClass().add("demo-subtitulo");
            subtitulo.setWrapText(true);
            subtitulo.setMaxWidth(Double.MAX_VALUE);
            contenido.getChildren().add(subtitulo);
        }
        return contenido;
    }

    private static HBox audio(boolean conControles) {
        var fila = new HBox(10, Iconos.icono("mdi2m-music-note", "icono-demo"));
        fila.getStyleClass().add("demo-audio");
        fila.setAlignment(Pos.CENTER_LEFT);
        if (conControles) {
            fila.getChildren().addAll(boton("Pausa", false), Iconos.icono("mdi2v-volume-high", "icono-demo"),
                    texto("Volumen", "demo-texto"));
        } else {
            fila.getChildren().add(texto("Reproduciendo…", "demo-texto"));
        }
        return fila;
    }

    private static Label enlace(String contenido) {
        var etiqueta = new Label(contenido);
        etiqueta.getStyleClass().add("demo-enlace");
        etiqueta.setWrapText(true);
        etiqueta.setMinWidth(0);
        return etiqueta;
    }

    private static HBox aviso(String contenido) {
        var fila = fila("demo-aviso", Iconos.icono("mdi2a-alert-circle", "icono-demo-error"),
                texto(contenido, "demo-error-texto"));
        fila.setAlignment(Pos.TOP_LEFT);
        return fila;
    }

    /** Línea del esquema de un documento; el nivel se muestra con la sangría y con el tamaño del texto. */
    private static Label esquema(String nombre, int nivel) {
        var etiqueta = new Label("H" + (nivel + 1) + "  " + nombre);
        etiqueta.getStyleClass().addAll("demo-esquema", "demo-nivel-" + Math.min(nivel, 3));
        etiqueta.setPadding(new javafx.geometry.Insets(0, 0, 0, 16.0 * Math.min(nivel, 3)));
        return etiqueta;
    }

    private static HBox menu(String... opciones) {
        var fila = new HBox(6);
        fila.getStyleClass().add("demo-menu");
        for (String opcion : opciones) {
            var etiqueta = new Label(opcion);
            etiqueta.getStyleClass().add("demo-opcion");
            etiqueta.setMinWidth(Region.USE_PREF_SIZE);
            fila.getChildren().add(etiqueta);
        }
        return fila;
    }

    private static HBox reloj() {
        var fila = new HBox(8, Iconos.icono("mdi2c-clock-outline", "icono-demo"), texto("Tiempo restante: 05:00", "demo-texto"));
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private static HBox carrusel() {
        var fila = new HBox(8, Iconos.icono("mdi2i-image-outline", "icono-demo"), texto("Diapositiva 2 de 5 (cambia sola)", "demo-texto"));
        fila.getStyleClass().add("demo-audio");
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private static HBox tarjetaArrastrable() {
        var fila = new HBox(8, Iconos.icono("mdi2d-drag", "icono-demo"), texto("Capital de México", "demo-texto"));
        fila.getStyleClass().add("demo-audio");
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }
}
