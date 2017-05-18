package edu.usc.hive.scoring;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * Author: vaclav.belak@deri.org
 * Date: 28-Oct-2010
 * Time: 16:22:53
 *
 * Classes implementing this interface compute scores of groups of vertices. For more information, see e.g.
 * Everett MG, Borgatti SP. The Centrality of Groups and Classes. Journal of Mathematical Sociology. 1999.
 *
 * <b>Acknowledgments:</b>This work was supported by Science Foundation Ireland (SFI) projects
 * Grant No. SFI/08/CE/I1380 (Lion-2) and Grant No. 08/SRC/I1407 (Clique: Graph & Network Analysis Cluster).
 */
public interface VertexGroupScorer<V,S extends Number> {

    public S getVertexGroupScore(Set<V> group);
}
