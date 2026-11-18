package mx.aulaaccesible;

import mx.aulaaccesible.informe.ExportadorInformeHtml;
import mx.aulaaccesible.informe.InformeEvaluacion;
import mx.aulaaccesible.model.Diagnostico.Conteos;
import mx.aulaaccesible.model.Evaluacion;
import mx.aulaaccesible.model.Valor;
import mx.aulaaccesible.prolog.ClienteProlog;
import mx.aulaaccesible.prolog.ConfiguracionProlog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InformeEvaluacionTest {
    @Test
    void exportaElDiagnosticoDePrologYEscapaLosCampos(@TempDir Path carpeta) throws Exception {
        var servicio = new ServicioEvaluacion(new ClienteProlog(ConfiguracionProlog.localizar()));
        var criterios = servicio.catalogo();
        var evaluacion = new Evaluacion("Aula <Módulo> & \"Lectura\"", "Evaluación 'A'");
        for (int i = 0; i < criterios.size(); i++) {
            Valor valor = i < 11 ? Valor.NO : i < 14 ? Valor.DESCONOCIDO : i < 17 ? null : Valor.SI;
            evaluacion.cambiar(criterios.get(i), valor, i == 0 ? "Botón <Enviar> & menú" : "", "");
        }
        var diagnostico = servicio.diagnosticar(evaluacion, criterios);
        var informe = new InformeEvaluacion(evaluacion, criterios, diagnostico);
        assertEquals(new Conteos(7, 11, 6, 0), diagnostico.conteos());
        assertEquals("11 barreras reportadas · 6 pendientes · 7 favorables", informe.resumen());
        assertEquals(7, informe.favorables().size());
        assertEquals(3, informe.pendientesPorEstado().get(InformeEvaluacion.REQUIERE_VERIFICACION).size());
        assertEquals(3, informe.pendientesPorEstado().get(InformeEvaluacion.SIN_RESPONDER).size());
        assertEquals(diagnostico.recomendaciones(), informe.recomendacionesPorPrioridad().values().stream()
                .flatMap(List::stream).toList());
        assertFalse(diagnostico.problemas().isEmpty());

        var archivo = carpeta.resolve("informe.html");
        ExportadorInformeHtml.guardar(informe, archivo);
        String html = Files.readString(archivo);
        assertTrue(html.startsWith("<!doctype html>"));
        assertTrue(html.contains("<html lang=\"es\">"));
        assertTrue(html.contains("@media print"));
        for (String texto : List.of(informe.resumen(), "Requiere atención prioritaria",
                "Se reportaron barreras de prioridad alta.", "Aula &lt;Módulo&gt; &amp; &quot;Lectura&quot;",
                "Evaluación &#39;A&#39;", "Ubicación: Botón &lt;Enviar&gt; &amp; menú",
                "Recomendaciones", "Problemas combinados", "Información visual difícil",
                "Acción: ", "Requiere verificación (3)", "Sin responder (3)", "Favorables", InformeEvaluacion.ALCANCE)) {
            assertTrue(html.contains(texto), texto);
        }
        assertFalse(html.contains("<Módulo>"));
        assertFalse(html.contains("<Enviar>"));
    }
}
