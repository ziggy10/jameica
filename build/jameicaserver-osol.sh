#!/bin/sh

# OpenSolaris Start-Script fuer Server-Betrieb.
# Jameica wird hierbei OHNE GUI gestartet.

cd `dirname $(readlink -f $0)`

java -Xmx256m -jar jameica-osol.jar -d $@