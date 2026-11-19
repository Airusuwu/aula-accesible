package mx.aulaaccesible.ui;

import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/** Relación de contraste de WCAG entre dos colores (de 1:1 a 21:1). */
final class Contraste {
    /** Mínimo de WCAG AA para texto normal. */
    static final double MINIMO_TEXTO = 4.5;

    private Contraste() {}

    static double relacion(Color a, Color b) {
        double la = luminancia(a);
        double lb = luminancia(b);
        return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
    }

    /** Luminancia relativa de un color sRGB. */
    static double luminancia(Color color) {
        return 0.2126 * lineal(color.getRed()) + 0.7152 * lineal(color.getGreen()) + 0.0722 * lineal(color.getBlue());
    }

    private static double lineal(double canal) {
        return canal <= 0.03928 ? canal / 12.92 : Math.pow((canal + 0.055) / 1.055, 2.4);
    }

    /** Color de un relleno ya resuelto por CSS; negro si no es un color plano. */
    static Color color(Paint relleno) {
        return relleno instanceof Color color ? color : Color.BLACK;
    }

    /** Color de fondo del primer relleno de una región, ya resuelto por CSS. */
    static Color fondo(Region region) {
        Background fondo = region.getBackground();
        if (fondo == null || fondo.getFills().isEmpty()) {
            return Color.WHITE;
        }
        return color(fondo.getFills().get(0).getFill());
    }
}
