package edu.usc.hive;

import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.usc.hive.scoring.SuccessiveGroupBetweenness;
import org.apache.commons.collections15.TransformerUtils;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by charith on 5/17/17.
 */
public class NxContingency {




    /**
     * args[0] input edge list
     * args[1] x
     * args[2] output format n: no output v: stdOut f: file output
     * args[3] outfile
     * @param args
     */
    public static void main(String[] args) throws  Exception {

        List<String> edges = new ArrayList<String>();
        HashSet<Integer> vertices = new HashSet<Integer>();
        UndirectedGraph<Integer, Integer> g = new UndirectedSparseGraph<Integer, Integer>();
        Map<Integer, Double> w = new HashMap<Integer, Double>();

        int x = Integer.parseInt(args[1]);
        String out = args[2];
        String outFile = null;
        if("f".equals(out)) {
            outFile = args[3];
        }

        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        String line = reader.readLine();

        int edgeId=0;
        while(line != null) {
            edges.add(line);
            String[] parts = line.split(",");
            int s = Integer.parseInt(parts[0]);

            if(!vertices.contains(s)) {
                g.addVertex(s);
            }
            vertices.add(s);

            int t = Integer.parseInt(parts[1]);
            if(!vertices.contains(t)) {
                g.addVertex(t);
            }

            vertices.add(t);

            g.addEdge(++edgeId, s, t);

            if(parts.length == 3) {
                w.put(edgeId, Double.parseDouble(parts[2]));
            } else {
                w.put(edgeId, 1d);
            }

            line = reader.readLine();
        }

        long startTime = System.currentTimeMillis();
        ArrayList<Integer> vals = new ArrayList<Integer>();
        vals.addAll(vertices);
        List<HashSet<Integer>> groups = Combination.getCombination(vals, x);

        Set<Integer> maxBC = null;
        double currentMax = Double.MIN_VALUE;
        SuccessiveGroupBetweenness<Integer, Integer> gb = new SuccessiveGroupBetweenness<Integer, Integer>(g,
                TransformerUtils.mapTransformer(w), false);

        for(HashSet<Integer> group: groups) {
            try {
                double score = gb.getVertexGroupScore(group);
                if (currentMax < score) {
                    maxBC = group;
                    currentMax = score;
                }
            } catch (Exception e) {

            }
        }

        long endTime  = System.currentTimeMillis();
        System.out.println("x: " + x + ", time: " + (endTime - startTime) + " ms");

        if("v".equals(out) && maxBC != null) {
            System.out.println("Group Centrality: " + currentMax);

            for(Integer v: maxBC) {
                System.out.println(v);
            }
        } else if ("f".equals(out) && maxBC != null) {
            PrintWriter writer = new PrintWriter(new FileWriter(outFile));
            writer.println("Group Centrality: " + currentMax);

            for(Integer v: maxBC) {
                writer.println(v);
            }
        }
    }




}
