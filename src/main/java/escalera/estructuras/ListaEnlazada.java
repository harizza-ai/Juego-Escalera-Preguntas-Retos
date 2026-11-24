package escalera.estructuras;

/**
 * Lista simplemente enlazada con referencia a la cabeza y al último nodo.
 *
 * Complejidades: agregarAlInicio O(1), agregarAlFinal O(1),
 * eliminarPrimero O(1), obtenerPrimero O(1), obtener(i) O(n),
 * recorrido con iterador O(n).
 *
 * Esta clase es la base de Cola y Pila: ambas la usan por composición,
 * por eso el Nodo puede seguir siendo privado.
 *
 * @param <T> tipo de elemento almacenado
 */
public class ListaEnlazada<T> {

    /** Nodo interno: guarda un dato y el enlace al siguiente nodo. */
    private static class Nodo<T> {
        private final T dato;
        private Nodo<T> siguiente;

        private Nodo(T dato) {
            this.dato = dato;
        }
    }

    private Nodo<T> cabeza;
    /** Último nodo de la lista antes se llamaba cola, se renombró para no confundirlo con la clase Cola. */
    private Nodo<T> ultimo;
    private int tamano;

    /** Inserta al inicio de la lista. O(1). */
    public void agregarAlInicio(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        nuevo.siguiente = cabeza;
        cabeza = nuevo;
        // Si la lista estaba vacía, el nuevo nodo también es el último.
        if (ultimo == null) {
            ultimo = nuevo;
        }
        tamano++;
    }

    /** Inserta al final de la lista. O(1) gracias a la referencia al último nodo. */
    public void agregarAlFinal(T dato) {
        Nodo<T> nuevo = new Nodo<>(dato);
        if (ultimo == null) {
            // Lista vacía: el nuevo nodo es a la vez cabeza y último.
            cabeza = nuevo;
        } else {
            ultimo.siguiente = nuevo;
        }
        ultimo = nuevo;
        tamano++;
    }

    /**
     * Elimina el primer elemento y lo devuelve. O(1).
     * Lo usan Cola (desencolar) y Pila (desapilar).
     *
     * @throws IllegalStateException si la lista está vacía
     */
    public T eliminarPrimero() {
        if (cabeza == null) {
            throw new IllegalStateException("La lista está vacía");
        }
        T dato = cabeza.dato;
        cabeza = cabeza.siguiente;
        // Si la lista quedó vacía, también hay que limpiar la referencia al último.
        if (cabeza == null) {
            ultimo = null;
        }
        tamano--;
        return dato;
    }

    /**
     * Devuelve el primer elemento sin eliminarlo. O(1).
     * Lo usan Cola y Pila para consultar el frente o la cima.
     *
     * @throws IllegalStateException si la lista está vacía
     */
    public T obtenerPrimero() {
        if (cabeza == null) {
            throw new IllegalStateException("La lista está vacía");
        }
        return cabeza.dato;
    }

    /**
     * Devuelve el elemento en la posición indicada (base 0). O(n).
     *
     * @throws IndexOutOfBoundsException si el índice no existe
     */
    public T obtener(int indice) {
        if (indice < 0 || indice >= tamano) {
            throw new IndexOutOfBoundsException("Índice fuera de rango: " + indice);
        }
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.siguiente;
        }
        return actual.dato;
    }

    public int tamano() {
        return tamano;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    /** Elimina todos los elementos. */
    public void limpiar() {
        cabeza = null;
        ultimo = null;
        tamano = 0;
    }

    /** @return un iterador que recorre la lista de cabeza a último nodo */
    public Iterador<T> iterador() {
        return new Iterador<T>() {
            private Nodo<T> actual = cabeza;

            @Override
            public boolean tieneSiguiente() {
                return actual != null;
            }

            @Override
            public T siguiente() {
                // Evita un NullPointerException si se pide un elemento y ya no quedan.
                if (actual == null) {
                    throw new IllegalStateException("No quedan elementos por recorrer");
                }
                T dato = actual.dato;
                actual = actual.siguiente;
                return dato;
            }
        };
    }
}