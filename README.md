Prerequisites
========================
*Java installation (1.7)

*Apache Maven installation (3.3)


Compiling Code
=======================
*<code>$mvn clean install</code>

Input Graph Fromat
=======================
* Input graph is provided as an edge list

	* source vertex id,target vertex id,edge weight (optional)

Running Code
=======================
* Executing from jar<code>$java -cp PATH_TO_JAR edu.usc.hive.NxContingency EDGE_LIST X OUT_TYPE(v=std out, n=no output, f=fileout) OUT_FILE_PATH </code>
	* ex: <code>java -cp target/Nx-Contingency-1.0-jar-with-dependencies.jar edu.usc.hive.NxContingency 14-bus.txt 3 v</code>

* Executing from bash file in batch mode to get performance numbers with varying x up to MAX_X <code>$./bin/run_baseline.sh EDGE_LIST MAX_X</code>
	* ex: <code>$./bin/run_baseline.sh 14-bus.txt 9</code>


OUTPUT
======================
* Set of x number of vertices with highest group betweenness centrality (i.e. the set of x number of entities, that has the largest impact if removed concurrently).

