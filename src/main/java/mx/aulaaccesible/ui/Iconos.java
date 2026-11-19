package mx.aulaaccesible.ui;

import mx.aulaaccesible.model.Valor;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Map;

/**
 * Iconos de presentación. Son decorativos: el nombre accesible siempre lo da el texto de cada control.
 * Cada estado de respuesta usa una forma distinta, así que no depende solo del color.
 */
final class Iconos {
    static final String RESPUESTA_PENDIENTE = "mdi2c-circle-outline";
    private static final String GENERICO = "mdi2c-circle-small";

    private static final Map<String, String> AREAS = Map.of(
            "navegacion_teclado", "mdi2k-keyboard",
            "presentacion_visual", "mdi2e-eye-outline",
            "contenido_multimedia", "mdi2i-image-multiple-outline",
            "formularios_evaluaciones", "mdi2c-clipboard-check-outline",
            "organizacion_comprension", "mdi2f-file-tree-outline",
            "interaccion_control", "mdi2g-gesture-tap");

    private static final Map<Valor, String> VALORES = Map.of(
            Valor.SI, "mdi2c-check-circle",
            Valor.NO, "mdi2c-close-circle",
            Valor.DESCONOCIDO, "mdi2h-help-circle",
            Valor.NO_APLICA, "mdi2m-minus-circle");

    private static final Map<String, String> ESTADOS = Map.of(
            "requiere_atencion_prioritaria", "mdi2a-alert-octagon-outline",
            "requiere_mejoras", "mdi2a-alert-circle-outline",
            "informacion_insuficiente", "mdi2h-help-circle-outline",
            "sin_barreras_reportadas", "mdi2c-check-decagram-outline",
            "sin_criterios_aplicables", "mdi2m-minus-circle-outline");

    private Iconos() {}

    static String codigoArea(String area) { return AREAS.getOrDefault(area, GENERICO); }

    static String codigoValor(Valor valor) {
        return valor == null ? RESPUESTA_PENDIENTE : VALORES.getOrDefault(valor, GENERICO);
    }

    static String codigoEstado(String estado) { return ESTADOS.getOrDefault(estado, GENERICO); }

    static boolean tieneIconoPropioArea(String area) { return AREAS.containsKey(area); }

    static boolean tieneIconoPropioValor(Valor valor) { return VALORES.containsKey(valor); }

    static boolean tieneIconoPropioEstado(String estado) { return ESTADOS.containsKey(estado); }

    static FontIcon area(String area) { return icono(codigoArea(area), "icono-area"); }

    static FontIcon valor(Valor valor) { return icono(codigoValor(valor), "icono-valor"); }

    static FontIcon estado(String estado) { return icono(codigoEstado(estado), "icono-estado"); }

    /** Icono suelto por código, sin significado propio para lectores de pantalla. */
    static FontIcon icono(String codigo, String estilo) {
        var icono = new FontIcon(codigo);
        icono.getStyleClass().add(estilo);
        icono.setAccessibleRole(javafx.scene.AccessibleRole.NODE);
        icono.setFocusTraversable(false);
        return icono;
    }
}
