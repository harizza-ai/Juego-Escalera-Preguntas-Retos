package main.java.escalera.modelo;

/**
 * Casilla del tablero (inmutable). Se crea con los métodos fábrica,
 * que dejan claro qué datos necesita cada tipo:
 * - ESCALERA / SERPIENTE: casilla destino del salto.
 * - RETO: dificultad (1-3) de la pregunta que se debe responder.
 */
public final class Casilla {

    /** Valor de 'destino' o 'dificultad' cuando el dato no aplica al tipo. */
    private static final int NO_APLICA = 0;

    private final int numero;
    private final TipoCasilla tipo;
    private final int destino;
    private final int dificultad;

    private Casilla(int numero, TipoCasilla tipo, int destino, int dificultad) {
        this.numero = numero;
        this.tipo = tipo;
        this.destino = destino;
        this.dificultad = dificultad;
    }

    public static Casilla normal(int numero) {
        return new Casilla(numero, TipoCasilla.NORMAL, NO_APLICA, NO_APLICA);
    }

    public static Casilla escalera(int numero, int destino) {
        return new Casilla(numero, TipoCasilla.ESCALERA, destino, NO_APLICA);
    }

    public static Casilla serpiente(int numero, int destino) {
        return new Casilla(numero, TipoCasilla.SERPIENTE, destino, NO_APLICA);
    }

    public static Casilla reto(int numero, int dificultad) {
        return new Casilla(numero, TipoCasilla.RETO, NO_APLICA, dificultad);
    }

    public int getNumero() {
        return numero;
    }

    public TipoCasilla getTipo() {
        return tipo;
    }

    /** @return casilla destino (solo tiene sentido en escaleras y serpientes) */
    public int getDestino() {
        return destino;
    }

    /** @return dificultad de la pregunta (solo tiene sentido en retos) */
    public int getDificultad() {
        return dificultad;
    }

    @Override
    public String toString() {
        switch (tipo) {
            case ESCALERA:
                return numero + " [ESCALERA -> " + destino + "]";
            case SERPIENTE:
                return numero + " [SERPIENTE -> " + destino + "]";
            case RETO:
                return numero + " [RETO nivel " + dificultad + "]";
            default:
                return numero + " [NORMAL]";
        }
    }
}
