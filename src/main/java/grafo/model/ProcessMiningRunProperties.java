package grafo.model;

import static grafo.model.DefaultProperties.*;

/**
 * Questa classe rappresenta un contenitore dei parametri utilizzati generalmente nell'algoritmo
 * di process mining. Infatti in essa si possono trovare i seguenti parametri:
 * <ul>
 *     <li><code>edgeEqualScore</code> - di default, il valore sta a <code>1</code></li>
 *     <li><code>edgeSemiEqualScore</code> - di default, il valore sta a <code>0</code></li>
 *     <li><code>edgeNotEqualScore</code> - di default, il valore sta a <code>0</code></li>
 *     <li><code>nodeEqualScore</code> - di default, il valore sta a <code>1</code></li>
 *     <li><code>nodeSemiEqualScore</code> - di default, il valore sta a <code>0</code></li>
 *     <li><code>nodeNotEqualsScore</code> - di default, il valore sta a <code>0</code></li>
 *     <li><code>gamma</code> - di default, il valore sta a <code>0</code></li>
 *     <li><code>grams</code> - di default, il valore sta a <code>3</code></li>
 * </ul>
 *
 * @see DefaultProperties
 * @author Bogdan Donici
 */
public class ProcessMiningRunProperties {

    private double edgeEqualScore;
    private double edgeSemiEqualScore;
    private double edgeNotEqualScore;
    private double nodeEqualScore;
    private double nodeSemiEqualScore;
    private double nodeNotEqualScore;
    private double gamma;
    private int grams;

    public ProcessMiningRunProperties() {
        this.edgeEqualScore = DEFAULT_EDGE_EQUAL_SCORE.getValue();
        this.edgeSemiEqualScore = DEFAULT_EDGE_SEMI_EQUAL_SCORE.getValue();
        this.edgeNotEqualScore = DEFAULT_EDGE_NOT_EQUAL_SCORE.getValue();
        this.nodeEqualScore = DEFAULT_NODE_EQUAL_SCORE.getValue();
        this.nodeSemiEqualScore = DEFAULT_NODE_SEMI_EQUAL_SCORE.getValue();
        this.nodeNotEqualScore = DEFAULT_NODE_NOT_EQUAL_SCORE.getValue();
        this.gamma = DEFAULT_GAMMA.getValue();
    }

    public ProcessMiningRunProperties(double edgeEqualScore, double edgeSemiEqualScore,
                                      double edgeNotEqualScore, double nodeEqualScore,
                                      double nodeSemiEqualScore, double nodeNotEqualScore, double gamma, int grams) {
        this.edgeEqualScore = edgeEqualScore;
        this.edgeSemiEqualScore = edgeSemiEqualScore;
        this.edgeNotEqualScore = edgeNotEqualScore;
        this.nodeEqualScore = nodeEqualScore;
        this.nodeSemiEqualScore = nodeSemiEqualScore;
        this.nodeNotEqualScore = nodeNotEqualScore;
        this.gamma = gamma;
        this.grams = grams;
    }

    public double getEdgeEqualScore() {
        return edgeEqualScore;
    }

    public void setEdgeEqualScore(double edgeEqualScore) {
        this.edgeEqualScore = edgeEqualScore;
    }

    public double getEdgeSemiScore() {
        return edgeSemiEqualScore;
    }

    public void setEdgeSemiScore(double edgeSemiScore) {
        this.edgeSemiEqualScore = edgeSemiScore;
    }

    public double getEdgeNotEqualScore() {
        return edgeNotEqualScore;
    }

    public void setEdgeNotEqualScore(double edgeNotEqualScore) {
        this.edgeNotEqualScore = edgeNotEqualScore;
    }

    public double getNodeEqualScore() {
        return nodeEqualScore;
    }

    public void setNodeEqualScore(double nodeEqualScore) {
        this.nodeEqualScore = nodeEqualScore;
    }

    public double getNodeSemiScore() {
        return nodeSemiEqualScore;
    }

    public void setNodeSemiScore(double nodeSemiScore) {
        this.nodeSemiEqualScore = nodeSemiScore;
    }

    public double getNodeNotEqualScore() {
        return nodeNotEqualScore;
    }

    public void setNodeNotEqualScore(double nodeNotEqualScore) {
        this.nodeNotEqualScore = nodeNotEqualScore;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public int getGrams() {
        return grams;
    }

    public void setGrams(int grams) {
        this.grams = grams;
    }

    @Override
    public String toString() {
        return "ProcessMiningRunProperties{" +
                "edgeEqualScore=" + edgeEqualScore +
                ", edgeSemiEqualScore=" + edgeSemiEqualScore +
                ", edgeNotEqualScore=" + edgeNotEqualScore +
                ", nodeEqualScore=" + nodeEqualScore +
                ", nodeSemiEqualScore=" + nodeSemiEqualScore +
                ", nodeNotEqualScore=" + nodeNotEqualScore +
                ", gamma=" + gamma +
                ", grams=" + grams +
                '}';
    }
}
