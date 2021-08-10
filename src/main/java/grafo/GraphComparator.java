package grafo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class GraphComparator {

	double nodeScore = (double) 0.00;
	double edgeScore = (double) 0.00;
	private Graph graph1;
	private Graph graph2;
	private List<Node> nodeSuperSet=new ArrayList<Node>();
	private List<Edge> edgeSuperSet=new ArrayList<Edge>();

	public GraphComparator(Graph g1, Graph g2) {
		this.graph1=g1;
		this.graph2=g2;

	}

	public GraphComparator() {
		this.graph1=null;
		this.graph2=null;

	}

	public Graph getGraph1() {
		return this.graph1;
	}

	public Graph getGraph2() {
		return this.graph2;
	}

	public void setGraph1(Graph g1) {
		this.graph1=g1;
	}

	public void setGraph2(Graph g2) {
		this.graph2=g2;
	}

	public void generateNodeSuperset() {

		Iterator<Node> Nit1 = graph1.nodes().iterator();
		while(Nit1.hasNext()) {
			Node n= Nit1.next();
			if(!nodeSuperSet.contains(n))
			nodeSuperSet.add(n);
		}

		Iterator<Node> Nit2 = graph2.nodes().iterator();
		while(Nit2.hasNext()) {
			Node n= Nit2.next();
			if(!nodeSuperSet.contains(n))
			nodeSuperSet.add(n);
		}

	}

	public void generateEdgeSuperset() {
		Iterator<Edge> Eit1 = graph1.edges().iterator();
		while(Eit1.hasNext()) {
			Edge e= Eit1.next();
			if(!edgeSuperSet.contains(e))
			edgeSuperSet.add(e);
		}

		Iterator<Edge> Eit2 = graph2.edges().iterator();
		while(Eit2.hasNext()) {
			Edge e= Eit2.next();
			if(!edgeSuperSet.contains(e))
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

		Iterator<Node> nIt = nodeSuperSet.iterator();
		while(nIt.hasNext()) {
			Node n= nIt.next();

			if(graph1.getNode(n.getId())==null) {

				// score = score + 0
			}else {

				if(graph2.getNode(n.getId())==null) {

					// score = score + 0
				}else {

					Node node1 = graph1.getNode(n.getId());
					// t12
					// t56
					Node node2 = graph2.getNode(n.getId());

					String string1 = (String) node1.getAttribute("ui.label");
					String string2 = (String) node2.getAttribute("ui.label");
					
					// t56
					// R_t56

					if(string1.equals(string2)) {
						nodeScore = nodeScore + 1.00;
					}else {
						if(string1.charAt(0)== 'R' && string2.charAt(0) != 'R')
							nodeScore = nodeScore + 1.0;
						else if(string2.charAt(0) == 'R' && string1.charAt(0) != 'R')
							nodeScore = nodeScore + 1.0;
						else
							throw new IllegalArgumentException();
					}
				}
			}
		}
	}

	public void EdgeCompare() {
		
		Iterator<Edge> eIt = edgeSuperSet.iterator();
		while(eIt.hasNext()) {
			Edge e = eIt.next();
			
			if(graph1.getEdge(e.getId())==null) {
				
				// score = score + 0;
				
			}else {
				
				if( graph2.getEdge(e.getId())==null) {
					
					//score = score + 0;
					
				}else {
					
					Edge edge1 = graph1.getEdge(e.getId());
					Edge edge2 = graph2.getEdge(e.getId());
					
					String label1 = (String) edge1.getAttribute("ui.label");
					String label2 = (String) edge2.getAttribute("ui.label");
					
					// selfR , R , null 
						
						// selfRt11t11 -> selfRt11t11
						
						// Rt34t35 -> Rt34t35
						// t56t67 -> t56t67
					
					if(label1 == null) {
						if(label2 == null) {
							edgeScore = edgeScore +1.00;
						}else if(label2.charAt(0)=='R') {
							edgeScore = edgeScore +0.5;
						}else {
							throw new IllegalArgumentException();
						}
					}else {
						if(label2 == null) {
							if(label1.charAt(0)=='R')
							edgeScore = edgeScore +0.5;
							else throw new IllegalArgumentException();
						}else {
							if(label1.equals(label2))
								edgeScore = edgeScore +1.00;
							else
								throw new IllegalArgumentException();
						}
					}
						
						
						// Edge va dall'ID di un Nodo all'ID di un altro Nodo.
						// es. t11 -> t56
						
						// ha senso invece che l'Edge vada da un Label di un Nodo al Label di un altro Nodo?
						// es. R_t11 -> t56
						// es. t45 -> R_t34
						// es. R_t11 -> R_t77
						// es. t67 -> t68
						
    
						// Rt34t35 -> t34t35
						// t34t35 -> Rt34t35
					
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
		
		double totalNodeScore = ((gamma)*this.nodeScore)/getSizeNodeSuperSet() ;
		double totalEdgeScore = ((negativeGamma)*this.edgeScore)/getSizeEdgeSuperSet() ;
		
		return totalNodeScore + totalEdgeScore;
	}

}
