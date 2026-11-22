package escalera.servicio;

import escalera.estructuras.ArbolAVL;
import escalera.estructuras.Iterador;
import escalera.estructuras.ListaEnlazada;
import escalera.modelo.ClaveRanking;
import escalera.modelo.Jugador;

/**
 * Tabla de posiciones construida con un árbol AVL ordenado por aciertos
 * (desempata por menos fallos y luego por nombre). Es una foto del estado
 * de los jugadores en el momento de crearla.
 */
public class Ranking {

    private final ArbolAVL<ClaveRanking, Jugador> arbol = new ArbolAVL<>();

    public Ranking(ListaEnlazada<Jugador> jugadores) {
        Iterador<Jugador> it = jugadores.iterador();
        while (it.tieneSiguiente()) {
            Jugador jugador = it.siguiente();
            arbol.insertar(new ClaveRanking(jugador), jugador);
        }
    }

    /** @return los jugadores del primero al último puesto. O(n). */
    public ListaEnlazada<Jugador> posiciones() {
        return arbol.enOrdenDescendente();
    }
}
