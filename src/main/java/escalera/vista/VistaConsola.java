package escalera.vista;

import escalera.estructuras.ListaEnlazada;
import escalera.modelo.Jugador;
import escalera.modelo.Tablero;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/** Implementación de Vista para la terminal (System.in / System.out). */
public class VistaConsola implements Vista {

    private final BufferedReader entrada = new BufferedReader(new InputStreamReader(System.in));
    private final RenderizadorTablero renderizador = new RenderizadorTablero();

    @Override
    public void mostrar(String mensaje) {
        System.out.println(mensaje);
    }

    @Override
    public String leer(String indicacion) {
        System.out.print(indicacion);
        System.out.flush();
        try {
            String linea = entrada.readLine();
            if (linea == null) {
                throw new IllegalStateException("Se cerró la entrada de datos");
            }
            return linea;
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer de la consola", e);
        }
    }

    @Override
    public void mostrarTablero(Tablero tablero, ListaEnlazada<Jugador> jugadores) {
        System.out.print(renderizador.dibujar(tablero, jugadores));
    }
}
