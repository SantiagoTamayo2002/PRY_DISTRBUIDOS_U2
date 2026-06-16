@echo off
REM Script para ejecutar un nodo individual en Windows con argumentos personalizados
REM NOTA: Se recomienda usar run-node.ps1 (PowerShell) para mejor compatibilidad
REM Uso: run-node.bat <nodeId> <port> [nodesAll]
REM 
REM Ejemplos para red local (localhost):
REM   run-node.bat 1 8081
REM   run-node.bat 2 8082 "1:localhost:8081,2:localhost:8082,3:localhost:8083"
REM
REM Ejemplos para red distribuida (equipos separados):
REM   run-node.bat 1 8081 "1:192.168.1.10:8081,2:192.168.1.20:8081,3:192.168.1.30:8081"
REM   run-node.bat 2 8082 "1:192.168.1.10:8081,2:192.168.1.20:8081,3:192.168.1.30:8081"

if "%1"=="" (
    echo Uso: run-node.bat ^<nodeId^> ^<port^> [nodesAll]
    echo.
    echo Ejemplos para LOCALHOST:
    echo   run-node.bat 1 8081
    echo   run-node.bat 2 8082
    echo   run-node.bat 3 8083
    echo.
    echo Ejemplos para RED DISTRIBUIDA ^(equipos separados^):
    echo   run-node.bat 1 8081 "1:192.168.1.10:8081,2:192.168.1.20:8081,3:192.168.1.30:8081"
    echo   run-node.bat 2 8082 "1:192.168.1.10:8081,2:192.168.1.20:8081,3:192.168.1.30:8081"
    echo.
    pause
    exit /b 1
)

set NODE_ID=%1
set PORT=%2
set NODES_ALL=%3

if "%NODES_ALL%"=="" (
    mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=%PORT% --node.id=%NODE_ID%"
) else (
    mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=%PORT% --node.id=%NODE_ID% --nodes.all=%NODES_ALL%"
)

pause
