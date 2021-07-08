package grafo;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.graphstream.algorithm.TarjanStronglyConnectedComponents;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSinkImages;
import org.graphstream.stream.file.FileSinkImages.LayoutPolicy;
import org.graphstream.stream.file.FileSinkImages.OutputType;
import org.graphstream.stream.file.images.Resolutions;

public class GraphTraceAnalyzer {
	
	private String trace;
	private Graph graph = new MultiGraph("RepeatingGraph");
	private Set<String> nodeIdList= new TreeSet<String>();
	private Set<String> edgeIdList= new TreeSet<String>();
	
	private String lastNodeId = null;
	private long startingTime;
	private long finishTime;
	private char delimiter='t';
	private boolean RepeatingAnnotation=true;
	
	public String getTrace() {
		return trace;
	}
	public void setTrace(String trace) {
		this.trace = trace;
	}
	public long getStartingTime() {
		return startingTime;
	}
	public void setStartingTime(long startingTime) {
		this.startingTime = startingTime;
	}
	public long getFinishTime() {
		return finishTime;
	}
	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}
	public char getDelimiter() {
		return delimiter;
	}
	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}
	public boolean isRepeatingAnnotation() {
		return RepeatingAnnotation;
	}
	public void setRepeatingAnnotation(boolean repeatingAnnotation) {
		RepeatingAnnotation = repeatingAnnotation;
	}
	public Graph getGraph() {
		return graph;
	}
	public Set<String> getNodeIdList() {
		return nodeIdList;
	}
	public Set<String> getEdgeIdList() {
		return edgeIdList;
	}
	public void GraphAnalyze() {


		this.startingTime=System.currentTimeMillis();
		StringBuffer check=new StringBuffer("");

		int checkpoint=0;
		boolean firstTime=true;

		for(int i=0;i<trace.length();i++) {
			i=checkpoint;

			//leggi se non si sfora
			if((i+1)<trace.length()) {

				//scorri e leggi tanti task quanti il grado

				//
				//t11t54t63t753t63t432
				int tcount=0;
				int j;
				for(j=i;j<trace.length();j++) {

					if(trace.charAt(j)==delimiter) { 
						if(tcount==1)
							checkpoint=j;
						tcount++;
						if(tcount>1)
							break;
					}
					check.append(trace.charAt(j));
				}
				//to exit
				i=j-1;


				if(check.length()!=0) {

					Node n=null;
					String edgeLabel=null;

					if(firstTime) {
						n=graph.addNode(check.toString());
						n.setAttribute("ui.label", n.getId());
						nodeIdList.add(n.getId());
						firstTime=false;
					}else {

						if(!nodeIdList.contains(check.toString())) {
							//new Node
							n=graph.addNode(check.toString());
							n.setAttribute("ui.label", n.getId());
							nodeIdList.add(n.getId());

							edgeLabel=lastNodeId+check.toString();
							Edge e=graph.addEdge(edgeLabel, lastNodeId, check.toString(),true);
							if(RepeatingAnnotation)
							e.setAttribute("times", 0);
							else
								e.setAttribute("times", 1);
							edgeIdList.add(edgeLabel);

						}else {
							// already contains that node
							edgeLabel=lastNodeId+check.toString();
							n= graph.getNode(check.toString());
							n.setAttribute("ui.label", "R_"+check.toString());

							// new edge?
							if(!edgeIdList.contains(edgeLabel)) {
								//new Edge
								if(lastNodeId.equals(check.toString())) {
									//self repeating
									Edge e=graph.addEdge(edgeLabel, lastNodeId, check.toString(),true);
									e.setAttribute("ui.label", "Self Repeating");
									e.setAttribute("times", 1);
									edgeIdList.add(edgeLabel);
								}else {
									//normal edge
									Edge e=graph.addEdge(edgeLabel, lastNodeId, check.toString(),true);
									if(RepeatingAnnotation)
										e.setAttribute("times", 0);
										else
											e.setAttribute("times", 1);
									edgeIdList.add(edgeLabel);
								}
							}else {
								// repeating Edge
								Edge e= graph.getEdge(edgeLabel);
								if(e.getAttribute("ui.label")==null) {
									if(RepeatingAnnotation)
									e.setAttribute("ui.label", "Repeating Edge ");
									else
										e.setAttribute("ui.label", "");

								}
								e.setAttribute("times", ((int)e.getAttribute("times"))+1 );
							}
						}

					}


				}
				lastNodeId= ""+check.toString()+"";
				check.delete(0, check.length());
			}
		}
		this.finishTime=System.currentTimeMillis()-this.startingTime;
		
		
	}
	
	public void GraphImage(String graphName) {
		System.setProperty("org.graphstream.ui","swing");
		graph.setAutoCreate(true);
		graph.setStrict(false);
		Iterator<Edge> it= graph.edges().iterator();
		while(it.hasNext()) {
			Edge e = it.next();
			
			if(RepeatingAnnotation) {
				String string = (String) e.getAttribute("ui.label");
				int times = (int) e.getAttribute("times");
				if(string!=null)
					e.setAttribute("ui.label", string+" "+times);
			}else {
			int times = (int) e.getAttribute("times");
			e.setAttribute("ui.label", String.valueOf(times));
			}
		}
		graph.setAttribute("ui.stylesheet",
				"graph { fill-color: white; } node { size: 30px, 30px; shape: box; fill-color: yellow; stroke-mode: plain; stroke-color: black; }  node:clicked { fill-color: red;} edge { shape: line; text-alignment: above;  fill-mode: dyn-plain; fill-color: #222, #555, green, yellow; arrow-size: 10px, 5px;}");

		graph.setAttribute("ui.quality");
		graph.setAttribute("ui.antialias");
		graph.display();
		
		
		FileSinkImages pic = FileSinkImages.createDefault();
		pic.setOutputType(OutputType.PNG);
		pic.setResolution(Resolutions.HD1080);
		 
		 pic.setLayoutPolicy(LayoutPolicy.COMPUTED_FULLY_AT_NEW_IMAGE);
		 	
		 try {
			
			pic.writeAll(graph, graphName+".png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		 
	}

	public void Tarjan() {

		TarjanStronglyConnectedComponents tscc = new TarjanStronglyConnectedComponents();
		tscc.init(graph);
		tscc.compute();

		graph.nodes().forEach( n -> {
			n.setAttribute("ui.label", n.getAttribute(tscc.getSCCIndexAttribute()));
		});
	}
	
	
	
	


}
