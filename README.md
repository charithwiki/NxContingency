
Baseline Algorithm for Nx Contingency
======================================

Prerequisites
==============
    * Java installation (1.7)

    * Apache Maven installation (3.3)

Compiling Code
===============
    * $mvn clean install

Input Graph Format
====================
    * Input graph is provided as an edge list
        * source vertex id,target vertex id,edge weight (optional)

Running Code
============
    * Executing from jar $java -cp PATH_TO_JAR edu.usc.hive.NxContingency EDGE_LIST X OUT_TYPE(v=std out, n=no output, f=fileout) OUT_FILE_PATH
        * ex: java -cp target/Nx-Contingency-1.0-jar-with-dependencies.jar edu.usc.hive.NxContingency 14-bus.txt 3 v
    * Executing from bash file in batch mode to get performance numbers with varying x up to MAX_X $./bin/run_baseline.sh EDGE_LIST MAX_X
        * ex: $./bin/run_baseline.sh 14-bus.txt 9
OUTPUT
============
    * Set of x number of vertices with highest group betweenness centrality (i.e. the set of x number of entities, that has the largest impact if removed concurrently).
