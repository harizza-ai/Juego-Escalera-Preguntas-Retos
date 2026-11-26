package escalera.app;

import escalera.modelo.Dado;
import escalera.modelo.Tablero;
import escalera.servicio.BancoPreguntas;
import escalera.servicio.CargadorPreguntas;
import escalera.servicio.JuegoService;
import escalera.vista.gui.DialogoJugadores;
import escalera.vista.gui.VistaJavaFX;

import java.util.Random;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de la interfaz gráfica del Juego Escalera.
 *
 * Arranca JavaFX, pide los nombres de los jugadores con un diálogo,
 * y lanza la partida en un hilo aparte para no congelar la ventana.
 * La lógica es exactamente la misma que en consola: JuegoService solo
 * conoce la interfaz Vista, aquí implementada por VistaJavaFX.
 *
 * — Generado con Grok para el punto extra de GUI —
 */
public class AppJavaFX extends Application {

    private static final String TITULO = "Juego Escalera + Preguntas / Retos";
    private static final String ARCHIVO_PREGUNTAS = "preguntas.txt";
    private static final int ANCHO = 1024;
    private static final int ALTO = 720;

    @Override
    public void start(Stage escenario) {
        VistaJavaFX vista = new VistaJavaFX();
        Scene escena = new Scene(vista.getRaiz(), ANCHO, ALTO);

        // Carga el CSS si existe en el classpath
        java.net.URL css = getClass().getResource("/escalera/vista/gui/escalera.css");
        if (css != null) {
            escena.getStylesheets().add(css.toExternalForm());
        }

        escenario.setTitle(TITULO);
        escenario.setScene(escena);
        escenario.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        escenario.show();

        // Pide jugadores y arranca la partida en segundo plano
        iniciarPartida(vista);
    }

    /**
     * Muestra el diálogo de jugadores y, si confirman, lanza el juego
     * en un hilo aparte (para que la GUI no se congele mientras
     * JuegoService ejecuta su bucle de turnos).
     */
    private void iniciarPartida(VistaJavaFX vista) {
        DialogoJugadores dialogo = new DialogoJugadores();
        java.util.Optional<String[]> resultado = dialogo.showAndWait();

        if (!resultado.isPresent() || resultado.get().length < JuegoService.MINIMO_JUGADORES) {
            Platform.exit();
            return;
        }

        String[] nombres = resultado.get();
        Thread hiloJuego = new Thread(() -> {
            try {
                Random azar = new Random();
                BancoPreguntas banco = new BancoPreguntas(azar);
                cargarPreguntas(banco);

                JuegoService juego = new JuegoService(
                        Tablero.crearEstandar(), banco, new Dado(azar), vista);

                for (String nombre : nombres) {
                    juego.registrarJugador(nombre);
                }

                juego.jugar();
                juego.mostrarRanking();

                // Ofrece nueva partida
                Platform.runLater(() -> {
                    vista.mostrar("\n¿Otra partida? Cierra la ventana o reinicia.");
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                    vista.mostrar("Error: " + e.getMessage()));
            }
        }, "hilo-juego");
        hiloJuego.setDaemon(true);
        hiloJuego.start();
    }

    /** Carga preguntas del archivo; si falla usa las de ejemplo de Main. */
    private void cargarPreguntas(BancoPreguntas banco) {
        try {
            new CargadorPreguntas().cargar(ARCHIVO_PREGUNTAS, banco);
        } catch (java.io.IOException e) {
            // Sin archivo: el juego sigue con lo que haya (o sin preguntas)
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
