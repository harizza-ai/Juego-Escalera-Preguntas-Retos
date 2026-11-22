package escalera.app;

import escalera.estructuras.ArbolAVL;
import escalera.estructuras.Cola;
import escalera.estructuras.Iterador;
import escalera.estructuras.ListaEnlazada;
import escalera.estructuras.Pila;
import escalera.estructuras.TablaHash;
import escalera.modelo.Casilla;
import escalera.modelo.Dado;
import escalera.modelo.Jugador;
import escalera.modelo.Pregunta;
import escalera.modelo.Tablero;
import escalera.modelo.TipoCasilla;
import escalera.vista.Vista;

import java.util.Random;

/**
 * Fase 1: demostración de cada estructura por separado, con los ejemplos
 * pedidos en el enunciado (lista de 20 casillas, cola de 3 jugadores, hash de
 * puntajes, pila de resultados del dado y árbol con 10 preguntas).
 */
public class DemoFase1 {

    private final Vista vista;

    public DemoFase1(Vista vista) {
        this.vista = vista;
    }

    public void ejecutar() {
        demostrarLista();
        demostrarCola();
        demostrarTablaHash();
        demostrarPila();
        demostrarArbol();
    }

    /** Lista enlazada: 20 casillas con 3 escaleras y 2 serpientes insertadas en sus nodos. */
    private void demostrarLista() {
        vista.mostrar("\n--- 1) Lista enlazada: tablero de 20 casillas ---");
        int[][] escaleras = {{3, 12}, {6, 15}, {9, 18}};
        int[][] serpientes = {{14, 4}, {19, 8}};
        int[] retos = {5, 11, 16};
        Tablero tablero = new Tablero(20, escaleras, serpientes, retos);

        Iterador<Casilla> it = tablero.iteradorCasillas();
        while (it.tieneSiguiente()) {
            Casilla casilla = it.siguiente();
            if (casilla.getTipo() != TipoCasilla.NORMAL) {
                vista.mostrar("  Casilla " + casilla);
            }
        }
        vista.mostrarTablero(tablero, new ListaEnlazada<Jugador>());
    }

    /** Cola: 3 jugadores, se rota el turno 5 veces. */
    private void demostrarCola() {
        vista.mostrar("\n--- 2) Cola: rotación de turnos ---");
        Cola<Jugador> turnos = new Cola<>();
        turnos.encolar(new Jugador("Ana", 1));
        turnos.encolar(new Jugador("Beto", 2));
        turnos.encolar(new Jugador("Carla", 3));
        for (int turno = 1; turno <= 5; turno++) {
            Jugador actual = turnos.desencolar();
            vista.mostrar("  Turno " + turno + ": juega " + actual.getNombre());
            turnos.encolar(actual);
        }
    }

    /** Tabla hash: nombre -> jugador con su puntaje. */
    private void demostrarTablaHash() {
        vista.mostrar("\n--- 3) Tabla hash: estado de cada jugador ---");
        TablaHash<String, Jugador> tabla = new TablaHash<>();
        String[] nombres = {"Ana", "Beto", "Carla"};
        for (int i = 0; i < nombres.length; i++) {
            Jugador jugador = new Jugador(nombres[i], i + 1);
            for (int aciertos = 0; aciertos <= i; aciertos++) {
                jugador.registrarAcierto();
            }
            tabla.poner(nombres[i], jugador);
        }
        vista.mostrar("  Consulta directa de 'Beto': " + tabla.obtener("Beto"));
        vista.mostrar("  ¿Existe 'Zoe'? " + tabla.contiene("Zoe"));
        vista.mostrar("  Jugadores guardados: " + tabla.tamano());
    }

    /** Pila: se apila el resultado de cada tirada del dado. */
    private void demostrarPila() {
        vista.mostrar("\n--- 4) Pila: historial de resultados del dado ---");
        Pila<String> resultados = new Pila<>();
        Dado dado = new Dado(new Random());
        for (int tirada = 1; tirada <= 7; tirada++) {
            resultados.apilar("Tirada " + tirada + ": " + dado.lanzar());
        }
        vista.mostrar("  Tope de la pila (la más reciente): " + resultados.verCima());
        vista.mostrar("  Últimas 5 jugadas:");
        Iterador<String> it = resultados.ultimos(5).iterador();
        while (it.tieneSiguiente()) {
            vista.mostrar("    " + it.siguiente());
        }
    }

    /** Árbol: 10 preguntas con dificultad 1 a 3; clave = nivel * 1000 + id. */
    private void demostrarArbol() {
        vista.mostrar("\n--- 5) Árbol AVL: catálogo de preguntas por dificultad ---");
        ArbolAVL<Integer, Pregunta> arbol = new ArbolAVL<>();
        int[] dificultades = {2, 1, 3, 1, 2, 3, 1, 2, 3, 1};
        for (int i = 0; i < dificultades.length; i++) {
            Pregunta pregunta = crearPreguntaDeEjemplo(i + 1, dificultades[i]);
            arbol.insertar(pregunta.clave(), pregunta);
        }
        vista.mostrar("  Preguntas: " + arbol.tamano() + ", altura del árbol: " + arbol.altura());
        vista.mostrar("  En orden de clave (primero nivel 1, luego 2 y 3):");
        Iterador<Pregunta> it = arbol.enOrden().iterador();
        while (it.tieneSiguiente()) {
            Pregunta p = it.siguiente();
            vista.mostrar("    clave " + p.clave() + " -> " + p.getEnunciado());
        }
        Pregunta buscada = arbol.buscar(2 * Pregunta.FACTOR_DIFICULTAD + 5);
        vista.mostrar("  Búsqueda de la clave 2005: " + (buscada == null ? "no existe" : buscada.getEnunciado()));
    }

    private Pregunta crearPreguntaDeEjemplo(int id, int dificultad) {
        String[] opciones = {"Opción A", "Opción B", "Opción C", "Opción D"};
        return new Pregunta(id, dificultad, "Demo",
                "Pregunta de ejemplo #" + id + " (nivel " + dificultad + ")", opciones, 0);
    }
}
