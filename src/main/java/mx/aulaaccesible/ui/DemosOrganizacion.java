package mx.aulaaccesible.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

import static mx.aulaaccesible.ui.Controles.texto;
import static mx.aulaaccesible.ui.Piezas.boton;
import static mx.aulaaccesible.ui.Piezas.escenario;
import static mx.aulaaccesible.ui.Piezas.etiqueta;
import static mx.aulaaccesible.ui.Piezas.nota;

/** Ejemplos del área «Organización y comprensión». */
final class DemosOrganizacion {
    private DemosOrganizacion() {}

    private record Encabezado(int nivel, String texto) {}

    static Demo encabezadosEstructurados(boolean cumple) {
        var encabezados = cumple
                ? List.of(new Encabezado(1, "Unidad 2: Fracciones"), new Encabezado(2, "¿Qué es una fracción?"),
                        new Encabezado(3, "Ejemplo con una pizza"), new Encabezado(2, "Actividad"))
                : List.of(new Encabezado(1, "Unidad 2: Fracciones"), new Encabezado(4, "¿Qué es una fracción?"),
                        new Encabezado(1, "Ejemplo con una pizza"), new Encabezado(4, "Actividad"));
        var mensaje = new Piezas.Mensaje();
        var filas = new ArrayList<Label>();
        for (var encabezado : encabezados) {
            var fila = new Label("H" + encabezado.nivel() + "  " + encabezado.texto());
            fila.getStyleClass().addAll("demo-esquema", "demo-nivel-" + Math.min(encabezado.nivel() - 1, 3));
            fila.setPadding(new Insets(2, 6, 2, 6 + 14.0 * (cumple ? encabezado.nivel() - 1 : 0)));
            fila.setMaxWidth(Double.MAX_VALUE);
            filas.add(fila);
        }
        var lista = new Piezas.ListaLector(filas, indice -> {
            var actual = encabezados.get(indice);
            mensaje.neutro("El lector dice: «Encabezado de nivel " + actual.nivel() + ": " + actual.texto() + "»");
        });
        var escenario = escenario(etiqueta("Página de la unidad"), lista.vista(), mensaje.nodo());
        return Demo.de(escenario, cumple
                ? "Recorre los encabezados con ↑ ↓ como lo hace un lector de pantalla: los niveles bajan de uno en uno."
                : "Recorre los encabezados con ↑ ↓ como lo hace un lector de pantalla: los niveles saltan de 1 a 4 "
                        + "y vuelven a 1, sin orden.", lista.vista()).conFlechas(lista::flecha);
    }

    static Demo enlacesDescriptivos(boolean cumple) {
        var enlaces = cumple
                ? List.of("Descargar guía de la unidad 2 (PDF)", "Ver el calendario de entregas",
                        "Escribir a tu profesor")
                : List.of("Haz clic aquí", "Haz clic aquí", "Más");
        var mensaje = new Piezas.Mensaje();
        var filas = new ArrayList<Label>();
        for (String enlace : enlaces) {
            var fila = new Label(enlace);
            fila.getStyleClass().add("demo-enlace");
            fila.setPadding(new Insets(2, 6, 2, 6));
            filas.add(fila);
        }
        var lista = new Piezas.ListaLector(filas, indice -> {
            mensaje.neutro("El lector dice: «Enlace: " + enlaces.get(indice) + "»");
        });
        var escenario = escenario(etiqueta("Lista de enlaces de la página"), lista.vista(), mensaje.nodo());
        return Demo.de(escenario, cumple
                ? "Recorre la lista de enlaces con ↑ ↓, sin ver el resto de la página: cada uno se entiende solo."
                : "Recorre la lista de enlaces con ↑ ↓, sin ver el resto de la página: no sabes a dónde lleva ninguno.",
                lista.vista()).conFlechas(lista::flecha);
    }

    static Demo instruccionesClaras(boolean cumple) {
        var mensaje = new Piezas.Mensaje();
        if (!cumple) {
            var parrafo = texto("Lee el texto y luego responde las preguntas, revisa tus respuestas y después envía "
                    + "la actividad cuando termines todo lo anterior.", "demo-texto");
            var pregunta = boton("¿Qué hago primero?", () ->
                    mensaje.error("No se sabe: todo está en una sola frase larga y sin orden."));
            var escenario = escenario(parrafo, pregunta, mensaje.nodo());
            return Demo.de(escenario, "Lee las instrucciones y pulsa «¿Qué hago primero?»: nadie lo puede decir.",
                    pregunta);
        }
        var pasos = new CheckBox[]{new CheckBox("1. Lee el texto."), new CheckBox("2. Responde las preguntas."),
                new CheckBox("3. Envía la actividad.")};
        for (int i = 0; i < pasos.length; i++) {
            int posicion = i;
            pasos[i].selectedProperty().addListener((obs, antes, marcado) -> {
                long hechos = java.util.Arrays.stream(pasos).filter(CheckBox::isSelected).count();
                if (hechos == pasos.length) {
                    mensaje.ok("¡Listo! Terminaste los " + pasos.length + " pasos.");
                } else if (marcado && posicion + 1 < pasos.length) {
                    mensaje.neutro("Paso " + (posicion + 1) + " listo. Sigue con el paso " + (posicion + 2) + ".");
                } else {
                    mensaje.neutro(hechos + " de " + pasos.length + " pasos hechos.");
                }
            });
        }
        var escenario = escenario(etiqueta("Actividad"), pasos[0], pasos[1], pasos[2], mensaje.nodo());
        return Demo.de(escenario, "Marca cada paso con Espacio: están numerados y en orden, así siempre sabes qué sigue.",
                pasos);
    }

    static Demo navegacionConsistente(boolean cumple) {
        var mensaje = new Piezas.Mensaje();
        var menu = new HBox(6);
        menu.getStyleClass().add("demo-menu");
        var contenido = nota("");
        int[] pantalla = {0};
        Runnable mostrar = () -> {
            List<String> opciones = pantalla[0] == 0 || cumple ? List.of("Inicio", "Lecciones", "Ayuda")
                    : List.of("Lecciones", "Soporte", "Inicio");
            menu.getChildren().clear();
            for (String opcion : opciones) {
                var etiqueta = new Label(opcion);
                etiqueta.getStyleClass().add("demo-opcion");
                menu.getChildren().add(etiqueta);
            }
            contenido.setText("Contenido de la pantalla " + (pantalla[0] + 1) + ".");
            mensaje.neutro("Menú: " + String.join(", ", opciones) + ".");
        };
        Node uno = boton("Pantalla 1", () -> { pantalla[0] = 0; mostrar.run(); });
        Node dos = boton("Pantalla 2", () -> { pantalla[0] = 1; mostrar.run(); });
        mostrar.run();
        var escenario = escenario(new FlowPane(8, 8, uno, dos), menu, contenido, mensaje.nodo());
        return Demo.de(escenario, cumple
                ? "Cambia de pantalla con los botones o con ← →: el menú siempre está igual y en el mismo orden."
                : "Cambia de pantalla con los botones o con ← →: el menú cambia de orden y de nombres.", uno, dos)
                .conFlechas(tecla -> {
                    if (tecla == KeyCode.LEFT || tecla == KeyCode.RIGHT) {
                        pantalla[0] = tecla == KeyCode.LEFT ? 0 : 1;
                        mostrar.run();
                    }
                });
    }
}
