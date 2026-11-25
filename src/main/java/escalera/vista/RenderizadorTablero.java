package escalera.vista;

import escalera.estructuras.Iterador;
import escalera.estructuras.ListaEnlazada;
import escalera.modelo.Casilla;
import escalera.modelo.Jugador;
import escalera.modelo.Tablero;

/**
 * Dibuja el tablero en texto (ASCII) y devuelve el resultado como String;
 * mostrarlo es tarea de la Vista. El recorrido va en zigzag como un tablero
 * real (1..10 de izquierda a derecha, 20..11 de derecha a izquierda, etc.).
 *
 * Cada casilla ocupa dos líneas: número con su tipo (E>15 escalera,
 * S>4 serpiente, ?2 reto de nivel 2, META) y las fichas de los jugadores.
 */
public class RenderizadorTablero {

    private static final int ANCHO_CELDA = 8;
    private static final String SEPARADOR_CELDA = "+" + repetir('-', ANCHO_CELDA);
    private static final String PREFIJO_FICHA = "J";
    private static final int POSICION_SALIDA = 0;

    /** @return el dibujo completo del tablero con la leyenda de jugadores */
    public String dibujar(Tablero tablero, ListaEnlazada<Jugador> jugadores) {
        Casilla[] casillas = casillasPorNumero(tablero);
        String[] fichas = fichasPorCasilla(tablero.getTamano(), jugadores);

        StringBuilder dibujo = new StringBuilder("\n");
        dibujo.append(filaSeparadora());
        int filas = tablero.getTamano() / Tablero.COLUMNAS;
        for (int fila = filas - 1; fila >= 0; fila--) {      // de la meta hacia la salida
            dibujo.append(filaDeCasillas(fila, casillas, fichas, tablero.getMeta()));
            dibujo.append(filaSeparadora());
        }
        dibujo.append(fichas[POSICION_SALIDA].isEmpty() ? "" : "Salida: " + fichas[POSICION_SALIDA] + "\n");
        dibujo.append(leyenda(jugadores));
        return dibujo.toString();
    }

    /** Pasa las casillas de la lista a un arreglo por número, recorriendo la lista una sola vez. */
    private Casilla[] casillasPorNumero(Tablero tablero) {
        Casilla[] casillas = new Casilla[tablero.getTamano() + 1];
        Iterador<Casilla> it = tablero.iteradorCasillas();
        while (it.tieneSiguiente()) {
            Casilla casilla = it.siguiente();
            casillas[casilla.getNumero()] = casilla;
        }
        return casillas;
    }

    /** Texto con las fichas de quienes están en cada casilla (la posición 0 es la salida). */
    private String[] fichasPorCasilla(int tamano, ListaEnlazada<Jugador> jugadores) {
        String[] fichas = new String[tamano + 1];
        for (int i = 0; i < fichas.length; i++) {
            fichas[i] = "";
        }
        Iterador<Jugador> it = jugadores.iterador();
        while (it.tieneSiguiente()) {
            Jugador jugador = it.siguiente();
            fichas[jugador.getPosicion()] += PREFIJO_FICHA + jugador.getFicha();
        }
        return fichas;
    }

    private String filaDeCasillas(int fila, Casilla[] casillas, String[] fichas, int meta) {
        StringBuilder linea1 = new StringBuilder();
        StringBuilder linea2 = new StringBuilder();
        for (int columna = 0; columna < Tablero.COLUMNAS; columna++) {
            int numero = numeroEn(fila, columna);
            linea1.append("|").append(rellenar(descripcion(casillas[numero], meta)));
            linea2.append("|").append(rellenar(fichas[numero]));
        }
        return linea1.append("|\n").append(linea2).append("|\n").toString();
    }

    /** Las filas impares se numeran de derecha a izquierda (zigzag). */
    private int numeroEn(int fila, int columna) {
        int posicionEnFila = (fila % 2 == 0) ? columna : Tablero.COLUMNAS - 1 - columna;
        return fila * Tablero.COLUMNAS + posicionEnFila + 1;
    }

    private String descripcion(Casilla casilla, int meta) {
        String numero = String.format("%2d ", casilla.getNumero());
        if (casilla.getNumero() == meta) {
            return numero + "META";
        }
        switch (casilla.getTipo()) {
            case ESCALERA:
                return numero + "E>" + casilla.getDestino();
            case SERPIENTE:
                return numero + "S>" + casilla.getDestino();
            case RETO:
                return numero + "?" + casilla.getDificultad();
            default:
                return numero;
        }
    }

    private String leyenda(ListaEnlazada<Jugador> jugadores) {
        StringBuilder texto = new StringBuilder("E>n escalera | S>n serpiente | ?n reto de nivel n\n");
        Iterador<Jugador> it = jugadores.iterador();
        while (it.tieneSiguiente()) {
            Jugador jugador = it.siguiente();
            texto.append(PREFIJO_FICHA).append(jugador.getFicha()).append(" = ").append(jugador.getNombre())
                    .append(" (casilla ").append(jugador.getPosicion()).append(")\n");
        }
        return texto.toString();
    }

    private String filaSeparadora() {
        StringBuilder linea = new StringBuilder();
        for (int columna = 0; columna < Tablero.COLUMNAS; columna++) {
            linea.append(SEPARADOR_CELDA);
        }
        return linea.append("+\n").toString();
    }

    private String rellenar(String texto) {
        return texto + repetir(' ', ANCHO_CELDA - texto.length());
    }

    private static String repetir(char caracter, int veces) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < veces; i++) {
            texto.append(caracter);
        }
        return texto.toString();
    }
}