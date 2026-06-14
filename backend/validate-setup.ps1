# Script de validacion - Verificar que todo esta listo
# Uso: .\validate-setup.ps1

Write-Host ""
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host "VALIDACION DE SETUP - Sistema Distribuido" -ForegroundColor Cyan
Write-Host "===================================================" -ForegroundColor Cyan
Write-Host ""

$allOk = $true

# 1. Verificar Java
Write-Host "[1/6] Verificando Java..." -ForegroundColor Yellow
try {
    $javaOutput = & java -version 2>&1
    $javaText = $javaOutput -join " "
    if ($javaText -match "(17|21|23|25)") {
        Write-Host "OK - Java encontrado (version compatible)" -ForegroundColor Green
    } else {
        Write-Host "ADVERTENCIA - Java encontrado pero version puede no ser 17+" -ForegroundColor Yellow
    }
} catch {
    Write-Host "ERROR - Java NO encontrado" -ForegroundColor Red
    $allOk = $false
}
Write-Host ""

# 2. Verificar Maven
Write-Host "[2/6] Verificando Maven..." -ForegroundColor Yellow
try {
    $mvnVersion = & mvn -version 2>&1 | Select-Object -First 1
    Write-Host "OK - Maven encontrado: $mvnVersion" -ForegroundColor Green
} catch {
    Write-Host "ERROR - Maven NO encontrado" -ForegroundColor Red
    $allOk = $false
}
Write-Host ""

# 3. Verificar pom.xml
Write-Host "[3/6] Verificando pom.xml..." -ForegroundColor Yellow
if (Test-Path ".\pom.xml") {
    Write-Host "OK - pom.xml encontrado" -ForegroundColor Green
} else {
    Write-Host "ERROR - pom.xml NO encontrado" -ForegroundColor Red
    $allOk = $false
}
Write-Host ""

# 4. Verificar estructura de carpetas
Write-Host "[4/6] Verificando estructura de carpetas..." -ForegroundColor Yellow
$folders = @(
    "src/main/java",
    "src/main/resources",
    "src/test/java"
)
$folderOk = $true
foreach ($folder in $folders) {
    if (Test-Path $folder) {
        Write-Host "   OK - $folder" -ForegroundColor Green
    } else {
        Write-Host "   ERROR - $folder NO encontrado" -ForegroundColor Red
        $folderOk = $false
        $allOk = $false
    }
}
Write-Host ""

# 5. Verificar scripts
Write-Host "[5/6] Verificando scripts de ejecucion..." -ForegroundColor Yellow
$scripts = @(
    "run-all-nodes.ps1",
    "run-node.ps1",
    "run-all-nodes.bat",
    "run-node.bat"
)
foreach ($script in $scripts) {
    if (Test-Path $script) {
        Write-Host "   OK - $script" -ForegroundColor Green
    } else {
        Write-Host "   ADVERTENCIA - $script NO encontrado" -ForegroundColor Yellow
    }
}
Write-Host ""

# 6. Intentar compilar
Write-Host "[6/6] Intentando compilacion rapida..." -ForegroundColor Yellow
Write-Host "   (Esto puede tardar la primera vez)" -ForegroundColor Gray
$compilationStart = Get-Date
try {
    $output = & mvn clean compile -q 2>&1
    $compilationEnd = Get-Date
    $duration = ($compilationEnd - $compilationStart).TotalSeconds
    if ($LASTEXITCODE -eq 0) {
        Write-Host "OK - Compilacion exitosa" -ForegroundColor Green
    } else {
        Write-Host "ERROR - Compilacion fallo" -ForegroundColor Red
        $allOk = $false
    }
} catch {
    Write-Host "ERROR - Compilacion fallo: $_" -ForegroundColor Red
    $allOk = $false
}
Write-Host ""

# Resumen
Write-Host "===================================================" -ForegroundColor Cyan
if ($allOk) {
    Write-Host "OK - TODO ESTA LISTO - Puedes ejecutar los nodos" -ForegroundColor Green
    Write-Host "===================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Proximos pasos:" -ForegroundColor Yellow
    Write-Host "   1. Ejecuta: .\run-all-nodes.ps1" -ForegroundColor Cyan
    Write-Host "   2. O ejecuta: .\run-node.ps1 -NodeId 1 -Port 8081" -ForegroundColor Cyan
    Write-Host "   3. Accede a http://localhost:8081" -ForegroundColor Cyan
    Write-Host ""
} else {
    Write-Host "ADVERTENCIA - ALGUNAS VALIDACIONES FALLARON" -ForegroundColor Red
    Write-Host "===================================================" -ForegroundColor Cyan
    Write-Host ""
}

Write-Host "Para mas informacion, ver HOW_TO_RUN.md" -ForegroundColor Cyan
Write-Host ""
