@echo off
REM Compila y ejecuta el juego en consola (Windows)
setlocal
cd /d "%~dp0"

if not exist out mkdir out

echo Compilando...
javac -encoding UTF-8 -d out src\main\java\escalera\estructuras\*.java src\main\java\escalera\modelo\*.java src\main\java\escalera\servicio\*.java src\main\java\escalera\vista\*.java src\main\java\escalera\app\Main.java src\main\java\escalera\app\DemoFase1.java
if errorlevel 1 (
    echo Error de compilacion.
    pause
    exit /b 1
)

echo Ejecutando...
java -Dfile.encoding=UTF-8 -cp out escalera.app.Main
pause
