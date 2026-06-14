# 📊 GUÍA DE DEMOSTRACIÓN - Exposición Magistral


Demostrar el funcionamiento de **3 algoritmos distribuidos** en tiempo real:
1. **Bully Algorithm** - Elección de líder
2. **Ricart-Agrawala** - Exclusión mutua
3. **Lamport Clock** - Ordenamiento causal


### 1. Verificar Que Todo Funciona

```bash
# En la carpeta backend/
cd backend

# Compilar
mvn clean compile -q

# Ejecutar los 3 nodos
.\run-nodes.bat all
```

### 2. Acceso al Frontend

El frontend se conecta automáticamente a los 3 nodos. Abre en navegador:
```
http://localhost:3000
```

O inicia el frontend por separado:
```bash
cd frontend
npm install
npm start
```

### 3. Tener las Siguientes Ventanas Preparadas

- **4 Terminales**: Una para cada nodo + una para comandos extra
- **2 Navegadores**: 
  - Ventana 1: Frontend en http://localhost:3000
  - Ventana 2: Endpoint `/api/stats` para ver estado JSON

---

# 🎪 PARTE 1: BULLY ALGORITHM (Elección de Líder)

## 📚 Teoría (1 minuto)

**¿Qué es?**
- Algoritmo para **elegir un coordinador (líder)** en un sistema distribuido
- Cada nodo tiene un ID: 1, 2, 3 (3 es el más fuerte)
- El nodo con mayor ID gana la elección

**Fases:**
1. **HEARTBEAT** - El líder envía pulsos cada 5 segundos
2. **ELECTION** - Si falta heartbeat, se inicia elección
3. **COORDINATOR** - Se anuncia el nuevo líder

---

## 🎬 DEMOSTRACIÓN EN VIVO

### Escenario 1: Elección Inicial

**Paso 1:** Mostrar pantalla con 3 nodos iniciándose

```
✅ Nodo 1: Puerto 8081
✅ Nodo 2: Puerto 8082
✅ Nodo 3: Puerto 8083
```

**Lo que verás en los logs:**
```
[Nodo 1] Starting election...
[Nodo 2] Starting election...
[Nodo 3] Starting election...

[Nodo 3] I am the new Coordinator!  ← Nodo 3 gana (ID más alto)
[Nodo 1] Coordinator is: 3
[Nodo 2] Coordinator is: 3
```

**Explicación a la audiencia:**
- Los 3 nodos comienzar a disputar quién es el líder
- Nodo 3 tiene el ID más alto → ¡**Nodo 3 gana la elección!**
- Todos los nodos reconocen a Nodo 3 como coordinador
- Cada 5 segundos, Nodo 3 envía heartbeat para confirmar que sigue vivo

---

### Escenario 2: Fallo del Coordinador (Simular Crash)

**Paso 1:** Identificar que Nodo 3 es el coordinador

En el frontend, verás:
```
COORDINADOR ACTUAL: Nodo 3 ⭐
```

**Paso 2:** Detener Nodo 3 (presionar Ctrl+C en su ventana)

```
C:\backend> mvn spring-boot:run
[INFO] Tomcat started on port 8083
^C  ← ¡CRASH DEL NODO 3!
```

**Paso 3:** Mostrar qué sucede en los otros nodos

En 5-10 segundos verás en los logs:

```
[Nodo 1] Coordinator 3 is presumed dead. Starting election.
[Nodo 2] Coordinator 3 is presumed dead. Starting election.

[Nodo 2] I am the new Coordinator!  ← Nodo 2 gana (mayor que 1)
[Nodo 1] Coordinator is now: 2
```

**Explicación:**
- Los nodos detectan que no reciben heartbeat del coordinador
- Automáticamente **inician una nueva elección**
- Nodo 2 (ID 2) > Nodo 1 (ID 1) → **¡Nodo 2 es el nuevo coordinador!**
- Sistema sigue funcionando sin interrupciones

---

### Escenario 3: Recuperación del Nodo (Reintentar)

**Paso 1:** Reiniciar el Nodo 3

```bash
# En la ventana del Nodo 3
# Vuelve a ejecutar
.\run-nodes.bat node3
```

**Paso 2:** Ver qué sucede

```
[Nodo 2] Received ELECTION from higher node 3
[Nodo 1] Received ELECTION from higher node 3

[Nodo 3] I am the new Coordinator!  ← Nodo 3 recupera su posición
[Nodo 1] Coordinator is now: 3
[Nodo 2] Coordinator is now: 3
```

**Explicación:**
- Nodo 3 vuelve a estar disponible
- Como su ID es mayor, automáticamente recupera la posición de coordinador
- **¡Sistema es tolerante a fallos!**

---

## 🖥️ QUÉ MOSTRAR EN PANTALLA (Bully)

### Frontend
```
┌─────────────────────────────────────┐
│ ESTADO DE NODOS DISTRIBUIDOS        │
├─────────────────────────────────────┤
│ 🟢 Nodo 1 (8081)   │ Clock: 15      │
│ 🟢 Nodo 2 (8082)   │ Clock: 15      │
│ ⭐ Nodo 3 (8083)   │ Clock: 15   ← COORDINADOR
├─────────────────────────────────────┤
│ LOGS EN VIVO:                       │
│ [12:30:45] Nodo 3: I am Coordinator│
│ [12:30:46] Nodo 1: Heartbeat OK    │
│ [12:30:47] Nodo 2: Heartbeat OK    │
└─────────────────────────────────────┘
```

### Terminal (Logs de Spring Boot)
```
[2026-06-13 12:30:45] [Nodo 3] Sending heartbeat...
[2026-06-13 12:30:46] [Nodo 1] Heartbeat received from 3
[2026-06-13 12:30:47] [Nodo 2] Heartbeat received from 3
```

---

---

# 🔒 PARTE 2: RICART-AGRAWALA (Exclusión Mutua)

## 📚 Teoría (1 minuto)

**¿Qué es?**
- Asegura que **solo UN nodo** acceda a un recurso compartido a la vez
- Ejemplo: Comprar acciones de forma sincronizada en bolsa distribuida

**Estados:**
- **RELEASED** - Puedo usar el recurso
- **WANTED** - Quiero acceder
- **HELD** - Tengo acceso exclusivo

**Mecanismo:**
1. Nodo solicita acceso → envía REQUEST
2. Otros nodos responden → envían REPLY (o difieren)
3. Nodo con todas las REPLY → entra a sección crítica
4. Sale y notifica → envía todos los deferred REPLY

---

## 🎬 DEMOSTRACIÓN EN VIVO

### Escenario: Compra Simultánea de Acciones

**Setup Inicial:**
```
Recurso Compartido: Acciones STR
Total disponible: 100 acciones
Precio: $150.00/acción
```

**Paso 1:** Mostrar estado inicial en frontend

```
┌─ MERCADO DISTRIBUIDO ────────────────┐
│ Símbolo: STR                          │
│ Precio: $150.00                       │
│ Disponibles: 100                      │
│ Mi Balance: $10,000 (cada nodo)       │
│ Mis Acciones: 0                       │
└──────────────────────────────────────┘
```

**Paso 2:** Iniciar compras simultáneas desde múltiples nodos

Abre 3 terminales (una por nodo) y ejecuta en paralelo:

**Terminal 1 (Nodo 1):**
```
curl -X POST http://localhost:8081/api/trade/buy \
  -H "Content-Type: application/json" \
  -d '{"symbol":"STR", "quantity":30}'
```

**Terminal 2 (Nodo 2):**
```
curl -X POST http://localhost:8082/api/trade/buy \
  -H "Content-Type: application/json" \
  -d '{"symbol":"STR", "quantity":40}'
```

**Terminal 3 (Nodo 3):**
```
curl -X POST http://localhost:8083/api/trade/buy \
  -H "Content-Type: application/json" \
  -d '{"symbol":"STR", "quantity":20}'
```

---

### Paso 3: Ver la Ejecución en Logs

**Lo que sucede:**

```
[Nodo 1] Requesting Critical Section (WANTED)
  → Envía REQUEST a Nodos 2 y 3
  → Clock de Ricart-Agrawala: timestamp=15

[Nodo 2] Requesting Critical Section (WANTED)
  → Envía REQUEST a Nodos 1 y 3
  → Clock: timestamp=16 (más reciente que Nodo 1)
  → Difiere su respuesta a Nodo 1

[Nodo 3] Requesting Critical Section (WANTED)
  → Envía REQUEST a Nodos 1 y 2
  → Clock: timestamp=17

[Nodo 1] ✅ Recibió todas las REPLY → ENTRA a Sección Crítica
  → Compra 30 acciones
  → Balance: $10,000 - (30 × $150) = $5,500 ✓

[Nodo 1] 🔓 Saliendo de sección crítica
  → Envía REPLY deferida a Nodo 2
  → Envía REPLY deferida a Nodo 3

[Nodo 2] ✅ Recibió todas las REPLY → ENTRA a Sección Crítica
  → Compra 40 acciones
  → Balance: $10,000 - (40 × $150) = $4,000 ✓

[Nodo 3] ✅ Recibió todas las REPLY → ENTRA a Sección Crítica
  → Compra 20 acciones
  → Balance: $10,000 - (20 × $150) = $7,000 ✓
```

**Paso 4:** Verificar resultado final

```bash
# Verificar estado final desde cada nodo
curl http://localhost:8081/api/state
curl http://localhost:8082/api/state
curl http://localhost:8083/api/state
```

**Resultado esperado:**
```json
{
  "nodeId": 1,
  "accountBalance": 5500.0,
  "lamportClock": 20,
  "stocks": [
    {
      "symbol": "STR",
      "quantity": 30,
      "totalValue": 4500.0
    }
  ]
}
```

---

### Paso 5: Mostrar Consistencia

**Verificación en Frontend:**

```
┌─ ESTADO FINAL ──────────────────────┐
│                                      │
│ Nodo 1: 30 acciones = $4,500         │
│ Nodo 2: 40 acciones = $6,000         │
│ Nodo 3: 20 acciones = $3,000         │
│ ─────────────────────────────────────│
│ Total: 90 acciones vendidas ✓        │
│ Dinero Total en Sistema: $13,500 ✓   │
│                                      │
│ ✅ CONSISTENCIA GARANTIZADA          │
│ ✅ SIN RACE CONDITIONS               │
│ ✅ SIN ACCIONES DUPLICADAS           │
└──────────────────────────────────────┘
```

**Explicación a la audiencia:**
- 3 nodos intentaron comprar simultáneamente
- **Ricart-Agrawala garantizó que solo UNO accediera a la vez**
- Cada compra fue **atómica y consistente**
- ¡Sin corrupción de datos!

---

## 🖥️ QUÉ MOSTRAR EN PANTALLA (Ricart-Agrawala)

### Frontend - Gráfico de Transacciones
```
HISTORIAL DE TRANSACCIONES
────────────────────────────────────
[12:30:50] Nodo 1: WANTED (Comprar 30 STR)
[12:30:51] Nodo 2: WANTED (Comprar 40 STR) 
[12:30:52] Nodo 3: WANTED (Comprar 20 STR)

[12:30:55] Nodo 1: ✅ HELD (Ejecutando)
[12:31:05] Nodo 1: ✅ DONE (Compró 30 STR)

[12:31:06] Nodo 2: ✅ HELD (Ejecutando)
[12:31:16] Nodo 2: ✅ DONE (Compró 40 STR)

[12:31:17] Nodo 3: ✅ HELD (Ejecutando)
[12:31:27] Nodo 3: ✅ DONE (Compró 20 STR)
```

---

---

# ⏰ PARTE 3: LAMPORT CLOCK (Ordenamiento Causal)

## 📚 Teoría (1 minuto)

**¿Qué es?**
- Asegura **ordenamiento causal de eventos** en sistema distribuido
- Sin sincronización de reloj físico
- Solo cuenta: ¿Quién pasó antes que quién?

**Regla:**
- Evento local → clock++
- Recibir mensaje → clock = max(local, recibido) + 1

---

## 🎬 DEMOSTRACIÓN EN VIVO

### Escenario: Secuencia de Eventos Distribuidos

**Paso 1:** Mostrar relojes iniciales

```
Nodo 1: Lamport Clock = 0
Nodo 2: Lamport Clock = 0
Nodo 3: Lamport Clock = 0
```

**Paso 2:** Generar eventos

Ejecutar comandos en rápida sucesión:

```bash
# Evento 1: Nodo 1 hace una acción local
curl -X POST http://localhost:8081/api/event

# Evento 2: Nodo 2 hace una acción local
curl -X POST http://localhost:8082/api/event

# Evento 3: Nodo 1 envía mensaje a Nodo 2
curl -X POST http://localhost:8081/api/send-message

# Evento 4: Nodo 3 recibe algo
curl -X POST http://localhost:8083/api/event
```

**Paso 3:** Ver la progresión en logs

```
[12:30:50.100] Nodo 1: Local evento → Clock: 0 → 1
[12:30:50.200] Nodo 2: Local evento → Clock: 0 → 1
[12:30:50.300] Nodo 1: Envía msg con Clock=2 a Nodo 2
[12:30:50.400] Nodo 2: Recibe msg Clock=2 → Clock: 1 → max(1,2)+1 = 3 ✓
[12:30:50.500] Nodo 3: Local evento → Clock: 0 → 1

ESTADO FINAL:
Nodo 1: Clock = 2
Nodo 2: Clock = 3
Nodo 3: Clock = 1
```

**Paso 4:** Verificar causalidad

```
Clock 1 (Nodo 1) → CAUSÓ →
Clock 2 (Nodo 1) → CAUSÓ →
Clock 3 (Nodo 2)

Nodo 3 permanece en Clock 1 (no se vio afectado)
```

---

### Explicación Visual

```
LÍNEA DE TIEMPO CAUSAL:

Nodo 1 ──1──2──────────────
         │  │
         │  └─→ Envía MsjClock=2
         │
Nodo 2 ──1──────3───────────
            ↑
            └─ Recibe MsjClock=2, 
               actualiza a max(1,2)+1=3

Nodo 3 ──1──────────────────
         (no comunicación)
```

---

## 🖥️ QUÉ MOSTRAR EN PANTALLA (Lamport Clock)

### Frontend - Monitor de Relojes
```
┌─ LAMPORT CLOCKS (Ordenamiento Causal) ──┐
│                                          │
│  Nodo 1 ████████████ [23]                │
│  Nodo 2 ████████████ [25]                │
│  Nodo 3 ████████████ [22]                │
│                                          │
│ Eventos Causales Ordenados:              │
│ 1. Nodo 1: 23 - Compra iniciada         │
│ 2. Nodo 1: 24 - Envía a Nodo 2          │
│ 3. Nodo 2: 25 - Recibe y procesa ✓      │
│                                          │
│ ✅ CAUSALIDAD GARANTIZADA               │
└──────────────────────────────────────────┘
```

### Tabla de Eventos Ordenados
```
EVENTO # │ NODO │ CLOCK │ ACCIÓN
────────┼──────┼───────┼─────────────────
   1    │  1   │  1    │ Compra iniciada
   2    │  2   │  1    │ Compra iniciada
   3    │  1   │  2    │ Envía reporte
   4    │  2   │  3    │ Recibe reporte
   5    │  3   │  1    │ Compra iniciada
   6    │  1   │  3    │ Procesa algo
   7    │  2   │  4    │ Ack recibido
```

---

---

# 🔥 ESCENARIOS COMBINADOS (Opcional - Bonus)

Si tienes tiempo extra, muestra cómo funcionan **todos juntos**:

## Escenario: Crisis del Sistema

**Paso 1:** Sistema normal con Nodo 3 como coordinador

```
COORDINADOR: Nodo 3
Transacciones: Fluyen sin problema
Relojes: Sincronizados
```

**Paso 2:** Fallan Nodo 3 Y Nodo 1 simultáneamente

```bash
# Ctrl+C en Nodo 3
# Ctrl+C en Nodo 1
```

**Paso 3:** Ver qué sucede

```
[Nodo 2] ⚠️ Dos nodos se cayeron
[Nodo 2] Iniciar elección...
[Nodo 2] 🎯 Nodo 2 es el NUEVO coordinador

[Nodo 2] Intentar ejecutar transacción...
[Nodo 2] ⏳ Esperando REPLY de Nodo 1 y 3...
[Nodo 2] ⚠️ Timeout - algunos nodos no responden
[Nodo 2] Ejecutar con nodos disponibles (QUÓRUM)
[Nodo 2] ✅ TRANSACCIÓN EXITOSA (tolerancia a fallos)
```

**Paso 4:** Recuperar Nodo 1

```bash
# Reiniciar Nodo 1
.\run-nodes.bat node1
```

```
[Nodo 1] 🔄 Recuperando estado...
[Nodo 1] 📥 Sincronizando con Nodo 2
[Nodo 1] ✅ Sincronización completa
[Nodo 2] Nodo 1 está de vuelta
```

**Explicación:**
- Sistema continuó funcionando con un solo nodo
- Se ejecutó una elección automática
- Se sincronizó el estado
- **¡Alta disponibilidad!**

---

---

# 📊 COMANDOS ÚTILES PARA LA DEMO

## Ver Estado de un Nodo
```bash
curl -s http://localhost:8081/api/state | jq
```

## Iniciar Transacción de Prueba
```bash
curl -X POST http://localhost:8081/api/trade/buy \
  -H "Content-Type: application/json" \
  -d '{"symbol":"STR", "quantity":10}'
```

## Forzar Elección
```bash
# Detener coordinador
# Ctrl+C en ventana del nodo coordinador
```

## Ver Logs en Tiempo Real
```bash
# En cada ventana de terminal, los logs aparecen automáticamente
# Scrollear hacia arriba para ver el flujo completo
```

## Limpiar Estado (Reset)
```bash
# Detener todos los nodos
# Eliminar archivos JSON
del node1_db.json node2_db.json node3_db.json

# Reiniciar
.\run-nodes.bat all
```

---

---

# 💡 TIPS PARA LA EXPOSICIÓN

## ✅ HACER
1. **Empezar con Bully** (más visual, crash obvio)
2. **Mostrar logs en vivo** - Scroll fast para mostrar volumen
3. **Usar splits de pantalla** - Mostrar 3 terminales + frontend
4. **Pause entre escenarios** - Deja que la audiencia procese
5. **Haz preguntas retóricas** - "¿Ven cómo el Nodo 2 detectó el crash?"
6. **Resalta cambios de color** - Los logs cambian, señala con cursor
7. **Pregunta a la audiencia** - "¿Qué creen que pasará si apago Nodo 1?"
8. **Toma capturas de pantalla** - Para después incluir en presentación

## ❌ NO HACER
1. ❌ Esperar a que compile (pre-compila todo)
2. ❌ Leer código en vivo (muy técnico, aburre)
3. ❌ Explicar matemáticas de Lamport Clock (muy denso)
4. ❌ Tener logs con ERROR (limpia antes, o explica que es esperado)
5. ❌ Clickear rápido sin pausas (la gente no ve)
6. ❌ Mostrar 3 algoritmos a la vez (uno a la vez)
7. ❌ Dejar ventanas minimizadas (todo a la vista)

---

---

# 🎤 GUIÓN SUGERIDO

```
INICIO:
"Buenos días. Hoy vamos a ver 3 algoritmos distribuidos 
funcionando en tiempo real con 3 máquinas virtuales que se comunican."

[Mostrar pantalla con 3 nodos]

BULLY:
"Primero: Bully Algorithm. Es como un playoff de basketball.
El nodo con ID más alto gana. Miren..."

[Mostrar Nodo 3 siendo elegido]

"Si falla el ganador, automáticamente hay un torneo nuevamente."

[Apagar Nodo 3]

"¡Miren! En segundos, Nodo 2 es el nuevo coordinador.
Sistema tolerante a fallos."

RICART-AGRAWALA:
"Problema: Supongamos que 3 bancos quieren transferir dinero 
a una cuenta común. ¿Cómo evitamos race conditions?
Ricart-Agrawala es la respuesta."

[Mostrar 3 transacciones simultáneas]

"Cada nodo dice: Quiero acceso (WANTED).
Los otros dicen: Espera o Sí (REPLY).
Solo cuando tienes todos los síes, entras a la sección crítica."

[Mostrar transacciones ejecutándose secuencialmente]

"¡Miren los logs! Se ejecutan en orden, no simultáneamente.
Garantiza consistencia sin corrupción de datos."

LAMPORT:
"Ahora: Lamport Clock. Reloj lógico que ordena eventos 
causales sin sincronizar hardware."

[Mostrar relojes incrementando]

"Cada evento local: +1. Cada mensaje recibido: max(local, recibido)+1.
Así todos entienden el orden de causa-efecto."

CIERRE:
"En resumen: 
- Bully: Elige líder automáticamente
- Ricart-Agrawala: Evita condiciones de carrera
- Lamport Clock: Ordena eventos causalmente

Juntos: Sistema distribuido robusto y confiable."

[Mostrar pantalla final con todos funcionando]
```

---

---

# 📁 ARCHIVOS A TENER LISTOS

```
backend/
├── src/main/java/com/example/backend/service/
│   ├── BullyService.java          ← Para explicar si alguien pregunta
│   ├── RicartAgrawalaService.java  ← Para explicar si alguien pregunta
│   └── LamportClockService.java    ← Para explicar si alguien pregunta
├── run-nodes.bat                    ← Inicia los 3 nodos
└── node*_db.json                    ← Se generan automáticamente

frontend/
└── src/App.jsx                      ← Muestra los 3 nodos en vivo
```

---

---

# 🎓 PREGUNTAS QUE PODRÍAN HACER

**P: ¿Qué pasa si se caen 2 nodos?**
R: "Con 3 nodos, si se caen 2, queda 1. Ricart-Agrawala puede ejecutar 
   con quórum (mayoría). Si Nodo 1 es el único vivo, necesitaría 
   respuestas de 1+1 (él mismo). Es tolerante, pero limitado."

**P: ¿Por qué Lamport Clock no usa hora del sistema?**
R: "Porque en sistemas distribuidos, los relojes de diferentes máquinas 
   no están sincronizados. Lamport Clock solo usa orden relativo, 
   no valores absolutos. Es más confiable."

**P: ¿Ricart-Agrawala es la mejor forma de exclusión mutua?**
R: "No, es clásico pero ineficiente. Hay mejores como Maekawa (menos msgs).
   Pero Ricart es pedagógico, muestra el concepto."

**P: ¿Puedo agregar más nodos?**
R: "¡Sí! Modifica run-nodes.bat para :node4, :node5, etc. 
   Y agrega IPs en application.properties en nodes.all."

---

**¡Listo para brillar en tu exposición! 🌟**
