package escalera.estructuras;

/**
 * Tabla hash con encadenamiento separado (cada cubeta es una lista de entradas).
 * Se redimensiona al doble cuando el factor de carga supera 0.75, por lo que
 * poner, obtener y eliminar son O(1) en promedio.
 *
 * @param <K> tipo de la clave
 * @param <V> tipo del valor
 */
public class TablaHash<K, V> {

    private static final int CAPACIDAD_INICIAL = 8;
    private static final double FACTOR_CARGA_MAXIMO = 0.75;

    /** Entrada clave-valor; las colisiones se encadenan con 'siguiente'. */
    private static class Entrada<K, V> {
        private final K clave;
        private V valor;
        private Entrada<K, V> siguiente;

        private Entrada(K clave, V valor, Entrada<K, V> siguiente) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = siguiente;
        }
    }

    private Entrada<K, V>[] cubetas;
    private int tamano;

    public TablaHash() {
        cubetas = crearCubetas(CAPACIDAD_INICIAL);
    }

    /** Asocia la clave con el valor; si la clave ya existía, reemplaza su valor. */
    public void poner(K clave, V valor) {
        Entrada<K, V> existente = buscarEntrada(clave);
        if (existente != null) {
            existente.valor = valor;
            return;
        }
        if (tamano + 1 > cubetas.length * FACTOR_CARGA_MAXIMO) {
            redimensionar();
        }
        int indice = indiceDe(clave, cubetas.length);
        cubetas[indice] = new Entrada<>(clave, valor, cubetas[indice]);
        tamano++;
    }

    /** @return el valor asociado a la clave, o null si no existe */
    public V obtener(K clave) {
        Entrada<K, V> entrada = buscarEntrada(clave);
        return entrada == null ? null : entrada.valor;
    }

    public boolean contiene(K clave) {
        return buscarEntrada(clave) != null;
    }

    /**
     * Elimina la clave de la tabla.
     *
     * @return true si la clave existía y fue eliminada
     */
    public boolean eliminar(K clave) {
        int indice = indiceDe(clave, cubetas.length);
        Entrada<K, V> anterior = null;
        Entrada<K, V> actual = cubetas[indice];
        while (actual != null) {
            if (actual.clave.equals(clave)) {
                if (anterior == null) {
                    cubetas[indice] = actual.siguiente;
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                tamano--;
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    public int tamano() {
        return tamano;
    }

    /** @return todas las claves (el orden no está garantizado) */
    public ListaEnlazada<K> claves() {
        ListaEnlazada<K> resultado = new ListaEnlazada<>();
        for (Entrada<K, V> cabeza : cubetas) {
            for (Entrada<K, V> e = cabeza; e != null; e = e.siguiente) {
                resultado.agregarAlFinal(e.clave);
            }
        }
        return resultado;
    }

    /** @return todos los valores (el orden no está garantizado) */
    public ListaEnlazada<V> valores() {
        ListaEnlazada<V> resultado = new ListaEnlazada<>();
        for (Entrada<K, V> cabeza : cubetas) {
            for (Entrada<K, V> e = cabeza; e != null; e = e.siguiente) {
                resultado.agregarAlFinal(e.valor);
            }
        }
        return resultado;
    }

    private Entrada<K, V> buscarEntrada(K clave) {
        Entrada<K, V> actual = cubetas[indiceDe(clave, cubetas.length)];
        while (actual != null) {
            if (actual.clave.equals(clave)) {
                return actual;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    /** Duplica la capacidad y vuelve a distribuir todas las entradas. */
    private void redimensionar() {
        Entrada<K, V>[] nuevas = crearCubetas(cubetas.length * 2);
        for (Entrada<K, V> cabeza : cubetas) {
            Entrada<K, V> actual = cabeza;
            while (actual != null) {
                Entrada<K, V> siguiente = actual.siguiente;
                int indice = indiceDe(actual.clave, nuevas.length);
                actual.siguiente = nuevas[indice];
                nuevas[indice] = actual;
                actual = siguiente;
            }
        }
        cubetas = nuevas;
    }

    /** Convierte el hashCode en un índice válido (no negativo) de cubeta. */
    private static int indiceDe(Object clave, int capacidad) {
        return (clave.hashCode() & 0x7fffffff) % capacidad;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <K, V> Entrada<K, V>[] crearCubetas(int capacidad) {
        return (Entrada<K, V>[]) new Entrada[capacidad];
    }
}
