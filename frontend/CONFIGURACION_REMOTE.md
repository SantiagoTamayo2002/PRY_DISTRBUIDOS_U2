# 🌐 Configuración del Frontend para Máquinas Distintas

## 📋 Resumen

El frontend está configurado para **automáticamente** conectarse a los nodos en máquinas distintas.

---

## 🎯 CASO 1: LOCALHOST (Misma Máquina)

### Setup

```bash
# Los 3 nodos en tu máquina local
cd backend
.\run-nodes.bat all

# Frontend en otra terminal
cd frontend
npm install
npm start
```

### Configuración

El archivo `.env.local` ya tiene los valores por defecto:
```
VITE_NODE_1_HOST=localhost
VITE_NODE_1_PORT=8081

VITE_NODE_2_HOST=localhost
VITE_NODE_2_PORT=8082

VITE_NODE_3_HOST=localhost
VITE_NODE_3_PORT=8083
```

### Acceso

```
Frontend: http://localhost:3000
```

---

## 🌐 CASO 2: MÁQUINAS DISTINTAS (Red 192.168.2.x)

### Setup

**Máquina 1 (192.168.2.2):**
```bash
cd backend
# Editar application.properties
# server.port=8081
# node.id=1
# nodes.all=1:192.168.2.2:8081,2:192.168.2.3:8081,3:192.168.2.4:8081

mvn clean compile -q
.\run-nodes.bat node1
```

**Máquina 2 (192.168.2.3):**
```bash
cd backend
# Editar application.properties
# server.port=8081
# node.id=2
# nodes.all=1:192.168.2.2:8081,2:192.168.2.3:8081,3:192.168.2.4:8081

mvn clean compile -q
.\run-nodes.bat node2
```

**Máquina 3 (192.168.2.4):**
```bash
cd backend
# Editar application.properties
# server.port=8081
# node.id=3
# nodes.all=1:192.168.2.2:8081,2:192.168.2.3:8081,3:192.168.2.4:8081

mvn clean compile -q
.\run-nodes.bat node3
```

### Configuración del Frontend

**Opción A: Desde cualquier máquina (incluso desde 192.168.2.2)**

Edita `frontend/.env.local`:

```env
VITE_NODE_1_HOST=192.168.2.2
VITE_NODE_1_PORT=8081

VITE_NODE_2_HOST=192.168.2.3
VITE_NODE_2_PORT=8081

VITE_NODE_3_HOST=192.168.2.4
VITE_NODE_3_PORT=8081
```

Luego inicia el frontend:
```bash
cd frontend
npm install  # Si es primera vez
npm start
```

### Acceso

```
Frontend: http://<IP-DE-TU-MÁQUINA>:3000
           (Si estás en 192.168.2.5: http://192.168.2.5:3000)
           (Si estás en otra red: http://localhost:3000)
```

---

## 🔄 Cambio Rápido de Configuración

### ¿Necesitas cambiar entre localhost y máquinas distintas?

#### Opción 1: Editar `.env.local`

```bash
# Comentar las líneas localhost
# VITE_NODE_1_HOST=localhost

# Descomentar las líneas de máquinas distintas
VITE_NODE_1_HOST=192.168.2.2
```

Luego:
```bash
cd frontend
npm start
```

El frontend **recargará automáticamente** con la nueva configuración.

#### Opción 2: Usar diferentes archivos .env

Crear `.env.production`:
```env
VITE_NODE_1_HOST=192.168.2.2
VITE_NODE_1_PORT=8081

VITE_NODE_2_HOST=192.168.2.3
VITE_NODE_2_PORT=8081

VITE_NODE_3_HOST=192.168.2.4
VITE_NODE_3_PORT=8081
```

Luego ejecutar:
```bash
npm run build  # Para producción
npm run preview  # Para previsualizar
```

---

## 📝 TABLA COMPARATIVA

| Escenario | VITE_NODE_1_HOST | VITE_NODE_1_PORT | Acceso Frontend |
|-----------|------------------|------------------|-----------------|
| **Localhost** | localhost | 8081 | http://localhost:3000 |
| **Red 192.168.2.x** | 192.168.2.2 | 8081 | http://192.168.2.X:3000 |
| **Puertos diferentes** | 192.168.2.2 | 8081 | http://192.168.2.X:3000 |

---

## 🐛 Troubleshooting

### Problema: "Connection refused" al conectar a un nodo

**Causa:** La IP o puerto en `.env.local` es incorrecta

**Solución:**
1. Verifica que la IP sea correcta: `ping 192.168.2.2`
2. Verifica que el nodo está ejecutándose
3. Revisa `.env.local` tiene la IP correcta
4. Reinicia el frontend: `npm start`

### Problema: El frontend se conecta a localhost pero los nodos están en otra máquina

**Causa:** `.env.local` no se editó correctamente

**Solución:**
```bash
# Verifica el contenido
cat frontend/.env.local

# Edita con tu editor favorito
# Cambiar localhost → 192.168.2.2
```

### Problema: CORS error al intentar conectar

**Causa:** Los navegadores modernos bloquean peticiones cross-origin si no están en localhost

**Solución:**
- Los nodos **DEBEN** permitir CORS (ya está configurado en Spring Boot)
- Si aún así hay problemas, verifica que `application.properties` tiene:
  ```properties
  # En cada nodo backend
  server.servlet.context-path=/
  ```

---

## ✅ CHECKLIST PARA MÁQUINAS DISTINTAS

### Antes de Iniciar

- [ ] Las 3 máquinas están conectadas a la misma red
- [ ] IPs son 192.168.2.2, 192.168.2.3, 192.168.2.4
- [ ] Firewall permite puerto 8081 en cada máquina
- [ ] `application.properties` en cada nodo tiene `nodes.all=1:192.168.2.2:8081,2:192.168.2.3:8081,3:192.168.2.4:8081`

### Frontend Setup

- [ ] Editaste `.env.local` con las IPs correctas
- [ ] Ejecutaste `npm install` (si es primera vez)
- [ ] Ejecutaste `npm start`

### Verificación

- [ ] Puedes hacer ping a cada máquina
- [ ] Cada nodo está ejecutándose
- [ ] Frontend carga en navegador
- [ ] Puedes ver los 3 nodos conectados (color verde)

---

## 🚀 Resumen Rápido

```bash
# 1. En cada máquina, editar application.properties
nodes.all=1:192.168.2.2:8081,2:192.168.2.3:8081,3:192.168.2.4:8081

# 2. Iniciar cada nodo
.\run-nodes.bat node1  # En 192.168.2.2
.\run-nodes.bat node2  # En 192.168.2.3
.\run-nodes.bat node3  # En 192.168.2.4

# 3. En otra máquina, editar frontend/.env.local
VITE_NODE_1_HOST=192.168.2.2
VITE_NODE_2_HOST=192.168.2.3
VITE_NODE_3_HOST=192.168.2.4

# 4. Iniciar frontend
npm start

# 5. Acceder
http://192.168.2.X:3000
```

---

**¡Listo! El frontend ahora soporta máquinas distintas.** 🎉
