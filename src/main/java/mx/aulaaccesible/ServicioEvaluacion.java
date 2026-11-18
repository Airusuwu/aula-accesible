package mx.aulaaccesible;

import mx.aulaaccesible.model.Criterio;
import mx.aulaaccesible.model.Diagnostico;
import mx.aulaaccesible.model.Evaluacion;
import mx.aulaaccesible.model.Respuesta;
import mx.aulaaccesible.model.Valor;
import mx.aulaaccesible.prolog.ClienteProlog;
import mx.aulaaccesible.prolog.PrologException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ServicioEvaluacion {
    private final ClienteProlog prolog;

    public ServicioEvaluacion(ClienteProlog prolog) {
        this.prolog = prolog;
    }

    public List<Criterio> catalogo() {
        List<Criterio> criterios = List.copyOf(prolog.catalogo());
        if (criterios.size() != 24 || criterios.stream().map(Criterio::id).distinct().count() != 24
                || criterios.stream().collect(Collectors.groupingBy(Criterio::area, Collectors.counting()))
                        .values().stream().anyMatch(n -> n != 4)) {
            throw new PrologException("La base debe contener seis secciones de cuatro criterios distintos.");
        }
        return criterios;
    }

    public Evaluacion ejemplo(List<Criterio> catalogo) {
        Map<String, Criterio> criterios = catalogo.stream()
                .collect(Collectors.toMap(Criterio::id, Function.identity()));
        Evaluacion evaluacion = new Evaluacion("Plataforma del ejemplo", "Sección didáctica");
        for (Respuesta respuesta : prolog.ejemplo()) {
            Criterio criterio = Objects.requireNonNull(criterios.get(respuesta.criterio()));
            validar(criterio, respuesta);
            evaluacion.cambiar(criterio, respuesta.valor(), respuesta.ubicacion(), respuesta.motivo());
        }
        if (evaluacion.contestadas() != catalogo.size()) {
            throw new PrologException("El ejemplo no contiene una respuesta por criterio.");
        }
        return evaluacion;
    }

    public Diagnostico diagnosticar(Evaluacion evaluacion, List<Criterio> catalogo) {
        List<Respuesta> respuestas = catalogo.stream().map(criterio -> {
            Respuesta respuesta = evaluacion.respuesta(criterio.id());
            if (respuesta != null) {
                validar(criterio, respuesta);
            }
            return respuesta;
        }).filter(r -> r != null && r.valor() != null).toList();
        return prolog.evaluar(evaluacion.id(), respuestas);
    }

    static void validar(Criterio criterio, Respuesta respuesta) {
        if (respuesta.valor() == Valor.NO_APLICA
                && (!criterio.permiteNoAplica() || !criterio.motivoNoAplica().equals(respuesta.motivo()))) {
            throw new IllegalArgumentException("Selecciona el motivo de «No aplica» en «" + criterio.titulo() + "».");
        }
    }
}
