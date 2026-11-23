package escalera.estructuras;

public class Pila <T> {
    private final ListaEnlazada<T> elementos = new ListaEnlazada<>();

     /** Coloca un elemento en la cima. O(1). */
    public void apilar(T dato) {
        elementos.agregarAlInicio(dato);
    }

    /**
     * Saca y devuelve el elemento de la cima. O(1).
     *
     * @throws IllegalStateException si la pila está vacía
     */
    public T desapilar() {
        validarNoVacia();
        return elementos.eliminarPrimero();
    }

    /**
     * Devuelve el elemento de la cima sin sacarlo. O(1).
     *
     * @throws IllegalStateException si la pila está vacía
     */
    public T verCima() {
        validarNoVacia();
        return elementos.obtenerPrimero();
    }

    /**
     * Devuelve una lista con los últimos n elementos apilados, del más reciente
     * al más antiguo, sin modificar la pila. Si hay menos de n, devuelve todos. O(n).
     */
    public ListaEnlazada<T> ultimos(int n) {
        ListaEnlazada<T> resultado = new ListaEnlazada<>();
        Iterador<T> iterador = elementos.iterador();
        // La cima es la cabeza, así que el iterador ya recorre del más reciente al más antiguo.
        while (iterador.tieneSiguiente() && resultado.tamano() < n) {
            resultado.agregarAlFinal(iterador.siguiente());
        }
        return resultado;
    }

    public int tamano() {
        return elementos.tamano();
    }

    public boolean estaVacia() {
        return elementos.estaVacia();
    }

    private void validarNoVacia() {
        if (elementos.estaVacia()) {
            throw new IllegalStateException("La pila está vacía");
        }
    }
}
