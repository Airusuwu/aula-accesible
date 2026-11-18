package mx.aulaaccesible.informe;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ExportadorInformeHtml {
    private ExportadorInformeHtml() {}

    public static void guardar(InformeEvaluacion informe, Path destino) throws IOException {
        Files.writeString(destino, generar(informe), StandardCharsets.UTF_8);
    }

    private static String generar(InformeEvaluacion informe) {
        var html = new StringBuilder("""
                <!doctype html>
                <html lang="es">
                <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width,initial-scale=1">
                <title>Informe de accesibilidad | Aula Accesible</title>
                <style>
                body{font:1rem/1.55 Arial,sans-serif;color:#17223b;margin:2rem auto;max-width:52rem;padding:0 1rem;overflow-wrap:anywhere}
                h1,h2,h3,h4{line-height:1.2;margin-block:1.25em .4em}
                p{margin-block:.4rem} .resumen{font-weight:bold}
                header,section{border-bottom:1px solid #ccd4e4;padding-bottom:1rem}
                li{margin-block:.6rem} dt{font-weight:bold} dd{margin:0 0 .5rem}
                @media print{body{margin:0;max-width:none}h1,h2,h3,h4{break-after:avoid}li{break-inside:avoid}}
                </style>
                </head>
                <body>
                <header><h1>Informe de accesibilidad</h1>
                """);
        if (!informe.software().isBlank() || !informe.seccion().isBlank()) {
            html.append("<dl>");
            dato(html, "Software educativo", informe.software());
            dato(html, "Sección evaluada", informe.seccion());
            html.append("</dl>");
        }
        html.append("<h2>").append(escapar(informe.diagnostico().tituloEstado())).append("</h2>");
        html.append("<p class=\"resumen\">").append(escapar(informe.resumen())).append("</p>");
        parrafo(html, informe.resumenNoAplica());
        parrafo(html, informe.explicacion());
        parrafo(html, InformeEvaluacion.ALCANCE);
        html.append("</header><main>");

        html.append("<section><h2>Recomendaciones</h2>");
        if (informe.diagnostico().recomendaciones().isEmpty()) {
            parrafo(html, "No hay recomendaciones por barreras reportadas.");
        }
        informe.recomendacionesPorPrioridad().forEach((prioridad, grupo) -> {
            tituloGrupo(html, "Prioridad " + prioridad, grupo.size());
            for (var recomendacion : grupo) {
                inicioFila(html, informe.criterio(recomendacion.criterio()).titulo());
                parrafo(html, recomendacion.accion());
                ubicacion(html, informe, recomendacion.criterio());
                html.append("</li>");
            }
            html.append("</ul>");
        });
        html.append("</section><section><h2>Problemas combinados</h2>");
        if (informe.diagnostico().problemas().isEmpty()) {
            parrafo(html, "Las respuestas no activaron problemas combinados.");
        }
        for (var problema : informe.diagnostico().problemas()) {
            html.append("<h3>").append(escapar(problema.titulo())).append("</h3>");
            parrafo(html, "Respuestas que activaron el hallazgo · Regla " + problema.regla());
            html.append("<ul>");
            for (var evidencia : problema.evidencias()) {
                inicioFila(html, informe.criterio(evidencia.criterio()).titulo() + " · Respuesta: " + evidencia.valor());
                parrafo(html, evidencia.pregunta());
                parrafo(html, "Acción: " + informe.accion(evidencia.criterio()));
                ubicacion(html, informe, evidencia.criterio());
                html.append("</li>");
            }
            html.append("</ul>");
        }
        html.append("</section><section><h2>Pendientes</h2>");
        if (informe.diagnostico().pendientes().isEmpty()) {
            parrafo(html, "No hay criterios pendientes.");
        }
        informe.pendientesPorEstado().forEach((estado, grupo) -> {
            tituloGrupo(html, estado, grupo.size());
            for (var pendiente : grupo) {
                var criterio = informe.criterio(pendiente.criterio());
                inicioFila(html, criterio.titulo());
                parrafo(html, criterio.pregunta());
                ubicacion(html, informe, criterio.id());
                html.append("</li>");
            }
            html.append("</ul>");
        });
        html.append("</section><section><h2>Favorables</h2>");
        if (informe.favorables().isEmpty()) {
            parrafo(html, "No se registraron respuestas favorables.");
        } else {
            html.append("<ul>");
            for (var criterio : informe.favorables()) {
                inicioFila(html, criterio.titulo());
                parrafo(html, criterio.pregunta());
                ubicacion(html, informe, criterio.id());
                html.append("</li>");
            }
            html.append("</ul>");
        }
        return html.append("</section></main></body></html>\n").toString();
    }

    private static void dato(StringBuilder html, String etiqueta, String valor) {
        if (!valor.isBlank()) {
            html.append("<dt>").append(escapar(etiqueta)).append("</dt><dd>").append(escapar(valor)).append("</dd>");
        }
    }

    private static void tituloGrupo(StringBuilder html, String titulo, int cantidad) {
        html.append("<h3>").append(escapar(titulo)).append(" (").append(cantidad).append(")</h3><ul>");
    }

    private static void inicioFila(StringBuilder html, String titulo) {
        html.append("<li><strong>").append(escapar(titulo)).append("</strong>");
    }

    private static void ubicacion(StringBuilder html, InformeEvaluacion informe, String id) {
        if (!informe.ubicacion(id).isBlank()) {
            parrafo(html, "Ubicación: " + informe.ubicacion(id));
        }
    }

    private static void parrafo(StringBuilder html, String texto) {
        html.append("<p>").append(escapar(texto)).append("</p>");
    }

    private static String escapar(String texto) {
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
