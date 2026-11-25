package escalera.servicio;

import escalera.estructuras.ListaEnlazada;
import escalera.modelo.Pregunta;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Lee preguntas desde un archivo de texto (UTF-8) y las agrega al BancoPreguntas,
 * que las guarda en el ArbolAVL.
 *
 * Formato de cada línea (campos separados por ';'):
 *   dificultad;categoria;enunciado;opcionA;opcionB;opcionC;opcionD;letraCorrecta
 *
 * Las líneas vacías y las que empiezan con '#' se ignoran. El archivo no trae id:
 * se asigna automáticamente (1, 2, 3...) dentro de cada dificultad.
 * Una línea inválida no detiene la carga: se omite y queda registrada con su número.
 */
public class CargadorPreguntas {

    private static final String SEPARADOR = ";";
    private static final String PREFIJO_COMENTARIO = "#";
    private static final char MARCA_UTF8 = '\uFEFF';   // algunos editores de Windows la agregan al inicio
    private static final int CAMPOS_ANTES_DE_OPCIONES = 3;   // dificultad, categoría y enunciado
    private static final int CAMPOS_DESPUES_DE_OPCIONES = 1; // letra de la respuesta correcta

    private ListaEnlazada<String> lineasInvalidas = new ListaEnlazada<>();

    /**
     * Carga el archivo en el banco. O(m log n), con m líneas y n preguntas en el árbol.
     *
     * @param rutaArchivo ruta del .txt
     * @param banco       banco donde se guardan las preguntas
     * @return cuántas preguntas se cargaron
     * @throws IOException si el archivo no existe, no se puede leer o no está en UTF-8
     */
    public int cargar(String rutaArchivo, BancoPreguntas banco) throws IOException {
        lineasInvalidas = new ListaEnlazada<>();
        int cargadas = 0;
        int numeroLinea = 0;

        try (BufferedReader lector = Files.newBufferedReader(Paths.get(rutaArchivo), StandardCharsets.UTF_8)) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                numeroLinea++;
                linea = limpiar(linea, numeroLinea);
                if (linea.isEmpty() || linea.startsWith(PREFIJO_COMENTARIO)) {
                    continue;
                }
                try {
                    banco.agregar(crearPregunta(linea, banco));
                    cargadas++;
                } catch (IllegalArgumentException e) {
                    lineasInvalidas.agregarAlFinal("Línea " + numeroLinea + ": " + e.getMessage());
                }
            }
        }
        return cargadas;
    }

    /** @return los mensajes de las líneas omitidas en la última carga (vacía si todo estuvo bien) */
    public ListaEnlazada<String> getLineasInvalidas() {
        return lineasInvalidas;
    }

    private String limpiar(String linea, int numeroLinea) {
        if (numeroLinea == 1 && !linea.isEmpty() && linea.charAt(0) == MARCA_UTF8) {
            linea = linea.substring(1);
        }
        return linea.trim();
    }

    /** Convierte una línea del archivo en una Pregunta; lanza IllegalArgumentException si es inválida. */
    private Pregunta crearPregunta(String linea, BancoPreguntas banco) {
        String[] campos = linea.split(SEPARADOR, -1);
        int cantidadOpciones = campos.length - CAMPOS_ANTES_DE_OPCIONES - CAMPOS_DESPUES_DE_OPCIONES;
        if (cantidadOpciones < 2) {
            throw new IllegalArgumentException("faltan campos (se esperan dificultad, categoría, "
                    + "enunciado, al menos 2 opciones y la letra correcta)");
        }

        int dificultad = leerDificultad(campos[0]);
        String[] opciones = new String[cantidadOpciones];
        for (int i = 0; i < cantidadOpciones; i++) {
            opciones[i] = campos[CAMPOS_ANTES_DE_OPCIONES + i];
        }
        int indiceCorrecto = leerIndiceCorrecto(campos[campos.length - 1], cantidadOpciones);
        int id = banco.preguntasDe(dificultad).tamano() + 1;   // siguiente id libre de ese nivel

        return new Pregunta(id, dificultad, campos[1], campos[2], opciones, indiceCorrecto);
    }

    private int leerDificultad(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("la dificultad debe ser un número: '" + texto.trim() + "'");
        }
    }

    /** Convierte la letra (A, B, C...) en la posición de la opción (0, 1, 2...). */
    private int leerIndiceCorrecto(String texto, int cantidadOpciones) {
        String letra = texto.trim().toUpperCase();
        if (letra.length() != 1) {
            throw new IllegalArgumentException("la respuesta correcta debe ser una sola letra: '" + texto.trim() + "'");
        }
        int indice = letra.charAt(0) - 'A';
        if (indice < 0 || indice >= cantidadOpciones) {
            throw new IllegalArgumentException("la letra '" + letra + "' no corresponde a ninguna opción");
        }
        return indice;
    }
}