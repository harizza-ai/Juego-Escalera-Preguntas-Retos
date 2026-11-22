package escalera.vista;

import escalera.estructuras.ListaEnlazada;
import escalera.modelo.Jugador;
import escalera.modelo.Tablero;

/**
 * Contrato de entrada/salida del juego. La lógica (JuegoService) solo conoce esta
 * interfaz, así que se puede cambiar la consola por una GUI Swing sin tocarla,
 * y en las pruebas se usa una vista simulada.
 */
public interface Vista {

    /** Muestra un mensaje al usuario. */
    void mostrar(String mensaje);

    /** Muestra una indicación y devuelve lo que escribe el usuario. */
    String leer(String indicacion);

    /** Dibuja el tablero con la posición de cada jugador. */
    void mostrarTablero(Tablero tablero, ListaEnlazada<Jugador> jugadores);
}
