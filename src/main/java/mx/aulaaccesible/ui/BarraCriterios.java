package mx.aulaaccesible.ui;

import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import mx.aulaaccesible.model.Criterio;
import mx.aulaaccesible.model.Evaluacion;
import mx.aulaaccesible.model.Valor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static mx.aulaaccesible.ui.Controles.*;

/**
 * Barra lateral del cuestionario: las áreas con sus criterios y el progreso global. Se construye una sola vez
 * por evaluación y solo actualiza estados, así conserva su posición de desplazamiento al navegar.
 */
final class BarraCriterios {
    private static final PseudoClass ACTUAL = PseudoClass.getPseudoClass("actual");

    private record FilaCriterio(Criterio criterio, Button boton, StackPane marca) {}
    private record EncabezadoArea(Label cantidad, List<Criterio> criterios) {}

    private final Evaluacion evaluacion;
    private final List<List<Criterio>> secciones;
    private final int total;
    private final List<List<FilaCriterio>> filas = new ArrayList<>();
    private final List<EncabezadoArea> encabezados = new ArrayList<>();
    private final Label textoProgreso = texto("", "texto-progreso");
    private final ProgressBar barra = new ProgressBar();
    private final ScrollPane desplazamiento;
    private final VBox lista = caja(2, "lista-criterios");
    private final VBox vista;
    private boolean primeraActualizacion = true;

    BarraCriterios(Evaluacion evaluacion, List<List<Criterio>> secciones,
                   BiConsumer<Integer, Integer> navegar, Runnable inicio) {
        this.evaluacion = evaluacion;
        this.secciones = secciones;
        this.total = secciones.stream().mapToInt(List::size).sum();

        for (int i = 0; i < secciones.size(); i++) {
            int indiceSeccion = i;
            var criterios = secciones.get(i);
            var primero = criterios.get(0);
            var cantidad = texto("", "cantidad-area");
            cantidad.setMinWidth(Label.USE_PREF_SIZE);
            var nombre = texto(primero.nombreArea(), "nombre-area");
            HBox.setHgrow(nombre, Priority.ALWAYS);
            var cabecera = fila("encabezado-area", Iconos.area(primero.area()), nombre, cantidad);
            cabecera.setAlignment(Pos.CENTER_LEFT);
            // El encabezado es solo un rótulo: los lectores de pantalla lo anuncian como parte del grupo.
            cabecera.setFocusTraversable(false);
            lista.getChildren().add(cabecera);
            encabezados.add(new EncabezadoArea(cantidad, criterios));

            var filasArea = new ArrayList<FilaCriterio>();
            for (int j = 0; j < criterios.size(); j++) {
                int indiceCriterio = j;
                var criterio = criterios.get(j);
                var marca = new StackPane();
                marca.getStyleClass().add("marca-estado");
                var boton = boton(criterio.titulo(), "criterio-lateral", () -> navegar.accept(indiceSeccion, indiceCriterio));
                boton.setGraphic(marca);
                boton.setContentDisplay(ContentDisplay.LEFT);
                boton.setGraphicTextGap(10);
                boton.setAlignment(Pos.CENTER_LEFT);
                boton.setMaxWidth(Double.MAX_VALUE);
                boton.setMinWidth(0);
                boton.setPrefWidth(0);
                boton.setWrapText(true);
                boton.setMnemonicParsing(false);
                lista.getChildren().add(boton);
                filasArea.add(new FilaCriterio(criterio, boton, marca));
            }
            filas.add(filasArea);
        }
        desplazamiento = desplazar(lista);
        VBox.setVgrow(desplazamiento, Priority.ALWAYS);

        barra.setMaxWidth(Double.MAX_VALUE);
        var volver = boton("Volver al inicio", "mdi2a-arrow-left", "enlace", inicio);
        var progreso = caja(8, "progreso-lateral",
                fila("etiqueta-progreso", texto("Progreso", "subtitulo"), espacio(), textoProgreso),
                barra, volver);
        vista = caja(0, "barra-criterios", desplazamiento, progreso);
        vista.setMinWidth(280);
        vista.setPrefWidth(300);
        vista.setMaxWidth(320);
        barra.setProgress(contestadas() / (double) total);
    }

    Node vista() { return vista; }

    /** Marca el criterio actual y refresca iconos, contadores por área y progreso. */
    void actualizar(int seccionActual, int criterioActual) {
        for (int i = 0; i < filas.size(); i++) {
            for (int j = 0; j < filas.get(i).size(); j++) {
                var fila = filas.get(i).get(j);
                boolean actual = i == seccionActual && j == criterioActual;
                fila.boton().pseudoClassStateChanged(ACTUAL, actual);
                pintar(fila, actual);
            }
        }
        for (int i = 0; i < encabezados.size(); i++) {
            var encabezado = encabezados.get(i);
            long hechas = encabezado.criterios().stream().filter(this::contestada).count();
            encabezado.cantidad().setText(hechas + "/" + encabezado.criterios().size());
        }
        actualizarProgreso();
        if (primeraActualizacion) {
            primeraActualizacion = false;
        }
        mostrarFila(filas.get(seccionActual).get(criterioActual).boton());
    }

    /** Llamar después de cambiar una respuesta del criterio indicado. */
    void respondido(int seccion, int criterio, boolean rebotar) {
        var fila = filas.get(seccion).get(criterio);
        pintar(fila, true);
        if (rebotar) {
            Movimiento.rebotar(fila.marca());
        }
        var encabezado = encabezados.get(seccion);
        long hechas = encabezado.criterios().stream().filter(this::contestada).count();
        encabezado.cantidad().setText(hechas + "/" + encabezado.criterios().size());
        actualizarProgreso();
    }

    private void pintar(FilaCriterio fila, boolean actual) {
        var respuesta = evaluacion.respuesta(fila.criterio().id());
        Valor valor = contestada(fila.criterio()) ? respuesta.valor() : null;
        fila.marca().getChildren().setAll(Iconos.valor(valor));
        fila.marca().getStyleClass().removeAll("estado-si", "estado-no", "estado-desconocido", "estado-no-aplica");
        if (valor != null) {
            fila.marca().getStyleClass().add(switch (valor) {
                case SI -> "estado-si";
                case NO -> "estado-no";
                case DESCONOCIDO -> "estado-desconocido";
                case NO_APLICA -> "estado-no-aplica";
            });
        }
        String estado = valor == null ? "sin responder" : "respondido " + valor;
        fila.boton().setAccessibleText(fila.criterio().titulo() + ", " + estado + (actual ? ", criterio actual" : ""));
    }

    private boolean contestada(Criterio criterio) {
        var respuesta = evaluacion.respuesta(criterio.id());
        return respuesta != null && respuesta.valor() != null
                && (respuesta.valor() != Valor.NO_APLICA || !respuesta.motivo().isBlank());
    }

    private long contestadas() {
        return secciones.stream().flatMap(List::stream).filter(this::contestada).count();
    }

    private void actualizarProgreso() {
        long hechas = contestadas();
        textoProgreso.setText(hechas + " de " + total);
        barra.setAccessibleText(hechas + " de " + total + " criterios respondidos");
        Movimiento.progreso(barra, hechas / (double) total);
    }

    /** Desplaza el lateral lo justo para que el criterio actual quede a la vista. */
    private void mostrarFila(Node boton) {
        javafx.application.Platform.runLater(() -> {
            lista.applyCss();
            lista.layout();
            double alto = lista.getHeight() - desplazamiento.getViewportBounds().getHeight();
            if (alto <= 0) {
                return;
            }
            double arriba = boton.getBoundsInParent().getMinY();
            double abajo = boton.getBoundsInParent().getMaxY();
            double visibleArriba = desplazamiento.getVvalue() * alto;
            double visibleAbajo = visibleArriba + desplazamiento.getViewportBounds().getHeight();
            if (arriba < visibleArriba) {
                desplazamiento.setVvalue(Math.max(0, (arriba - 48) / alto));
            } else if (abajo > visibleAbajo) {
                desplazamiento.setVvalue(Math.min(1, (abajo + 16 - desplazamiento.getViewportBounds().getHeight()) / alto));
            }
        });
    }
}
