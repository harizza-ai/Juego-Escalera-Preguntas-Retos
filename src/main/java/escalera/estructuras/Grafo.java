package escalera.estructuras;

/**
 * Grafo dirigido con listas de adyacencia. Los nodos son enteros 1..n.
 * En el juego modela los "saltos": cada escalera o serpiente es una arista
 * casillaOrigen -> casillaDestino.
 *
 * Restricción propia del juego: cada nodo tiene como máximo UNA arista saliente
 * (una casilla no puede ser escalera y serpiente a la vez).
 */
public class Grafo {

    /** Valor que indica que un nodo no tiene arista saliente. */
    public static final int SIN_SALTO = -1;

    private static final int NO_VISITADO = 0;
    private static final int EN_PROCESO = 1;
    private static final int TERMINADO = 2;

    /** Arista en la lista de adyacencia de un nodo. */
    private static class Arista {
        private final int destino;
        private final Arista siguiente;

        private Arista(int destino, Arista siguiente) {
            this.destino = destino;
            this.siguiente = siguiente;
        }
    }

    private final Arista[] adyacencia;   // índice = número de nodo (se ignora el 0)
    private final int numeroNodos;

    public Grafo(int numeroNodos) {
        if (numeroNodos < 1) {
            throw new IllegalArgumentException("El grafo necesita al menos un nodo");
        }
        this.numeroNodos = numeroNodos;
        this.adyacencia = new Arista[numeroNodos + 1];
    }

    /**
     * Agrega la arista dirigida origen -> destino.
     *
     * @throws IllegalArgumentException si algún nodo no existe o el origen ya tiene salto
     */
    public void agregarArista(int origen, int destino) {
        validarNodo(origen);
        validarNodo(destino);
        if (adyacencia[origen] != null) {
            throw new IllegalArgumentException("La casilla " + origen + " ya tiene un salto");
        }
        adyacencia[origen] = new Arista(destino, null);
    }

    /** @return el destino del salto que parte de 'origen', o SIN_SALTO si no hay */
    public int destinoDesde(int origen) {
        validarNodo(origen);
        return adyacencia[origen] == null ? SIN_SALTO : adyacencia[origen].destino;
    }

    public boolean tieneSalto(int origen) {
        return destinoDesde(origen) != SIN_SALTO;
    }

    /**
     * Detecta ciclos con una búsqueda en profundidad (DFS) usando tres colores.
     * Un ciclo (p. ej. escalera 5->20 y serpiente 20->5) dejaría al jugador
     * saltando para siempre, por eso se valida al construir el tablero. O(V + E).
     */
    public boolean tieneCiclo() {
        int[] estado = new int[numeroNodos + 1];
        for (int nodo = 1; nodo <= numeroNodos; nodo++) {
            if (estado[nodo] == NO_VISITADO && hayCicloDesde(nodo, estado)) {
                return true;
            }
        }
        return false;
    }

    private boolean hayCicloDesde(int nodo, int[] estado) {
        estado[nodo] = EN_PROCESO;
        for (Arista a = adyacencia[nodo]; a != null; a = a.siguiente) {
            if (estado[a.destino] == EN_PROCESO) {
                return true;                       // volvimos a un nodo de la ruta actual
            }
            if (estado[a.destino] == NO_VISITADO && hayCicloDesde(a.destino, estado)) {
                return true;
            }
        }
        estado[nodo] = TERMINADO;
        return false;
    }

    private void validarNodo(int nodo) {
        if (nodo < 1 || nodo > numeroNodos) {
            throw new IllegalArgumentException("Nodo inexistente: " + nodo);
        }
    }
}
