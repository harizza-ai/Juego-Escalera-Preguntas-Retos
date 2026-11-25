package escalera;

import escalera.estructuras.ArbolAVL;
import escalera.estructuras.Cola;
import escalera.estructuras.Grafo;
import escalera.estructuras.Iterador;
import escalera.estructuras.ListaEnlazada;
import escalera.estructuras.Pila;
import escalera.estructuras.TablaHash;
import escalera.modelo.Dado;
import escalera.modelo.Jugador;
import escalera.modelo.Pregunta;
import escalera.modelo.Tablero;
import escalera.modelo.TipoCasilla;
import escalera.servicio.BancoPreguntas;
import escalera.servicio.CargadorPreguntas;
import escalera.servicio.JuegoService;
import escalera.servicio.Ranking;
import escalera.vista.RenderizadorTablero;
import escalera.vista.Vista;

import java.io.IOException;
import java.util.Random;

/**
 * Pruebas sin dependencias externas (no requiere JUnit). Ejecutar con
 * "java -cp out escalera.PruebasManuales" desde la carpeta del proyecto.
 * Termina con código 1 si alguna verificación falla.
 */
public class PruebasManuales {

    private static int verificaciones;
    private static int fallos;

    public static void main(String[] args) throws IOException {
        probarLista();
        probarColaYPila();
        probarTablaHash();
        probarArbol();
        probarGrafo();
        probarTablero();
        probarPregunta();
        probarCargadorYBanco();
        probarRanking();
        probarRenderizado();
        probarPartidasCompletas();
        System.out.println("\nVerificaciones: " + verificaciones + ", fallos: " + fallos);
        System.exit(fallos == 0 ? 0 : 1);
    }

    // ------------------------------------------------------------------ estructuras

    private static void probarLista() {
        ListaEnlazada<Integer> lista = new ListaEnlazada<>();
        verificar(lista.estaVacia(), "lista nueva vacía");
        lista.agregarAlFinal(2);
        lista.agregarAlFinal(3);
        lista.agregarAlInicio(1);
        verificar(lista.tamano() == 3 && lista.obtener(0) == 1 && lista.obtener(2) == 3, "orden de lista");
        verificar(lanza(() -> lista.obtener(3)), "obtener fuera de rango lanza excepción");
        Iterador<Integer> it = lista.iterador();
        int suma = 0;
        while (it.tieneSiguiente()) {
            suma += it.siguiente();
        }
        verificar(suma == 6, "iterador recorre todo");
        lista.limpiar();
        verificar(lista.estaVacia(), "limpiar");
    }

    private static void probarColaYPila() {
        Cola<String> cola = new Cola<>();
        cola.encolar("a");
        cola.encolar("b");
        verificar(cola.verFrente().equals("a") && cola.desencolar().equals("a"), "cola FIFO");
        verificar(cola.desencolar().equals("b") && cola.estaVacia(), "cola se vacía");
        verificar(lanza(cola::desencolar), "desencolar vacía lanza excepción");
        cola.encolar("z");
        verificar(cola.verFrente().equals("z"), "cola reutilizable tras vaciarse");

        Pila<Integer> pila = new Pila<>();
        for (int i = 1; i <= 8; i++) {
            pila.apilar(i);
        }
        verificar(pila.verCima() == 8 && pila.tamano() == 8, "pila LIFO");
        ListaEnlazada<Integer> ultimos = pila.ultimos(5);
        verificar(ultimos.tamano() == 5 && ultimos.obtener(0) == 8 && ultimos.obtener(4) == 4, "últimos 5 de la pila");
        verificar(pila.tamano() == 8, "ultimos() no modifica la pila");
        verificar(pila.ultimos(50).tamano() == 8, "ultimos(n) con n mayor al tamaño");
        verificar(pila.desapilar() == 8 && pila.tamano() == 7, "desapilar");
        verificar(lanza(() -> new Pila<Integer>().desapilar()), "desapilar vacía lanza excepción");
    }

    private static void probarTablaHash() {
        TablaHash<String, Integer> tabla = new TablaHash<>();
        for (int i = 0; i < 1000; i++) {
            tabla.poner("clave" + i, i);
        }
        boolean todas = true;
        for (int i = 0; i < 1000; i++) {
            todas &= tabla.obtener("clave" + i) != null && tabla.obtener("clave" + i) == i;
        }
        verificar(tabla.tamano() == 1000 && todas, "1000 claves tras varios redimensionamientos");
        tabla.poner("clave5", 555);
        verificar(tabla.obtener("clave5") == 555 && tabla.tamano() == 1000, "poner reemplaza valor existente");
        verificar(tabla.eliminar("clave5") && !tabla.contiene("clave5") && tabla.tamano() == 999, "eliminar");
        verificar(!tabla.eliminar("noExiste"), "eliminar clave inexistente");
        verificar(tabla.obtener("noExiste") == null, "obtener inexistente = null");
        verificar(tabla.claves().tamano() == 999 && tabla.valores().tamano() == 999, "claves y valores");
    }

    private static void probarArbol() {
        ArbolAVL<Integer, String> arbol = new ArbolAVL<>();
        for (int i = 1; i <= 1000; i++) {           // claves ya ordenadas: peor caso de un BST simple
            arbol.insertar(i, "v" + i);
        }
        verificar(arbol.tamano() == 1000, "tamaño del árbol");
        verificar(arbol.altura() <= 14, "altura AVL logarítmica (altura " + arbol.altura() + ")");
        verificar("v500".equals(arbol.buscar(500)) && arbol.buscar(1001) == null, "búsqueda");
        ListaEnlazada<String> rango = arbol.buscarEnRango(10, 14);
        verificar(rango.tamano() == 5 && rango.obtener(0).equals("v10") && rango.obtener(4).equals("v14"), "rango");
        verificar(arbol.buscarEnRango(2000, 3000).estaVacia(), "rango vacío");
        verificar(arbol.enOrden().obtener(0).equals("v1"), "en orden ascendente");
        verificar(arbol.enOrdenDescendente().obtener(0).equals("v1000"), "en orden descendente");
        arbol.insertar(500, "nuevo");
        verificar(arbol.tamano() == 1000 && "nuevo".equals(arbol.buscar(500)), "clave repetida reemplaza valor");

        ArbolAVL<Integer, Integer> inverso = new ArbolAVL<>();
        for (int i = 500; i >= 1; i--) {
            inverso.insertar(i, i);
        }
        verificar(inverso.altura() <= 11, "altura AVL con claves en orden inverso");
    }

    private static void probarGrafo() {
        Grafo grafo = new Grafo(10);
        grafo.agregarArista(2, 7);
        verificar(grafo.destinoDesde(2) == 7 && grafo.destinoDesde(3) == Grafo.SIN_SALTO, "destino de arista");
        verificar(!grafo.tieneCiclo(), "sin ciclo");
        verificar(lanza(() -> grafo.agregarArista(2, 9)), "un nodo no admite dos saltos");
        verificar(lanza(() -> grafo.agregarArista(0, 3)) && lanza(() -> grafo.agregarArista(3, 11)), "nodos inválidos");
        grafo.agregarArista(7, 4);
        grafo.agregarArista(4, 2);
        verificar(grafo.tieneCiclo(), "detecta ciclo 2->7->4->2");
    }

    // ------------------------------------------------------------------ modelo

    private static void probarTablero() {
        Tablero tablero = Tablero.crearEstandar();
        verificar(tablero.getTamano() == 50 && tablero.getMeta() == 50, "tablero estándar de 50");
        verificar(tablero.obtenerCasilla(3).getTipo() == TipoCasilla.ESCALERA && tablero.resolverSalto(3) == 15, "escalera 3->15");
        verificar(tablero.obtenerCasilla(17).getTipo() == TipoCasilla.SERPIENTE && tablero.resolverSalto(17) == 4, "serpiente 17->4");
        verificar(tablero.obtenerCasilla(6).getDificultad() == 1 && tablero.obtenerCasilla(23).getDificultad() == 2
                && tablero.obtenerCasilla(46).getDificultad() == 3, "dificultad de retos por zona");
        verificar(tablero.resolverSalto(5) == 5, "casilla sin salto");
        verificar(lanza(() -> tablero.obtenerCasilla(51)) && lanza(() -> tablero.obtenerCasilla(0)), "casilla inexistente");

        int[][] sinSaltos = {};
        int[] sinRetos = {};
        verificar(lanza(() -> new Tablero(25, sinSaltos, sinSaltos, sinRetos)), "tamaño no múltiplo de 10");
        verificar(lanza(() -> new Tablero(20, new int[][]{{5, 3}}, sinSaltos, sinRetos)), "escalera que baja");
        verificar(lanza(() -> new Tablero(20, sinSaltos, new int[][]{{5, 8}}, sinRetos)), "serpiente que sube");
        verificar(lanza(() -> new Tablero(20, new int[][]{{5, 9}}, new int[][]{{9, 2}}, sinRetos)),
                "salto que termina en otro salto (cadena/ciclo)");
        verificar(lanza(() -> new Tablero(20, new int[][]{{5, 9}}, sinSaltos, new int[]{5})), "reto sobre escalera");
        verificar(!lanza(() -> new Tablero(20, new int[][]{{5, 20}}, sinSaltos, sinRetos)),
                "una escalera puede terminar en la meta");
        verificar(lanza(() -> new Tablero(20, new int[][]{{20, 25}}, sinSaltos, sinRetos)), "origen en la meta");
        verificar(lanza(() -> new Tablero(20, sinSaltos, sinSaltos, new int[]{1})), "reto en la salida");
    }

    private static void probarPregunta() {
        String[] opciones = {"uno", "dos", "tres", "cuatro"};
        Pregunta p = new Pregunta(7, 2, "Prueba", "¿Cuál?", opciones, 2);
        verificar(p.clave() == 2007, "clave = nivel*1000 + id");
        verificar(p.esCorrecta("c") && p.esCorrecta(" C ") && !p.esCorrecta("a"), "validación de respuesta");
        verificar(!Pregunta.esRespuestaValida("") && !Pregunta.esRespuestaValida("AB")
                && !Pregunta.esRespuestaValida("E") && !Pregunta.esRespuestaValida(null), "respuestas inválidas");
        verificar(lanza(() -> new Pregunta(0, 1, "x", "y", opciones, 0)), "id inválido");
        verificar(lanza(() -> new Pregunta(1, 1, "x", "y", new String[]{"a"}, 0)), "opciones inválidas");
        verificar(p.letraCorrecta() == 'C', "letra correcta");
    }

    private static void probarCargadorYBanco() throws IOException {
        BancoPreguntas banco = new BancoPreguntas(new Random(1));
        int cargadas = new CargadorPreguntas().cargar("preguntas.txt", banco);
        verificar(cargadas == 30 && banco.cantidadTotal() == 30, "se cargan 30 preguntas del archivo");
        verificar(banco.cantidadDeNivel(1) == 10 && banco.cantidadDeNivel(2) == 10 && banco.cantidadDeNivel(3) == 10,
                "10 preguntas por nivel");

        boolean ordenado = true;
        int anterior = -1;
        Iterador<Pregunta> it = banco.catalogoOrdenado().iterador();
        while (it.tieneSiguiente()) {
            int clave = it.siguiente().clave();
            ordenado &= clave > anterior;
            anterior = clave;
        }
        verificar(ordenado, "catálogo ordenado por clave");

        // Sin repetir hasta agotar el nivel
        TablaHash<Integer, Boolean> vistas = new TablaHash<>();
        boolean sinRepetidas = true;
        for (int i = 0; i < 10; i++) {
            Pregunta p = banco.obtenerAleatoria(2);
            sinRepetidas &= p.getDificultad() == 2 && !vistas.contiene(p.clave());
            vistas.poner(p.clave(), Boolean.TRUE);
        }
        verificar(sinRepetidas, "10 preguntas de nivel 2 sin repetir");
        verificar(banco.obtenerAleatoria(2) != null, "tras agotar el nivel se reinicia");
        verificar(banco.obtenerAleatoria(9) == null, "nivel sin preguntas = null");

        verificar(lanza(() -> new CargadorPreguntas().cargar("no_existe.txt", new BancoPreguntas(new Random()))),
                "archivo inexistente lanza IOException");
        java.io.File temporal = java.io.File.createTempFile("preguntas", ".txt");
        try (java.io.FileWriter w = new java.io.FileWriter(temporal)) {
            w.write("1;Cat;Pregunta incompleta;A;B\n");
        }
        verificar(lanza(() -> new CargadorPreguntas().cargar(temporal.getPath(), new BancoPreguntas(new Random()))),
                "línea con campos faltantes lanza excepción");
        temporal.delete();
    }

    private static void probarRanking() {
        Jugador a = new Jugador("Ana", 1);
        Jugador b = new Jugador("Beto", 2);
        Jugador c = new Jugador("Carla", 3);
        Jugador d = new Jugador("Dani", 4);
        b.registrarAcierto();
        b.registrarAcierto();
        a.registrarAcierto();
        c.registrarAcierto();
        c.registrarFallo();          // Carla: mismo acierto que Ana pero con un fallo
        ListaEnlazada<Jugador> lista = new ListaEnlazada<>();
        lista.agregarAlFinal(a);
        lista.agregarAlFinal(b);
        lista.agregarAlFinal(c);
        lista.agregarAlFinal(d);
        ListaEnlazada<Jugador> pos = new Ranking(lista).posiciones();
        verificar(pos.obtener(0) == b && pos.obtener(1) == a && pos.obtener(2) == c && pos.obtener(3) == d,
                "orden del ranking: aciertos, luego menos fallos");
    }

    private static void probarRenderizado() {
        Tablero tablero = Tablero.crearEstandar();
        Jugador a = new Jugador("Ana", 1);
        Jugador b = new Jugador("Beto", 2);
        a.moverA(50);
        b.moverA(0);
        ListaEnlazada<Jugador> lista = new ListaEnlazada<>();
        lista.agregarAlFinal(a);
        lista.agregarAlFinal(b);
        String dibujo = new RenderizadorTablero().dibujar(tablero, lista);
        String[] lineas = dibujo.split("\n");
        verificar(lineas.length == 1 + 5 * 4 + 1 + 1, "líneas del dibujo (" + lineas.length + ")");
        verificar(lineas[0].contains("2"), "ficha 2 en la salida");
        verificar(lineas[2].startsWith("|   41    |") && lineas[2].contains("|   50    |"), "fila superior: 41..50 de izq. a der.");
        verificar(lineas[4].contains("1"), "ficha 1 en la meta");
        verificar(lineas[6].startsWith("|   40    |") && lineas[6].contains("|   31    |"), "fila siguiente en zigzag: 40..31");
    }

    // ------------------------------------------------------------------ partidas

    /** Vista simulada: guarda todo lo mostrado y responde preguntas al azar. */
    private static class VistaSimulada implements Vista {
        private final ListaEnlazada<String> salida = new ListaEnlazada<>();
        private final Random azar;
        private final String letraFija;

        private VistaSimulada(Random azar, String letraFija) {
            this.azar = azar;
            this.letraFija = letraFija;
        }

        @Override
        public void mostrar(String mensaje) {
            salida.agregarAlFinal(mensaje);
        }

        @Override
        public String leer(String indicacion) {
            if (indicacion.contains("respuesta")) {
                return letraFija != null ? letraFija : String.valueOf("ABCD".charAt(azar.nextInt(4)));
            }
            return "";
        }

        @Override
        public void mostrarTablero(Tablero tablero, ListaEnlazada<Jugador> jugadores) {
            // Invariante: nadie puede estar fuera del tablero.
            Iterador<Jugador> it = jugadores.iterador();
            while (it.tieneSiguiente()) {
                Jugador j = it.siguiente();
                if (j.getPosicion() < 0 || j.getPosicion() > tablero.getMeta()) {
                    fallos++;
                    System.out.println("FALLO: " + j + " fuera del tablero");
                }
            }
        }
    }

    private static void probarPartidasCompletas() throws IOException {
        int partidas = 300;
        boolean todasTerminan = true;
        boolean ganadorEnMeta = true;
        boolean turnoExtraRespetado = true;
        boolean penalizacionesCoherentes = true;
        boolean historialCoherente = true;

        for (int semilla = 0; semilla < partidas; semilla++) {
            Random azar = new Random(semilla);
            VistaSimulada vista = new VistaSimulada(azar, null);
            BancoPreguntas banco = new BancoPreguntas(azar);
            new CargadorPreguntas().cargar("preguntas.txt", banco);
            JuegoService juego = new JuegoService(Tablero.crearEstandar(), banco, new Dado(azar), vista);
            int numeroJugadores = 2 + semilla % 3;
            for (int i = 1; i <= numeroJugadores; i++) {
                juego.registrarJugador("J" + i);
            }
            Jugador ganador = juego.jugar();
            todasTerminan &= ganador != null && juego.getGanador() == ganador;
            ganadorEnMeta &= ganador.getPosicion() == 50;

            // Después de "gana un turno extra", el mismo jugador debe volver a lanzar.
            String extraDe = null;
            int fallosRegistrados = 0;
            int turnosPerdidos = 0;
            Iterador<String> it = vista.salida.iterador();
            while (it.tieneSiguiente()) {
                String linea = it.siguiente();
                if (linea.contains("gana un turno extra")) {
                    extraDe = linea.trim().substring(linea.indexOf("¡Correcto!") >= 0 ? 11 : 0).split(" ")[0];
                } else if (linea.startsWith(">>") && linea.contains("lanza el dado")) {
                    if (extraDe != null) {
                        turnoExtraRespetado &= linea.startsWith(">> " + extraDe + " lanza");
                        extraDe = null;
                    }
                } else if (linea.contains("pierde este turno")) {
                    turnosPerdidos++;
                } else if (linea.contains("Incorrecto.")) {
                    fallosRegistrados++;
                }
            }
            int pendientes = fallosRegistrados - turnosPerdidos;
            penalizacionesCoherentes &= pendientes >= 0 && pendientes <= numeroJugadores;

            Jugador j1 = juego.obtenerJugador("j1");   // búsqueda por hash sin distinguir mayúsculas
            historialCoherente &= j1 != null && juego.ultimosMovimientos().tamano() <= 5
                    && juego.ultimosMovimientos().tamano() >= 1;
        }
        verificar(todasTerminan, partidas + " partidas terminan con ganador");
        verificar(ganadorEnMeta, "el ganador siempre queda exactamente en la meta");
        verificar(turnoExtraRespetado, "acertar un reto da turno extra al mismo jugador");
        verificar(penalizacionesCoherentes, "cada fallo se traduce en un turno perdido");
        verificar(historialCoherente, "historial de últimas jugadas y búsqueda por nombre");

        // Registro de jugadores
        BancoPreguntas banco = new BancoPreguntas(new Random(1));
        JuegoService juego = new JuegoService(Tablero.crearEstandar(), banco, new Dado(new Random(1)),
                new VistaSimulada(new Random(1), "A"));
        juego.registrarJugador("Ana");
        verificar(lanza(() -> juego.registrarJugador("ana")), "nombre repetido (sin distinguir mayúsculas)");
        verificar(lanza(() -> juego.registrarJugador("   ")), "nombre vacío");
        verificar(lanza(juego::jugar), "no se puede jugar con un solo jugador");
        juego.registrarJugador("Beto");
        juego.registrarJugador("Carla");
        juego.registrarJugador("Dani");
        verificar(lanza(() -> juego.registrarJugador("Eva")), "máximo 4 jugadores");

        // Partida sin preguntas cargadas: los retos se omiten y el juego igual termina.
        JuegoService sinPreguntas = new JuegoService(Tablero.crearEstandar(), new BancoPreguntas(new Random(2)),
                new Dado(new Random(2)), new VistaSimulada(new Random(2), "A"));
        sinPreguntas.registrarJugador("X");
        sinPreguntas.registrarJugador("Y");
        verificar(sinPreguntas.jugar() != null, "partida sin banco de preguntas termina");
    }

    // ------------------------------------------------------------------ utilidades

    private static void verificar(boolean condicion, String descripcion) {
        verificaciones++;
        if (!condicion) {
            fallos++;
            System.out.println("FALLO: " + descripcion);
        } else {
            System.out.println("ok   : " + descripcion);
        }
    }

    /** @return true si la acción lanza alguna excepción (comprobada o no) */
    private static boolean lanza(AccionConIO accion) {
        try {
            accion.ejecutar();
            return false;
        } catch (IOException | RuntimeException e) {
            return true;
        }
    }

    private interface AccionConIO {
        void ejecutar() throws IOException;
    }
}
