package escalera.servicio;

import escalera.estructuras.ArbolAVL;
import escalera.estructuras.ListaEnlazada;
import escalera.modelo.Pregunta;

import java.util.Random;

/**
 * Catálogo de preguntas guardado en un ArbolAVL. Como la clave es
 * dificultad * 1000 + id, las preguntas de un nivel quedan juntas y se piden
 * por rango en O(log n + k) sin recorrer todo el árbol.
 */
public class BancoPreguntas {

    private final ArbolAVL<Integer, Pregunta> catalogo = new ArbolAVL<>();
    private final Random azar;

    public BancoPreguntas(Random azar) {
        this.azar = azar;
    }

    /**
     * Agrega una pregunta al catálogo. O(log n).
     *
     * @throws IllegalArgumentException si es nula o ya existe el mismo nivel e id
     */
    public void agregar(Pregunta pregunta) {
        if (pregunta == null) {
            throw new IllegalArgumentException("La pregunta no puede ser nula");
        }
        if (catalogo.contiene(pregunta.clave())) {
            throw new IllegalArgumentException("Ya existe la pregunta " + pregunta.getId()
                    + " de dificultad " + pregunta.getDificultad());
        }
        catalogo.insertar(pregunta.clave(), pregunta);
    }

    /** @return una pregunta al azar de esa dificultad, o null si no hay ninguna */
    public Pregunta obtenerAleatoria(int dificultad) {
        ListaEnlazada<Pregunta> delNivel = preguntasDe(dificultad);
        if (delNivel.estaVacia()) {
            return null;
        }
        return delNivel.obtener(azar.nextInt(delNivel.tamano()));
    }

    /** @return todas las preguntas de una dificultad, ordenadas por id */
    public ListaEnlazada<Pregunta> preguntasDe(int dificultad) {
        return catalogo.enRango(Pregunta.claveMinima(dificultad), Pregunta.claveMaxima(dificultad));
    }

    /** @return la pregunta con ese nivel e id, o null si no existe */
    public Pregunta buscar(int dificultad, int id) {
        return catalogo.buscar(dificultad * Pregunta.FACTOR_DIFICULTAD + id);
    }

    public int cantidad() {
        return catalogo.tamano();
    }
}
