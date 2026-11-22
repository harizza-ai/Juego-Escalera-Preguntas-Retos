package main.java.escalera.estructuras;

/**
 * Iterador propio para recorrer nuestras estructuras sin depender de
 * las interfaces de colecciones de java.util.
 *
 * @param <T> tipo de elemento recorrido
 */
public interface Iterador<T> {

    /** @return true si quedan elementos por recorrer */
    boolean tieneSiguiente();

    /** @return el siguiente elemento y avanza una posición */
    T siguiente();
}
