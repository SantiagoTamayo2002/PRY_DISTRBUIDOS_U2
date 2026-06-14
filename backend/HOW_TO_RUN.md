# 🚀 Guía de Uso - Ejecución de Nodos Distribuidos

## ⚡ Inicio Rápido

### Opción 1: PowerShell (Recomendado ✅)
**Ventajas:** Mejor compatibilidad, manejo superior de argumentos, colores en la consola

**Todos los nodos:**
```powershell
.\run-all-nodes.ps1
```

**Nodo específico:**
```powershell
.\run-node.ps1 -NodeId 1 -Port 8081
.\run-node.ps1 -NodeId 2 -Port 8082
.\run-node.ps1 -NodeId 3 -Port 8083
```

**Con configuración personalizada:**
```powershell
.\run-node.ps1 -NodeId 1 -Port 9001 -NodesAll "1:9001,2:9002,3:9003"
```

### Opción 2: CMD (Batch)
**Todos los nodos:**
```batch
run-all-nodes.bat
```

**Nodo específico:**
```batch
run-node.bat 1 8081
run-node.bat 2 8082
run-node.bat 3 8083
```

**Con configuración personalizada:**
```batch
run-node.bat 1 9001 "1:9001,2:9002,3:9003"
```

## 📋 Scripts Disponibles

| Script | Tipo | Descripción |
|--------|------|-------------|
| `run-all-nodes.ps1` | PowerShell | ⭐ Ejecuta todos los 3 nodos (Recomendado) |
| `run-all-nodes.bat` | Batch | Ejecuta todos los 3 nodos |
| `run-node.ps1` | PowerShell | ⭐ Ejecuta un nodo individual |
| `run-node.bat` | Batch | Ejecuta un nodo individual |

## 🔐 Habilitar Ejecución de Scripts PowerShell (Primera vez)

Si PowerShell bloquea los scripts, ejecuta en PowerShell como administrador:

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

Luego confirma escribiendo `Y` y presionando Enter.

## 🌐 Acceso a los Nodos

Después de iniciar los nodos, accede a ellos:

| Nodo | Puerto | URL |
|------|--------|-----|
| Nodo 1 | 8081 | http://localhost:8081 |
| Nodo 2 | 8082 | http://localhost:8082 |
| Nodo 3 | 8083 | http://localhost:8083 |

## 💡 Casos de Uso

### Caso 1: Ejecutar todos los nodos (Desarrollo)
```powershell
.\run-all-nodes.ps1
```
→ Se abrirán 3 ventanas de terminal, una para cada nodo

### Caso 2: Ejecutar un nodo específico (Testing)
```powershell
.\run-node.ps1 -NodeId 1 -Port 8081
```
→ Ejecuta en la ventana actual, útil para debugging

### Caso 3: Configuración personalizada (Producción simulada)
```powershell
.\run-node.ps1 -NodeId 1 -Port 9001 -NodesAll "1:9001,2:9002,3:9003"
```
→ Configura puertos y nodos personalizados

### Caso 4: Ejecutar en máquinas diferentes (Red)
En cada máquina, edita la IP en lugar de localhost:
```powershell
.\run-node.ps1 -NodeId 1 -Port 8081 -NodesAll "1:192.168.1.10:8081,2:192.168.1.11:8082,3:192.168.1.12:8083"
```

## 🛑 Detener un Nodo

- **PowerShell:** Presiona `Ctrl+C` en la ventana del nodo
- **CMD:** Presiona `Ctrl+C` en la ventana del nodo o cierra la ventana

## 🔧 Comandos Maven Equivalentes

Si prefieres ejecutar manualmente sin scripts:

```bash
# Nodo 1
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081 --node.id=1 --nodes.all=1:8081,2:8082,3:8083"

# Nodo 2
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082 --node.id=2 --nodes.all=1:8081,2:8082,3:8083"

# Nodo 3
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083 --node.id=3 --nodes.all=1:8081,2:8082,3:8083"
```

## ⚙️ Variables de Entorno

Alternativamente, establece variables de entorno y ejecuta:

**PowerShell:**
```powershell
$env:PORT=8081; $env:NODE_ID=1; mvn spring-boot:run
```

**CMD:**
```batch
set PORT=8081 & set NODE_ID=1 & mvn spring-boot:run
```

## 📖 Archivos de Configuración

- **pom.xml:** Configuración Maven y dependencias
- **application.properties:** Propiedades de Spring Boot
- **MAVEN_MIGRATION.md:** Documentación técnica completa
- **QUICK_START.md:** Referencia rápida

## 🐛 Troubleshooting

### Error: "No se puede cargar el archivo ... porque está deshabilitada la ejecución de scripts"
**Solución:** Ejecuta en PowerShell como administrador:
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### Error: "Maven no se encuentra"
**Solución:** Instala Maven o añádelo al PATH:
```powershell
mvn -version
```

### Puerto ya en uso
**Solución:** Busca y termina el proceso:
```powershell
# Encontrar proceso usando puerto 8081
netstat -ano | findstr :8081

# Terminar proceso (reemplaza PID)
taskkill /PID <PID> /F
```

### Los nodos no se comunican
**Solución:** Verifica que `nodes.all` tenga la configuración correcta en `application.properties` o en los argumentos

## 📝 Notas

- **Primera ejecución:** Maven descargará las dependencias (puede tardar)
- **Compilación automática:** Los scripts compilan el proyecto antes de ejecutar
- **Logs:** Ver la consola de cada nodo para ver logs en tiempo real
- **WebSocket:** Los nodos se comunican vía WebSocket para eventos en tiempo real
- **Sincronización:** Los nodos mantienen sincronización usando Lamport Clock y Ricart-Agrawala

## 🎯 Siguiente Paso

Una vez que los nodos estén ejecutándose:
1. Abre http://localhost:8081 en el navegador
2. Prueba las funcionalidades distribuidas
3. Observa la comunicación entre nodos en los logs

---

**Creado:** 2026-06-12  
**Estado:** ✅ Scripts funcionales  
**Recomendación:** Usar PowerShell para mejor experiencia
