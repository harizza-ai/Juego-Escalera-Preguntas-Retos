package escalera.estructuras;

public class Cola<T> {
    private final ListaEnlazada<T> elementos = new ListaEnlazada<>();

    /** Agrega un elemento al final de la cola. O(1). */
    public void encolar(T dato) {
        elementos.agregarAlFinal(dato);
    }

    public T desencolar() {
        validarNoVacia();
        return elementos.eliminarPrimero();
    }

    /**
     * Devuelve el elemento del frente sin sacarlo. O(1).
     *
     * @throws IllegalStateException si la cola está vacía
     */
    public T verFrente() {
        validarNoVacia();
        return elementos.obtenerPrimero();
    }

     public int tamano() {
        return elementos.tamano();
    }

    public boolean estaVacia() {
        return elementos.estaVacia();
    }

    /** Evita el mensaje genérico de la lista y deja uno que habla de la cola. */
    private void validarNoVacia() {
        if (elementos.estaVacia()) {
            throw new IllegalStateException("La cola está vacía");
        }
    }

}
