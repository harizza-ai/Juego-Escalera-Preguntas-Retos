@echo off
REM Compila y ejecuta la interfaz grafica JavaFX del Juego Escalera (Windows)
setlocal
cd /d "%~dp0"

set JFX_LIB=%~dp0javafx-sdk\lib
if not exist "%JFX_LIB%\javafx.controls.jar" (
    echo No se encuentra javafx-sdk\lib
    echo Descarga OpenJFX 21 para Windows y colocalo en la carpeta javafx-sdk
    echo https://gluonhq.com/products/javafx/
    pause
    exit /b 1
)

if not exist out mkdir out

echo Compilando...
javac -encoding UTF-8 --module-path "%JFX_LIB%" --add-modules javafx.controls,javafx.graphics -d out src\main\java\escalera\estructuras\*.java src\main\java\escalera\modelo\*.java src\main\java\escalera\servicio\*.java src\main\java\escalera\vista\*.java src\main\java\escalera\vista\gui\*.java src\main\java\escalera\app\*.java
if errorlevel 1 (
    echo Error de compilacion.
    pause
    exit /b 1
)

copy /Y "src\main\resources\escalera\vista\gui\escalera.css" "out\escalera\vista\gui\escalera.css" >nul
if errorlevel 1 exit /b 1

echo Ejecutando interfaz grafica...
java -Dfile.encoding=UTF-8 --module-path "%JFX_LIB%" --add-modules javafx.controls,javafx.graphics -cp out escalera.app.AppJavaFX
pause
