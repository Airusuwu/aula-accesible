package mx.aulaaccesible.ui;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;

/** Aplica el tema oscuro o claro: base de AtlantaFX más la clase {@code tema-claro} para la marca. */
public final class Tema {
    public static final String CLASE_CLARO = "tema-claro";

    private Tema() {}

    /** Cambia la hoja base de la aplicación y la clase de la raíz de la escena. */
    public static void aplicar(Scene escena, boolean oscuro) {
        Application.setUserAgentStylesheet(
                (oscuro ? new PrimerDark() : new PrimerLight()).getUserAgentStylesheet());
        marcar(escena.getRoot(), oscuro);
    }

    /** Copia el tema actual a una raíz ajena, por ejemplo la de un diálogo. */
    public static void marcar(Parent raiz, boolean oscuro) {
        if (oscuro) {
            raiz.getStyleClass().remove(CLASE_CLARO);
        } else if (!raiz.getStyleClass().contains(CLASE_CLARO)) {
            raiz.getStyleClass().add(CLASE_CLARO);
        }
    }
}
