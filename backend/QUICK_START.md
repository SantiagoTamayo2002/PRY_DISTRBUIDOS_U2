# GUÍA RÁPIDA: Maven en Sistemas Distribuidos

## ✅ Migración Completada
El proyecto ha sido migrado de **Gradle** a **Maven** con soporte completo para múltiples nodos.

## 🚀 Inicio Rápido

### Opción 1: Ejecutar Todos los Nodos (Recomendado)
```batch
run-all-nodes.bat
```

### Opción 2: Ejecutar un Nodo Específico
```batch
run-node.bat 1 8081
run-node.bat 2 8082
run-node.bat 3 8083
```

### Opción 3: Ejecutar con Configuración Personalizada
```batch
run-node.bat 1 9001 "1:9001,2:9002,3:9003"
```

## 📋 Archivos Creados

| Archivo | Descripción |
|---------|-----------|
| `pom.xml` | Configuración Maven principal |
| `run-all-nodes.bat` | Ejecuta 3 nodos automáticamente |
| `run-all-nodes.sh` | Versión Linux/macOS |
| `run-node.bat` | Ejecuta un nodo específico |
| `run-node.sh` | Versión Linux/macOS |
| `MAVEN_MIGRATION.md` | Documentación completa |

## 🔧 Comandos Maven Útiles

```bash
# Compilar
mvn clean compile

# Empaquetar
mvn clean package

# Ejecutar tests
mvn test

# Ejecutar nodo específico (perfiles)
mvn spring-boot:run -Pnode1
mvn spring-boot:run -Pnode2
mvn spring-boot:run -Pnode3

# Limpiar caché
mvn dependency:purge-local-repository
```

## 🌐 Acceso a Nodos

| Nodo | Puerto | URL |
|------|--------|-----|
| 1 | 8081 | http://localhost:8081 |
| 2 | 8082 | http://localhost:8082 |
| 3 | 8083 | http://localhost:8083 |

## 📦 Dependencias Incluidas

- ✅ Spring Boot 3.3.0
- ✅ Spring Web (REST + MVC)
- ✅ Spring WebSocket
- ✅ Lombok
- ✅ Jackson (JSON)
- ✅ Testing (JUnit + Spring Test)

## 💡 Características

- **Multi-nodo**: Soporte para múltiples instancias distribuidas
- **Algoritmos Distribuidos**: Bully, Ricart-Agrawala, Lamport Clock
- **WebSocket**: Comunicación en tiempo real
- **Configurable**: Fácil personalización de puertos y nodos

## ⚙️ Configuración

Editar `src/main/resources/application.properties`:

```properties
# Configuración por defecto
server.port=8081
node.id=1
nodes.all=1:8081,2:8082,3:8083
```

Usa variables de entorno para sobrescribir:
```bash
PORT=8081 NODE_ID=1 mvn spring-boot:run
```

## 🔍 Verificación

Compilación exitosa ✅
```bash
mvn clean compile -q
```

## 📖 Más Información

Ver `MAVEN_MIGRATION.md` para documentación completa, troubleshooting y comandos avanzados.

---

**Estado**: ✅ Migración completada  
**Versión**: Maven 3.8.1+  
**Java**: 17+  
**Spring Boot**: 3.3.0
