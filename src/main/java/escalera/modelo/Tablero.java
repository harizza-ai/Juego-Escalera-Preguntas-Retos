package escalera.modelo;

import escalera.estructuras.Grafo;
import escalera.estructuras.Iterador;
import escalera.estructuras.ListaEnlazada;

/**
 * Tablero de juego. Combina dos estructuras:
 * - una lista enlazada con las casillas 1..N (tipo, destino, dificultad);
 * - un grafo dirigido con los saltos (escalera/serpiente) origen -> destino.
 *
 * Al construirse valida que la configuración sea coherente (ver validar...).
 */
public class Tablero {

    public static final int COLUMNAS = 10;
    public static final int TAMANO_ESTANDAR = 50;

    private static final int NIVEL_FACIL = 1;
    private static final int NIVEL_MEDIO = 2;
    private static final int NIVEL_DIFICIL = 3;

    private final int tamano;
    private final ListaEnlazada<Casilla> casillas = new ListaEnlazada<>();
    private final Grafo saltos;

    /**
     * @param tamano     número de casillas (múltiplo de COLUMNAS)
     * @param escaleras  pares {origen, destino}, con destino > origen
     * @param serpientes pares {origen, destino}, con destino < origen
     * @param retos      números de las casillas de reto
     */
    public Tablero(int tamano, int[][] escaleras, int[][] serpientes, int[] retos) {
        validarTamano(tamano);
        this.tamano = tamano;
        this.saltos = new Grafo(tamano);

        TipoCasilla[] tipos = new TipoCasilla[tamano + 1];
        int[] destinos = new int[tamano + 1];
        for (int i = 1; i <= tamano; i++) {
            tipos[i] = TipoCasilla.NORMAL;
        }

        marcarSaltos(tipos, destinos, escaleras, TipoCasilla.ESCALERA);
        marcarSaltos(tipos, destinos, serpientes, TipoCasilla.SERPIENTE);
        marcarRetos(tipos, retos);
        validarDestinos(tipos, destinos);

        construirCasillas(tipos, destinos);
        if (saltos.tieneCiclo()) {
            throw new IllegalArgumentException("Las escaleras y serpientes forman un ciclo");
        }
    }

    /** Tablero estándar de 50 casillas: 4 escaleras, 5 serpientes y 11 retos. */
    public static Tablero crearEstandar() {
        int[][] escaleras = {{3, 15}, {8, 27}, {21, 40}, {33, 45}};
        int[][] serpientes = {{17, 4}, {31, 12}, {38, 19}, {43, 24}, {48, 29}};
        int[] retos = {6, 11, 14, 18, 23, 26, 30, 35, 37, 41, 46};
        return new Tablero(TAMANO_ESTANDAR, escaleras, serpientes, retos);
    }

    /** @return la casilla con ese número (1..tamaño). O(n) por ser lista enlazada. */
    public Casilla obtenerCasilla(int numero) {
        if (numero < 1 || numero > tamano) {
            throw new IllegalArgumentException("Casilla inexistente: " + numero);
        }
        return casillas.obtener(numero - 1);
    }

    /**
     * Sigue la arista del grafo si la casilla tiene escalera o serpiente.
     *
     * @return la casilla final del salto, o la misma casilla si no hay salto
     */
    public int resolverSalto(int numero) {
        int destino = saltos.destinoDesde(numero);
        return destino == Grafo.SIN_SALTO ? numero : destino;
    }

    public int getTamano() {
        return tamano;
    }

    /** @return la meta: la última casilla del tablero */
    public int getMeta() {
        return tamano;
    }

    /** @return un iterador sobre las casillas, de la 1 a la meta */
    public Iterador<Casilla> iteradorCasillas() {
        return casillas.iterador();
    }

    // ------------------------------------------------------------- construcción

    private void marcarSaltos(TipoCasilla[] tipos, int[] destinos, int[][] pares, TipoCasilla tipo) {
        for (int[] par : pares) {
            int origen = par[0];
            int destino = par[1];
            validarCasillaIntermedia(origen);
            if (tipos[origen] != TipoCasilla.NORMAL) {
                throw new IllegalArgumentException("La casilla " + origen + " ya tiene un tipo asignado");
            }
            boolean sentidoCorrecto = tipo == TipoCasilla.ESCALERA ? destino > origen : destino < origen;
            if (!sentidoCorrecto || destino < 1 || destino > tamano) {
                throw new IllegalArgumentException(tipo + " inválida: " + origen + " -> " + destino);
            }
            tipos[origen] = tipo;
            destinos[origen] = destino;
        }
    }

    private void marcarRetos(TipoCasilla[] tipos, int[] retos) {
        for (int numero : retos) {
            validarCasillaIntermedia(numero);
            if (tipos[numero] != TipoCasilla.NORMAL) {
                throw new IllegalArgumentException("La casilla " + numero + " ya tiene un tipo asignado");
            }
            tipos[numero] = TipoCasilla.RETO;
        }
    }

    /** Un salto debe terminar en una casilla NORMAL: así no encadena saltos ni retos. */
    private void validarDestinos(TipoCasilla[] tipos, int[] destinos) {
        for (int i = 1; i <= tamano; i++) {
            boolean esSalto = tipos[i] == TipoCasilla.ESCALERA || tipos[i] == TipoCasilla.SERPIENTE;
            if (esSalto && tipos[destinos[i]] != TipoCasilla.NORMAL) {
                throw new IllegalArgumentException(
                        "El salto " + i + " -> " + destinos[i] + " debe terminar en una casilla normal");
            }
        }
    }

    /** Crea los nodos de la lista y las aristas del grafo a partir de la configuración. */
    private void construirCasillas(TipoCasilla[] tipos, int[] destinos) {
        for (int numero = 1; numero <= tamano; numero++) {
            switch (tipos[numero]) {
                case ESCALERA:
                    casillas.agregarAlFinal(Casilla.escalera(numero, destinos[numero]));
                    saltos.agregarArista(numero, destinos[numero]);
                    break;
                case SERPIENTE:
                    casillas.agregarAlFinal(Casilla.serpiente(numero, destinos[numero]));
                    saltos.agregarArista(numero, destinos[numero]);
                    break;
                case RETO:
                    casillas.agregarAlFinal(Casilla.reto(numero, dificultadPara(numero)));
                    break;
                default:
                    casillas.agregarAlFinal(Casilla.normal(numero));
            }
        }
    }

    /** La dificultad crece con el avance: primer tercio fácil, segundo medio, último difícil. */
    private int dificultadPara(int numero) {
        if (numero <= tamano / 3) {
            return NIVEL_FACIL;
        }
        return numero <= (2 * tamano) / 3 ? NIVEL_MEDIO : NIVEL_DIFICIL;
    }

    private void validarTamano(int tamano) {
        if (tamano < COLUMNAS || tamano % COLUMNAS != 0) {
            throw new IllegalArgumentException(
                    "El tamaño debe ser un múltiplo de " + COLUMNAS + " (mínimo " + COLUMNAS + ")");
        }
    }

    /** La casilla 1 (salida) y la meta no pueden tener escalera, serpiente ni reto. */
    private void validarCasillaIntermedia(int numero) {
        if (numero <= 1 || numero >= tamano) {
            throw new IllegalArgumentException(
                    "La casilla " + numero + " no puede ser especial (debe estar entre 2 y " + (tamano - 1) + ")");
        }
    }
}
