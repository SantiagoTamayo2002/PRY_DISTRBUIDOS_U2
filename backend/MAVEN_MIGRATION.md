# Migración de Gradle a Maven

## Cambios Realizados

### 1. **Creación de pom.xml**
Se ha creado un archivo `pom.xml` que reemplaza la configuración de Gradle con equivalentes de Maven:
- Spring Boot 3.3.0 como parent POM
- Java 17 como versión objetivo
- Todas las dependencias necesarias (Web, WebSocket, Lombok, Testing)
- Plugins configurados: spring-boot-maven-plugin, maven-compiler-plugin, maven-surefire-plugin

### 2. **Perfiles Maven para Múltiples Nodos**
Se incluyen tres perfiles en el pom.xml:
- `node1`: NODE_ID=1, Puerto=8081
- `node2`: NODE_ID=2, Puerto=8082
- `node3`: NODE_ID=3, Puerto=8083

**Uso de perfiles:**
```bash
mvn spring-boot:run -Pnode1
mvn spring-boot:run -Pnode2
mvn spring-boot:run -Pnode3
```

### 3. **Scripts de Ejecución Múltiple**

#### Windows: `run-all-nodes.bat`
Inicia los tres nodos en ventanas separadas automáticamente.
```bash
run-all-nodes.bat
```

#### Linux/macOS: `run-all-nodes.sh`
Inicia los tres nodos en procesos separados.
```bash
chmod +x run-all-nodes.sh
./run-all-nodes.sh
```

## Comandos Importantes

### Compilación y Build
```bash
# Compilar
mvn clean compile

# Compilar y empaquetar
mvn clean package

# Compilar sin tests
mvn clean compile -DskipTests
```

### Ejecución de Nodos

#### Nodo Individual
```bash
# Nodo 1
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081 --node.id=1 --nodes.all=1:8081,2:8082,3:8083"

# Nodo 2
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082 --node.id=2 --nodes.all=1:8081,2:8082,3:8083"

# Nodo 3
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083 --node.id=3 --nodes.all=1:8081,2:8082,3:8083"
```

#### Con Perfiles Maven
```bash
mvn spring-boot:run -Pnode1
mvn spring-boot:run -Pnode2
mvn spring-boot:run -Pnode3
```

### Ejecución con Variables de Entorno
```bash
# Linux/macOS
PORT=8081 NODE_ID=1 mvn spring-boot:run
PORT=8082 NODE_ID=2 mvn spring-boot:run
PORT=8083 NODE_ID=3 mvn spring-boot:run

# Windows PowerShell
$env:PORT=8081; $env:NODE_ID=1; mvn spring-boot:run
$env:PORT=8082; $env:NODE_ID=2; mvn spring-boot:run
$env:PORT=8083; $env:NODE_ID=3; mvn spring-boot:run
```

### Pruebas
```bash
# Ejecutar todos los tests
mvn test

# Ejecutar una clase de test específica
mvn test -Dtest=BackendApplicationTests

# Ejecutar sin tests
mvn package -DskipTests
```

## Configuración de Nodos

La configuración en `application.properties` utiliza variables de entorno:
```properties
spring.application.name=backend
server.port=${PORT:8081}
node.id=${NODE_ID:1}
nodes.all=${NODES_ALL:1:8081,2:8082,3:8083}
logging.level.com.example.backend=INFO
```

**Valores por defecto:**
- PORT: 8081
- NODE_ID: 1
- NODES_ALL: 1:8081,2:8082,3:8083 (formato: id:puerto,id:puerto,...)

## Estructura de Red

El proyecto soporta múltiples nodos con las siguientes características:

### Nodos Disponibles
| Nodo ID | Puerto | URL Local |
|---------|--------|-----------|
| 1 | 8081 | http://localhost:8081 |
| 2 | 8082 | http://localhost:8082 |
| 3 | 8083 | http://localhost:8083 |

### Algoritmos Distribuidos Implementados
- **Bully Algorithm**: Para elección de líder
- **Ricart-Agrawala**: Para exclusión mutua
- **Lamport Clock**: Para sincronización de eventos

### Servicios WebSocket
- Comunicación en tiempo real entre nodos
- Transmisión de actualización de mercado (Stock Updates)
- Sincronización de estado distribuido

## Requisitos Previos

1. **Java 17 o superior**
   ```bash
   java -version
   ```

2. **Maven 3.8.1 o superior**
   ```bash
   mvn -version
   ```

3. **Conectividad de Red**
   - Los nodos se comunican vía HTTP en localhost
   - Para red real, modificar `nodes.all` en application.properties

## Pasos para Ejecutar

### Opción 1: Scripts Automatizados (Recomendado)
```bash
# Windows
run-all-nodes.bat

# Linux/macOS
./run-all-nodes.sh
```

### Opción 2: Ejecución Manual en Ventanas Separadas
```bash
# Terminal 1
mvn spring-boot:run -Pnode1

# Terminal 2
mvn spring-boot:run -Pnode2

# Terminal 3
mvn spring-boot:run -Pnode3
```

### Opción 3: Con Variables de Entorno
```bash
# Linux/macOS
PORT=8081 NODE_ID=1 mvn spring-boot:run &
PORT=8082 NODE_ID=2 mvn spring-boot:run &
PORT=8083 NODE_ID=3 mvn spring-boot:run &
```

## Limpieza

Para remover archivos de Gradle (opcional):
```bash
# Remover directorio build de Gradle
rmdir /s build

# Remover wrapper de Gradle (Windows)
del gradlew.bat
del gradlew

# Remover wrapper de Gradle (Linux/macOS)
rm gradlew
rm gradlew.bat
```

## Troubleshooting

### El puerto ya está en uso
```bash
# Windows: Encontrar proceso usando el puerto
netstat -ano | findstr :8081

# Linux/macOS: Encontrar proceso
lsof -i :8081

# Matar proceso (Windows)
taskkill /PID <PID> /F

# Matar proceso (Linux/macOS)
kill -9 <PID>
```

### Maven no encontrado
Asegúrese de que Maven esté instalado y en el PATH:
```bash
mvn -version
```

### Dependencias no descargan
```bash
# Limpiar caché de Maven y descargar nuevamente
mvn clean dependency:resolve
```

## Notas Importantes

- El proyecto utiliza **Spring Boot 3.3.0** con compatibilidad a Java 17+
- La configuración de nodos es flexible y puede adaptarse para redes reales
- Los scripts de ejecución múltiple crean procesos independientes por nodo
- Para detener todos los nodos: Ctrl+C en cada ventana o proceso
