package mx.aulaaccesible;

import mx.aulaaccesible.model.Criterio;
import mx.aulaaccesible.model.Diagnostico.Conteos;
import mx.aulaaccesible.model.Diagnostico.Evidencia;
import mx.aulaaccesible.model.Diagnostico.Problema;
import mx.aulaaccesible.model.Evaluacion;
import mx.aulaaccesible.model.Respuesta;
import mx.aulaaccesible.model.Valor;
import mx.aulaaccesible.prolog.ClienteProlog;
import mx.aulaaccesible.prolog.ConfiguracionProlog;
import mx.aulaaccesible.prolog.PrologException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntegracionPrologTest {
    private final ConfiguracionProlog configuracion = ConfiguracionProlog.localizar();
    private final ClienteProlog cliente = new ClienteProlog(configuracion);
    private final ServicioEvaluacion servicio = new ServicioEvaluacion(cliente);

    @Test
    void ejemploEditableYEvaluacionNueva() {
        var catalogo = servicio.catalogo();
        var ejemplo = servicio.ejemplo(catalogo);
        assertEquals(24, ejemplo.contestadas());
        assertTrue(catalogo.get(0).pregunta().startsWith("¿"));

        var inicial = servicio.diagnosticar(ejemplo, catalogo);
        assertEquals(new Conteos(11, 6, 1, 6), inicial.conteos());
        assertEquals("Requiere atención prioritaria", inicial.tituloEstado());
        assertEquals(List.of("Interacción restringida", "Captura y corrección difíciles"),
                inicial.problemas().stream().map(Problema::titulo).toList());
        var evidencia = inicial.problemas().get(0).evidencias();
        assertEquals(List.of("teclado_completo", "alternativa_arrastre"),
                evidencia.stream().map(Evidencia::criterio).toList());
        assertTrue(evidencia.stream().allMatch(e -> e.valor() == Valor.NO));
        assertEquals("desconocido", inicial.pendientes().get(0).motivo());

        var arrastre = catalogo.get(23);
        ejemplo.cambiar(arrastre, Valor.SI, "Actividad «Arrastra» / sección 2", "");
        var corregido = servicio.diagnosticar(ejemplo, catalogo);
        assertEquals(new Conteos(12, 5, 1, 6), corregido.conteos());
        assertEquals(inicial.estado(), corregido.estado());
        assertEquals(List.of("captura_y_correccion_dificiles"),
                corregido.problemas().stream().map(Problema::id).toList());
        assertEquals(corregido, servicio.diagnosticar(ejemplo, catalogo));

        var nueva = new Evaluacion("", "");
        assertEquals(0, nueva.contestadas());
        assertEquals(new Conteos(0, 0, 24, 0), servicio.diagnosticar(nueva, catalogo).conteos());
        nueva.cambiar(catalogo.get(0), Valor.SI, "", "");
        var parcial = servicio.diagnosticar(nueva, catalogo);
        assertEquals(new Conteos(1, 0, 23, 0), parcial.conteos());
        assertEquals("informacion_insuficiente", parcial.estado());
        assertTrue(parcial.pendientes().stream().allMatch(p -> p.motivo().equals("omitido")));
    }

    @Test
    void validarAplicabilidadEnJavaYProlog() {
        var catalogo = servicio.catalogo();
        Criterio general = catalogo.get(0), video = catalogo.get(9);
        var evaluacion = new Evaluacion("", "");
        evaluacion.cambiar(video, Valor.NO_APLICA, "", "");
        assertThrows(IllegalArgumentException.class, () -> servicio.diagnosticar(evaluacion, catalogo));
        assertThrows(PrologException.class, () -> cliente.evaluar("prueba",
                List.of(new Respuesta(general.id(), Valor.NO_APLICA, "", ""))));
        assertThrows(PrologException.class, () -> cliente.evaluar("prueba",
                List.of(new Respuesta(video.id(), Valor.NO_APLICA, "", ""))));

        var si = new Respuesta(general.id(), Valor.SI, "", "");
        assertThrows(PrologException.class, () -> cliente.evaluar("prueba", List.of(si, si)));
        evaluacion.cambiar(video, Valor.NO_APLICA, "Lección 1", video.motivoNoAplica());
        assertEquals(new Conteos(0, 0, 23, 1), servicio.diagnosticar(evaluacion, catalogo).conteos());
    }

    @Test
    void muestraErroresDeConexionYTiempoLimite() {
        var ausente = new ClienteProlog(new ConfiguracionProlog(
                configuracion.puente().resolveSibling("no-existe-swipl.exe").toString(), configuracion.puente()));
        assertTrue(assertThrows(PrologException.class, ausente::catalogo).getMessage().contains("AULA_SWIPL"));
        var limite = new ClienteProlog(configuracion, Duration.ZERO);
        assertTrue(assertThrows(PrologException.class, limite::catalogo).getMessage().contains("tardó demasiado"));
    }
}
