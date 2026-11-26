#!/bin/sh
# Compila y ejecuta la interfaz gráfica JavaFX del Juego Escalera.
# Busca JavaFX en $JAVAFX_LIB, en javafx-sdk/lib o en el sistema.
# — Script ajustado con Grok —
set -e

DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"

# 1) Buscar el SDK de JavaFX
if [ -n "$JAVAFX_LIB" ] && [ -d "$JAVAFX_LIB" ]; then
    JFX_LIB="$JAVAFX_LIB"
elif [ -d "$DIR/javafx-sdk/lib" ]; then
    JFX_LIB="$DIR/javafx-sdk/lib"
else
    # Intenta la ruta del sistema (Arch: pacman -S java-openjfx)
    JFX_SYS="/usr/lib/jvm/java-21-openjdk/lib"
    if [ -d "$JFX_SYS" ]; then
        JFX_LIB="$JFX_SYS"
    else
        echo "No se encuentra JavaFX."
        echo "Opciones:"
        echo "  1. export JAVAFX_LIB=/ruta/a/javafx-sdk/lib"
        echo "  2. Coloca OpenJFX 21 en javafx-sdk/ dentro de esta carpeta"
        echo "  3. Instala JavaFX del sistema (Arch: sudo pacman -S java-openjfx)"
        exit 1
    fi
fi

mkdir -p out
echo "Compilando..."
javac -encoding UTF-8 \
      --module-path "$JFX_LIB" \
      --add-modules javafx.controls,javafx.graphics \
      -d out \
      $(find src/main -name '*.java')

# Crea la carpeta destino del CSS antes de copiar (si no existe, el cp falla)
mkdir -p out/escalera/vista/gui
cp "src/main/resources/escalera/vista/gui/escalera.css" "out/escalera/vista/gui/escalera.css"

echo "Ejecutando interfaz gráfica..."
java -Dfile.encoding=UTF-8 \
     --module-path "$JFX_LIB" \
     --add-modules javafx.controls,javafx.graphics \
     -cp out \
     escalera.app.AppJavaFX "$@"
