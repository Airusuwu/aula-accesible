package mx.aulaaccesible.ui;

import mx.aulaaccesible.ServicioEvaluacion;
import mx.aulaaccesible.model.Criterio;
import mx.aulaaccesible.model.Valor;
import mx.aulaaccesible.prolog.ClienteProlog;
import mx.aulaaccesible.prolog.ConfiguracionProlog;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprueba que cada área del catálogo real de Prolog, cada valor de respuesta y cada estado de diagnóstico
 * tienen un icono propio. Si se agrega un área en Prolog sin icono, esta prueba falla en vez de mostrar el genérico.
 */
class IconosTest {
    private static final List<String> ESTADOS = List.of(
            "requiere_atencion_prioritaria", "requiere_mejoras", "informacion_insuficiente",
            "sin_barreras_reportadas", "sin_criterios_aplicables");

    @Test
    void cadaAreaDelCatalogoTieneIconoPropio() {
        var catalogo = new ServicioEvaluacion(new ClienteProlog(ConfiguracionProlog.localizar())).catalogo();
        var areas = catalogo.stream().map(Criterio::area).collect(Collectors.toCollection(HashSet::new));
        assertEquals(6, areas.size());
        for (String area : areas) {
            assertTrue(Iconos.tieneIconoPropioArea(area), "Falta icono para el área " + area);
        }
        assertEquals(areas.size(), areas.stream().map(Iconos::codigoArea).distinct().count(),
                "Dos áreas comparten icono");
    }

    @Test
    void cadaValorTieneIconoDistintoYSinResponderOtro() {
        for (Valor valor : Valor.values()) {
            assertTrue(Iconos.tieneIconoPropioValor(valor), "Falta icono para " + valor);
        }
        var codigos = new HashSet<String>();
        for (Valor valor : Valor.values()) {
            codigos.add(Iconos.codigoValor(valor));
        }
        codigos.add(Iconos.codigoValor(null));
        // Sí, No, No verificado, No aplica y sin responder: cinco formas, no solo colores distintos.
        assertEquals(Valor.values().length + 1, codigos.size());
    }

    @Test
    void cadaEstadoDeDiagnosticoTieneIcono() {
        for (String estado : ESTADOS) {
            assertTrue(Iconos.tieneIconoPropioEstado(estado), "Falta icono para el estado " + estado);
        }
    }
}
