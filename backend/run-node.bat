@echo off
REM Script para ejecutar un nodo individual en Windows con argumentos personalizados
REM NOTA: Se recomienda usar run-node.ps1 (PowerShell) para mejor compatibilidad
REM Uso: run-node.bat <nodeId> <port> [nodesAll]
REM Ejemplo: run-node.bat 1 8081 "1:8081,2:8082,3:8083"

if "%1"=="" (
    echo Uso: run-node.bat ^<nodeId^> ^<port^> [nodesAll]
    echo.
    echo Ejemplos:
    echo   run-node.bat 1 8081
    echo   run-node.bat 2 8082 "1:8081,2:8082,3:8083"
    echo   run-node.bat 1 9001 "1:9001,2:9002"
    echo.
    pause
    exit /b 1
)

set NODE_ID=%1
set PORT=%2
set NODES_ALL=%3

if "%NODES_ALL%"=="" (
    set NODES_ALL=1:8081,2:8082,3:8083
)

echo.
echo ============================================
echo Ejecutando Nodo %NODE_ID%
echo ============================================
echo NODE_ID: %NODE_ID%
echo Puerto:  %PORT%
echo Nodos:   %NODES_ALL%
echo.

mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=%PORT% --node.id=%NODE_ID% --nodes.all=%NODES_ALL%"

pause
