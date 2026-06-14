#!/bin/bash

# Script para ejecutar un nodo individual en Linux/macOS con argumentos personalizados
# Uso: ./run-node.sh <nodeId> <port> [nodesAll]
# Ejemplo: ./run-node.sh 1 8081 "1:8081,2:8082,3:8083"

if [ $# -lt 2 ]; then
    echo "Uso: ./run-node.sh <nodeId> <port> [nodesAll]"
    echo ""
    echo "Ejemplos:"
    echo "  ./run-node.sh 1 8081"
    echo "  ./run-node.sh 2 8082 \"1:8081,2:8082,3:8083\""
    echo "  ./run-node.sh 1 9001 \"1:9001,2:9002\""
    echo ""
    exit 1
fi

NODE_ID=$1
PORT=$2
NODES_ALL=${3:-"1:8081,2:8082,3:8083"}

echo ""
echo "============================================"
echo "Ejecutando Nodo $NODE_ID"
echo "============================================"
echo "NODE_ID: $NODE_ID"
echo "Puerto:  $PORT"
echo "Nodos:   $NODES_ALL"
echo ""

mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=$PORT --node.id=$NODE_ID --nodes.all=$NODES_ALL"
