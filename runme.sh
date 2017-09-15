#!/bin/bash
mvn package
mvn exec:java -Dexec.mainClass="Repo"
#-Dexec.args="a0 a1" for the args
