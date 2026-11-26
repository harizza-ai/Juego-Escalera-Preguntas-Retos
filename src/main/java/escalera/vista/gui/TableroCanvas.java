package escalera.vista.gui;

import escalera.estructuras.Iterador;
import escalera.estructuras.ListaEnlazada;
import escalera.modelo.Casilla;
import escalera.modelo.Jugador;
import escalera.modelo.Tablero;
import escalera.modelo.TipoCasilla;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Dibuja el tablero del Juego Escalera en un Canvas de JavaFX.
 *
 * El recorrido va en zigzag, igual que el tablero ASCII y que un
 * tablero real: fila 1 de izquierda a derecha, fila 2 de derecha
 * a izquierda, etc. Colores por tipo de casilla:
 *   verde = escalera, rojo = serpiente, amarillo = reto, dorado = meta.
 * Las flechas representan escaleras (↑) y serpientes (↓), y cada
 * jugador se muestra como una ficha de color.
 *
 * — Generado con Grok para el punto extra de GUI —
 */
public class TableroCanvas extends Canvas {

    private static final Color COLOR_NORMAL    = Color.web("#e8e8e8");
    private static final Color COLOR_ESCALERA  = Color.web("#4caf50");
    private static final Color COLOR_SERPIENTE = Color.web("#f44336");
    private static final Color COLOR_RETO      = Color.web("#ffc107");
    private static final Color COLOR_META      = Color.web("#ffd700");
    private static final Color COLOR_BORDE     = Color.web("#555555");
    private static final Color[] COLORES_FICHA = {
            Color.web("#2196f3"), Color.web("#e91e63"),
            Color.web("#9c27b0"), Color.web("#ff9800")
    };

    private static final Font FUENTE_NUMERO = Font.font("Monospaced", 11);
    private static final Font FUENTE_TIPO   = Font.font("Monospaced", 9);
    private static final Font FUENTE_FICHA  = Font.font("SansSerif", 10);

    public TableroCanvas(double ancho, double alto) {
        super(ancho, alto);
    }

    /** Redibuja todo el tablero con la posición actual de cada jugador. */
    public void dibujar(Tablero tablero, ListaEnlazada<Jugador> jugadores) {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();
        gc.clearRect(0, 0, w, h);

        int columnas = Tablero.COLUMNAS;
        int filas = tablero.getTamano() / columnas;
        double anchoC = w / columnas;
        double altoC = h / filas;

        // Precarga las casillas en un arreglo para acceso rápido
        Casilla[] casillas = new Casilla[tablero.getTamano() + 1];
        Iterador<Casilla> itC = tablero.iteradorCasillas();
        while (itC.tieneSiguiente()) {
            Casilla c = itC.siguiente();
            casillas[c.getNumero()] = c;
        }

        // Dibuja las casillas
        for (int fila = 0; fila < filas; fila++) {
            for (int col = 0; col < columnas; col++) {
                int numero = numeroEn(fila, col, columnas);
                Casilla casilla = casillas[numero];
                double x = col * anchoC;
                double y = (filas - 1 - fila) * altoC;    // la fila 0 va abajo

                // Fondo por tipo
                gc.setFill(colorDe(casilla, tablero.getMeta()));
                gc.fillRect(x, y, anchoC, altoC);

                // Borde
                gc.setStroke(COLOR_BORDE);
                gc.setLineWidth(0.5);
                gc.strokeRect(x, y, anchoC, altoC);

                // Número
                gc.setFill(Color.BLACK);
                gc.setFont(FUENTE_NUMERO);
                gc.setTextAlign(TextAlignment.LEFT);
                gc.fillText(String.valueOf(numero), x + 3, y + 13);

                // Indicador de tipo
                gc.setFont(FUENTE_TIPO);
                String indicador = indicadorDe(casilla, tablero.getMeta());
                if (!indicador.isEmpty()) {
                    gc.fillText(indicador, x + 3, y + 24);
                }
            }
        }

        // Dibuja flechas de escaleras y serpientes
        for (int n = 1; n <= tablero.getTamano(); n++) {
            Casilla c = casillas[n];
            if (c.getTipo() == TipoCasilla.ESCALERA || c.getTipo() == TipoCasilla.SERPIENTE) {
                int destino = c.getDestino();
                double[] desde = centroDe(n, filas, columnas, anchoC, altoC);
                double[] hasta = centroDe(destino, filas, columnas, anchoC, altoC);

                gc.setStroke(c.getTipo() == TipoCasilla.ESCALERA
                        ? COLOR_ESCALERA.darker() : COLOR_SERPIENTE.darker());
                gc.setLineWidth(2);
                gc.strokeLine(desde[0], desde[1], hasta[0], hasta[1]);

                // Punta de flecha
                dibujarFlecha(gc, desde[0], desde[1], hasta[0], hasta[1]);
            }
        }

        // Dibuja fichas de los jugadores
        gc.setFont(FUENTE_FICHA);
        gc.setTextAlign(TextAlignment.CENTER);
        Iterador<Jugador> itJ = jugadores.iterador();
        int offset = 0;
        while (itJ.tieneSiguiente()) {
            Jugador jugador = itJ.siguiente();
            int pos = jugador.getPosicion();
            if (pos < 1 || pos > tablero.getTamano()) {
                offset++;
                continue;
            }
            double[] centro = centroDe(pos, filas, columnas, anchoC, altoC);
            double fx = centro[0] + (offset - 1) * 12;
            double fy = centro[1] + 8;
            int colorIdx = (jugador.getFicha() - 1) % COLORES_FICHA.length;

            gc.setFill(COLORES_FICHA[colorIdx]);
            gc.fillOval(fx - 8, fy - 8, 16, 16);
            gc.setFill(Color.WHITE);
            gc.fillText("J" + jugador.getFicha(), fx, fy + 4);
            offset++;
        }
    }

    // ---------------------------------------------------------------- utilidades

    /** Número de casilla en la posición (fila, columna) con zigzag. */
    private int numeroEn(int fila, int columna, int columnas) {
        int col = (fila % 2 == 0) ? columna : columnas - 1 - columna;
        return fila * columnas + col + 1;
    }

    /** Coordenadas del centro de una casilla (para flechas y fichas). */
    private double[] centroDe(int numero, int filas, int columnas, double anchoC, double altoC) {
        int fila = (numero - 1) / columnas;
        int col = (numero - 1) % columnas;
        if (fila % 2 != 0) {
            col = columnas - 1 - col;
        }
        double x = col * anchoC + anchoC / 2;
        double y = (filas - 1 - fila) * altoC + altoC / 2;
        return new double[]{x, y};
    }

    /** Color de fondo según el tipo de casilla. */
    private Color colorDe(Casilla casilla, int meta) {
        if (casilla.getNumero() == meta) {
            return COLOR_META;
        }
        switch (casilla.getTipo()) {
            case ESCALERA:  return COLOR_ESCALERA.brighter();
            case SERPIENTE: return COLOR_SERPIENTE.brighter();
            case RETO:      return COLOR_RETO;
            default:        return COLOR_NORMAL;
        }
    }

    /** Texto indicador del tipo (E>n, S>n, ?n, META). */
    private String indicadorDe(Casilla casilla, int meta) {
        if (casilla.getNumero() == meta) {
            return "META";
        }
        switch (casilla.getTipo()) {
            case ESCALERA:  return "E>" + casilla.getDestino();
            case SERPIENTE: return "S>" + casilla.getDestino();
            case RETO:      return "?" + casilla.getDificultad();
            default:        return "";
        }
    }

    /** Dibuja una punta de flecha sencilla en el extremo destino. */
    private void dibujarFlecha(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        double angulo = Math.atan2(y2 - y1, x2 - x1);
        double largo = 8;
        double ax = x2 - largo * Math.cos(angulo - Math.PI / 6);
        double ay = y2 - largo * Math.sin(angulo - Math.PI / 6);
        double bx = x2 - largo * Math.cos(angulo + Math.PI / 6);
        double by = y2 - largo * Math.sin(angulo + Math.PI / 6);

        gc.setFill(gc.getStroke() instanceof Color ? (Color) gc.getStroke() : Color.BLACK);
        gc.fillPolygon(new double[]{x2, ax, bx}, new double[]{y2, ay, by}, 3);
    }
}
