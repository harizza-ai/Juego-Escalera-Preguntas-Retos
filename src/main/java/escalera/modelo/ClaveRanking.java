package escalera.modelo;

/**
 * Clave de ordenamiento del ranking en el árbol: más aciertos es mejor; a igual
 * aciertos, menos fallos es mejor; el nombre desempata para que la clave sea única.
 * El orden natural es de PEOR a MEJOR, por eso el ranking se lee en orden descendente.
 */
public final class ClaveRanking implements Comparable<ClaveRanking> {

    private final int aciertos;
    private final int fallos;
    private final String nombre;

    public ClaveRanking(Jugador jugador) {
        this.aciertos = jugador.getAciertos();
        this.fallos = jugador.getFallos();
        this.nombre = jugador.getNombre();
    }

    @Override
    public int compareTo(ClaveRanking otra) {
        if (aciertos != otra.aciertos) {
            return Integer.compare(aciertos, otra.aciertos);
        }
        if (fallos != otra.fallos) {
            return Integer.compare(otra.fallos, fallos);      // menos fallos = mayor
        }
        return otra.nombre.compareTo(nombre);                  // alfabético primero = mayor
    }
}
