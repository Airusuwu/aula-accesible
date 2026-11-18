package mx.aulaaccesible.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class Evaluacion {
    private final String id = UUID.randomUUID().toString();
    private final String software;
    private final String seccion;
    private final Map<String, Respuesta> respuestas = new LinkedHashMap<>();

    public Evaluacion(String software, String seccion) {
        this.software = software.strip();
        this.seccion = seccion.strip();
    }

    public String id() { return id; }
    public String software() { return software; }
    public String seccion() { return seccion; }
    public Respuesta respuesta(String criterio) { return respuestas.get(criterio); }
    public List<Respuesta> respuestas() { return List.copyOf(respuestas.values()); }

    public void cambiar(Criterio criterio, Valor valor, String ubicacion, String motivo) {
        // Se conserva la nota mientras se corrige una respuesta, incluso si queda pendiente.
        if (valor == null && (ubicacion == null || ubicacion.isBlank())) {
            respuestas.remove(criterio.id());
        } else {
            respuestas.put(criterio.id(), new Respuesta(
                    criterio.id(), valor, ubicacion, valor == Valor.NO_APLICA ? motivo : ""));
        }
    }

    public long contestadas() {
        return respuestas.values().stream().filter(r -> r.valor() != null).count();
    }
}
