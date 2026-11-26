@echo off
REM Compila y ejecuta la interfaz grafica JavaFX del Juego Escalera (Windows)
REM Busca JavaFX en la variable JAVAFX_LIB o en javafx-sdk\lib
REM — Script ajustado con Grok —
setlocal
cd /d "%~dp0"

REM 1) Buscar el SDK de JavaFX
if defined JAVAFX_LIB (
    if exist "%JAVAFX_LIB%\javafx.controls.jar" (
        set JFX_LIB=%JAVAFX_LIB%
        goto :found
    )
)
set JFX_LIB=%~dp0javafx-sdk\lib
if exist "%JFX_LIB%\javafx.controls.jar" goto :found

echo No se encuentra javafx-sdk\lib
echo Descarga OpenJFX 21 para Windows y colocalo en la carpeta javafx-sdk
echo https://gluonhq.com/products/javafx/
echo O define la variable JAVAFX_LIB con la ruta a la carpeta lib
pause
exit /b 1

:found
if not exist out mkdir out

echo Compilando...
javac -encoding UTF-8 --module-path "%JFX_LIB%" --add-modules javafx.controls,javafx.graphics -d out src\main\java\escalera\estructuras\*.java src\main\java\escalera\modelo\*.java src\main\java\escalera\servicio\*.java src\main\java\escalera\vista\*.java src\main\java\escalera\vista\gui\*.java src\main\java\escalera\app\*.java
if errorlevel 1 (
    echo Error de compilacion.
    pause
    exit /b 1
)

REM Crea la carpeta destino del CSS antes de copiar
if not exist "out\escalera\vista\gui" mkdir "out\escalera\vista\gui"
copy /Y "src\main\resources\escalera\vista\gui\escalera.css" "out\escalera\vista\gui\escalera.css" >nul
if errorlevel 1 exit /b 1

echo Ejecutando interfaz grafica...
java -Dfile.encoding=UTF-8 --module-path "%JFX_LIB%" --add-modules javafx.controls,javafx.graphics -cp out escalera.app.AppJavaFX
pause
