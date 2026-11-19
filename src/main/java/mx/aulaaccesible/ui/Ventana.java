package mx.aulaaccesible.ui;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Tooltip;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import mx.aulaaccesible.ServicioEvaluacion;
import mx.aulaaccesible.informe.InformeEvaluacion;
import mx.aulaaccesible.model.Criterio;
import mx.aulaaccesible.model.Evaluacion;
import mx.aulaaccesible.model.Respuesta;
import mx.aulaaccesible.model.Valor;
import mx.aulaaccesible.prolog.ClienteProlog;
import mx.aulaaccesible.prolog.ConfiguracionProlog;
import org.kordamp.ikonli.javafx.FontIcon;

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
    @FXML private Button botonTema;
    @FXML private Button botonMovimiento;
    @FXML private Button botonSonido;
    @FXML private FontIcon iconoTema;
    @FXML private FontIcon iconoMovimiento;
    @FXML private FontIcon iconoSonido;
    @FXML private Tooltip ayudaTema;
    @FXML private Tooltip ayudaMovimiento;
    @FXML private Tooltip ayudaSonido;

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
        Preferencias.temaOscuro().addListener((obs, anterior, oscuro) -> actualizarBotonTema());
        Preferencias.reducirMovimiento().addListener((obs, anterior, reducido) -> actualizarBotonMovimiento());
        Preferencias.sonidos().addListener((obs, anterior, activos) -> actualizarBotonSonido());
        actualizarBotonTema();
        actualizarBotonMovimiento();
        actualizarBotonSonido();
        inicio(false);
        conectar();
    }

    @FXML
    private void alternarTema() {
        Preferencias.temaOscuro().set(!Preferencias.temaOscuro().get());
    }

    @FXML
    private void alternarMovimiento() {
        Preferencias.reducirMovimiento().set(!Preferencias.reducirMovimiento().get());
    }

    @FXML
    private void alternarSonido() {
        Preferencias.sonidos().set(!Preferencias.sonidos().get());
    }

    /** El icono muestra a qué tema se pasa al pulsar, y el nombre accesible lo dice con palabras. */
    private void actualizarBotonTema() {
        boolean oscuro = Preferencias.temaOscuro().get();
        String nombre = oscuro ? "Cambiar a tema claro" : "Cambiar a tema oscuro";
        iconoTema.setIconLiteral(oscuro ? "mdi2w-white-balance-sunny" : "mdi2w-weather-night");
        ayudaTema.setText(nombre);
        botonTema.setAccessibleText(nombre);
    }

    private void actualizarBotonMovimiento() {
        boolean reducido = Preferencias.reducirMovimiento().get();
        String nombre = reducido ? "Activar animaciones" : "Reducir movimiento";
        iconoMovimiento.setIconLiteral(reducido ? "mdi2m-motion-pause-outline" : "mdi2m-motion-play-outline");
        ayudaMovimiento.setText(nombre);
        botonMovimiento.setAccessibleText(nombre);
    }

    /** El icono muestra el estado actual (con o sin sonido) y el nombre accesible, la acción del botón. */
    private void actualizarBotonSonido() {
        boolean activos = Preferencias.sonidos().get();
        String nombre = activos ? "Silenciar sonidos" : "Activar sonidos";
        iconoSonido.setIconLiteral(activos ? "mdi2v-volume-high" : "mdi2v-volume-off");
        ayudaSonido.setText(nombre);
        botonSonido.setAccessibleText(nombre);
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
        var iniciar = boton("Iniciar evaluación", "mdi2p-play-circle-outline", "primario",
                () -> nueva(software.getText(), seccionSoftware.getText()));
        var cargar = boton("Probar con un ejemplo", "mdi2f-flask-outline", "enlace", this::cargarEjemplo);
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
        // FlowPane: si la tarjeta es estrecha, «Probar con un ejemplo» baja de línea en vez de cortarse.
        var acciones = new FlowPane(12, 8, iniciar, cargar);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.getStyleClass().add("acciones-inicio");
        var formulario = caja(20, "formulario-inicio",
                texto("Ambos campos son opcionales.", "ayuda"),
                datosSoftware, datosSeccion, acciones);
        if (evaluacion != null) {
            formulario.getChildren().add(boton("Continuar evaluación", "mdi2a-arrow-right", "enlace",
                    () -> mostrarCuestionario(seccion, indiceCriterio)));
        }
        var marca = caja(32, "panel-marca", caja(8, "cabecera-inicio",
                texto("Nueva evaluación", "etiqueta-inicio"), titulo, texto(contexto, "introduccion")));
        List<Node> tarjetas = conectado ? tarjetasAreas() : List.of();
        if (!tarjetas.isEmpty()) {
            marca.getChildren().add(rejillaAreas(tarjetas));
        }
        var tarjetaFormulario = caja(20, "tarjeta-formulario", formulario);
        tarjetaFormulario.setMaxWidth(520);
        // Sin esto el StackPane la estira a toda la altura: debe medir lo que mide su contenido.
        tarjetaFormulario.setMaxHeight(Region.USE_PREF_SIZE);
        Node foco = titulo;
        if (conexionFallida) {
            var reintentar = boton("Reintentar conexión", "mdi2r-refresh", "secundario", this::conectar);
            tarjetaFormulario.getChildren().add(0, caja(8, "aviso-inicio",
                    texto("No pudimos conectar con SWI-Prolog.", "error-campo"),
                    texto("Comprueba que SWI-Prolog esté disponible y reintenta. Si cambiaste su configuración, vuelve a abrir la aplicación.", "texto"),
                    reintentar));
            foco = reintentar;
        }
        // Dos columnas que ocupan todo el ancho: marca y áreas a la izquierda, formulario a la derecha.
        var derecha = new StackPane(tarjetaFormulario);
        derecha.getStyleClass().add("panel-formulario");
        var rejilla = new GridPane();
        rejilla.getStyleClass().add("pantalla-inicio");
        var columnaMarca = new ColumnConstraints();
        columnaMarca.setPercentWidth(56);
        var columnaFormulario = new ColumnConstraints();
        columnaFormulario.setPercentWidth(44);
        rejilla.getColumnConstraints().addAll(columnaMarca, columnaFormulario);
        var filaUnica = new RowConstraints();
        filaUnica.setVgrow(Priority.ALWAYS);
        filaUnica.setFillHeight(true);
        rejilla.getRowConstraints().add(filaUnica);
        rejilla.add(marca, 0, 0);
        rejilla.add(derecha, 1, 0);
        var pagina = desplazar(rejilla);
        // Con fitToHeight el contenido se comprimía en ventanas bajas y cortaba los textos; así ocupa al menos
        // el alto visible, pero crece a su tamaño natural y se desplaza si no cabe.
        rejilla.minHeightProperty().bind(pagina.viewportBoundsProperty().map(Bounds::getHeight));
        cambiarPantalla(pagina, pasoInicio);
        Movimiento.escalonar(tarjetas);
        if (moverFoco) {
            enfocar(foco);
        }
    }

    /** Una tarjeta por área, generada del catálogo que entrega Prolog. */
    private List<Node> tarjetasAreas() {
        var tarjetas = new java.util.ArrayList<Node>();
        for (var criterios : secciones) {
            var primero = criterios.get(0);
            var icono = new StackPane(Iconos.area(primero.area()));
            icono.getStyleClass().add("insignia-area");
            var nombre = texto(primero.nombreArea(), "subtitulo");
            var cantidad = texto(criterios.size() + " criterios", "ayuda");
            var tarjeta = fila("tarjeta-area", icono, caja(2, "datos-area", nombre, cantidad));
            tarjeta.setAlignment(Pos.CENTER_LEFT);
            tarjetas.add(tarjeta);
        }
        return tarjetas;
    }

    private static GridPane rejillaAreas(List<Node> tarjetas) {
        var rejilla = new GridPane();
        rejilla.getStyleClass().add("rejilla-areas");
        rejilla.setHgap(12);
        rejilla.setVgap(12);
        for (int i = 0; i < 2; i++) {
            var columna = new ColumnConstraints();
            columna.setPercentWidth(50);
            rejilla.getColumnConstraints().add(columna);
        }
        for (int i = 0; i < tarjetas.size(); i++) {
            var tarjeta = tarjetas.get(i);
            GridPane.setHgrow(tarjeta, Priority.ALWAYS);
            rejilla.add(tarjeta, i % 2, i / 2);
        }
        return rejilla;
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
        Tema.marcar(aviso.getDialogPane(), Preferencias.temaOscuro().get());
        aviso.setTitle("Reemplazar evaluación");
        aviso.setHeaderText("La evaluación actual tiene cambios");
        return aviso.showAndWait().orElse(cancelar) == reemplazar;
    }

    private void iniciarEvaluacion(Evaluacion nueva) {
        evaluacion = nueva;
        estadoInicial = nueva.respuestas();
        // La vista se crea una vez por evaluación; navegar solo cambia la tarjeta de la pregunta.
        cuestionario = new Cuestionario(evaluacion, secciones, () -> inicio(true), this::diagnosticar,
                (indiceSeccion, indicePregunta) -> {
                    seccion = indiceSeccion;
                    indiceCriterio = indicePregunta;
                });
        mostrarCuestionario(0, 0);
    }

    private void mostrarCuestionario(int indiceSeccion, int indicePregunta) {
        boolean yaVisible = pantalla.getCenter() == cuestionario.vista();
        cuestionario.mostrar(indiceSeccion, indicePregunta);
        if (!yaVisible) {
            cambiarPantalla(cuestionario.vista(), pasoCuestionario);
        }
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
            Sonidos.interfaz("diagnostico");
        });
    }

    private void cambiarPantalla(Node contenido, Label pasoActual) {
        zonaEstado.getChildren().clear();
        pantalla.setCenter(contenido);
        Movimiento.entrar(contenido);
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
        var zonaCarga = fila("zona-carga", carga, texto(mensaje, "texto"));
        zonaEstado.getChildren().setAll(zonaCarga);
        Movimiento.entrar(zonaCarga);
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
