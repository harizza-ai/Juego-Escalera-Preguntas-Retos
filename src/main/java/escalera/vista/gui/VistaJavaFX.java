package escalera.vista.gui;

import escalera.estructuras.Iterador;
import escalera.estructuras.ListaEnlazada;
import escalera.modelo.Jugador;
import escalera.modelo.Tablero;
import escalera.vista.Vista;

import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Implementación de Vista para JavaFX. Cumple el mismo contrato que VistaConsola:
 * JuegoService la usa sin saber si es consola o ventana.
 *
 * El método {@code leer} bloquea el hilo del juego (que NO es el hilo de JavaFX)
 * hasta que el usuario pulsa un botón o Enter. Se usa un CompletableFuture
 * para sincronizar ambos hilos sin polling.
 *
 * — Generado con Grok para el punto extra de GUI —
 */
public class VistaJavaFX implements Vista {

    private final BorderPane raiz;
    private final TableroCanvas canvasTablero;
    private final TextArea registro;
    private final VBox panelJugadores;
    private final Label labelDado;
    private final Button botonLanzar;
    private final Button botonHistorial;
    private final Button[] botonesOpcion;

    /** Future que el hilo del juego espera; la GUI lo completa al pulsar un botón. */
    private volatile CompletableFuture<String> entradaPendiente;

    public VistaJavaFX() {
        // — Tablero gráfico (centro) —
        canvasTablero = new TableroCanvas(560, 420);

        // — Panel derecho: jugadores + dado + botones —
        panelJugadores = new VBox(4);
        panelJugadores.setPadding(new Insets(8));
        panelJugadores.setStyle("-fx-border-color: #888; -fx-border-radius: 4;");

        labelDado = new Label("🎲 —");
        labelDado.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        botonLanzar = new Button("Lanzar dado");
        botonLanzar.setMaxWidth(Double.MAX_VALUE);
        botonLanzar.setOnAction(e -> responder(""));

        botonHistorial = new Button("Historial");
        botonHistorial.setMaxWidth(Double.MAX_VALUE);
        botonHistorial.setOnAction(e -> responder("H"));

        botonesOpcion = new Button[4];
        HBox filaOpciones = new HBox(4);
        filaOpciones.setAlignment(Pos.CENTER);
        for (int i = 0; i < 4; i++) {
            char letra = (char) ('A' + i);
            botonesOpcion[i] = new Button(String.valueOf(letra));
            botonesOpcion[i].setPrefWidth(50);
            final String respuesta = String.valueOf(letra);
            botonesOpcion[i].setOnAction(e -> responder(respuesta));
            filaOpciones.getChildren().add(botonesOpcion[i]);
        }

        VBox panelDerecho = new VBox(8,
                new Label("Jugadores:"), panelJugadores,
                labelDado, botonLanzar, botonHistorial,
                new Label("Responder reto:"), filaOpciones);
        panelDerecho.setPadding(new Insets(10));
        panelDerecho.setPrefWidth(220);
        panelDerecho.setAlignment(Pos.TOP_CENTER);

        // — Registro de eventos (abajo) —
        registro = new TextArea();
        registro.setEditable(false);
        registro.setWrapText(true);
        registro.setPrefRowCount(8);
        ScrollPane scrollRegistro = new ScrollPane(registro);
        scrollRegistro.setFitToWidth(true);
        VBox.setVgrow(scrollRegistro, Priority.ALWAYS);

        // — Layout principal —
        raiz = new BorderPane();
        raiz.setCenter(canvasTablero);
        raiz.setRight(panelDerecho);
        raiz.setBottom(registro);
        raiz.setPadding(new Insets(8));

        desactivarBotones();
    }

    /** @return el nodo raíz para montar en la Scene */
    public BorderPane getRaiz() {
        return raiz;
    }

    // ------------------------------------------------------------------ Vista

    @Override
    public void mostrar(String mensaje) {
        Platform.runLater(() -> {
            registro.appendText(mensaje + "\n");

            // Si el mensaje tiene el dado, actualizar la etiqueta
            if (mensaje.contains("lanza el dado y obtiene")) {
                try {
                    String valor = mensaje.replaceAll(".*obtiene (\\d+).*", "$1");
                    labelDado.setText("🎲 " + valor);
                } catch (Exception ignorada) {
                    // Si no se puede parsear, no pasa nada
                }
            }
        });
    }

    /**
     * Bloquea el hilo del juego hasta que el usuario pulse un botón.
     * El hilo de JavaFX nunca se bloquea; solo el hilo secundario espera.
     */
    @Override
    public String leer(String indicacion) {
        CompletableFuture<String> future = new CompletableFuture<>();
        entradaPendiente = future;

        Platform.runLater(() -> {
            registro.appendText(indicacion);
            activarBotones(indicacion);
        });

        try {
            String valor = future.get();    // bloquea el hilo del juego, NO el de JavaFX
            return valor;
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void mostrarTablero(Tablero tablero, ListaEnlazada<Jugador> jugadores) {
        Platform.runLater(() -> {
            canvasTablero.dibujar(tablero, jugadores);
            actualizarPanelJugadores(jugadores);
        });
    }

    // ------------------------------------------------------------------ botones

    /** Completa el future con la respuesta del usuario. */
    private void responder(String texto) {
        CompletableFuture<String> f = entradaPendiente;
        if (f != null && !f.isDone()) {
            desactivarBotones();
            f.complete(texto);
        }
    }

    /** Activa los botones relevantes según lo que espera el juego. */
    private void activarBotones(String indicacion) {
        boolean esReto = indicacion.contains("respuesta") || indicacion.contains("A-");
        botonLanzar.setDisable(esReto);
        botonHistorial.setDisable(esReto);
        for (Button b : botonesOpcion) {
            b.setDisable(!esReto);
        }
    }

    private void desactivarBotones() {
        botonLanzar.setDisable(true);
        botonHistorial.setDisable(true);
        for (Button b : botonesOpcion) {
            b.setDisable(true);
        }
    }

    /** Refresca el panel lateral con la info de cada jugador. */
    private void actualizarPanelJugadores(ListaEnlazada<Jugador> jugadores) {
        panelJugadores.getChildren().clear();
        Iterador<Jugador> it = jugadores.iterador();
        while (it.tieneSiguiente()) {
            Jugador j = it.siguiente();
            Label etiqueta = new Label(
                    "J" + j.getFicha() + " " + j.getNombre()
                    + " — casilla " + j.getPosicion()
                    + " (✓" + j.getAciertos() + " ✗" + j.getFallos() + ")");
            etiqueta.setStyle("-fx-font-size: 12px;");
            panelJugadores.getChildren().add(etiqueta);
        }
    }
}
