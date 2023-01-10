package grafo;

import grafo.model.MiningScoreProperties;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class that compares two LogGraph using their Node and Edges List (or Activity and Transition list)
 *
 * @author luigi.bucchicchioAtgmail.com
 */
public class GraphComparator {

    private MiningScoreProperties miningScoreProperties
            = new MiningScoreProperties(1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0);

    public double nodeScore = 0.00;
    public double edgeScore = 0.00;
    private Graph graph1;
    private Graph graph2;
    private final List<Node> nodeSuperSet = new ArrayList<>();
    private final List<Edge> edgeSuperSet = new ArrayList<>();

    public GraphComparator(Graph g1, Graph g2) {
        this.graph1 = g1;
        this.graph2 = g2;

    }

    public GraphComparator() {
        this.graph1 = null;
        this.graph2 = null;

    }

    public void setGraph1(Graph g1) {
        this.graph1 = g1;
    }

    public void setGraph2(Graph g2) {
        this.graph2 = g2;
    }

    public void generateNodeSuperset() {

        Iterator<Node> Nit1 = graph1.nodes().iterator();
        while (Nit1.hasNext()) {
            Node n = Nit1.next();
            boolean duplicate = false;
            for (Node edges : nodeSuperSet) {
                if (n.getId().equals(edges.getId()))
                    duplicate = true;
            }
            if (!duplicate)
                nodeSuperSet.add(n);
        }

        Iterator<Node> Nit2 = graph2.nodes().iterator();
        while (Nit2.hasNext()) {
            Node n = Nit2.next();
            boolean duplicate = false;
            for (Node edges : nodeSuperSet) {
                if (n.getId().equals(edges.getId()))
                    duplicate = true;
            }
            if (!duplicate)
                nodeSuperSet.add(n);
        }

    }

    public void generateEdgeSuperset() {
        Iterator<Edge> Eit1 = graph1.edges().iterator();
        while (Eit1.hasNext()) {
            Edge e = Eit1.next();
            boolean duplicate = false;
            for (Edge edge : edgeSuperSet) {
                if (e.getId().equals(edge.getId()))
                    duplicate = true;
            }
            if (!duplicate)
                edgeSuperSet.add(e);
        }

        Iterator<Edge> Eit2 = graph2.edges().iterator();
        while (Eit2.hasNext()) {
            Edge e = Eit2.next();
            boolean duplicate = false;
            for (Edge edge : edgeSuperSet) {
                if (e.getId().equals(edge.getId()))
                    duplicate = true;
            }
            if (!duplicate)
                edgeSuperSet.add(e);
        }
    }

    public int getSizeNodeSuperSet() {
        return this.nodeSuperSet.size();
    }

    public int getSizeEdgeSuperSet() {
        return this.edgeSuperSet.size();
    }

    public void NodeCompare() {

        for (Node n : nodeSuperSet) {
            if (graph1.getNode(n.getId()) == null) {

                nodeScore = nodeScore + miningScoreProperties.getNodeNotEqualScore();

            } else {

                if (graph2.getNode(n.getId()) == null) {

                    nodeScore = nodeScore + miningScoreProperties.getNodeNotEqualScore();

                } else {

                    Node node1 = graph1.getNode(n.getId());
                    // t12
                    // t56
                    Node node2 = graph2.getNode(n.getId());

                    String string1 = (String) node1.getAttribute("ui.label");
                    String string2 = (String) node2.getAttribute("ui.label");

                    // t56
                    // R_t56

                    if (string1.equals(string2)) {
                        nodeScore = nodeScore + miningScoreProperties.getNodeEqualScore();
                    } else {
                        //R_A A
                        if (string1.length() > 1 && string2.length() == 1) {
                            nodeScore = nodeScore + miningScoreProperties.getNodeSemiScore();
                            //A R_A
                        } else if (string2.length() > 1 && string1.length() == 1) {
                            nodeScore = nodeScore + miningScoreProperties.getNodeSemiScore();
                            //something R_something
                        } else if (string2.charAt(1) == '_' && string1.charAt(1) != '_') {
                            nodeScore = nodeScore + miningScoreProperties.getNodeSemiScore();
                            //R_something something
                        } else if (string1.charAt(1) == '_' && string2.charAt(1) != '_') {
                            nodeScore = nodeScore + miningScoreProperties.getNodeSemiScore();
                        } else {
                            System.out.println(string1 + " " + string2);
                            throw new IllegalArgumentException();
                        }

                    }
                }
            }
        }
    }

    public void EdgeCompare() {

        for (Edge e : edgeSuperSet) {
            if (graph1.getEdge(e.getId()) == null) {
                edgeScore = edgeScore + miningScoreProperties.getEdgeNotEqualScore();
            } else {
                if (graph2.getEdge(e.getId()) == null) {
                    edgeScore = edgeScore + miningScoreProperties.getEdgeNotEqualScore();
                } else {

                    Edge edge1 = graph1.getEdge(e.getId());
                    Edge edge2 = graph2.getEdge(e.getId());

                    String label1 = (String) edge1.getAttribute("ui.label");
                    String label2 = (String) edge2.getAttribute("ui.label");

                    //R , null

                    if (label1 == null) {
                        if (label2 == null) {
                            edgeScore = edgeScore + miningScoreProperties.getEdgeEqualScore();
                        } else if (label2.charAt(0) == 'R') {
                            edgeScore = edgeScore + miningScoreProperties.getEdgeSemiScore();
                        } else {
                            throw new IllegalArgumentException();
                        }
                    } else {
                        if (label2 == null) {
                            if (label1.charAt(0) == 'R')
                                edgeScore = edgeScore + miningScoreProperties.getEdgeSemiScore();
                            else throw new IllegalArgumentException();
                        } else {
                            if (label1.equals(label2))
                                edgeScore = edgeScore + miningScoreProperties.getEdgeEqualScore();
                            else
                                throw new IllegalArgumentException();
                        }
                    }

                }
            }
        }
    }

    public double getMetrics(double gamma) {
        double negativeGamma = 1 - gamma;
        generateNodeSuperset();
        generateEdgeSuperset();
        NodeCompare();
        EdgeCompare();
        double totalNodeScore = getTotalNodeScore(gamma);
        double totalEdgeScore = getTotalEdgeScore(negativeGamma);
        return totalNodeScore + totalEdgeScore;
    }

    private double getTotalEdgeScore(double negativeGamma) {
        double totalEdgeScore;
        if (getSizeEdgeSuperSet() == 0)
            totalEdgeScore = 0.0;
        else
            totalEdgeScore = (negativeGamma * this.edgeScore) / getSizeEdgeSuperSet();
        return totalEdgeScore;
    }

    private double getTotalNodeScore(double gamma) {
        double totalNodeScore;
        if (getSizeNodeSuperSet() == 0)
            totalNodeScore = 0.0;
        else
            totalNodeScore = (gamma * this.nodeScore) / getSizeNodeSuperSet();
        return totalNodeScore;
    }


    public void setEdgeEqualScore(double edgeEqualScore) {
        miningScoreProperties.setEdgeEqualScore(edgeEqualScore);
    }

    public void setEdgeSemiScore(double edgeSemiScore) {
        miningScoreProperties.setEdgeSemiScore(edgeSemiScore);
    }

    public void setEdgeNotEqualScore(double edgeNotEqualScore) {
        miningScoreProperties.setEdgeNotEqualScore(edgeNotEqualScore);
    }

    public void setNodeEqualScore(double nodeEqualScore) {
        miningScoreProperties.setNodeEqualScore(nodeEqualScore);
    }

    public void setNodeSemiScore(double nodeSemiScore) {
        miningScoreProperties.setNodeSemiScore(nodeSemiScore);
    }

    public void setNodeNotEqualScore(double nodeNotEqualScore) {
        miningScoreProperties.setNodeNotEqualScore(nodeNotEqualScore);
    }

    public void setGamma(double gamma) {
        miningScoreProperties.setGamma(gamma);
    }

    public void setMiningScoreProperties(MiningScoreProperties miningScoreProperties) {
        this.miningScoreProperties = miningScoreProperties;
    }

}
