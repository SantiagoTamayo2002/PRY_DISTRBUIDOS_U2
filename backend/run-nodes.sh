#!/bin/bash

# Script simple para ejecutar todos los nodos en Linux/macOS
# Uso: ./run-nodes.sh [opcion]
# Opciones: all, node1, node2, node3

if [ $# -eq 0 ]; then
    OPTION="all"
else
    OPTION=$1
fi

case $OPTION in
    all)
        echo ""
        echo "========================================"
        echo "Compilando proyecto..."
        echo "========================================"
        mvn clean compile -q
        if [ $? -ne 0 ]; then
            echo "Error durante la compilacion"
            exit 1
        fi
        echo "Compilacion completada"
        echo ""
        
        echo "Iniciando 3 nodos..."
        echo ""
        
        # Ejecutar nodos en background
        ./run-nodes.sh node1 &
        PID1=$!
        sleep 2
        
        ./run-nodes.sh node2 &
        PID2=$!
        sleep 2
        
        ./run-nodes.sh node3 &
        PID3=$!
        
        echo "Todos los nodos han sido iniciados (PIDs: $PID1, $PID2, $PID3)"
        echo "URLs: http://localhost:8081, http://localhost:8082, http://localhost:8083"
        echo ""
        echo "Para detener todos, presione Ctrl+C"
        wait
        ;;
    
    node1)
        mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081 --node.id=1 --nodes.all=1:8081,2:8082,3:8083"
        ;;
    
    node2)
        mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082 --node.id=2 --nodes.all=1:8081,2:8082,3:8083"
        ;;
    
    node3)
        mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083 --node.id=3 --nodes.all=1:8081,2:8082,3:8083"
        ;;
    
    *)
        echo "Uso: ./run-nodes.sh [all|node1|node2|node3]"
        echo ""
        echo "Ejemplos:"
        echo "  ./run-nodes.sh all     (ejecuta los 3 nodos)"
        echo "  ./run-nodes.sh node1   (ejecuta solo nodo 1)"
        exit 1
        ;;
esac
