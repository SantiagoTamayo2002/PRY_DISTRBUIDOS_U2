# Script PowerShell para ejecutar todos los nodos en Windows
# Requiere Maven instalado y disponible en el PATH

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "Iniciando Sistema Distribuido - Todos los Nodos" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""

# Compilar el proyecto una sola vez
Write-Host "[1/4] Compilando proyecto..." -ForegroundColor Yellow
mvn clean compile -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "Error durante la compilacion" -ForegroundColor Red
    exit 1
}
Write-Host "[1/4] Compilacion completada" -ForegroundColor Green
Write-Host ""

# Definir argumentos para cada nodo
$argsNode1 = "--server.port=8081 --node.id=1 --nodes.all=1:8081,2:8082,3:8083"
$argsNode2 = "--server.port=8082 --node.id=2 --nodes.all=1:8081,2:8082,3:8083"
$argsNode3 = "--server.port=8083 --node.id=3 --nodes.all=1:8081,2:8082,3:8083"

# Funcion para ejecutar un nodo en una nueva ventana
function Start-Node {
    param(
        [int]$NodeId,
        [int]$Port,
        [string]$Args
    )
    
    $windowTitle = "Nodo $NodeId - Puerto $Port"
    
    Write-Host "Iniciando $windowTitle..." -ForegroundColor Cyan
    
    # Crear un archivo .cmd temporal para cada nodo
    $cmdFile = "$env:TEMP\node$NodeId.cmd"
    $cmdContent = @"
@echo off
cd /d "$PWD"
mvn spring-boot:run -Dspring-boot.run.arguments="$Args"
pause
"@
    
    Set-Content -Path $cmdFile -Value $cmdContent -Encoding ASCII
    
    # Ejecutar el archivo .cmd en una nueva ventana
    Start-Process cmd.exe -ArgumentList "/k `"$cmdFile`"" -WindowStyle Normal -ErrorAction SilentlyContinue
}

# Ejecutar Nodo 1
Write-Host "[2/4] Iniciando Nodo 1 en puerto 8081..." -ForegroundColor Yellow
Start-Node -NodeId 1 -Port 8081 -Args $argsNode1
Start-Sleep -Seconds 2

# Ejecutar Nodo 2
Write-Host "[3/4] Iniciando Nodo 2 en puerto 8082..." -ForegroundColor Yellow
Start-Node -NodeId 2 -Port 8082 -Args $argsNode2
Start-Sleep -Seconds 2

# Ejecutar Nodo 3
Write-Host "[4/4] Iniciando Nodo 3 en puerto 8083..." -ForegroundColor Yellow
Start-Node -NodeId 3 -Port 8083 -Args $argsNode3

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "Todos los nodos han sido iniciados" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host "URLs disponibles:" -ForegroundColor Cyan
Write-Host "  Nodo 1: http://localhost:8081" -ForegroundColor White
Write-Host "  Nodo 2: http://localhost:8082" -ForegroundColor White
Write-Host "  Nodo 3: http://localhost:8083" -ForegroundColor White
Write-Host ""
Write-Host "Para detener un nodo, cierre su ventana o presione Ctrl+C" -ForegroundColor Yellow
Write-Host ""
