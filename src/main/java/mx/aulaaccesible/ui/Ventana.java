package mx.aulaaccesible.ui;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mx.aulaaccesible.ServicioEvaluacion;
import mx.aulaaccesible.model.Criterio;
import mx.aulaaccesible.model.Evaluacion;
import mx.aulaaccesible.model.Respuesta;
import mx.aulaaccesible.model.Valor;
import mx.aulaaccesible.prolog.ClienteProlog;
import mx.aulaaccesible.prolog.ConfiguracionProlog;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static mx.aulaaccesible.ui.Controles.*;

public final class Ventana {
    @FXML private BorderPane pantalla;
    @FXML private Label pasoInicio;
    @FXML private Label pasoCuestionario;
    @FXML private Label pasoResultados;
    @FXML private VBox zonaEstado;

    private final ServicioEvaluacion servicio = new ServicioEvaluacion(new ClienteProlog(ConfiguracionProlog.localizar()));
    private List<Criterio> catalogo = List.of();
    private List<List<Criterio>> secciones = List.of();
    private List<Respuesta> estadoInicial = List.of();
    private Evaluacion evaluacion;
    private int seccion;
    private int indiceCriterio;
    private Cuestionario cuestionario;

    @FXML
    private void initialize() {
        inicio(false);
        conectar();
    }

    private void conectar() {
        enSegundoPlano("Conectando con SWI-Prolog…", servicio::catalogo, criterios -> {
            catalogo = criterios;
            secciones = criterios.stream().collect(Collectors.groupingBy(
                    Criterio::area, LinkedHashMap::new, Collectors.toList())).values().stream().toList();
            inicio(true);
        });
    }

    private void inicio(boolean moverFoco) {
        var software = campo("", "Ej. Plataforma de matemáticas", 120);
        var seccionSoftware = campo("", "Ej. Cuestionario de la unidad 2", 120);
        var iniciar = boton("Nueva evaluación", "primario",
                () -> nueva(software.getText(), seccionSoftware.getText()));
        var cargar = boton("Cargar ejemplo", "secundario", this::cargarEjemplo);
        boolean conectado = !catalogo.isEmpty();
        iniciar.setDisable(!conectado);
        cargar.setDisable(!conectado);
        var campos = caja(14, "formulario-inicio", texto("Nueva evaluación", "titulo-seccion"),
                texto("Si quieres identificar el diagnóstico, añade estos datos.", "ayuda"),
                campoConEtiqueta("Software educativo (opcional)", software),
                campoConEtiqueta("Sección evaluada (opcional)", seccionSoftware), iniciar);
        var ejemplo = caja(7, "tarjeta-ejemplo", texto("Explora un caso de prueba", "subtitulo"),
                texto("Abre un cuestionario con respuestas que puedes revisar y editar.", "ayuda"), cargar);
        var panel = caja(0, "panel-inicio", campos, ejemplo);
        if (evaluacion != null) {
            panel.getChildren().add(caja(0, "sesion-inicio",
                    boton("Continuar evaluación", "enlace", () -> mostrarCuestionario(seccion, indiceCriterio))));
        }

        var titulo = texto("Evalúa la accesibilidad de una sección", "titulo-inicio");
        titulo.setFocusTraversable(true);
        var indicadores = fila("indicadores-inicio", caja(0, "dato-inicio", texto("24", "cifra-inicio"),
                        texto("criterios", "dato-texto")),
                caja(0, "dato-inicio", texto("6", "cifra-inicio"), texto("áreas", "dato-texto")));
        var hero = caja(18, "hero", titulo,
                texto("Identifica barreras en software educativo y descubre qué corregir primero.", "introduccion"),
                indicadores);
        var contenido = fila("pagina-inicio", hero, panel);
        HBox.setHgrow(hero, Priority.ALWAYS);
        HBox.setHgrow(panel, Priority.ALWAYS);
        hero.setMaxWidth(Double.MAX_VALUE);
        panel.setMaxWidth(Double.MAX_VALUE);
        contenido.setMaxWidth(1140);
        var estado = fila("estado-inicio", texto(conectado ? "Motor de diagnóstico listo" : "Motor de diagnóstico sin conectar",
                conectado ? "estado-conectado" : "ayuda"));
        if (!conectado) {
            estado.getChildren().add(boton("Reintentar conexión", "secundario", this::conectar));
        }
        estado.setMaxWidth(1140);
        VBox pagina = caja(12, "marco-inicio", contenido, estado);
        cambiarPantalla(desplazar(pagina), pasoInicio);
        if (moverFoco) {
            enfocar(titulo);
        }
    }

    private void nueva(String software, String seccionSoftware) {
        if (confirmarReemplazo()) {
            iniciarEvaluacion(new Evaluacion(software, seccionSoftware));
        }
    }

    private void cargarEjemplo() {
        if (confirmarReemplazo()) {
            enSegundoPlano("Cargando el caso del PDF…", () -> servicio.ejemplo(catalogo), this::iniciarEvaluacion);
        }
    }

    private boolean confirmarReemplazo() {
        if (evaluacion == null || new HashSet<>(evaluacion.respuestas()).equals(new HashSet<>(estadoInicial))) {
            return true;
        }
        var reemplazar = new ButtonType("Reemplazar evaluación", ButtonBar.ButtonData.OK_DONE);
        var cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        var aviso = new Alert(Alert.AlertType.CONFIRMATION,
                "Al reemplazarla se perderán las respuestas y anotaciones que modificaste.", cancelar, reemplazar);
        aviso.initOwner(pantalla.getScene().getWindow());
        aviso.setTitle("Reemplazar evaluación");
        aviso.setHeaderText("La evaluación actual tiene cambios");
        return aviso.showAndWait().orElse(cancelar) == reemplazar;
    }

    private void iniciarEvaluacion(Evaluacion nueva) {
        evaluacion = nueva;
        estadoInicial = nueva.respuestas();
        mostrarCuestionario(0, 0);
    }

    private void mostrarCuestionario(int indiceSeccion, int indicePregunta) {
        seccion = indiceSeccion;
        indiceCriterio = indicePregunta;
        cuestionario = new Cuestionario(evaluacion, secciones, indiceSeccion, indicePregunta,
                this::mostrarCuestionario, () -> inicio(true), this::diagnosticar);
        cambiarPantalla(cuestionario.vista(), pasoCuestionario);
        cuestionario.enfocarTitulo();
    }

    private void diagnosticar() {
        for (int i = 0; i < secciones.size(); i++) {
            var criterios = secciones.get(i);
            for (int j = 0; j < criterios.size(); j++) {
                var criterio = criterios.get(j);
                var respuesta = evaluacion.respuesta(criterio.id());
                if (respuesta != null && respuesta.valor() == Valor.NO_APLICA && respuesta.motivo().isEmpty()) {
                    mostrarCuestionario(i, j);
                    cuestionario.mostrarErrorMotivo();
                    return;
                }
            }
        }
        enSegundoPlano("Prolog está evaluando las respuestas…", () -> servicio.diagnosticar(evaluacion, catalogo), resultado -> {
            var vista = new Resultados(evaluacion, catalogo, resultado,
                    () -> mostrarCuestionario(seccion, indiceCriterio), () -> nueva("", ""), () -> inicio(true));
            cambiarPantalla(vista.vista(), pasoResultados);
            vista.enfocarTitulo();
        });
    }

    private void cambiarPantalla(Node contenido, Label pasoActual) {
        zonaEstado.getChildren().clear();
        pantalla.setCenter(contenido);
        for (Label paso : List.of(pasoInicio, pasoCuestionario, pasoResultados)) {
            paso.getStyleClass().remove("paso-activo");
            paso.setAccessibleText(paso.getText() + (paso == pasoActual ? ", actual" : ""));
        }
        pasoActual.getStyleClass().add("paso-activo");
    }

    private void mensaje(String contenido, String estilo) {
        var texto = texto(contenido, "texto");
        zonaEstado.getChildren().setAll(fila(estilo, texto));
    }

    private <T> void enSegundoPlano(String mensaje, Callable<T> trabajo, Consumer<T> completar) {
        Node foco = pantalla.getScene() == null ? null : pantalla.getScene().getFocusOwner();
        var carga = new ProgressIndicator();
        carga.setPrefSize(19, 19);
        pantalla.setDisable(true);
        zonaEstado.getChildren().setAll(fila("zona-carga", carga, texto(mensaje, "texto")));
        Task<T> tarea = new Task<>() {
            @Override protected T call() throws Exception { return trabajo.call(); }
        };
        tarea.setOnSucceeded(evento -> {
            pantalla.setDisable(false);
            zonaEstado.getChildren().clear();
            completar.accept(tarea.getValue());
        });
        tarea.setOnFailed(evento -> {
            pantalla.setDisable(false);
            String detalle = tarea.getException().getMessage();
            System.err.println(detalle);
            mensaje(detalle == null ? "No se pudo completar la operación." : detalle.lines().findFirst().orElse(""), "aviso");
            if (foco != null) {
                enfocar(foco);
            }
        });
        var hilo = new Thread(tarea, "consulta-prolog");
        hilo.setDaemon(true);
        hilo.start();
    }
}
