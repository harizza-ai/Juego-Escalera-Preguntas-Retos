package escalera.app;

import escalera.estructuras.Iterador;
import escalera.modelo.Dado;
import escalera.modelo.Pregunta;
import escalera.modelo.Tablero;
import escalera.servicio.BancoPreguntas;
import escalera.servicio.CargadorPreguntas;
import escalera.servicio.JuegoService;
import escalera.vista.Vista;
import escalera.vista.VistaConsola;

import java.io.IOException;
import java.util.Random;

/** Punto de entrada: menú principal del Juego Escalera + Preguntas/Retos. */
public class Main {

    private static final String OPCION_JUGAR = "1";
    private static final String OPCION_DEMO = "2";
    private static final String OPCION_SALIR = "3";
    private static final String ARCHIVO_PREGUNTAS = "preguntas.txt";

    public static void main(String[] args) {
        Vista vista = new VistaConsola();
        try {
            mostrarMenu(vista);
        } catch (IllegalStateException e) {
            // Pasa, por ejemplo, si se cierra la entrada de datos en medio del juego.
            vista.mostrar("\nEl programa terminó: " + e.getMessage());
        }
    }

    private static void mostrarMenu(Vista vista) {
        String opcion = "";
        while (!opcion.equals(OPCION_SALIR)) {
            vista.mostrar("\n===== JUEGO ESCALERA + PREGUNTAS / RETOS =====");
            vista.mostrar(OPCION_JUGAR + ". Jugar partida");
            vista.mostrar(OPCION_DEMO + ". Demostración de las estructuras (Fase 1)");
            vista.mostrar(OPCION_SALIR + ". Salir");
            opcion = vista.leer("Elige una opción: ").trim();

            if (opcion.equals(OPCION_JUGAR)) {
                jugarPartida(vista);
            } else if (opcion.equals(OPCION_DEMO)) {
                new DemoFase1(vista).ejecutar();
            } else if (!opcion.equals(OPCION_SALIR)) {
                vista.mostrar("Opción no válida.");
            }
        }
        vista.mostrar("¡Hasta pronto!");
    }

    /** Prepara una partida (preguntas, jugadores), la juega y muestra el ranking. */
    private static void jugarPartida(Vista vista) {
        Random azar = new Random();
        BancoPreguntas banco = new BancoPreguntas(azar);
        cargarPreguntas(vista, banco);

        JuegoService juego = new JuegoService(Tablero.crearEstandar(), banco, new Dado(azar), vista);
        registrarJugadores(vista, juego);

        juego.jugar();
        mostrarResumen(vista, juego);
    }

    /** Pide cuántos jugadores hay y el nombre de cada uno, repitiendo si el nombre no es válido. */
    private static void registrarJugadores(Vista vista, JuegoService juego) {
        int cantidad = pedirCantidadJugadores(vista);
        for (int numero = 1; numero <= cantidad; numero++) {
            boolean registrado = false;
            while (!registrado) {
                try {
                    juego.registrarJugador(vista.leer("Nombre del jugador " + numero + ": "));
                    registrado = true;
                } catch (IllegalArgumentException e) {
                    vista.mostrar("  " + e.getMessage() + ". Intenta de nuevo.");
                }
            }
        }
    }

    private static int pedirCantidadJugadores(Vista vista) {
        String indicacion = "¿Cuántos jugadores (" + JuegoService.MINIMO_JUGADORES
                + " a " + JuegoService.MAXIMO_JUGADORES + ")? ";
        while (true) {
            try {
                int cantidad = Integer.parseInt(vista.leer(indicacion).trim());
                if (cantidad >= JuegoService.MINIMO_JUGADORES && cantidad <= JuegoService.MAXIMO_JUGADORES) {
                    return cantidad;
                }
            } catch (NumberFormatException e) {
                // No era un número: se vuelve a preguntar abajo.
            }
            vista.mostrar("  Escribe un número entre " + JuegoService.MINIMO_JUGADORES
                    + " y " + JuegoService.MAXIMO_JUGADORES + ".");
        }
    }

    /** Tabla de posiciones (ranking) y las últimas jugadas. */
    private static void mostrarResumen(Vista vista, JuegoService juego) {
        vista.mostrar("\n--- Fin de la partida ---");
        juego.mostrarRanking();
        juego.mostrarHistorial();
    }

    /**
     * Carga las preguntas desde preguntas.txt. Si el archivo no se puede leer o no
     * tiene ninguna pregunta válida, usa las de ejemplo para que el juego siga funcionando.
     */
    private static void cargarPreguntas(Vista vista, BancoPreguntas banco) {
        CargadorPreguntas cargador = new CargadorPreguntas();
        try {
            int cargadas = cargador.cargar(ARCHIVO_PREGUNTAS, banco);
            vista.mostrar("Preguntas cargadas desde " + ARCHIVO_PREGUNTAS + ": " + cargadas);
            mostrarLineasInvalidas(vista, cargador);
        } catch (IOException e) {
            vista.mostrar("No se pudo leer " + ARCHIVO_PREGUNTAS
                    + ". Revisa que exista junto al programa y esté guardado en UTF-8.");
        }
        if (banco.cantidad() == 0) {
            vista.mostrar("Se usarán las preguntas de ejemplo.");
            cargarPreguntasDeEjemplo(banco);
        }
    }

    private static void mostrarLineasInvalidas(Vista vista, CargadorPreguntas cargador) {
        Iterador<String> it = cargador.getLineasInvalidas().iterador();
        while (it.tieneSiguiente()) {
            vista.mostrar("  Se omitió -> " + it.siguiente());
        }
    }

    /**
     * Preguntas de ejemplo (3 por nivel), solo como respaldo si preguntas.txt no se puede leer.
     */
    private static void cargarPreguntasDeEjemplo(BancoPreguntas banco) {
        banco.agregar(crearPregunta(1, 1, "Cultura", "¿Cuál es la capital de Colombia?",
                "Medellín", "Bogotá", "Cali", "Popayán", 1));
        banco.agregar(crearPregunta(2, 1, "Matemáticas", "¿Cuánto es 7 x 8?",
                "54", "56", "63", "48", 1));
        banco.agregar(crearPregunta(3, 1, "Ciencia", "¿Qué planeta es conocido como el planeta rojo?",
                "Venus", "Marte", "Júpiter", "Mercurio", 1));

        banco.agregar(crearPregunta(1, 2, "Estructuras de datos", "¿Qué estructura sigue la regla LIFO?",
                "Cola", "Pila", "Árbol", "Grafo", 1));
        banco.agregar(crearPregunta(2, 2, "Programación", "¿Qué palabra clave de Java crea un objeto?",
                "this", "new", "static", "void", 1));
        banco.agregar(crearPregunta(3, 2, "Matemáticas", "¿Cuánto es 2 elevado a la 10?",
                "512", "1000", "1024", "2048", 2));

        banco.agregar(crearPregunta(1, 3, "Estructuras de datos", "¿Qué recorrido de un árbol binario de búsqueda da las claves de menor a mayor?",
                "Preorden", "Inorden", "Postorden", "Por niveles", 1));
        banco.agregar(crearPregunta(2, 3, "Grafos", "¿Qué recorrido de un grafo visita primero a todos los vecinos de un nodo?",
                "DFS", "BFS", "Búsqueda binaria", "Quicksort", 1));
        banco.agregar(crearPregunta(3, 3, "Clean Code", "¿Qué principio dice que una clase debe tener una única razón para cambiar?",
                "Abierto/Cerrado", "Responsabilidad única", "Liskov", "Inversión de dependencias", 1));
    }

    private static Pregunta crearPregunta(int id, int dificultad, String categoria, String enunciado,
                                          String a, String b, String c, String d, int correcta) {
        return new Pregunta(id, dificultad, categoria, enunciado, new String[]{a, b, c, d}, correcta);
    }
}