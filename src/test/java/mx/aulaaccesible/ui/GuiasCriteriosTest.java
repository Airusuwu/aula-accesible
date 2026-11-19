package mx.aulaaccesible.ui;

import mx.aulaaccesible.ServicioEvaluacion;
import mx.aulaaccesible.model.Criterio;
import mx.aulaaccesible.prolog.ClienteProlog;
import mx.aulaaccesible.prolog.ConfiguracionProlog;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprueba contra el catálogo real de Prolog que cada criterio tiene su guía con pasos, un ejemplo que cumple y
 * otro que no, y que no quedan guías ni demos de criterios que ya no existen.
 */
class GuiasCriteriosTest {
    private final List<Criterio> catalogo =
            new ServicioEvaluacion(new ClienteProlog(ConfiguracionProlog.localizar())).catalogo();

    @Test
    void cadaCriterioTieneGuiaConEjemploQueCumpleYQueNoCumple() {
        assertEquals(24, catalogo.size());
        for (Criterio criterio : catalogo) {
            var guia = GuiasCriterios.para(criterio.id());
            assertNotNull(guia, "Falta la guía de " + criterio.id());
            assertFalse(guia.pasos().isEmpty(), "Sin pasos en " + criterio.id());
            assertFalse(guia.condicion().isBlank(), "Sin condición en " + criterio.id());
            assertFalse(guia.cumple().isBlank(), "Falta «Así se cumple» en " + criterio.id());
            assertFalse(guia.noCumple().isBlank(), "Falta «Así no se cumple» en " + criterio.id());
            assertNotEquals(guia.cumple(), guia.noCumple(), "Ejemplos idénticos en " + criterio.id());
        }
    }

    @Test
    void guiasYDemosSoloReferencianCriteriosDelCatalogo() {
        Set<String> ids = catalogo.stream().map(Criterio::id).collect(Collectors.toSet());
        assertTrue(ids.containsAll(GuiasCriterios.ids()), "Hay guías de criterios inexistentes");
        assertTrue(ids.containsAll(MuestrasCriterios.ids()), "Hay demos de criterios inexistentes");
    }
}
