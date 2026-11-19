package mx.aulaaccesible.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/** Preferencias de la persona usuaria que sobreviven entre sesiones: tema, movimiento y sonidos. */
public final class Preferencias {
    private static final Preferences ALMACEN = Preferences.userNodeForPackage(Preferencias.class);
    private static final String TEMA_OSCURO = "temaOscuro";
    private static final String REDUCIR_MOVIMIENTO = "reducirMovimiento";

    private static final String SONIDOS = "sonidos";

    private static final BooleanProperty OSCURO = new SimpleBooleanProperty(leer(TEMA_OSCURO, true));
    private static final BooleanProperty REDUCIDO = new SimpleBooleanProperty(leer(REDUCIR_MOVIMIENTO, false));
    private static final BooleanProperty CON_SONIDOS = new SimpleBooleanProperty(leer(SONIDOS, true));

    static {
        OSCURO.addListener((obs, anterior, valor) -> guardar(TEMA_OSCURO, valor));
        REDUCIDO.addListener((obs, anterior, valor) -> guardar(REDUCIR_MOVIMIENTO, valor));
        CON_SONIDOS.addListener((obs, anterior, valor) -> guardar(SONIDOS, valor));
    }

    private Preferencias() {}

    /** Tema oscuro por defecto; la persona puede cambiarlo a claro. */
    public static BooleanProperty temaOscuro() { return OSCURO; }

    public static BooleanProperty reducirMovimiento() { return REDUCIDO; }

    /** Sonidos breves de la interfaz (responder, completar un área, errores…). Activos por defecto. */
    public static BooleanProperty sonidos() { return CON_SONIDOS; }

    private static boolean leer(String clave, boolean predeterminado) {
        try {
            return ALMACEN.getBoolean(clave, predeterminado);
        } catch (RuntimeException e) {
            return predeterminado;
        }
    }

    private static void guardar(String clave, boolean valor) {
        try {
            ALMACEN.putBoolean(clave, valor);
            ALMACEN.flush();
        } catch (BackingStoreException | RuntimeException e) {
            // Si no se puede persistir, la preferencia sigue valiendo durante esta sesión.
        }
    }
}
