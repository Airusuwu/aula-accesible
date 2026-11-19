package mx.aulaaccesible.ui;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContrasteTest {
    @Test
    void blancoSobreNegroEs21A1() {
        assertEquals(21.0, Contraste.relacion(Color.WHITE, Color.BLACK), 0.01);
    }

    @Test
    void elMismoColorEs1A1() {
        assertEquals(1.0, Contraste.relacion(Color.web("#3ddc97"), Color.web("#3ddc97")), 0.0001);
    }

    @Test
    void laRelacionNoDependeDelOrden() {
        var a = Color.web("#102d27");
        var b = Color.web("#f7f7f3");
        assertEquals(Contraste.relacion(a, b), Contraste.relacion(b, a), 0.0001);
    }

    /** La demo «Así no se cumple» debe fallar de verdad el mínimo de WCAG, y la que cumple, superarlo. */
    @Test
    void lasDemosDeContrasteFallanYCumplenComoSeAnuncia() {
        var fondoOscuro = Color.web("#1d2a25");
        var fondoClaro = Color.web("#ffffff");
        assertTrue(Contraste.relacion(Color.web("#e6efe9"), fondoOscuro) >= Contraste.MINIMO_TEXTO);
        assertTrue(Contraste.relacion(Color.web("#51655c"), fondoOscuro) < 3);
        assertTrue(Contraste.relacion(Color.web("#102d27"), fondoClaro) >= Contraste.MINIMO_TEXTO);
        assertTrue(Contraste.relacion(Color.web("#a9b8b1"), fondoClaro) < 3);
    }
}
