#!/bin/sh
# Compila y ejecuta la interfaz gráfica JavaFX del Juego Escalera.
# Usa JDK 25 y el SDK de JavaFX en la carpeta javafx-sdk/.
set -e

DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR"

JFX_LIB="$DIR/javafx-sdk/lib"
if [ ! -d "$JFX_LIB" ]; then
    echo "No se encuentra javafx-sdk/lib."
    echo "Descarga OpenJFX 21 y colócalo en javafx-sdk/ dentro de esta carpeta."
    exit 1
fi

mkdir -p out
echo "Compilando..."
javac -encoding UTF-8 \
      --module-path "$JFX_LIB" \
      --add-modules javafx.controls,javafx.graphics \
      -d out \
      $(find src/main -name '*.java')

cp "src/main/resources/escalera/vista/gui/escalera.css" "out/escalera/vista/gui/escalera.css"

echo "Ejecutando interfaz gráfica..."
java -Dfile.encoding=UTF-8 \
     --module-path "$JFX_LIB" \
     --add-modules javafx.controls,javafx.graphics \
     -cp out \
     escalera.app.AppJavaFX "$@"
