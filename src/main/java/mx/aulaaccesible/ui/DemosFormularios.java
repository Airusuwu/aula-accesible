package mx.aulaaccesible.ui;

import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;

import java.util.List;

import static mx.aulaaccesible.ui.Controles.caja;
import static mx.aulaaccesible.ui.Piezas.boton;
import static mx.aulaaccesible.ui.Piezas.campo;
import static mx.aulaaccesible.ui.Piezas.escenario;
import static mx.aulaaccesible.ui.Piezas.etiqueta;
import static mx.aulaaccesible.ui.Piezas.nota;

/** Ejemplos del área «Formularios y evaluaciones». */
final class DemosFormularios {
    private DemosFormularios() {}

    static Demo etiquetasCampos(boolean cumple) {
        var voz = new Piezas.Voz();
        var mensaje = new Piezas.Mensaje();
        var campo = campo(cumple ? "ana@escuela.mx" : "Escribe aquí");
        String clip = cumple ? "lector-etiqueta-si" : "lector-etiqueta-no";
        var escuchar = boton("Escuchar con lector", () -> {
            voz.decir(clip);
            mensaje.neutro("El lector dice: «" + Sonidos.texto(clip) + "»");
        });
        escuchar.setGraphic(Iconos.icono("mdi2v-volume-high", "icono-boton"));
        escuchar.setContentDisplay(ContentDisplay.LEFT);
        var escenario = escenario();
        if (cumple) {
            var titulo = etiqueta("Correo electrónico");
            titulo.setLabelFor(campo);
            escenario.getChildren().add(titulo);
        }
        escenario.getChildren().addAll(campo, escuchar, mensaje.nodo());
        return Demo.de(escenario, cumple
                ? "Pulsa «Escuchar con lector» con el cursor en el campo: dice su nombre. Escribe y verás que la "
                        + "etiqueta no desaparece."
                : "Escribe en el campo: el texto de ejemplo desaparece y ya no sabes qué se pedía. "
                        + "Pulsa «Escuchar con lector» y compruébalo.", campo, escuchar).conSalida(voz::detener);
    }

    static Demo instruccionesCampos(boolean cumple) {
        var mensaje = new Piezas.Mensaje();
        var fecha = campo("dd/mm/aaaa");
        var titulo = etiqueta(cumple ? "Fecha (dd/mm/aaaa) · Obligatorio" : "Fecha");
        titulo.setLabelFor(fecha);
        var enviar = boton("Enviar", () -> {
            if (fecha.getText().trim().matches("\\d{2}/\\d{2}/\\d{4}")) {
                mensaje.ok("¡Enviado!");
            } else {
                mensaje.error(cumple ? "Formato incorrecto. Escribe la fecha como dd/mm/aaaa."
                        : "Formato no válido. Debía ser dd/mm/aaaa, pero nadie lo dijo antes.");
            }
        });
        var escenario = escenario(titulo, fecha, enviar, mensaje.nodo());
        return Demo.de(escenario, cumple
                ? "Escribe la fecha como quieras y pulsa «Enviar»: el formato ya estaba escrito desde el inicio."
                : "Escribe la fecha como quieras y pulsa «Enviar»: el formato solo se descubre después del error.",
                fecha, enviar);
    }

    static Demo erroresExplicados(boolean cumple) {
        var mensaje = new Piezas.Mensaje();
        var correo = campo("tu@correo.mx");
        var enviar = boton("Enviar", () -> {
            String texto = correo.getText().trim();
            if (Piezas.correoValido(texto)) {
                mensaje.ok("¡Enviado!");
            } else if (!cumple) {
                mensaje.error("Error en el formulario");
            } else if (texto.isEmpty()) {
                mensaje.error("Correo: escribe tu correo. Ejemplo: ana@escuela.mx");
            } else if (!texto.contains("@")) {
                mensaje.error("Correo: falta la @. Ejemplo: ana@escuela.mx");
            } else {
                mensaje.error("Correo: falta el dominio después de la @. Ejemplo: ana@escuela.mx");
            }
        });
        var titulo = etiqueta("Correo");
        titulo.setLabelFor(correo);
        var escenario = escenario(titulo, correo, enviar, mensaje.nodo());
        return Demo.de(escenario, cumple
                ? "Pulsa «Enviar» con el campo vacío o con un correo mal escrito: el mensaje dice qué falla y cómo corregirlo."
                : "Pulsa «Enviar» con el campo vacío o con un correo mal escrito: el mensaje no dice qué corregir.",
                correo, enviar);
    }

    private enum Paso { FORMULARIO, RESUMEN, FIN }

    static Demo revisionEnvio(boolean cumple) {
        var mensaje = new Piezas.Mensaje();
        var respuestas = new ToggleGroup[2];
        var opciones = new ToggleButton[4];
        String[] preguntas = {"¿El agua hierve a 100 °C?", "¿La Tierra es plana?"};
        var formulario = caja(8, "demo-grupo");
        for (int i = 0; i < 2; i++) {
            respuestas[i] = new ToggleGroup();
            for (int j = 0; j < 2; j++) {
                var opcion = new ToggleButton(j == 0 ? "Verdadero" : "Falso");
                opcion.getStyleClass().add("demo-boton");
                opcion.setToggleGroup(respuestas[i]);
                opcion.setAccessibleText(preguntas[i] + " " + opcion.getText());
                opciones[i * 2 + j] = opcion;
            }
            formulario.getChildren().addAll(nota(preguntas[i]), new FlowPane(8, 8, opciones[i * 2], opciones[i * 2 + 1]));
        }
        var resumenTexto = nota("");
        var editar = boton("Editar", () -> { });
        var entregar = boton("Enviar definitivamente", () -> { });
        var resumen = caja(8, "demo-grupo", resumenTexto, new FlowPane(8, 8, editar, entregar));
        var reiniciar = boton("Reiniciar ejemplo", () -> { });
        var fin = caja(8, "demo-grupo", reiniciar);
        var principal = boton(cumple ? "Revisar respuestas" : "Enviar", () -> { });

        formulario.getChildren().add(principal);
        formulario.managedProperty().bind(formulario.visibleProperty());
        for (Node panel : List.of(resumen, fin)) {
            panel.setVisible(false);
            panel.managedProperty().bind(panel.visibleProperty());
        }
        var estado = new Paso[]{Paso.FORMULARIO};
        java.util.function.Consumer<Paso> mostrar = paso -> {
            estado[0] = paso;
            formulario.setVisible(paso == Paso.FORMULARIO);
            resumen.setVisible(paso == Paso.RESUMEN);
            fin.setVisible(paso == Paso.FIN);
            Node foco = switch (paso) {
                case FORMULARIO -> opciones[0];
                case RESUMEN -> editar;
                case FIN -> reiniciar;
            };
            foco.requestFocus();
        };
        principal.setOnAction(evento -> {
            if (respuestas[0].getSelectedToggle() == null || respuestas[1].getSelectedToggle() == null) {
                mensaje.error("Responde las dos preguntas antes de continuar.");
                return;
            }
            mensaje.ocultar();
            if (cumple) {
                resumenTexto.setText("Tus respuestas: 1) " + ((ToggleButton) respuestas[0].getSelectedToggle()).getText()
                        + ", 2) " + ((ToggleButton) respuestas[1].getSelectedToggle()).getText()
                        + ". Revísalas antes de entregar.");
                mostrar.accept(Paso.RESUMEN);
            } else {
                mensaje.error("Entregado. Ya no puedes cambiar nada, aunque te hayas equivocado.");
                mostrar.accept(Paso.FIN);
            }
        });
        editar.setOnAction(evento -> mostrar.accept(Paso.FORMULARIO));
        entregar.setOnAction(evento -> {
            mensaje.ok("Entregado correctamente.");
            mostrar.accept(Paso.FIN);
        });
        reiniciar.setOnAction(evento -> {
            for (var grupo : respuestas) {
                grupo.selectToggle(null);
            }
            mensaje.ocultar();
            mostrar.accept(Paso.FORMULARIO);
        });
        var escenario = escenario(formulario, resumen, fin, mensaje.nodo());
        return Demo.dinamico(escenario, cumple
                ? "Responde las dos preguntas y pulsa «Revisar respuestas»: puedes volver a editar antes de entregar."
                : "Responde las dos preguntas y pulsa «Enviar»: la entrega es inmediata y ya no se puede corregir.",
                () -> switch (estado[0]) {
                    case FORMULARIO -> List.<Node>of(opciones[0], opciones[1], opciones[2], opciones[3], principal);
                    case RESUMEN -> List.<Node>of(editar, entregar);
                    case FIN -> List.<Node>of(reiniciar);
                });
    }
}
