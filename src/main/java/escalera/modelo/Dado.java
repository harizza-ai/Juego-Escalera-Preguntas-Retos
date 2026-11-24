package escalera.modelo;

import java.util.Random;

/** Dado de seis caras. Recibe el generador para poder fijar una semilla en las pruebas. */
public class Dado {

    private static final int CARAS = 6;

    private final Random azar;

    public Dado(Random azar) {
        this.azar = azar;
    }

    /** @return un valor entre 1 y 6 */
    public int lanzar() {
        return azar.nextInt(CARAS) + 1;
    }
}
