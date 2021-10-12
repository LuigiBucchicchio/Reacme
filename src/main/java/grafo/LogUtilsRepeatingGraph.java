package grafo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JFileChooser;

import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.graphstream.graph.Graph;

import com.opencsv.CSVWriter;


public class LogUtilsRepeatingGraph {

	private File[] fileList;
	private List<Graph> graphList = new ArrayList<Graph>();
	private String[][] graphSimilarity;
	
	//scores
	private boolean scoreChange=false;
	private double edgeEqualScore= (double) 1.0;
	private double edgeSemiScore= (double) 0.5;
	private double edgeNotEqualScore = (double) 0.0;
	private double nodeEqualScore= (double) 1.0;
	private double nodeSemiScore= (double) 1.0;
	private double nodeNotEqualScore = (double) 0.0;

	static double startingTime;

	public XLog parseXES(String filePath) throws Exception {
		XesXmlParser parser = new XesXmlParser();
		return parser.parse(new File(filePath)).get(0);	
	}

	public void getTraces(File[] listOfFiles) {
		startingTime=System.currentTimeMillis();



		//int filePointer=0;
		for (int i = 0; i < listOfFiles.length; i++) {
			File file=listOfFiles[i];
			Set<ArrayList<String>> traceSet=new HashSet<ArrayList<String>>();

			List<Trace> traceList = new ArrayList<Trace>();

			try {
				XLog xlog=parseXES(file.getAbsolutePath());
				//XLog xlog=parseXES("/home/cippus/Downloads/log (5).xes");

				for (XTrace xTrace : xlog) {



					ArrayList<String> trace=new ArrayList<String>();
					//GraphTraceAnalyzer analyzer= new GraphTraceAnalyzer();

					StringBuffer traceLine=new StringBuffer("");
					// Trace = [t11, t45, t63, t12, t113, t9]
					// traceLine = t11t45t63t12t113t9

					for (XEvent xevent : xTrace) {



						String activity=xevent.getAttributes().get("concept:name").toString();

						//conversion for 43 log
						//String activity = "t"+xevent.getAttributes().get("concept:name").toString();

						//t11
						//t34
						traceLine.append(activity);

						trace.add(activity);


					}

					Trace genericTrace = new Trace();
					genericTrace.setTraceLine(traceLine.toString());
					genericTrace.setActivitySet(trace);
					genericTrace.setLogId(listOfFiles[i].getName());
					genericTrace.setTraceId(xTrace.getAttributes().get("concept:name").toString());
					traceList.add(genericTrace);

					traceSet.add(trace);


					//analyzer.setTrace(traceLine.toString());
				}

				GraphLogAnalyzer analyzer = new GraphLogAnalyzer();
				analyzer.setTraceSet(traceList);
				analyzer.LogAnalyze();
				graphList.add(analyzer.getGraph());
				//analyzer.GraphImage("Log "+listOfFiles[i].getName()+" graph");

				//filePointer++;

				
			} catch (Exception e) {
				e.printStackTrace();
			}

		} 
	}

	public void convertToCSV(String[][] data) {
		try {
			File csvFile = new File("DistanceGraph.csv");
			CSVWriter writer = new CSVWriter(new FileWriter(csvFile));

			for (String[] array : data) {
				writer.writeNext(array);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}

	public File[] selectFolder() {
		JFileChooser chooser = new JFileChooser(".");

		System.out.println("\u2705 " +"Please select the folder containing the XES Files" +" \u2705");

		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to open this folder: " + chooser.getSelectedFile().getName());
		}
		File folder= new File(chooser.getSelectedFile().getAbsolutePath());
		return folder.listFiles();

	}
	
public String[][] generateDistanceMatrix(LogUtilsRepeatingGraph log, double a) {
		log.graphSimilarity = new String[log.graphList.size()][log.graphList.size()];

		for(int i=0; i< log.graphList.size(); i++) {
			Graph graph1 = log.graphList.get(i);
			// j=0
			for(int j=i+1;j<log.graphList.size(); j++) {
				Graph graph2 = log.graphList.get(j);
				if(j==i)
					log.graphSimilarity[i][j] = "     ";
				else {
					if(log.graphSimilarity[i][j]==null) {
						GraphComparator comp = new GraphComparator();
						
						if(scoreChange) {
						comp.setEdgeEqualScore(edgeEqualScore);
						comp.setEdgeNotEqualScore(edgeNotEqualScore);
						comp.setEdgeSemiScore(edgeSemiScore);
						comp.setNodeEqualScore(nodeEqualScore);
						comp.setNodeNotEqualScore(nodeNotEqualScore);
						comp.setNodeSemiScore(nodeSemiScore);
						}
						
						
						comp.setGraph1(graph1);
						comp.setGraph2(graph2);
						Double metrics = (comp.getMetrics(a))*100;

						// trasformazione da SIMILARITY a DISSIMILARITY
						metrics = 100 - metrics;

						DecimalFormat df = new DecimalFormat("#.00");
						log.graphSimilarity[i][j] = String.valueOf(df.format(metrics));
						//log.graphSimilarity[j][i] = String.valueOf(df.format(metrics));
					}
				}
			}
		}

		// Adding the Header with LOG files names

		String [][] distanceMatrix = new String [log.graphSimilarity.length+1][log.graphSimilarity.length+1];

		for(int i=0;i<distanceMatrix.length;i++) {
			for(int j=0; j<distanceMatrix.length;j++) {
				if(j==i) {
					distanceMatrix[i][j] = " ";
				}else {
					if(distanceMatrix[i][j]==null) {
						if(i==0) {
							File f= log.fileList[j-1];
							String filename = f.getName();
							int extension = filename.lastIndexOf('.');
							String nameOnly = filename.substring(0, extension);
							distanceMatrix[i][j] = nameOnly;
							distanceMatrix[j][i] = nameOnly;
						}else {
							distanceMatrix[i][j] = log.graphSimilarity[i-1][j-1];
							distanceMatrix[j][i] = log.graphSimilarity[j-1][i-1];
						}
					}
				}
			}
		}
		return distanceMatrix;
	}


public double startMenu(Scanner tastiera) {
	
    String input = null;
    double a=(double)0.5;
    
    do {
    	System.out.println("Change the gamma value (default 0.5)? <<y>> or <<n>>");
    	input = tastiera.nextLine();
    }while((!input.equals("y")) && (!input.equals("n")));

    if(input.equals("y")) {
    	System.out.println("\u2705 "+"Please insert the value of Gamma in a range between 0.0 and 1.0"+" \u2705");
    	a = Double.valueOf(tastiera.nextLine());
    }

    input=null;
    do {
    	System.out.println("Change the Score settings? <<y>> or <<n>>");
    	input = tastiera.nextLine();
    }while((!input.equals("y")) && (!input.equals("n")));
    
    if(input.equals("y")) {
        this.scoreChange=true;
        
        double newScore= (double)1.0;
        System.out.println("\u2705 "+"Insert the Node_Equal score (default 1.0)"+" \u2705");
    	newScore = Double.valueOf(tastiera.nextLine());
        this.nodeEqualScore =newScore;
        
        newScore= (double)0.0;
        System.out.println("\u2705 "+"Insert the Node_NOT_Equal score (default 0.0)"+" \u2705");
    	newScore = Double.valueOf(tastiera.nextLine());
    	this.nodeNotEqualScore =newScore;
        
    	newScore= (double)1.0;
        System.out.println("\u2705 "+"Insert the Node_Semi_Equal score (default 1.0)"+" \u2705");
    	newScore = Double.valueOf(tastiera.nextLine());
    	this.nodeSemiScore =newScore;
    	
    	newScore= (double)1.0;
        System.out.println("\u2705 "+"Insert the Edge_Equal score (default 1.0)"+" \u2705");
    	newScore = Double.valueOf(tastiera.nextLine());
    	this.edgeEqualScore =newScore;
    	
    	newScore= (double)0.0;
        System.out.println("\u2705 "+"Insert the Edge_NOT_Equal score (default 0.0)"+" \u2705");
    	newScore = Double.valueOf(tastiera.nextLine());
    	this.edgeNotEqualScore =newScore;
    	
    	newScore= (double)0.5;
        System.out.println("\u2705 "+"Insert the Edge_Semi_Equal score (default 0.5)"+" \u2705");
    	newScore = Double.valueOf(tastiera.nextLine());
    	this.edgeSemiScore =newScore;
    }
    return a;
	
}

	public static void main(String[] args) {

		Locale.setDefault(Locale.US);
		System.out.println("Log evaluation - ");
		LogUtilsRepeatingGraph log=new LogUtilsRepeatingGraph();
		File[] files= log.selectFolder();
		log.fileList= files;
		Scanner tastiera = new Scanner(System.in);
		double a = log.startMenu(tastiera);
		
		//
		//				PrintStream fileOut;
		//				try {
		//					fileOut = new PrintStream("./ConsoleOutput.txt");
		//					System.setOut(fileOut);
		//				} catch (FileNotFoundException e) {
		//					e.printStackTrace();
		//				}
		//

		log.getTraces(files);
		
		String[][] distanceMatrix = log.generateDistanceMatrix(log, a);

		log.convertToCSV(distanceMatrix);

		System.out.println("Execution Time:" + String.valueOf(System.currentTimeMillis()-startingTime));

		
		System.out.println("Use the file \"DistanceGraph.csv\" in project directory to make clusters\n");
		
//		String input = null;
//		
//		
//		do {
//		System.out.println("Clusters Analysis - continue? <<y>> or <<n>>");
//		input = tastiera.nextLine();
//		}while((!input.equals("y")) && (!input.equals("n")));
//		
//		if(input.equals("n")) {
//		tastiera.close();
//		//END
//		}else {
//			
//			//robe
//			System.out.println(" ... ");
//			
//			tastiera.close();
//		}

	}
	
}