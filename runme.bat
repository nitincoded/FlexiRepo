#To execute with working dir set, pushd C:\the\dir & C:\the\dir\runme.bat & popd
mvn package
mvn exec:java -Dexec.mainClass="Repo"
#-Dexec.args="a0 a1" for the args
