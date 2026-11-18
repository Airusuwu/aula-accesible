package mx.aulaaccesible.prolog;

public final class PrologException extends RuntimeException {
    public PrologException(String mensaje) {
        super(mensaje);
    }

    public PrologException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
