package grafo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import javax.swing.JFileChooser;

import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.graphstream.graph.Graph;

import com.opencsv.CSVWriter;


public class LogUtilsRepeatingGraph {

	private File[] fileList;
	private int[] traceNum;
	private double[] avgTraceLen;
	private List<Graph> graphList = new ArrayList<Graph>();
	private String[][] graphSimilarity;
	private String outputFileName= new String("");
	
	//scores
	private boolean scoreChange=false;
	private double edgeEqualScore= (double) 1.0;
	private double edgeSemiScore= (double) 0.5;
	private double edgeNotEqualScore = (double) 0.0;
	private double nodeEqualScore= (double) 1.0;
	private double nodeSemiScore= (double) 1.0;
	private double nodeNotEqualScore = (double) 0.0;
	private double gamma = (double)0.5;
	
	static double startingTime;
	
	public XLog parseXES(String filePath) throws Exception {
		XesXmlParser parser = new XesXmlParser();
		return parser.parse(new File(filePath)).get(0);	
	}

	public void analyzeTraces() {
		startingTime=System.currentTimeMillis();

		for (int i = 0; i < fileList.length; i++) {
			File file=fileList[i];

			List<Trace> traceList = new ArrayList<Trace>();

			try {
				XLog xlog=parseXES(file.getAbsolutePath());
				//XLog xlog=parseXES("/home/cippus/Downloads/log (5).xes");

				for (XTrace xTrace : xlog) {

					ArrayList<String> activitySequence=new ArrayList<String>();

					StringBuffer traceLine=new StringBuffer("");
					// activitySequence = [t11, t45, t63, t12, t113, t9]
					// traceLine = t11t45t63t12t113t9

					for (XEvent xevent : xTrace) {

						String activity=xevent.getAttributes().get("concept:name").toString();

						traceLine.append(activity);

						activitySequence.add(activity);

					}

					Trace genericTrace = new Trace();
					genericTrace.setTraceLine(traceLine.toString());
					genericTrace.setActivitySequence(activitySequence);
					genericTrace.setLogId(fileList[i].getName());
					genericTrace.setTraceId(xTrace.getAttributes().get("concept:name").toString());
					traceList.add(genericTrace);
					//analyzer.setTrace(traceLine.toString());
				}

				GraphLogAnalyzer analyzer = new GraphLogAnalyzer();
				analyzer.setTraceSet(traceList);
				analyzer.LogAnalyze();
				graphList.add(analyzer.getGraph());
				
//				System.out.println("--------------------------- Node list of log graph:"+fileList[i].getName()+""
//						+ "---------------------------");
//				analyzer.printNodeSet();
//				System.out.println("--------------------------- Edge list of log graph:"+fileList[i].getName()+""
//						+ "---------------------------");
//				analyzer.printEdgeSet();
//				System.out.println("---------------------------"
//						+ "---------------------------\n");
//				
//				analyzer.GraphImage("Log "+fileList[i].getName()+" graph");
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			int numeroTracce = traceList.size();
			int tot=0;
			for(int l=0;l<traceList.size();l++) {
				tot=tot+traceList.get(l).getTraceLength();
			}
			getAvgTraceLen()[i] = tot/numeroTracce;
			getTraceNum()[i]=numeroTracce;
		} 
	}

	public void convertToCSV(String[][] data) {
		int logNumber=fileList.length;
		
		String Sgamma= String.valueOf(gamma);
		Sgamma=Sgamma.replace(".0", "");
		Sgamma=Sgamma.replace(".", "");
		String s1= String.valueOf(nodeEqualScore);
		s1=s1.replace(".0", "");
		s1=s1.replace(".", "");
		String s2= String.valueOf(nodeNotEqualScore);
		s2=s2.replace(".0", "");
		s2=s2.replace(".", "");
		String s3= String.valueOf(nodeSemiScore);
		s3=s3.replace(".0", "");
		s3=s3.replace(".", "");
		String s4= String.valueOf(edgeEqualScore);
		s4=s4.replace(".0", "");
		s4=s4.replace(".", "");
		String s5= String.valueOf(edgeNotEqualScore);
		s5=s5.replace(".0", "");
		s5=s5.replace(".", "");
		String s6= String.valueOf(edgeSemiScore);
		s6=s6.replace(".0", "");
		s6=s6.replace(".", "");
		
		setOutputFileName("DistanceGraph_"+logNumber+"Logs_gamma"+Sgamma+"_"+s1+s2+s3+"_"+s4+s5+s6+".csv");
		
		try {
			
			File csvFile = new File(getOutputFileName());
			CSVWriter writer = new CSVWriter(new FileWriter(csvFile));

			for (String[] array : data) {
				writer.writeNext(array);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}

	public void selectFolder() {
		JFileChooser chooser = new JFileChooser(".");

		System.out.println("\u2705 " +"Please select the folder containing the XES Files" +" \u2705");

		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to open this folder: " + chooser.getSelectedFile().getName());
		}
		File folder= new File(chooser.getSelectedFile().getAbsolutePath());
		
		this.fileList= folder.listFiles();
		int x = fileList.length;
		setTraceNum(new int[x]);
		setAvgTraceLen(new double[x]);

	}
	
public String[][] generateDistanceMatrix() throws RuntimeException {
	
        if(graphList.size()==0) {
        	throw new RuntimeException("invalid procedure: No Graph Found");
        }
        
		graphSimilarity = new String[graphList.size()][graphList.size()];

		for(int i=0; i< graphList.size(); i++) {
			Graph graph1 = graphList.get(i);
			// j=0
			for(int j=0;j<graphList.size(); j++) {
				Graph graph2 = graphList.get(j);
				if(j==i)
					graphSimilarity[i][j] = "0.0";
				else {
					if(graphSimilarity[i][j]==null) {
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
						comp.setLogUtilsGamma(gamma);
						Double metrics = (comp.getMetrics(gamma))*100;

						// trasformazione da SIMILARITY a DISSIMILARITY
						
						metrics = 100 - metrics;

						DecimalFormat df = new DecimalFormat("#.00");
						graphSimilarity[i][j] = String.valueOf(df.format(metrics));
						graphSimilarity[j][i] = String.valueOf(df.format(metrics));
					}
				}
			}
		}

		// Adding the Header with LOG files names

		String [][] distanceMatrix = new String [graphSimilarity.length+1][graphSimilarity.length+1];

		for(int i=0;i<distanceMatrix.length;i++) {
			for(int j=0; j<distanceMatrix.length;j++) {
				if(j==i) {
					distanceMatrix[i][j] = "0.0";
				}else {
					if(distanceMatrix[i][j]==null) {
						if(i==0) {
							File f= fileList[j-1];
							String filename = f.getName();
							int extension = filename.lastIndexOf('.');
							String nameOnly = filename.substring(0, extension);
							distanceMatrix[i][j] = nameOnly;
							distanceMatrix[j][i] = nameOnly;
						}else {
							distanceMatrix[i][j] = graphSimilarity[i-1][j-1];
							distanceMatrix[j][i] = graphSimilarity[j-1][i-1];
						}
					}
				}
			}
		}
		distanceMatrix[0][0]=" ";
		return distanceMatrix;
	}


public void startMenu(Scanner tastiera) {
	
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
    this.gamma=a;
	
}

	public static void main(String[] args) {

		Locale.setDefault(Locale.US);
		System.out.println("Log evaluation - ");
		LogUtilsRepeatingGraph log=new LogUtilsRepeatingGraph();
		log.selectFolder();
		Scanner tastiera = new Scanner(System.in);
		log.startMenu(tastiera);
		
		//log.consoleOutToFile();
		
		log.analyzeTraces();
		
		String[][] distanceMatrix = log.generateDistanceMatrix();

		log.convertToCSV(distanceMatrix);

		System.out.println("Execution Time:" + String.valueOf(System.currentTimeMillis()-startingTime));
		
		System.out.println("Use the file "+log.getOutputFileName()+" in project directory to make clusters\n");
//		
//		DatabaseConnection dc= new DatabaseConnection(log);
//		dc.insertAll("B", "Modelli validazione con varianti, non perturbati");

	}
	
	public void consoleOutToFile() {
		PrintStream fileOut;
		try {
			fileOut = new PrintStream("./ConsoleOutput.txt");
			System.setOut(fileOut);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public String getOutputFileName() {
		return outputFileName;
	}

	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	public File[] getFileList() {
		return fileList;
	}

	public void setFileList(File[] fileList) {
		this.fileList = fileList;
	}

	public boolean isScoreChange() {
		return scoreChange;
	}

	public void setScoreChange(boolean scoreChange) {
		this.scoreChange = scoreChange;
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

	public double getStartingTime() {
		return startingTime;
	}

	public static void setStartingTime(double startingTime) {
		LogUtilsRepeatingGraph.startingTime = startingTime;
	}

	public List<Graph> getGraphList() {
		return graphList;
	}

	public String[][] getGraphSimilarity() {
		return graphSimilarity;
	}

	public int[] getTraceNum() {
		return traceNum;
	}

	public double[] getAvgTraceLen() {
		return avgTraceLen;
	}

	public void setAvgTraceLen(double[] avgTraceLen) {
		this.avgTraceLen = avgTraceLen;
	}

	public void setTraceNum(int[] traceNum) {
		this.traceNum = traceNum;
	}
	
	
	
}