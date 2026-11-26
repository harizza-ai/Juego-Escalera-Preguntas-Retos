package escalera.vista.gui;

import escalera.servicio.JuegoService;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

/**
 * Diálogo modal de JavaFX para elegir de 2 a 4 jugadores y sus nombres.
 * Devuelve un arreglo de String con los nombres, o vacío si se cancela.
 *
 * — Generado con Grok para el punto extra de GUI —
 */
public class DialogoJugadores extends Dialog<String[]> {

    private static final int MAX_CAMPOS = JuegoService.MAXIMO_JUGADORES;

    private final Spinner<Integer> spinnerCantidad;
    private final TextField[] camposNombre;
    private final Label[] etiquetas;

    public DialogoJugadores() {
        setTitle("Juego Escalera — Jugadores");
        setHeaderText("¿Cuántos jugadores y cómo se llaman?");

        ButtonType botonJugar = new ButtonType("Jugar", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(botonJugar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(20));

        // Spinner para elegir cantidad
        spinnerCantidad = new Spinner<>();
        spinnerCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                JuegoService.MINIMO_JUGADORES, JuegoService.MAXIMO_JUGADORES,
                JuegoService.MINIMO_JUGADORES));
        spinnerCantidad.setPrefWidth(80);
        grid.add(new Label("Cantidad:"), 0, 0);
        grid.add(spinnerCantidad, 1, 0);

        // Campos de nombre
        camposNombre = new TextField[MAX_CAMPOS];
        etiquetas = new Label[MAX_CAMPOS];
        for (int i = 0; i < MAX_CAMPOS; i++) {
            etiquetas[i] = new Label("Jugador " + (i + 1) + ":");
            camposNombre[i] = new TextField("Jugador" + (i + 1));
            camposNombre[i].setPromptText("Nombre del jugador " + (i + 1));
            grid.add(etiquetas[i], 0, i + 1);
            grid.add(camposNombre[i], 1, i + 1);
        }

        actualizarVisibilidad(spinnerCantidad.getValue());
        spinnerCantidad.valueProperty().addListener(
                (obs, viejo, nuevo) -> actualizarVisibilidad(nuevo));

        getDialogPane().setContent(grid);

        // Convierte el resultado del diálogo en un arreglo de nombres
        setResultConverter(boton -> {
            if (boton == botonJugar) {
                int cantidad = spinnerCantidad.getValue();
                String[] nombres = new String[cantidad];
                for (int i = 0; i < cantidad; i++) {
                    String nombre = camposNombre[i].getText().trim();
                    nombres[i] = nombre.isEmpty() ? "Jugador" + (i + 1) : nombre;
                }
                return nombres;
            }
            return new String[0];
        });
    }

    /** Muestra u oculta los campos de nombre según la cantidad elegida. */
    private void actualizarVisibilidad(int cantidad) {
        for (int i = 0; i < MAX_CAMPOS; i++) {
            boolean visible = i < cantidad;
            etiquetas[i].setVisible(visible);
            etiquetas[i].setManaged(visible);
            camposNombre[i].setVisible(visible);
            camposNombre[i].setManaged(visible);
        }
        getDialogPane().getScene().getWindow().sizeToScene();
    }
}
