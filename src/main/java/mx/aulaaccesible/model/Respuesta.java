package mx.aulaaccesible.model;

public record Respuesta(String criterio, Valor valor, String ubicacion, String motivo) {
    public Respuesta {
        ubicacion = ubicacion == null ? "" : ubicacion.strip();
        motivo = motivo == null ? "" : motivo.strip();
    }
}
