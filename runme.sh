#!/bin/bash
#To execute with working dir set, (cd /the/dir ; /the/dir/runme.sh )

mvn -T 4 package
#Parallelism set to 4 threads; supported from Maven 3.x

mvn exec:java -Dexec.mainClass="Repo"
#-Dexec.args="a0 a1" for the args
