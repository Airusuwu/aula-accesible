package mx.aulaaccesible.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Valor {
    @JsonProperty("si") SI("Sí"),
    @JsonProperty("no") NO("No"),
    @JsonProperty("desconocido") DESCONOCIDO("Desconocido"),
    @JsonProperty("no_aplica") NO_APLICA("No aplica");

    private final String etiqueta;

    Valor(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
