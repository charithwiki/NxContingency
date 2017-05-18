package edu.usc.hive.scoring;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import org.apache.commons.collections15.Transformer;
import edu.usc.hive.shortestpath.DijkstraShortestPathCount;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * Author: Vaclav.Belak@deri.org
 * Date: 26-Oct-2010
 * Time: 10:55:50
 * <p/>
 * Implementation of the group betweenness algorithm presented in Puzis et. al (2007): 'Fast algorithm for successive
 * computation of group betweenness centrality'. For a graph with <code>n</code> nodes and <code>m</code> edges it
 * computes a betweenness of a group with <code>k</code> members in <code>O(nk^2)</code>, which is dominated by
 * computation of shortest paths counts, partial dependencies, and distances (<code>O(nm)</code>) if <code>k<=sqrt(m)</code>.
 * However, these computations are done only once, so any group is then computed in <code>O(nk^2)</code>. The space
 * complexity is <code>O(n^2)</code>.
 * <p/>
 * Note that this algorithm differs from the original definition of group betweenness by Everett and Borgatti (Journal of
 * Mathematical Sociology, 1999): it doesn't discard paths which originate or end in a group node. If you don't want to
 * include them, instantiate this class with <code>includePeriphery=false</code>.
 * <p/>
 * This implementation works with weighted, undirected graphs. Unlike the original version presented
 * in the paper, this implementation works also with disconnected graphs - it then treats each connected component as a
 * separate graph. However, each group has to be connected, so if it is not, an exception is thrown.
 * <p/>
 * Note that shortest paths are determined by weights, which usually characterize importance, so you might want to
 * actually rather consider their complements, i.e. <code>(1-w)</code>.
 * <p/>
 * <b>Acknowledgments:</b>This work was supported by Science Foundation Ireland (SFI) projects
 * Grant No. SFI/08/CE/I1380 (Lion-2) and Grant No. 08/SRC/I1407 (Clique: Graph & Network Analysis Cluster).
 */
public class SuccessiveGroupBetweenness<V, E> implements VertexGroupScorer<V, Double> {

    protected Graph<V, E> graph;
    protected DijkstraShortestPathCount<V, E> distance;
    protected Transformer<E, Double> weight;
    protected Map<V, Map<V, Double>> pathBetweenness;   // B matrix according to Puzis et al. (2007)
    protected boolean includePeriphery;
    protected boolean isConnected;

    /**
     * Creates a group betweenness scorer for a weighted undirected graph
     *
     * @param graph            Graph on which the score will be computed - may be disconnected.
     * @param weight           Mapping <code>E</code>-><code>R</code> representing edges's weights
     * @param includePeriphery True if paths starting or ending in a group should be included, false otherwise
     */
    public SuccessiveGroupBetweenness(UndirectedGraph<V, E> graph, Transformer<E, Double> weight, boolean includePeriphery) {
        this.graph = graph;
        this.weight = weight;
        this.distance = new DijkstraShortestPathCount<V, E>(graph, weight, true);
        this.pathBetweenness = new HashMap<V, Map<V, Double>>();
        this.includePeriphery = includePeriphery;
        this.isConnected = distance.isConnected();
    }

    /**
     * Compute path betweenness of edge XY
     * Using Eq. 6 from Puzis et al. (2007)
     *
     * @param x First node
     * @param y Second node
     * @return Betweenness of the paths starting at any vertex <code>s</code> and then going first through <code>x</code> and then <code>y</code>
     */
    protected double getPathBetweenness(V x, V y) {
        if (pathBetweenness.containsKey(x)) {
            if (pathBetweenness.get(x).containsKey(y)) {
                return pathBetweenness.get(x).get(y);
            }
        } else {
            pathBetweenness.put(x, new HashMap<V, Double>());
        }

        double pathBetXY = 0;
        for (V s : graph.getVertices()) {

            double deltaSY = distance.getPartialDependency(s, y);
            long sigmaSXY = distance.getShortestPathCount(s, x, y);
            long sigmaSY = distance.getShortestPathCount(s, y);

            if (deltaSY != 0 && sigmaSXY != 0 && sigmaSY != 0) {  // add only non-zero contributions
                pathBetXY += deltaSY * (((double) sigmaSXY) / sigmaSY);
            }
        }

        pathBetweenness.get(x).put(y, pathBetXY);

        return pathBetXY;
    }

    /**
     * Computes the betweenness of the group.
     *
     * @param group Set of vertices defining the group
     * @return Betweenness of the group
     */
    public Double getVertexGroupScore(Set<V> group) {
        if (!isConnected && !isGroupConnected(group))
            throw new IllegalArgumentException("The group is not connected!");

        Map<V, Map<V, Double>> sigmaM = new HashMap<V, Map<V, Double>>(group.size());
        Map<V, Map<V, Double>> pathBetweennessM = new HashMap<V, Map<V, Double>>(group.size()); // 'BM with ~' matrix
        for (V x : group) {
            sigmaM.put(x, new HashMap<V, Double>());
            pathBetweennessM.put(x, new HashMap<V, Double>());
            for (V y : group) {
                sigmaM.get(x).put(y, (double) distance.getShortestPathCount(x, y));
                pathBetweennessM.get(x).put(y, getPathBetweenness(x, y));
            }
        }

        double groupBetweenness = 0; // 'B with colon above'

        for (V v : group) {
            groupBetweenness += pathBetweennessM.get(v).get(v);
            // temporary matrices with {v}UM
            Map<V, Map<V, Double>> sigmaMUV = new HashMap<V, Map<V, Double>>(group.size());
            Map<V, Map<V, Double>> pathBetweennessMUV = new HashMap<V, Map<V, Double>>(group.size());
            for (V x : group) {
                sigmaMUV.put(x, new HashMap<V, Double>());
                pathBetweennessMUV.put(x, new HashMap<V, Double>());
                for (V y : group) {
                    double dXVY = 0, dXYV = 0, dVXY = 0;
                    if (!(sigmaM.get(x).get(y) == 0 || sigmaM.get(x).get(v) == 0 || sigmaM.get(y).get(v) == 0)) {
                        if (distance.getDistanceAsDouble(x, v) ==
                                distance.getDistanceAsDouble(x, y) + distance.getDistanceAsDouble(y, v)) {
                            dXYV = ((double) sigmaM.get(x).get(y)) * sigmaM.get(y).get(v) / sigmaM.get(x).get(v);
                        }
                        if (distance.getDistanceAsDouble(x, y) ==
                                distance.getDistanceAsDouble(x, v) + distance.getDistanceAsDouble(v, y)) {
                            dXVY = ((double) sigmaM.get(x).get(v)) * sigmaM.get(v).get(y) / sigmaM.get(x).get(y);
                        }
                        if (distance.getDistanceAsDouble(v, y) ==
                                distance.getDistanceAsDouble(v, x) + distance.getDistanceAsDouble(x, y)) {
                            dVXY = ((double) sigmaM.get(v).get(x)) * sigmaM.get(x).get(y) / sigmaM.get(v).get(y);
                        }
                    }
                    sigmaMUV.get(x).put(y, sigmaM.get(x).get(y) * (1 - dXVY));
                    pathBetweennessMUV.get(x).put(y, pathBetweennessM.get(x).get(y) - pathBetweennessM.get(x).get(y) * dXVY);
                    if (!y.equals(v)) {
                        double pb = pathBetweennessMUV.get(x).get(y);
                        pathBetweennessMUV.get(x).put(y, pb - pathBetweennessM.get(x).get(v) * dXYV);
                    }
                    if (!x.equals(v)) {
                        double pb = pathBetweennessMUV.get(x).get(y);
                        pathBetweennessMUV.get(x).put(y, pb - pathBetweennessM.get(v).get(y) * dVXY);
                    }
                }
            }
            sigmaM = sigmaMUV;
            pathBetweennessM = pathBetweennessMUV;
        }

        groupBetweenness /= 2;  // undirected graph

        if (includePeriphery) {
            return groupBetweenness;
        } else {
            return groupBetweenness - group.size() * (2 * getSizeOfConnectedComponent(group) - group.size() - 1) / 2d;
        }
    }

    /**
     * Computes the normalized GBC as defined in Everett and Borgatti.
     * @param group of vertices
     * @param groupBetweenness corresponding group betweenness centrality value
     * @param relativeToWholeGraph true if the normalization should be computed relatively to the entire graph, false if only to the size of the corresponding connected component
     * @return normalized group betweenness centrality
     */
    public double getNormalizedGBC(Set<V> group, Double groupBetweenness, boolean relativeToWholeGraph) {
        if (includePeriphery)
            throw new IllegalStateException("Normalized betweenness implemented only for GBC excluding periphery.");
        if (groupBetweenness == null) groupBetweenness = getVertexGroupScore(group);

        int componentSize;
        if (relativeToWholeGraph) {
            componentSize = graph.getVertexCount();
        } else {
            componentSize = getSizeOfConnectedComponent(group);
        }
        double divisor = (componentSize - group.size()) * (componentSize - group.size() - 1);
        if (divisor == 0) { // the group spreads over the entire component - GBC is 0 in that case
            return 0;
        } else {
            return (2 * groupBetweenness) / divisor;
        }
    }

    /**
     * Computes the size of the connected component the group is part of.
     * @param group Set of vertices
     * @return Size of the corresponding connected component
     */
    private int getSizeOfConnectedComponent(Set<V> group) {
        int size = 0;
        for (V v : group) {
            int count = distance.getAccessibleVerticesCount(v);
            if (count > size) size = count;
        }
        return size;
    }

    /**
     * Tests whether the group vertices is connected, i.e. whether there is a path between each pair of nodes in the group.
     * @param group Set of vertices.
     * @return True if the group is connected, false otherwise.
     */
    private boolean isGroupConnected(Set<V> group) {
        for (V v : group) {
            for (V s : group) {
                if (distance.getDistance(v, s) == null) return false;
            }
        }
        return true;
    }
}
