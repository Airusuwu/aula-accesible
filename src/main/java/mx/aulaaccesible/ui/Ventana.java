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
import javafx.scene.layout.VBox;
import mx.aulaaccesible.ServicioEvaluacion;
import mx.aulaaccesible.informe.InformeEvaluacion;
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

    private ServicioEvaluacion servicio;
    private List<Criterio> catalogo = List.of();
    private List<List<Criterio>> secciones = List.of();
    private List<Respuesta> estadoInicial = List.of();
    private Evaluacion evaluacion;
    private int seccion;
    private int indiceCriterio;
    private Cuestionario cuestionario;
    private String borradorSoftware = "";
    private String borradorSeccion = "";
    private boolean conexionFallida;

    @FXML
    private void initialize() {
        inicio(false);
        conectar();
    }

    private void conectar() {
        servicio = new ServicioEvaluacion(new ClienteProlog(ConfiguracionProlog.localizar()));
        enSegundoPlano("Preparando la evaluación…", servicio::catalogo, criterios -> {
            catalogo = criterios;
            secciones = criterios.stream().collect(Collectors.groupingBy(
                    Criterio::area, LinkedHashMap::new, Collectors.toList())).values().stream().toList();
            conexionFallida = false;
            inicio(true);
        }, error -> {
            conexionFallida = true;
            inicio(true);
        });
    }

    private void inicio(boolean moverFoco) {
        var software = campo(borradorSoftware, "Ej. Aula virtual", 120);
        var seccionSoftware = campo(borradorSeccion, "Ej. Entrega de tareas", 120);
        software.textProperty().addListener((obs, anterior, valor) -> borradorSoftware = valor);
        seccionSoftware.textProperty().addListener((obs, anterior, valor) -> borradorSeccion = valor);
        var iniciar = boton("Iniciar evaluación", "primario",
                () -> nueva(software.getText(), seccionSoftware.getText()));
        var cargar = boton("Probar con un ejemplo", "enlace", this::cargarEjemplo);
        boolean conectado = !catalogo.isEmpty();
        iniciar.setDisable(!conectado);
        cargar.setDisable(!conectado);
        var titulo = texto("¿Qué vas a evaluar?", "titulo-inicio");
        titulo.setFocusTraversable(true);
        var contexto = conectado
                ? "Revisa " + catalogo.size() + " criterios de accesibilidad en " + secciones.size() + " áreas."
                : "Revisa la accesibilidad básica y consulta qué corregir primero.";
        var datosSoftware = campoConEtiqueta("Software educativo", software);
        var datosSeccion = campoConEtiqueta("Sección a evaluar", seccionSoftware);
        var formulario = caja(20, "formulario-inicio",
                texto("Ambos campos son opcionales.", "ayuda"),
                datosSoftware, datosSeccion,
                fila("acciones-inicio", iniciar, cargar));
        if (evaluacion != null) {
            formulario.getChildren().add(boton("Continuar evaluación", "enlace",
                    () -> mostrarCuestionario(seccion, indiceCriterio)));
        }
        var contenido = caja(20, "contenido-inicio", caja(8, "cabecera-inicio",
                texto("Nueva evaluación", "etiqueta-inicio"), titulo, texto(contexto, "introduccion")),
                formulario);
        contenido.setMaxWidth(620);
        Node foco = titulo;
        if (conexionFallida) {
            var reintentar = boton("Reintentar conexión", "secundario", this::conectar);
            contenido.getChildren().add(1, caja(8, "aviso-inicio",
                    texto("No pudimos conectar con SWI-Prolog.", "error-campo"),
                    texto("Comprueba que SWI-Prolog esté disponible y reintenta. Si cambiaste su configuración, vuelve a abrir la aplicación.", "texto"),
                    reintentar));
            foco = reintentar;
        }
        VBox pagina = caja(0, "marco-inicio", contenido);
        cambiarPantalla(desplazar(pagina), pasoInicio);
        if (moverFoco) {
            enfocar(foco);
        }
    }

    private void nueva(String software, String seccionSoftware) {
        if (confirmarReemplazo()) {
            iniciarEvaluacion(new Evaluacion(software, seccionSoftware));
        }
    }

    private void cargarEjemplo() {
        if (confirmarReemplazo()) {
            enSegundoPlano("Cargando el ejemplo…", () -> servicio.ejemplo(catalogo), this::iniciarEvaluacion);
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
        aviso.getDialogPane().getStylesheets().addAll(pantalla.getScene().getStylesheets());
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

    private void mostrarCriterio(String id) {
        for (int i = 0; i < secciones.size(); i++) {
            var criterios = secciones.get(i);
            for (int j = 0; j < criterios.size(); j++) {
                if (criterios.get(j).id().equals(id)) {
                    mostrarCuestionario(i, j);
                    return;
                }
            }
        }
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
        enSegundoPlano("Generando el diagnóstico…", () -> new InformeEvaluacion(
                evaluacion, catalogo, servicio.diagnosticar(evaluacion, catalogo)), informe -> {
            var vista = new Resultados(informe, this::mostrarCriterio,
                    () -> mostrarCuestionario(seccion, indiceCriterio), () -> nueva("", ""), () -> inicio(true));
            cambiarPantalla(vista.vista(), pasoResultados);
            vista.enfocarTitulo();
        });
    }

    private void cambiarPantalla(Node contenido, Label pasoActual) {
        zonaEstado.getChildren().clear();
        pantalla.setCenter(contenido);
        for (Label paso : List.of(pasoInicio, pasoCuestionario, pasoResultados)) {
            paso.getParent().getStyleClass().remove("paso-activo");
            paso.setAccessibleText(paso.getText() + (paso == pasoActual ? ", actual" : ""));
        }
        pasoActual.getParent().getStyleClass().add("paso-activo");
    }

    private void mensaje(String contenido, String estilo) {
        var texto = texto(contenido, "texto");
        zonaEstado.getChildren().setAll(fila(estilo, texto));
    }

    private <T> void enSegundoPlano(String mensaje, Callable<T> trabajo, Consumer<T> completar) {
        enSegundoPlano(mensaje, trabajo, completar, error -> {
            String detalle = error.getMessage();
            mensaje(detalle == null ? "No se pudo completar la operación." : detalle.lines().findFirst().orElse(""), "aviso");
        });
    }

    private <T> void enSegundoPlano(String mensaje, Callable<T> trabajo, Consumer<T> completar,
                                    Consumer<Throwable> fallar) {
        Node foco = pantalla.getScene() == null ? null : pantalla.getScene().getFocusOwner();
        var carga = new ProgressIndicator();
        carga.setPrefSize(20, 20);
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
            var error = tarea.getException();
            System.err.println(error.getMessage());
            zonaEstado.getChildren().clear();
            if (foco != null && foco.getScene() != null) {
                enfocar(foco);
            }
            fallar.accept(error);
        });
        var hilo = new Thread(tarea, "consulta-prolog");
        hilo.setDaemon(true);
        hilo.start();
    }
}
