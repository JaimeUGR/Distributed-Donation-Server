#!/bin/bash

echo "Lanzando el servidor"
java -cp . -Djava.rmi.server.codebase=file:./ \
-Djava.rmi.server.hostname=localhost -Djava.security.policy=server.policy\
 Servidor $1 $2 $3 $4 $5
