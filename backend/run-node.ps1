# Script PowerShell para ejecutar un nodo individual con argumentos personalizados
# Uso: .\run-node.ps1 -NodeId 1 -Port 8081 [-NodesAll "1:8081,2:8082,3:8083"]
# Ejemplo: .\run-node.ps1 -NodeId 1 -Port 8081

param(
    [Parameter(Mandatory = $true, HelpMessage = "ID del nodo a ejecutar")]
    [int]$NodeId,
    
    [Parameter(Mandatory = $true, HelpMessage = "Puerto en el que ejecutar el nodo")]
    [int]$Port,
    
    [Parameter(Mandatory = $false, HelpMessage = "Configuracion de todos los nodos")]
    [string]$NodesAll = "1:8081,2:8082,3:8083"
)

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "Ejecutando Nodo $NodeId" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host "NODE_ID: $NodeId" -ForegroundColor Yellow
Write-Host "Puerto:  $Port" -ForegroundColor Yellow
Write-Host "Nodos:   $NodesAll" -ForegroundColor Yellow
Write-Host ""

$arguments = "--server.port=$Port --node.id=$NodeId --nodes.all=$NodesAll"

# Crear archivo .cmd temporal para ejecutar Maven de forma segura
$cmdFile = "$env:TEMP\node_single_$NodeId.cmd"
$cmdContent = @"
@echo off
cd /d "$PWD"
mvn spring-boot:run -Dspring-boot.run.arguments="$Arguments"
"@

Set-Content -Path $cmdFile -Value $cmdContent -Encoding ASCII

# Ejecutar el comando
cmd.exe /c $cmdFile

# Limpiar archivo temporal
Remove-Item -Path $cmdFile -Force -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "Nodo $NodeId finalizado" -ForegroundColor Yellow
