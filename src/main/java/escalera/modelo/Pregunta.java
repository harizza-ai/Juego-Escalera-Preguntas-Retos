package escalera.modelo;

/**
 * Pregunta de opción múltiple que se hace al caer en una casilla de RETO.
 * Es inmutable. Su clave en el árbol es dificultad * 1000 + id, así las
 * preguntas de un mismo nivel quedan juntas y se pueden pedir por rango.
 */
public final class Pregunta {

    /** Separa los niveles dentro de la clave (permite hasta 999 ids por nivel). */
    public static final int FACTOR_DIFICULTAD = 1000;
    public static final int DIFICULTAD_MINIMA = 1;
    public static final int DIFICULTAD_MAXIMA = 3;
    private static final int ID_MAXIMO = FACTOR_DIFICULTAD - 1;
    private static final int OPCIONES_MINIMAS = 2;

    private final int id;
    private final int dificultad;
    private final String categoria;
    private final String enunciado;
    private final String[] opciones;
    private final int indiceCorrecta;

    /**
     * @param id             identificador dentro de su dificultad (1 a 999)
     * @param dificultad     nivel de 1 (fácil) a 3 (difícil)
     * @param categoria      tema de la pregunta
     * @param enunciado      texto de la pregunta
     * @param opciones       al menos 2 opciones de respuesta
     * @param indiceCorrecta posición (base 0) de la opción correcta
     * @throws IllegalArgumentException si algún dato es inválido
     */
    public Pregunta(int id, int dificultad, String categoria, String enunciado,
                    String[] opciones, int indiceCorrecta) {
        validarDatos(id, dificultad, categoria, enunciado, opciones, indiceCorrecta);
        this.id = id;
        this.dificultad = dificultad;
        this.categoria = categoria.trim();
        this.enunciado = enunciado.trim();
        this.opciones = opciones.clone();   // copia para que la pregunta no cambie desde afuera
        this.indiceCorrecta = indiceCorrecta;
    }

    /** @return la clave con la que se guarda en el ArbolAVL. */
    public int clave() {
        return claveMinima(dificultad) + id;
    }

    /** Clave más baja de un nivel: inicio de la consulta por rango. */
    public static int claveMinima(int dificultad) {
        return dificultad * FACTOR_DIFICULTAD;
    }

    /** Clave más alta de un nivel: fin de la consulta por rango. */
    public static int claveMaxima(int dificultad) {
        return claveMinima(dificultad) + ID_MAXIMO;
    }

    /** @return true si el índice elegido es la opción correcta. */
    public boolean esCorrecta(int indiceElegido) {
        return indiceElegido == indiceCorrecta;
    }

    public int getId() {
        return id;
    }

    public int getDificultad() {
        return dificultad;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public String[] getOpciones() {
        return opciones.clone();
    }

    public int cantidadOpciones() {
        return opciones.length;
    }

    public String getTextoCorrecto() {
        return opciones[indiceCorrecta];
    }

    @Override
    public String toString() {
        return "[Nivel " + dificultad + " | " + categoria + "] " + enunciado;
    }

    private static void validarDatos(int id, int dificultad, String categoria, String enunciado,
                                     String[] opciones, int indiceCorrecta) {
        if (id < 1 || id > ID_MAXIMO) {
            throw new IllegalArgumentException("El id debe estar entre 1 y " + ID_MAXIMO + ": " + id);
        }
        if (dificultad < DIFICULTAD_MINIMA || dificultad > DIFICULTAD_MAXIMA) {
            throw new IllegalArgumentException("La dificultad debe estar entre "
                    + DIFICULTAD_MINIMA + " y " + DIFICULTAD_MAXIMA + ": " + dificultad);
        }
        validarTexto(categoria, "La categoría");
        validarTexto(enunciado, "El enunciado");
        if (opciones == null || opciones.length < OPCIONES_MINIMAS) {
            throw new IllegalArgumentException("Se necesitan al menos " + OPCIONES_MINIMAS + " opciones");
        }
        for (String opcion : opciones) {
            validarTexto(opcion, "Cada opción");
        }
        if (indiceCorrecta < 0 || indiceCorrecta >= opciones.length) {
            throw new IllegalArgumentException("El índice de la respuesta correcta no existe: " + indiceCorrecta);
        }
    }

    private static void validarTexto(String texto, String descripcion) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException(descripcion + " no puede estar vacío");
        }
    }
}