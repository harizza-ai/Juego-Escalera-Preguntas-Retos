package escalera.modelo;

public class Jugador {
    /** Posición antes de salir: todavía no está en ninguna casilla del tablero. */
    private static final int POSICION_SALIDA = 0;

    private final String nombre;
    private final int ficha;          // número que lo identifica en el tablero
    private int posicion = POSICION_SALIDA;
    private int aciertos;
    private int fallos;
    private boolean pierdeTurno;

       /**
     * @param nombre nombre del jugador (no puede estar vacío)
     * @param ficha  número de ficha (1 a 4)
     * @throws IllegalArgumentException si el nombre es nulo o está vacío
     */
    public Jugador(String nombre, int ficha) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del jugador no puede estar vacío");
        }
        this.nombre = nombre.trim();
        this.ficha = ficha;
    }

    /** Cambia la posición del jugador a la casilla indicada. */
    public void moverA(int nuevaPosicion) {
        posicion = nuevaPosicion;
    }

    public void registrarAcierto() {
        aciertos++;
    }

    public void registrarFallo() {
        fallos++;
    }

    /** Marca que el jugador pierde su próximo turno (se usa al fallar un reto). */
    public void marcarPierdeTurno() {
        pierdeTurno = true;
    }

    /**
     * Cobra la penalización: devuelve true solo una vez si debía perder el turno
     * y deja de estar penalizado.
     */
    public boolean consumirPenalizacion() {
        boolean debiaPerder = pierdeTurno;
        pierdeTurno = false;
        return debiaPerder;
    }

    public String getNombre() {
        return nombre;
    }

    public int getFicha() {
        return ficha;
    }

    public int getPosicion() {
        return posicion;
    }

    public int getAciertos() {
        return aciertos;
    }

    public int getFallos() {
        return fallos;
    }

    @Override
    public String toString() {
        return nombre + " (ficha " + ficha + ", casilla " + posicion
                + ", aciertos " + aciertos + ", fallos " + fallos + ")";
    }
    
}
