package mx.aulaaccesible.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;

import java.util.List;

import static mx.aulaaccesible.ui.Controles.caja;
import static mx.aulaaccesible.ui.Piezas.boton;
import static mx.aulaaccesible.ui.Piezas.campo;
import static mx.aulaaccesible.ui.Piezas.escenario;
import static mx.aulaaccesible.ui.Piezas.etiqueta;
import static mx.aulaaccesible.ui.Piezas.nota;

/** Ejemplos del área «Navegación con teclado». */
final class DemosTeclado {
    private DemosTeclado() {}

    static Demo tecladoCompleto(boolean cumple) {
        var mensaje = new Piezas.Mensaje();
        var a = boton("Opción A", () -> mensaje.neutro("Elegiste la opción A."));
        var b = boton("Opción B", () -> mensaje.neutro("Elegiste la opción B."));
        Node enviar;
        if (cumple) {
            enviar = boton("Enviar", () -> mensaje.ok("¡Enviado!"));
        } else {
            // Parece un botón y responde al ratón, pero no se puede alcanzar con Tab.
            var falso = new Label("Enviar");
            falso.getStyleClass().add("demo-boton");
            falso.setOnMouseClicked(evento -> mensaje.ok("¡Enviado con el ratón!"));
            enviar = Piezas.sinTeclado(falso);
        }
        var escenario = escenario(etiqueta("Elige una opción y envía"), new FlowPane(8, 8, a, b, enviar),
                mensaje.nodo());
        return cumple
                ? Demo.de(escenario, "Presiona Tab para llegar a cada botón y Intro o Espacio para activarlo.",
                        a, b, enviar)
                : Demo.de(escenario, "Presiona Tab: nunca llegas a «Enviar». Solo responde al clic del ratón.", a, b);
    }

    static Demo focoVisible(boolean cumple) {
        var mensaje = new Piezas.Mensaje();
        var botones = new Button[3];
        String[] nombres = {"Atrás", "Guardar", "Enviar"};
        for (int i = 0; i < botones.length; i++) {
            String nombre = nombres[i];
            botones[i] = boton(nombre, () -> mensaje.ok("Pulsaste «" + nombre + "»."));
            botones[i].focusedProperty().addListener((obs, antes, enfocado) -> {
                if (enfocado) {
                    mensaje.neutro("El foco está en «" + nombre + "»."
                            + (cumple ? "" : " (¿lo ves? No hay ninguna señal.)"));
                }
            });
        }
        var escenario = escenario(etiqueta("Recorre los botones"), new FlowPane(8, 8, botones), mensaje.nodo());
        if (!cumple) {
            escenario.getStyleClass().add("demo-sin-foco");
        }
        return Demo.de(escenario, cumple
                ? "Presiona Tab y observa cómo un borde grueso marca el botón activo."
                : "Presiona Tab y observa: el foco se mueve, pero no hay ninguna señal visible.", botones);
    }

    static Demo ordenFoco(boolean cumple) {
        var mensaje = new Piezas.Mensaje();
        var nombre = campo("Tu nombre");
        var correo = campo("tu@correo.mx");
        var contrasena = campo("Contraseña");
        var campos = List.of(nombre, correo, contrasena);
        var titulos = List.of("Nombre", "Correo", "Contraseña");
        var escenario = escenario(nota("Se leen de arriba abajo: Nombre, Correo, Contraseña."));
        for (int i = 0; i < campos.size(); i++) {
            escenario.getChildren().add(fila(titulos.get(i), campos.get(i)));
            String titulo = titulos.get(i);
            campos.get(i).focusedProperty().addListener((obs, antes, enfocado) -> {
                if (enfocado) {
                    mensaje.neutro("El foco está en «" + titulo + "».");
                }
            });
        }
        escenario.getChildren().add(mensaje.nodo());
        return cumple
                ? Demo.de(escenario, "Presiona Tab y comprueba que el foco sigue el orden en que se leen los campos.",
                        nombre, correo, contrasena)
                : Demo.de(escenario, "Presiona Tab: el foco salta a «Contraseña» antes que a «Correo».",
                        nombre, contrasena, correo);
    }

    private static HBox fila(String titulo, TextField campo) {
        var etiqueta = etiqueta(titulo);
        etiqueta.setMinWidth(90);
        etiqueta.setLabelFor(campo);
        var fila = new HBox(8, etiqueta, campo);
        fila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(campo, javafx.scene.layout.Priority.ALWAYS);
        return fila;
    }

    static Demo sinTrampasTeclado(boolean cumple) {
        var mensaje = new Piezas.Mensaje();
        var abrir = boton("Abrir menú", () -> { });
        var items = List.of(boton("Opción 1", () -> { }), boton("Opción 2", () -> { }),
                boton("Opción 3", () -> { }));
        var menu = caja(4, "demo-menu-abierto");
        menu.getChildren().addAll(items);
        menu.setVisible(false);
        menu.managedProperty().bind(menu.visibleProperty());

        Runnable cerrar = () -> {
            abrir.requestFocus();
            menu.setVisible(false);
            mensaje.neutro("Menú cerrado. El foco volvió a «Abrir menú».");
        };
        abrir.setOnAction(evento -> {
            menu.setVisible(true);
            items.get(0).requestFocus();
            mensaje.neutro("Menú abierto.");
        });
        for (int i = 0; i < items.size(); i++) {
            int posicion = i;
            var item = items.get(i);
            if (cumple) {
                item.setOnAction(evento -> {
                    mensaje.ok("Elegiste «Opción " + (posicion + 1) + "».");
                    abrir.requestFocus();
                    menu.setVisible(false);
                });
                Piezas.alPulsar(item, KeyCode.ESCAPE, cerrar);
            } else {
                item.setOnAction(evento -> mensaje.error("El menú no responde ni se cierra."));
            }
        }
        var escenario = escenario(etiqueta("Menú de la actividad"), abrir, menu, mensaje.nodo());
        return Demo.dinamico(escenario,
                        cumple ? "Abre el menú con Intro. Recórrelo con Tab o con ↑ ↓ y ciérralo con Esc."
                                : "Abre el menú e intenta salir con Tab: te quedas atrapado. "
                                        + "(Aquí Esc te saca del ejemplo; en una app real no habría salida.)",
                        () -> menu.isVisible()
                                ? (cumple ? List.<Node>of(abrir, items.get(0), items.get(1), items.get(2))
                                        : List.<Node>copyOf(items))
                                : List.<Node>of(abrir))
                .conTrampa(() -> !cumple && menu.isVisible())
                .conFlechas(tecla -> {
                    // ↑ ↓ recorren las opciones del menú abierto.
                    var dueno = abrir.getScene() == null ? null : abrir.getScene().getFocusOwner();
                    int actual = items.indexOf(dueno);
                    if (!menu.isVisible() || actual < 0) {
                        return;
                    }
                    if (tecla == KeyCode.DOWN) {
                        items.get(Math.min(items.size() - 1, actual + 1)).requestFocus();
                    } else if (tecla == KeyCode.UP) {
                        items.get(Math.max(0, actual - 1)).requestFocus();
                    }
                });
    }
}
