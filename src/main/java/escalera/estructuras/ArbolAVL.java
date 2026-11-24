package escalera.estructuras;

/**
 * Árbol binario de búsqueda auto-balanceado (AVL): en cada nodo la diferencia
 * de alturas de sus subárboles es como máximo 1, y se corrige con rotaciones.
 * Así la altura es siempre O(log n), incluso con claves insertadas en orden.
 *
 * Se usa para el catálogo de preguntas (clave = dificultad * 1000 + id)
 * y para el ranking de jugadores (clave = ClaveRanking).
 *
 * @param <K> tipo de la clave
 * @param <V> tipo del valor asociado
 */
public class ArbolAVL<K extends Comparable<K>, V> {

    private static class Nodo<K, V> {
        private final K clave;
        private V valor;
        private int altura = 1;
        private Nodo<K, V> izquierdo;
        private Nodo<K, V> derecho;

        private Nodo(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
        }
    }

    private Nodo<K, V> raiz;
    private int tamano;

    /** Inserta el par; si la clave ya existía, reemplaza su valor. O(log n). */
    public void insertar(K clave, V valor) {
        validarClave(clave);
        raiz = insertar(raiz, clave, valor);
    }

    /** @return el valor de la clave, o null si no existe. O(log n). */
    public V buscar(K clave) {
        validarClave(clave);
        Nodo<K, V> actual = raiz;
        while (actual != null) {
            int comparacion = clave.compareTo(actual.clave);
            if (comparacion == 0) {
                return actual.valor;
            }
            actual = (comparacion < 0) ? actual.izquierdo : actual.derecho;
        }
        return null;
    }

    public boolean contiene(K clave) {
        return buscar(clave) != null;
    }

    public int tamano() {
        return tamano;
    }

    public boolean estaVacio() {
        return tamano == 0;
    }

    /** @return altura del árbol (0 si está vacío). O(1). */
    public int altura() {
        return alturaDe(raiz);
    }

    /** @return los valores de menor a mayor clave. O(n). */
    public ListaEnlazada<V> enOrden() {
        ListaEnlazada<V> resultado = new ListaEnlazada<>();
        recorrerAscendente(raiz, resultado);
        return resultado;
    }

    /** @return los valores de mayor a menor clave. O(n). */
    public ListaEnlazada<V> enOrdenDescendente() {
        ListaEnlazada<V> resultado = new ListaEnlazada<>();
        recorrerDescendente(raiz, resultado);
        return resultado;
    }

    /**
     * Devuelve los valores con clave entre 'desde' y 'hasta' (incluidas), de
     * menor a mayor. Descarta las ramas fuera del rango. O(log n + k).
     * Se usa para obtener las preguntas de una dificultad.
     */
    public ListaEnlazada<V> enRango(K desde, K hasta) {
        validarClave(desde);
        validarClave(hasta);
        ListaEnlazada<V> resultado = new ListaEnlazada<>();
        recorrerRango(raiz, desde, hasta, resultado);
        return resultado;
    }

    private Nodo<K, V> insertar(Nodo<K, V> nodo, K clave, V valor) {
        if (nodo == null) {
            tamano++;
            return new Nodo<>(clave, valor);
        }
        int comparacion = clave.compareTo(nodo.clave);
        if (comparacion < 0) {
            nodo.izquierdo = insertar(nodo.izquierdo, clave, valor);
        } else if (comparacion > 0) {
            nodo.derecho = insertar(nodo.derecho, clave, valor);
        } else {
            nodo.valor = valor;
            return nodo;
        }
        return balancear(nodo);
    }

    /** Actualiza la altura y aplica la rotación necesaria (4 casos: II, ID, DD, DI). */
    private Nodo<K, V> balancear(Nodo<K, V> nodo) {
        actualizarAltura(nodo);
        int balance = factorBalance(nodo);

        if (balance > 1) {
            if (factorBalance(nodo.izquierdo) < 0) {
                nodo.izquierdo = rotarIzquierda(nodo.izquierdo);
            }
            return rotarDerecha(nodo);
        }
        if (balance < -1) {
            if (factorBalance(nodo.derecho) > 0) {
                nodo.derecho = rotarDerecha(nodo.derecho);
            }
            return rotarIzquierda(nodo);
        }
        return nodo;
    }

    /** Rotación a la derecha: el hijo izquierdo sube y el nodo baja a su derecha. */
    private Nodo<K, V> rotarDerecha(Nodo<K, V> y) {
        Nodo<K, V> x = y.izquierdo;
        y.izquierdo = x.derecho;
        x.derecho = y;
        actualizarAltura(y);
        actualizarAltura(x);
        return x;
    }

    /** Rotación a la izquierda: simétrica de rotarDerecha. */
    private Nodo<K, V> rotarIzquierda(Nodo<K, V> x) {
        Nodo<K, V> y = x.derecho;
        x.derecho = y.izquierdo;
        y.izquierdo = x;
        actualizarAltura(x);
        actualizarAltura(y);
        return y;
    }

    private void recorrerAscendente(Nodo<K, V> nodo, ListaEnlazada<V> destino) {
        if (nodo == null) {
            return;
        }
        recorrerAscendente(nodo.izquierdo, destino);
        destino.agregarAlFinal(nodo.valor);
        recorrerAscendente(nodo.derecho, destino);
    }

    private void recorrerDescendente(Nodo<K, V> nodo, ListaEnlazada<V> destino) {
        if (nodo == null) {
            return;
        }
        recorrerDescendente(nodo.derecho, destino);
        destino.agregarAlFinal(nodo.valor);
        recorrerDescendente(nodo.izquierdo, destino);
    }

    private void recorrerRango(Nodo<K, V> nodo, K desde, K hasta, ListaEnlazada<V> destino) {
        if (nodo == null) {
            return;
        }
        int contraDesde = desde.compareTo(nodo.clave);
        int contraHasta = hasta.compareTo(nodo.clave);

        if (contraDesde < 0) {
            recorrerRango(nodo.izquierdo, desde, hasta, destino);
        }
        if (contraDesde <= 0 && contraHasta >= 0) {
            destino.agregarAlFinal(nodo.valor);
        }
        if (contraHasta > 0) {
            recorrerRango(nodo.derecho, desde, hasta, destino);
        }
    }

    private int alturaDe(Nodo<K, V> nodo) {
        return (nodo == null) ? 0 : nodo.altura;
    }

    private void actualizarAltura(Nodo<K, V> nodo) {
        nodo.altura = 1 + Math.max(alturaDe(nodo.izquierdo), alturaDe(nodo.derecho));
    }

    /** Positivo si pesa más la izquierda, negativo si pesa más la derecha. */
    private int factorBalance(Nodo<K, V> nodo) {
        return alturaDe(nodo.izquierdo) - alturaDe(nodo.derecho);
    }

    private void validarClave(K clave) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave no puede ser nula");
        }
    }
}