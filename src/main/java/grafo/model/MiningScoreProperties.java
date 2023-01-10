package grafo.model;

public class MiningScoreProperties {
    private double edgeEqualScore;
    private double edgeSemiScore;
    private double edgeNotEqualScore;
    private double nodeEqualScore;
    private double nodeSemiScore;
    private double nodeNotEqualScore;
    private double gamma;

    public MiningScoreProperties(double edgeEqualScore, double edgeSemiScore,
                                 double edgeNotEqualScore, double nodeEqualScore,
                                 double nodeSemiScore, double nodeNotEqualScore, double gamma) {
        this.edgeEqualScore = edgeEqualScore;
        this.edgeSemiScore = edgeSemiScore;
        this.edgeNotEqualScore = edgeNotEqualScore;
        this.nodeEqualScore = nodeEqualScore;
        this.nodeSemiScore = nodeSemiScore;
        this.nodeNotEqualScore = nodeNotEqualScore;
        this.gamma = gamma;
    }

    public MiningScoreProperties() {
        this(1, 0, 0, 1, 0, 0, 0);
    }

    public double getEdgeEqualScore() {
        return edgeEqualScore;
    }

    public void setEdgeEqualScore(double edgeEqualScore) {
        this.edgeEqualScore = edgeEqualScore;
    }

    public double getEdgeSemiScore() {
        return edgeSemiScore;
    }

    public void setEdgeSemiScore(double edgeSemiScore) {
        this.edgeSemiScore = edgeSemiScore;
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
        return nodeSemiScore;
    }

    public void setNodeSemiScore(double nodeSemiScore) {
        this.nodeSemiScore = nodeSemiScore;
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
}
