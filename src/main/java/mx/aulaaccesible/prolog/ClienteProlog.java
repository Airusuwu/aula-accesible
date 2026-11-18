package mx.aulaaccesible.prolog;

import com.fasterxml.jackson.databind.ObjectMapper;
import mx.aulaaccesible.model.Criterio;
import mx.aulaaccesible.model.Diagnostico;
import mx.aulaaccesible.model.Respuesta;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class ClienteProlog {
    private final ConfiguracionProlog configuracion;
    private final ObjectMapper json = new ObjectMapper();
    private final Duration limite;

    public ClienteProlog(ConfiguracionProlog configuracion) {
        this(configuracion, Duration.ofSeconds(15));
    }

    public ClienteProlog(ConfiguracionProlog configuracion, Duration limite) {
        this.configuracion = configuracion;
        this.limite = limite;
    }

    public List<Criterio> catalogo() {
        return consultar(Map.of("operacion", "catalogo"), Catalogo.class).criterios();
    }

    public List<Respuesta> ejemplo() {
        return consultar(Map.of("operacion", "ejemplo"), Ejemplo.class).respuestas();
    }

    public Diagnostico evaluar(String id, List<Respuesta> respuestas) {
        return consultar(Map.of("operacion", "evaluar", "evaluacion", id, "respuestas", respuestas),
                Diagnostico.class);
    }

    private <T> T consultar(Map<String, ?> solicitud, Class<T> tipo) {
        if (!Files.isRegularFile(configuracion.puente())
                || !Files.isRegularFile(configuracion.puente().resolveSibling("aula_accesible.pl"))) {
            throw new PrologException("No se encontró la base de conocimientos en "
                    + configuracion.puente().getParent() + ". Revisa AULA_CONOCIMIENTOS.");
        }
        Process proceso;
        var directorioSwi = configuracion.directorioSwi();
        var comando = new ArrayList<String>();
        comando.add(configuracion.ejecutable());
        if (directorioSwi != null) {
            comando.add("--home=" + directorioSwi);
        }
        comando.addAll(List.of("-q", "-f", "none", "-s", configuracion.puente().toString(), "-g", "main", "-t", "halt"));
        try {
            proceso = new ProcessBuilder(comando).start();
        } catch (IOException e) {
            throw new PrologException("No se pudo iniciar SWI-Prolog. Revisa la carpeta del programa "
                    + "o configura AULA_SWIPL con la ruta de swipl.exe.", e);
        }

        var flujos = Executors.newFixedThreadPool(3);
        try {
            var salida = flujos.submit(() -> proceso.getInputStream().readAllBytes());
            var errores = flujos.submit(() -> proceso.getErrorStream().readAllBytes());
            var entrada = flujos.submit(() -> {
                try (var stdin = proceso.getOutputStream()) {
                    json.writeValue(stdin, solicitud);
                }
                return null;
            });
            if (!proceso.waitFor(limite.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new PrologException("SWI-Prolog tardó demasiado. Vuelve a intentarlo.");
            }
            String detalle = new String(errores.get(), StandardCharsets.UTF_8).strip();
            if (proceso.exitValue() != 0 || detalle.lines().anyMatch(linea -> directorioSwi == null
                    || !linea.startsWith("WARNING: Invalid SWI-Prolog home directory ")
                    || !linea.endsWith(": no ABI file"))) {
                // En SWI-Prolog 10.0.2 para Windows la comprobación de ABI puede advertir
                // al arrancar desde una ruta con acentos, aun si cargó el runtime indicado.
                throw new PrologException("SWI-Prolog no pudo completar la consulta."
                        + (detalle.isEmpty() ? "" : "\n" + detalle));
            }
            entrada.get();
            return json.readValue(salida.get(), tipo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PrologException("La consulta se interrumpió.", e);
        } catch (ExecutionException | IOException e) {
            throw new PrologException("No se pudo leer la respuesta de SWI-Prolog. Revisa la base de conocimientos.", e);
        } finally {
            if (proceso.isAlive()) {
                proceso.destroyForcibly();
            }
            flujos.shutdownNow();
        }
    }

    public record Catalogo(List<Criterio> criterios) {}
    public record Ejemplo(List<Respuesta> respuestas) {}
}
