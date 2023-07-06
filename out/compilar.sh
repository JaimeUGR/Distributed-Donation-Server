#!/bin/sh -e
# ejecutar = Macro para compilacion y ejecucion del programa ejemplo
# en una sola maquina Unix de nombre localhost.

echo
echo "Compilando con javac ..."
javac -d . ../src/*.java
echo
