# Sistema Distribuido - Unidad 2

Sistema de 3 nodos distribuidos que implementa algoritmos de sincronización:
- **Bully Algorithm** - Elección de líder
- **Ricart-Agrawala** - Exclusión mutua
- **Lamport Clock** - Ordenamiento de eventos

---

## 🚀 Ejecución en LOCALHOST (Misma Máquina)

### Requisitos
- Java 17+
- Maven 3.9.15+
- Windows/Linux/macOS

### Ejecución Rápida

**Windows (CMD/PowerShell):**
```batch
cd backend
.\run-nodes.bat all
```

**Linux/macOS:**
```bash
cd backend
./run-nodes.sh all
```

### Acceso
- Nodo 1: http://localhost:8081
- Nodo 2: http://localhost:8082
- Nodo 3: http://localhost:8083

---

## 🌐 Ejecución en MÁQUINAS DISTINTAS (Red)

### Configuración
- **Nodo 1:** 192.168.2.2 (Puerto 8081)
- **Nodo 2:** 192.168.2.3 (Puerto 8081)
- **Nodo 3:** 192.168.2.4 (Puerto 8081)

### Requisitos en Cada Máquina
- ✅ Java 17+
- ✅ Maven 3.9.15+
- ✅ Proyecto clonado en la misma ubicación (`backend/`)
- ✅ Acceso de red entre máquinas (ping debe funcionar)

---

## 📋 INSTRUCCIONES PASO A PASO

### PASO 1: Preparar la Configuración

En **CADA MÁQUINA**, edita el archivo:
```
backend/src/main/resources/application.properties
```

Busca la línea:
```properties
nodes.all=1:localhost:8081,2:localhost:8082,3:localhost:8083
```

**Reemplázala con:**
```properties
nodes.all=1:192.168.2.2:8081,2:192.168.2.3:8081,3:192.168.2.4:8081
```

### PASO 2: Compilar el Proyecto

En **CADA MÁQUINA**, abre terminal en la carpeta `backend/`:

**Windows:**
```batch
mvn clean compile -q
```

**Linux/macOS:**
```bash
mvn clean compile -q
```

Espera a que termine la compilación (2-3 minutos en primera ejecución).

### PASO 3: Ejecutar Nodo 1 (192.168.2.2)

En la **MÁQUINA 1 (192.168.2.2)**:

**Windows:**
```batch
.\run-nodes.bat node1
```

**Linux/macOS:**
```bash
./run-nodes.sh node1
```

Verás el startup de Spring Boot. Espera a que aparezca:
```
Tomcat started on port(s): 8081
```

### PASO 4: Ejecutar Nodo 2 (192.168.2.3)

En la **MÁQUINA 2 (192.168.2.3)**:

**Windows:**
```batch
.\run-nodes.bat node2
```

**Linux/macOS:**
```bash
./run-nodes.sh node2
```

Verás el startup de Spring Boot. Espera a que aparezca:
```
Tomcat started on port(s): 8081
```

### PASO 5: Ejecutar Nodo 3 (192.168.2.4)

En la **MÁQUINA 3 (192.168.2.4)**:

**Windows:**
```batch
.\run-nodes.bat node3
```

**Linux/macOS:**
```bash
./run-nodes.sh node3
```

Verás el startup de Spring Boot. Espera a que aparezca:
```
Tomcat started on port(s): 8081
```

---

## ✅ VERIFICACIÓN

### Verificar Conectividad Entre Nodos

Desde **CADA MÁQUINA**, abre navegador:

| Máquina | URL | Esperado |
|---------|-----|----------|
| 192.168.2.2 | http://192.168.2.2:8081 | ✅ Conecta |
| 192.168.2.2 | http://192.168.2.3:8081 | ✅ Conecta |
| 192.168.2.2 | http://192.168.2.4:8081 | ✅ Conecta |

Si **NO** conecta, verifica:
1. ✅ Firewall permite puerto 8081
2. ✅ Las máquinas pueden hacer ping: `ping 192.168.2.3`
3. ✅ Los nodos están ejecutándose

### Ver Logs

En cada ventana de terminal donde ejecutaste un nodo, verás los logs de Spring Boot y de los algoritmos distribuidos.

Busca mensajes como:
- `WebSocket connection established`
- `Bully election started`
- `Ricart-Agrawala lock acquired`
- `Lamport clock synchronized`

---

## 🔧 CONFIGURACIÓN AVANZADA

### Cambiar Puertos (Si 8081 No Está Disponible)

En `application.properties`:

```properties
# Para Nodo 1 (máquina 192.168.2.2)
server.port=8081
node.id=1

# Para Nodo 2 (máquina 192.168.2.3)
server.port=8082
node.id=2

# Para Nodo 3 (máquina 192.168.2.4)
server.port=8083
node.id=3
```

Luego actualiza `nodes.all`:
```properties
nodes.all=1:192.168.2.2:8081,2:192.168.2.3:8082,3:192.168.2.4:8083
```

### Agregar Más Nodos

1. Agrega entrada en `nodes.all`:
```properties
nodes.all=1:192.168.2.2:8081,2:192.168.2.3:8081,3:192.168.2.4:8081,4:192.168.2.5:8081
```

2. En `application.properties` de la nueva máquina:
```properties
node.id=4
```

---

## 🐛 TROUBLESHOOTING

### Problema: "Connection refused" al acceder a otro nodo

**Causa:** Firewall bloqueando puerto 8081

**Solución:**
```batch
# Windows - Permitir puerto en firewall
netsh advfirewall firewall add rule name="Allow 8081" dir=in action=allow protocol=tcp localport=8081

# Linux
sudo ufw allow 8081
```

### Problema: Maven no encuentra mvn

**Solución:** Agregar Maven al PATH
```batch
# Windows
setx PATH "%PATH%;C:\Program Files\Apache\maven\bin"

# Linux/macOS
export PATH=$PATH:/usr/local/maven/bin
```

### Problema: "Port 8081 is already in use"

**Solución:** Cambiar puerto en `application.properties`:
```properties
server.port=9001
```

### Problema: Los nodos no se comunican

**Checklist:**
1. ✅ `nodes.all` tiene IPs correctas
2. ✅ `node.id` correcto en cada máquina (1, 2, 3)
3. ✅ Los 3 nodos están ejecutándose
4. ✅ Firewall permite comunicación
5. ✅ Red tiene conectividad: `ping 192.168.2.3`

---

## 📁 Estructura del Proyecto

```
PRY_DISTRBUIDOS_U2/
├── backend/
│   ├── pom.xml                          ← Configuración Maven
│   ├── src/main/resources/
│   │   ├── application.properties       ← Editar aquí IPs
│   │   └── ...
│   ├── src/main/java/com/example/backend/
│   │   ├── controller/
│   │   ├── service/                     ← Algoritmos distribuidos
│   │   ├── dto/
│   │   └── model/
│   ├── run-nodes.bat                    ← Windows
│   └── run-nodes.sh                     ← Linux/macOS
└── frontend/
    ├── package.json
    └── ...
```

---

## 📝 Notas Importantes

⚠️ **Importante:**
- Edita `application.properties` **EN CADA MÁQUINA** con sus IPs locales
- El `node.id` debe ser único: 1, 2, 3 (una máquina por ID)
- Asegúrate de que las máquinas estén en la misma red
- El firewall debe permitir puerto 8081 en ambas máquinas
- Inicia los 3 nodos **con pocos segundos de diferencia** para que se sincronicen

---

## 🎯 Resumen Rápido

| Paso | Acción | Máquina |
|------|--------|---------|
| 1 | Editar `application.properties` | 192.168.2.2, .3, .4 |
| 2 | `mvn clean compile -q` | 192.168.2.2, .3, .4 |
| 3 | `run-nodes.bat node1` | 192.168.2.2 |
| 4 | `run-nodes.bat node2` | 192.168.2.3 |
| 5 | `run-nodes.bat node3` | 192.168.2.4 |
| 6 | Acceder a http://192.168.2.2:8081 | Tu máquina |

---

**¡Sistema distribuido listo para ejecutar en red!** 🚀