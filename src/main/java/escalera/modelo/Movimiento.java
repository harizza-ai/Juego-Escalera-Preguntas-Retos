package escalera.modelo;

/**
 * Registro de una jugada (inmutable). Es lo que se apila en el historial
 * para poder mostrar las últimas 5 jugadas.
 */

public class Movimiento {
    private final String jugador;
    private final int dado;
    private final int desde;
    private final int hasta;
    private final String detalle;

    public Movimiento(String jugador, int dado, int desde, int hasta, String detalle) {
        this.jugador = jugador;
        this.dado = dado;
        this.desde = desde;
        this.hasta = hasta;
        this.detalle = detalle == null ? "" : detalle;
    }

    public String getJugador() {
        return jugador;
    }

    public int getDado() {
        return dado;
    }

    public int getDesde() {
        return desde;
    }

    public int getHasta() {
        return hasta;
    }

    public String getDetalle() {
        return detalle;
    }

    @Override
    public String toString() {
        String texto = jugador + ": dado " + dado + ", casilla " + desde + " -> " + hasta;
        return detalle.isEmpty() ? texto : texto + " (" + detalle + ")";
    }
}

