#!/bin/bash
#To execute with working dir set, (cd /the/dir ; /the/dir/runme.sh )
mvn package
mvn exec:java -Dexec.mainClass="Repo"
#-Dexec.args="a0 a1" for the args
