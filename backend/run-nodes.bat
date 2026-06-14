@echo off
setlocal enabledelayedexpansion

REM Script para ejecutar todos los nodos - VERSION SIMPLE Y CONFIABLE

if "%1"=="" (
    goto :all
)

if /i "%1"=="all" goto :all
if /i "%1"=="node1" goto :node1
if /i "%1"=="node2" goto :node2
if /i "%1"=="node3" goto :node3

echo Uso: run-nodes.bat [all^|node1^|node2^|node3]
exit /b 1

:all
cd /d "%~dp0"
echo Compilando...
call mvn clean compile -q
echo Compilacion completada
echo.
echo Iniciando 3 nodos...
timeout /t 2 /nobreak

REM Crear archivos batch temporales para cada nodo
set TMPDIR=%TEMP%\pry_distribuidos
if not exist "%TMPDIR%" mkdir "%TMPDIR%"

REM Nodo 1
(
  echo @echo off
  echo cd /d "%~dp0"
  echo mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081 --node.id=1 --nodes.all=1:8081,2:8082,3:8083"
  echo pause
) > "%TMPDIR%\node1.bat"

REM Nodo 2
(
  echo @echo off
  echo cd /d "%~dp0"
  echo mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082 --node.id=2 --nodes.all=1:8081,2:8082,3:8083"
  echo pause
) > "%TMPDIR%\node2.bat"

REM Nodo 3
(
  echo @echo off
  echo cd /d "%~dp0"
  echo mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083 --node.id=3 --nodes.all=1:8081,2:8082,3:8083"
  echo pause
) > "%TMPDIR%\node3.bat"

start "Nodo 1 - Puerto 8081" "%TMPDIR%\node1.bat"
timeout /t 1 /nobreak
start "Nodo 2 - Puerto 8082" "%TMPDIR%\node2.bat"
timeout /t 1 /nobreak
start "Nodo 3 - Puerto 8083" "%TMPDIR%\node3.bat"

echo.
echo URLs: http://localhost:8081, http://localhost:8082, http://localhost:8083
echo.
pause
exit /b 0

:node1
cd /d "%~dp0"
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081 --node.id=1 --nodes.all=1:8081,2:8082,3:8083"
pause
exit /b 0

:node2
cd /d "%~dp0"
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082 --node.id=2 --nodes.all=1:8081,2:8082,3:8083"
pause
exit /b 0

:node3
cd /d "%~dp0"
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083 --node.id=3 --nodes.all=1:8081,2:8082,3:8083"
pause
exit /b 0
