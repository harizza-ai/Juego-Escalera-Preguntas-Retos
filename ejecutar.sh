#!/bin/sh
# Compila y ejecuta el juego de consola (sin JavaFX).
set -e
mkdir -p out
# Excluye clases JavaFX
javac -encoding UTF-8 -d out \
      $(find src/main -name '*.java' ! -path '*/gui/*' ! -name 'AppJavaFX.java')
java -Dfile.encoding=UTF-8 -cp out escalera.app.Main "$@"
