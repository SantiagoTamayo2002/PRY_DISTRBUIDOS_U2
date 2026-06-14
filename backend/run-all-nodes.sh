#!/bin/bash

# Script para ejecutar todos los nodos en Linux/macOS
# Requiere Maven instalado y disponible en el PATH

echo ""
echo "============================================"
echo "Iniciando Sistema Distribuido - Todos los Nodos"
echo "============================================"
echo ""

# Compilar el proyecto una sola vez
echo "[1/4] Compilando proyecto..."
mvn clean compile
if [ $? -ne 0 ]; then
    echo "Error durante la compilacion"
    exit 1
fi

# Función para ejecutar un nodo
run_node() {
    local node_id=$1
    local port=$2
    echo "[*] Iniciando Nodo $node_id en puerto $port..."
    mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=$port --node.id=$node_id --nodes.all=1:8081,2:8082,3:8083" &
    NODE_PIDS[$node_id]=$!
    sleep 2
}

# Ejecutar Nodo 1
echo "[2/4] Iniciando Nodo 1..."
run_node 1 8081

# Ejecutar Nodo 2
echo "[3/4] Iniciando Nodo 2..."
run_node 2 8082

# Ejecutar Nodo 3
echo "[4/4] Iniciando Nodo 3..."
run_node 3 8083

echo ""
echo "============================================"
echo "Todos los nodos han sido iniciados"
echo "============================================"
echo ""
echo "URLs disponibles:"
echo "  Nodo 1: http://localhost:8081"
echo "  Nodo 2: http://localhost:8082"
echo "  Nodo 3: http://localhost:8083"
echo ""
echo "Para detener todos los nodos, presione Ctrl+C"
echo ""

# Esperar a que se interrumpa
wait
