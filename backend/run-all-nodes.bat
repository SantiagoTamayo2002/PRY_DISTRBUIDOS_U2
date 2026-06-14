@echo off
REM Script para ejecutar todos los nodos en Windows
REM NOTA: Se recomienda usar run-all-nodes.ps1 (PowerShell) para mejor compatibilidad
REM Requiere Maven instalado y disponible en el PATH

echo.
echo ============================================
echo Iniciando Sistema Distribuido - Todos los Nodos
echo ============================================
echo.

echo [1/4] Compilando proyecto...
call mvn clean compile -q
if errorlevel 1 (
    echo Error durante la compilacion
    exit /b 1
)
echo [1/4] Compilacion completada
echo.

REM Ejecutar Nodo 1 en una nueva ventana
echo [2/4] Iniciando Nodo 1 en puerto 8081...
start "Nodo 1 - Puerto 8081" cmd /k mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081 --node.id=1 --nodes.all=1:8081,2:8082,3:8083"

REM Esperar un poco para que el primer nodo inicie
timeout /t 2 /nobreak

REM Ejecutar Nodo 2 en una nueva ventana
echo [3/4] Iniciando Nodo 2 en puerto 8082...
start "Nodo 2 - Puerto 8082" cmd /k mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082 --node.id=2 --nodes.all=1:8081,2:8082,3:8083"

REM Esperar un poco
timeout /t 2 /nobreak

REM Ejecutar Nodo 3 en una nueva ventana
echo [4/4] Iniciando Nodo 3 en puerto 8083...
start "Nodo 3 - Puerto 8083" cmd /k mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083 --node.id=3 --nodes.all=1:8081,2:8082,3:8083"

echo.
echo ============================================
echo Todos los nodos han sido iniciados
echo ============================================
echo.
echo URLs disponibles:
echo   Nodo 1: http://localhost:8081
echo   Nodo 2: http://localhost:8082
echo   Nodo 3: http://localhost:8083
echo.
echo Para detener un nodo, cierre su ventana o presione Ctrl+C
echo.
pause
