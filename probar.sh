#!/bin/sh
# Compila todo (excepto la GUI JavaFX) y ejecuta las pruebas.
set -e
mkdir -p out
javac -encoding UTF-8 -d out \
      $(find src -name '*.java' ! -path '*/gui/*' ! -name 'AppJavaFX.java')
java -Dfile.encoding=UTF-8 -cp out escalera.PruebasManuales
