# Proyecto 3 — Juego Escalera + Preguntas / Retos

Juego de tablero para 2 a 4 jugadores (casillas 1 a 50) con escaleras, serpientes y casillas de reto,
con versión de **consola** y versión con **interfaz gráfica (JavaFX)**. En un reto se responde una
pregunta de opción múltiple: **acierto = turno extra, fallo = se pierde el siguiente turno**.
Gana quien llega **exacto** a la meta (si el dado se pasa, no se mueve).

**Todas las estructuras de datos están implementadas desde cero** (paquete `escalera.estructuras`).
No se usa ninguna colección de `java.util` (`List`, `Stack`, `Queue`, `Hashtable`, `TreeMap`, etc.).
La consola solo toma de `java.util` la clase `Random` (el dado y el azar de las preguntas). La interfaz
gráfica usa además utilidades que no son estructuras de datos (`Optional`, `CompletableFuture`, `Pattern`)
y las clases de JavaFX.

## Cómo ejecutarlo

Requiere **JDK 21 o superior**. Ejecutar siempre **desde la carpeta del proyecto** (allí está `preguntas.txt`).

### Consola
- **Terminal (Linux / macOS):** `./ejecutar.sh`
- **Windows:** `ejecutar.bat`
- **Pruebas:** `./probar.sh`
- **NetBeans:** *File → Open Project* y elegir esta carpeta (tiene `pom.xml`); clase principal `escalera.app.Main`.

El menú ofrece: (1) partida completa, (2) demostración de la Fase 1, (3) salir.
En cada turno: **Enter** lanza el dado, **H** muestra el historial (últimas 5 jugadas).

### Interfaz gráfica (JavaFX)
- **Linux / macOS:** `./ejecutar-gui.sh`  **Windows:** `ejecutar-gui.bat`
- Clase principal: `escalera.app.AppJavaFX`.
- Necesita JavaFX 21 o superior. El script busca el SDK, en este orden: variable `JAVAFX_LIB`
  (ruta a la carpeta `lib`), carpeta `javafx-sdk/lib` dentro del proyecto (excluida de Git) o la
  instalación del sistema (en Arch: `sudo pacman -S java-openjfx`).

Qué ofrece la interfaz:
- **Tablero dibujado** en un `Canvas`, en zigzag igual que el tablero ASCII. Colores por tipo de casilla:
  verde = escalera, rojo = serpiente, amarillo = reto, dorado = meta. Las escaleras y serpientes se
  muestran como flechas y cada jugador es una ficha de color.
- **Panel de jugadores** (casilla, aciertos y fallos), **dado animado** y **registro** de lo que ocurre.
- **Botones:** *Lanzar dado* (también con Enter), *Historial* y A/B/C/D para responder los retos.
- **Diálogo inicial** para elegir de 2 a 4 jugadores y sus nombres; al terminar muestra el ranking
  y permite iniciar una nueva partida.
- **Arquitectura:** `JuegoService` (la lógica) corre en un hilo aparte y solo conoce la interfaz `Vista`;
  `VistaJavaFX` la implementa igual que `VistaConsola`, por lo que la lógica es la misma en ambas versiones.

## Qué estructura resuelve cada problema

| Estructura (clase propia) | Uso en el juego | Costo |
|---|---|---|
| `ListaEnlazada<Casilla>` | Tablero 1..N; cada nodo es una casilla (NORMAL, ESCALERA, SERPIENTE, RETO) | agregar O(1), buscar casilla O(n) |
| `Cola<Jugador>` | Turnos rotativos: se desencola, juega y se encola al final | O(1) |
| `Pila<Movimiento>` | Historial de jugadas; `ultimos(5)` | apilar O(1), `ultimos(k)` O(k) |
| `TablaHash<String, Jugador>` | Estado rápido de cada jugador por nombre; evita nombres repetidos | O(1) promedio |
| `ArbolAVL<Integer, Pregunta>` | Catálogo de preguntas, clave = `nivel*1000 + id`; búsqueda por rango de nivel (`enRango`) | O(log n) |
| `ArbolAVL<ClaveRanking, Jugador>` | Ranking por aciertos (Fase 3) | O(log n) |
| `Grafo` | Escaleras y serpientes como aristas dirigidas; detección de ciclos con DFS | O(V + E) |

Decisiones de diseño:

- **AVL en lugar de BST simple:** las preguntas del archivo llegan ordenadas por nivel; un BST normal
  degeneraría en una lista (O(n)). El AVL mantiene la altura en O(log n).
- **Clave compuesta `nivel*1000 + id`:** deja todas las preguntas de un nivel en un rango contiguo,
  así `enRango` descarta las ramas fuera del rango y devuelve solo ese nivel (O(log n + k)).
- **Validación del tablero:** una escalera debe subir, una serpiente bajar, todo salto termina en una casilla
  normal y el grafo no puede tener ciclos (`Grafo.tieneCiclo`).
- **Dificultad por zona:** los retos del primer tercio del tablero son de nivel 1, los del segundo de nivel 2
  y los del último de nivel 3.
- **Capas separadas (POO):** `JuegoService` (lógica) solo conoce la interfaz `Vista`; hay una `VistaConsola`
  y una `VistaJavaFX`, y en las pruebas se usa una vista simulada.
- **`Iterador<T>` propio** en lugar de `java.util.Iterator`, para no depender de `java.util`.

## Estructura del proyecto

```
src/main/java/escalera/
  estructuras/  ListaEnlazada, Cola, Pila, TablaHash, ArbolAVL, Grafo, Iterador
  modelo/       Casilla, TipoCasilla, Tablero, Jugador, Pregunta, Movimiento, Dado, ClaveRanking
  servicio/     JuegoService, BancoPreguntas, CargadorPreguntas, Ranking
  vista/        Vista (interfaz), VistaConsola, RenderizadorTablero
  vista/gui/    VistaJavaFX, TableroCanvas, DialogoJugadores
  app/          Main, DemoFase1, AppJavaFX
src/main/resources/escalera/vista/gui/escalera.css   (estilos de la interfaz gráfica)
src/test/java/escalera/PruebasManuales.java          (81 verificaciones + 300 partidas simuladas)
preguntas.txt                                         (30 preguntas, 10 por nivel)
```

## Fases del enunciado

- **Fase 1:** `DemoFase1` (opción 2 del menú): lista de 20 casillas con 3 escaleras y 2 serpientes, cola de
  3 jugadores rotando 5 turnos, hash con puntajes, pila con resultados del dado y árbol con 10 preguntas de nivel 1 a 3.
- **Fase 2:** `JuegoService` (meta exacta, retos con pregunta según la zona del tablero, grafo para los saltos,
  bucle hasta que haya ganador) y `RenderizadorTablero` (tablero ASCII con las fichas).
- **Fase 3:** `CargadorPreguntas` lee `preguntas.txt` y `Ranking` ordena por aciertos usando un árbol.
- **Punto extra:** interfaz gráfica con JavaFX (`AppJavaFX`).

## Formato de `preguntas.txt`

```
dificultad;categoria;enunciado;opcionA;opcionB;opcionC;opcionD;letraCorrecta
```
`dificultad` es 1, 2 o 3. Se ignoran las líneas vacías y las que empiezan con `#`. Se pueden agregar
preguntas libremente (hasta 999 por nivel); si una línea tiene un error, se omite y el programa indica su número de línea.

## Integrantes y responsabilidades


| Integrante | Clases |
|---|---|
| Harold | `Iterador`, `ListaEnlazada`, `Casilla`, `TipoCasilla`, `Tablero`, `Dado` |
| Maicol | `TablaHash`, `Grafo`, `Ranking`, `ClaveRanking`, `Vista`, `VistaConsola`, `DemoFase1` |
| Edwin | `Cola`, `Pila`, `Jugador`, `Movimiento`, `JuegoService`, `Main` |
| Fabian | `ArbolAVL`, `Pregunta`, `BancoPreguntas`, `CargadorPreguntas`, `RenderizadorTablero` |

`JuegoService` fue revisado entre los cuatro: es donde se conectan todas las estructuras.

## Uso de IA

Declaramos las herramientas de IA usadas, para qué se usaron y qué se verificó o modificó a mano.

| Herramienta | Para qué se usó | Prompt / resumen | Qué se modificó o verificó |
|---|---|---|---|
| Grok | Generación de la interfaz gráfica JavaFX (punto extra) | "Genera las clases JavaFX para el Juego Escalera con Canvas, diálogo de jugadores y botones A/B/C/D" | Se verificó que usa la interfaz Vista y no cambia la lógica de JuegoService |

## Video en Youtube del Proyecto

https://youtu.be/_ehG603Y7y4
