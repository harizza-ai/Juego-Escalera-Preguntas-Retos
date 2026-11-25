package escalera.servicio;

import escalera.estructuras.Cola;
import escalera.estructuras.ListaEnlazada;
import escalera.estructuras.Pila;
import escalera.estructuras.Iterador;
import escalera.estructuras.TablaHash;
import escalera.modelo.Casilla;
import escalera.modelo.Dado;
import escalera.modelo.Jugador;
import escalera.modelo.Movimiento;
import escalera.modelo.Pregunta;
import escalera.modelo.Tablero;
import escalera.modelo.TipoCasilla;
import escalera.vista.Vista;

/**
 * Controla la partida: registra jugadores y ejecuta el bucle de turnos hasta que
 * alguien llegue EXACTO a la meta.
 *
 * Estructuras que usa:
 * - Cola de turnos: el jugador que juega pasa al final.
 * - Tabla hash nombre -> Jugador: buscar un jugador y evitar nombres repetidos.
 * - Pila de movimientos: historial de jugadas (se consultan las últimas 5).
 * - Tablero (lista de casillas + grafo de saltos) y BancoPreguntas (árbol).
 *
 * La cola, la tabla hash y la lista 'orden' comparten los MISMOS objetos Jugador,
 * por eso al actualizar un jugador el cambio se ve en todas las estructuras.
 */
public class JuegoService {

    public static final int MINIMO_JUGADORES = 2;
    public static final int MAXIMO_JUGADORES = 4;
    public static final int ULTIMAS_JUGADAS = 5;

    private static final String COMANDO_HISTORIAL = "h";
    private static final char PRIMERA_LETRA = 'A';

    private final Tablero tablero;
    private final BancoPreguntas banco;
    private final Dado dado;
    private final Vista vista;

    private final Cola<Jugador> turnos = new Cola<>();
    private final TablaHash<String, Jugador> jugadores = new TablaHash<>();
    private final ListaEnlazada<Jugador> orden = new ListaEnlazada<>();   // orden de inscripción
    private final Pila<Movimiento> historial = new Pila<>();

    private Jugador ganador;
    private boolean partidaIniciada;

    public JuegoService(Tablero tablero, BancoPreguntas banco, Dado dado, Vista vista) {
        this.tablero = tablero;
        this.banco = banco;
        this.dado = dado;
        this.vista = vista;
    }

    // ------------------------------------------------------------- preparación

    /**
     * Inscribe un jugador. Los nombres no se pueden repetir (sin distinguir mayúsculas).
     *
     * @throws IllegalArgumentException si el nombre está vacío o ya existe
     * @throws IllegalStateException    si la partida ya empezó o ya hay 4 jugadores
     */
    public void registrarJugador(String nombre) {
        if (partidaIniciada) {
            throw new IllegalStateException("La partida ya comenzó");
        }
        if (orden.tamano() >= MAXIMO_JUGADORES) {
            throw new IllegalStateException("Máximo " + MAXIMO_JUGADORES + " jugadores");
        }
        Jugador jugador = new Jugador(nombre, orden.tamano() + 1);   // valida que el nombre no esté vacío
        String clave = claveDe(jugador.getNombre());
        if (jugadores.contiene(clave)) {
            throw new IllegalArgumentException("Ya existe un jugador llamado " + jugador.getNombre());
        }
        jugadores.poner(clave, jugador);
        orden.agregarAlFinal(jugador);
        turnos.encolar(jugador);
    }

    /** @return el jugador con ese nombre (sin distinguir mayúsculas), o null si no existe. O(1) promedio. */
    public Jugador obtenerJugador(String nombre) {
        return jugadores.obtener(claveDe(nombre));
    }

    // ------------------------------------------------------------- partida

    /**
     * Juega hasta que alguien llegue exacto a la meta.
     * Cada vuelta: se desencola un jugador, juega su turno y se encola al final.
     *
     * @return el ganador
     * @throws IllegalStateException si hay menos de 2 jugadores o la partida ya terminó
     */
    public Jugador jugar() {
        if (orden.tamano() < MINIMO_JUGADORES) {
            throw new IllegalStateException("Se necesitan entre " + MINIMO_JUGADORES
                    + " y " + MAXIMO_JUGADORES + " jugadores");
        }
        if (ganador != null) {
            throw new IllegalStateException("La partida ya terminó");
        }
        partidaIniciada = true;
        vista.mostrar("\n=== ¡Comienza la partida! Gana quien llegue EXACTO a la casilla "
                + tablero.getMeta() + " ===");
        while (ganador == null) {
            Jugador actual = turnos.desencolar();
            ejecutarTurno(actual);
            turnos.encolar(actual);
        }
        vista.mostrarTablero(tablero, orden);
        vista.mostrar("\n*** ¡" + ganador.getNombre() + " llegó a la meta y GANA la partida! ***");
        return ganador;
    }

    public Jugador getGanador() {
        return ganador;
    }

    /** @return los jugadores en el orden en que se inscribieron */
    public ListaEnlazada<Jugador> getJugadores() {
        return orden;
    }

    /** @return la tabla de posiciones actual (foto de aciertos y fallos en este momento) */
    public Ranking ranking() {
        return new Ranking(orden);
    }

    /** Muestra en pantalla la tabla de posiciones, del primer al último puesto. */
    public void mostrarRanking() {
        vista.mostrar("--- Tabla de posiciones ---");
        Iterador<Jugador> it = ranking().posiciones().iterador();
        int puesto = 1;
        while (it.tieneSiguiente()) {
            Jugador jugador = it.siguiente();
            vista.mostrar(puesto + ". " + jugador.getNombre() + " - aciertos: "
                    + jugador.getAciertos() + ", fallos: " + jugador.getFallos());
            puesto++;
        }
    }

    /** @return las últimas 5 jugadas, de la más reciente a la más antigua */
    public ListaEnlazada<Movimiento> ultimosMovimientos() {
        return historial.ultimos(ULTIMAS_JUGADAS);
    }

    /** Muestra en pantalla las últimas 5 jugadas. */
    public void mostrarHistorial() {
        vista.mostrar("--- Últimas " + ULTIMAS_JUGADAS + " jugadas ---");
        ListaEnlazada<Movimiento> ultimas = ultimosMovimientos();
        if (ultimas.estaVacia()) {
            vista.mostrar("(todavía no hay jugadas)");
            return;
        }
        Iterador<Movimiento> it = ultimas.iterador();
        int numero = 1;
        while (it.tieneSiguiente()) {
            vista.mostrar(numero + ". " + it.siguiente());
            numero++;
        }
    }

    // ------------------------------------------------------------- turnos

    /** Un turno completo: se repite mientras el jugador acierte retos (turno extra). */
    private void ejecutarTurno(Jugador jugador) {
        if (jugador.consumirPenalizacion()) {
            vista.mostrar("\n>> " + jugador.getNombre() + " pierde este turno por haber fallado un reto.");
            return;
        }
        boolean turnoExtra;
        do {
            turnoExtra = lanzarYMover(jugador);
        } while (turnoExtra && ganador == null);
    }

    /**
     * Una tirada: lanza el dado, mueve al jugador y resuelve la casilla donde cae.
     *
     * @return true si el jugador gana un turno extra
     */
    private boolean lanzarYMover(Jugador jugador) {
        vista.mostrarTablero(tablero, orden);
        esperarLanzamiento(jugador);

        int resultado = dado.lanzar();
        int origen = jugador.getPosicion();
        int destino = origen + resultado;
        vista.mostrar(">> " + jugador.getNombre() + " lanza el dado y obtiene " + resultado + ".");

        // Regla de meta exacta: si se pasa de la meta, no se mueve.
        if (destino > tablero.getMeta()) {
            vista.mostrar("   Se pasaría de la meta (debe llegar exacto). No se mueve.");
            registrar(jugador, resultado, origen, origen, "no se mueve: meta exacta");
            return false;
        }
        jugador.moverA(destino);
        boolean turnoExtra = resolverCasilla(jugador, resultado, origen);
        if (jugador.getPosicion() == tablero.getMeta()) {
            ganador = jugador;
        }
        return turnoExtra;
    }

    /** Espera Enter para lanzar. Si escribe H, muestra el historial y vuelve a preguntar. */
    private void esperarLanzamiento(Jugador jugador) {
        String indicacion = "Turno de " + jugador.getNombre() + " (casilla " + jugador.getPosicion()
                + "). Enter = lanzar, H = historial: ";
        String entrada = vista.leer(indicacion);
        while (entrada.trim().equalsIgnoreCase(COMANDO_HISTORIAL)) {
            mostrarHistorial();
            entrada = vista.leer(indicacion);
        }
    }

    // ------------------------------------------------------------- casillas

    /** @return true si la casilla donde cayó el jugador le da un turno extra */
    private boolean resolverCasilla(Jugador jugador, int resultado, int origen) {
        Casilla casilla = tablero.obtenerCasilla(jugador.getPosicion());
        if (casilla.getTipo() == TipoCasilla.ESCALERA || casilla.getTipo() == TipoCasilla.SERPIENTE) {
            resolverSalto(jugador, resultado, origen, casilla);
            return false;
        }
        if (casilla.getTipo() == TipoCasilla.RETO) {
            return resolverReto(jugador, resultado, origen, casilla);
        }
        boolean esMeta = casilla.getNumero() == tablero.getMeta();
        vista.mostrar("   Avanza a la casilla " + casilla.getNumero() + (esMeta ? " (¡META!)." : "."));
        registrar(jugador, resultado, origen, casilla.getNumero(), esMeta ? "llega a la meta" : "");
        return false;
    }

    /** Sigue la arista del grafo (origen -> destino) para subir por la escalera o bajar por la serpiente. */
    private void resolverSalto(Jugador jugador, int resultado, int origen, Casilla casilla) {
        int destinoFinal = tablero.resolverSalto(casilla.getNumero());
        jugador.moverA(destinoFinal);
        String nombreSalto = casilla.getTipo() == TipoCasilla.ESCALERA ? "escalera" : "serpiente";
        vista.mostrar("   ¡Cae en una " + nombreSalto + "! Pasa de la casilla "
                + casilla.getNumero() + " a la " + destinoFinal + ".");
        registrar(jugador, resultado, origen, destinoFinal, nombreSalto + " desde " + casilla.getNumero());
    }

    /**
     * Hace una pregunta de la dificultad de la casilla.
     * Acierto = turno extra; fallo = pierde su próximo turno.
     *
     * @return true si el jugador gana un turno extra
     */
    private boolean resolverReto(Jugador jugador, int resultado, int origen, Casilla casilla) {
        int casillaReto = casilla.getNumero();
        vista.mostrar("   ¡Casilla de RETO! Debe responder una pregunta de nivel " + casilla.getDificultad() + ".");
        Pregunta pregunta = banco.obtenerAleatoria(casilla.getDificultad());
        if (pregunta == null) {
            vista.mostrar("   No hay preguntas de ese nivel: el reto se omite.");
            registrar(jugador, resultado, origen, casillaReto, "reto omitido");
            return false;
        }
        mostrarPregunta(pregunta);
        int elegida = pedirOpcion(pregunta.cantidadOpciones());

        if (pregunta.esCorrecta(elegida)) {
            jugador.registrarAcierto();
            vista.mostrar("   ¡Correcto! " + jugador.getNombre() + " gana un turno extra.");
            registrar(jugador, resultado, origen, casillaReto, "reto acertado: turno extra");
            return true;
        }
        jugador.registrarFallo();
        jugador.marcarPierdeTurno();
        vista.mostrar("   Incorrecto. La respuesta era: " + pregunta.getTextoCorrecto()
                + ". " + jugador.getNombre() + " pierde su próximo turno.");
        registrar(jugador, resultado, origen, casillaReto, "reto fallado: pierde turno");
        return false;
    }

    /** Muestra el enunciado y sus opciones etiquetadas A, B, C... */
    private void mostrarPregunta(Pregunta pregunta) {
        vista.mostrar("   " + pregunta);
        String[] opciones = pregunta.getOpciones();
        for (int i = 0; i < opciones.length; i++) {
            vista.mostrar("      " + letraDe(i) + ") " + opciones[i]);
        }
    }

    /** Pide una letra hasta que sea válida y devuelve la posición de la opción elegida (base 0). */
    private int pedirOpcion(int cantidadOpciones) {
        String indicacion = "   Tu respuesta (A-" + letraDe(cantidadOpciones - 1) + "): ";
        int indice = indiceDeLetra(vista.leer(indicacion), cantidadOpciones);
        while (indice < 0) {
            indice = indiceDeLetra(vista.leer("   Respuesta no válida. " + indicacion), cantidadOpciones);
        }
        return indice;
    }

    /** @return la posición de la letra (A = 0, B = 1...), o -1 si no es una letra de las opciones */
    private int indiceDeLetra(String texto, int cantidadOpciones) {
        String limpio = texto.trim();
        if (limpio.length() != 1) {
            return -1;
        }
        int indice = Character.toUpperCase(limpio.charAt(0)) - PRIMERA_LETRA;
        return indice >= 0 && indice < cantidadOpciones ? indice : -1;
    }

    private char letraDe(int indice) {
        return (char) (PRIMERA_LETRA + indice);
    }

    // ------------------------------------------------------------- historial

    private void registrar(Jugador jugador, int resultado, int desde, int hasta, String detalle) {
        historial.apilar(new Movimiento(jugador.getNombre(), resultado, desde, hasta, detalle));
    }

    private String claveDe(String nombre) {
        return nombre.trim().toLowerCase();
    }
}