package grafo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import javax.swing.JFileChooser;

import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import com.opencsv.CSVWriter;

/**
 * Classe che permette di analizzare e confrontare files di Log (XES). <br>
 * Vengono creati dei Grafi riconducibili ad una lista di Nodi/attività e una lista di Archi/Transizioni <br>
 * Nodi e Archi possono essere Repeating o Not Repeating (ripetendosi più di una volta per traccia o meno) <br>
 * Ogni grafo rappresenta un Log, riassumendo l'analisi di tutte le tracce. <br>
 * I grafi generati vengono così comparati e come output viene data una DistanceMatrix in CSV. <br>
 * <br>
 * per funzionare, dopo aver creato un Oggetto di tipo LogUtilsRepeatingGraph, va eseguito il main <br>
 * oppure vanno eseguiti i seguenti passaggi: <br>
 * <br>
 * 1) selectFolder() o in alternativa setFileList() <br>
 * <br>
 * setTraceNum() (opzionale) <br>
 * setAvgTraceLen() (opzionale) <br>
 * <br>
 * 2) startMenu() o in alternativa importare setScoreChange(true) oppure setScoreChange(false). Nel primo caso impostare anche: <br>
 * setGamma() <br>
 * setNodeEqualsScore() <br>
 * setNodeNotEqualScore() <br>
 * setNodeSemiScore() <br>
 * setEdgeEqualsScore() <br>
 * setEdgeNotEqualsScore() <br>
 * setEdgeSemiScore() <br>
 * <br>
 * 3) analyzeTraces() <br> 
 * 4) generateDistanceMatrix() <br>
 * 5) convertToCSV() <br>
 * @author luigi.bucchicchioAtgmail.com
 * 
 */
public class LogUtilsRepeatingGraph {

	private File[] fileList;
	private int[] traceNum;
	private double[] avgTraceLen;
	private List<Graph> graphList = new ArrayList<Graph>();
	private String[][] graphDissimilarity;
	private String outputFileName= new String("");
	
	//scores
	private boolean scoreChange=false;
	private double edgeEqualScore= (double) 1.0;
	private double edgeSemiScore= (double) 0.0;
	private double edgeNotEqualScore = (double) 0.0;
	private double nodeEqualScore= (double) 1.0;
	private double nodeSemiScore= (double) 0.0;
	private double nodeNotEqualScore = (double) 0.0;
	private double gamma = (double)0.0;
	
	static double startingTime;
	
	/**
	 * XES parser
	 * @param filePath 
	 * @return XLog structured file
	 * @throws Exception
	 */
	public XLog parseXES(String filePath) throws Exception {
		XesXmlParser parser = new XesXmlParser();
		return parser.parse(new File(filePath)).get(0);	
	}

	/**
	 *  L'algoritmo analizza le tracce di ogni singolo XES, costruendosi una lista di Grafi che rappesentano ognuno un Log. Click per dettagli  <br>
	 * <br>
	 * le tracce vengono lette in due diversi modi:<br>
	 * come TraceLine, in una singola stringa: es. "ABBBABBABABA" "a1a2a3a4a5a1a2a3" "activity1activity2activity3"<br>
	 * come Sequenza di attività in una lista: es. "[ A,B,B,B,.....] [a1,a2,a3...] [activity1,activity2....]<br>
	 * sebbene si utilizzi prettamente il secondo metodo di lettura, alcuni algortimi sulle stringhe possono richiedere la TraceLine<br>
	 * <br>
	 * Info aggiuntive come il log di provenienza e l'id della traccia vengono salvate anche se non vengono usate<br>
	 * Tutte le info vengono salvate mediante una classe di supporto, Trace, che confluisce in una traceList che viene passata all'analyzer<br>
	 * l'analyzer eseguirà LogAnalyze() creando un Graph, ottenibile con getGraph() e aggiunto in una lista di Grafi.<br>
	 * @author luigi.bucchicchioAtgmail.com
	 * 
	 */
	public void analyzeTraces() {
		startingTime=System.currentTimeMillis();

		for (int i = 0; i < fileList.length; i++) {
			File file=fileList[i];

			List<Trace> traceList = new ArrayList<Trace>();

			try {
				XLog xlog=parseXES(file.getAbsolutePath());
				//da cippus lives
				//XLog xlog=parseXES("/home/cippus/Downloads/log (5).xes");

				for (XTrace xTrace : xlog) {

					ArrayList<String> activitySequence=new ArrayList<String>();

					StringBuffer traceLine=new StringBuffer("");
					// activitySequence = [t11, t45, t63, t12, t113, t9]
					// traceLine = t11t45t63t12t113t9

					for (XEvent xevent : xTrace) {

						
						String activity=xevent.getAttributes().get("concept:name").toString();
//						activity = activity.substring(0, 3);

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
				
				// Useful Info, can be used in combo with "consoleOutToFile()" (see main comment)
				
//				System.out.println("--------------------------- Node list of log graph:"+fileList[i].getName()+""
//						+ "---------------------------");
//				analyzer.printNodeSet();
//				System.out.println("--------------------------- Edge list of log graph:"+fileList[i].getName()+""
//						+ "---------------------------");
//				analyzer.printEdgeSet();
//				System.out.println("---------------------------"
//						+ "---------------------------\n");
				
				// GraphImage has Heavy RAM usage-> (use with nested folder + batch).equals("YOUR PC GOT NUKED") returned "true".
				
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

	/**
	 * In output, il CSV con il nome formattato come segue:<br>
	 * DistanceGraph_nomeCartella_numeroFilesLogs_gamma_parametriNodiOAttività_parametriArchiOTransizioni.csv
	 * @author luigi.bucchicchioAtgmail.com
	 * @param data La distanceMatrix
	 * 
	 */
	public void convertToCSV(String[][] data) {
		int nLog = fileList.length;
		String directoryName = fileList[0].getParentFile().getName();
		
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
		
		// TUTT' STUBBURDELL' PE' RINOMINA' L'OUTPUT
		
		if(getOutputFileName().equals(""))
		setOutputFileName("DistanceGraph_"+directoryName+"_"+nLog+"Logs_gamma"+Sgamma+"_"+s1+s2+s3+"_"+s4+s5+s6+".csv");
		
		try {
			//cartellina
			File f = new File("output");
			f.mkdir();
			File csvFile = new File(f.getAbsolutePath()+"\\"+getOutputFileName());
			//scriba, pls
			CSVWriter writer = new CSVWriter(new FileWriter(csvFile));

			for (String[] array : data) {
				writer.writeNext(array);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}

	/**
	 * Selettore di cartella
	 * @author luigi.bucchicchioAtgmail.com
	 * 
	 */
	public void selectFolder() {
		JFileChooser chooser = new JFileChooser(".");

		System.out.println("\u2705 " +"Please select the folder containing the XES Files" +" \u2705");

		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to open this folder: " + chooser.getSelectedFile().getName());
		}
		
		// Ce manca l'else con scritto "TI SI SBAGLIATO, FRA?"
		
		File folder= new File(chooser.getSelectedFile().getAbsolutePath());
		
		this.fileList= folder.listFiles();
		int x = fileList.length;
		setTraceNum(new int[x]);
		setAvgTraceLen(new double[x]);

	}
	
	/**
	 *  Da invocare dopo analyzeTraces(), utilizza la lista di Grafi (ognuno rappresentante un Log) per generare la distance Matrix. Click per dettagli <br>
	 * Il metodo utilizza la classe GraphComparator per generare la differenza tra due Grafi (due Log).<br>
	 * Il punteggio risultante è la Similarità in percentuale, poi convertita in Dissimilarità o Distanza.<br>
	 * Viene eseguito a due a due sulla lista di Grafi.<br>
	 * @author luigi.bucchicchioAtgmail.com
	 * @return la Dissimilarity/Distance Matrix
	 * @throws RuntimeException
	 * 
	 */
public String[][] generateDistanceMatrix() throws RuntimeException {
	
        if(graphList.size()==0) {
        	throw new RuntimeException("invalid procedure: No Graph Found");
        }
        
		graphDissimilarity = new String[graphList.size()][graphList.size()];

		for(int i=0; i< graphList.size(); i++) {
			Graph graph1 = graphList.get(i);
			// j=0
			for(int j=0;j<graphList.size(); j++) {
				Graph graph2 = graphList.get(j);
				if(j==i)
					graphDissimilarity[i][j] = "0.0";
				else {
					if(graphDissimilarity[i][j]==null) {
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
						// trasformazione da SIMILARITY a DISSIMILARITY/DISTANCE
						
						metrics = 100 - metrics;

						DecimalFormat df = new DecimalFormat("#.00");
						graphDissimilarity[i][j] = String.valueOf(df.format(metrics));
						graphDissimilarity[j][i] = String.valueOf(df.format(metrics));
					}
				}
			}
		}

		// Adding the Header with LOG files names

		String [][] distanceMatrix = new String [graphDissimilarity.length+1][graphDissimilarity.length+1];

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
							distanceMatrix[i][j] = graphDissimilarity[i-1][j-1];
							distanceMatrix[j][i] = graphDissimilarity[j-1][i-1];
						}
					}
				}
			}
		}
		distanceMatrix[0][0]=" ";
		return distanceMatrix;
	}

/**
 * il metodo chiede se si vogliono cambiare i parametri o tenere quelli di default, considerando che nel confronto esistono: Click per dettagli <br>
 * Nodi/Attività o Archi/Transizioni NotRepeating (che appaiono una volta sola per singola traccia) A <br>
 * Nodi/Attività o Archi/Transizioni Repeating (cha appaiono più di una volta per singola traccia) A_R <br>
 * i parametri sono: <br>
 * Gamma, ovvero il peso tra archi/transizioni e Nodi/Attività, che va da 0.0 (solo archi) a 1.0 (solo nodi) <br>
 * Equal score, ovvero il punteggio che si vuole dare tra Nodi e Archi Uguali tra loro (es. A con A = 1.0; A_R con A_R = 1.0) <br>
 * Not Equal score, ovvero il punteggio che si vuole dare tra Nodi e Archi Disuguali(presente/mancante) tra loro (es A con null = 0.0) <br>
 * Repeating score, ovvero il putneggio che si vuole dare quando, nel confronto, uno è Repeating e l'altro è NotRepeating (es. A con A_R = 0.5) <br>
 * @author luigi.bucchicchioAtgmail.com
 * @param tastiera Lo Scanner, generalmente con System.in
 * */
public void startMenu(Scanner tastiera) {
	
    String input = null;
    double a=(double)0.0;
    
    do {
    	System.out.println("Change the gamma value (default "+gamma+")? <<y>> or <<n>>");
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
        System.out.println("\u2705 "+"Insert the Node_Equal score (default "+nodeEqualScore+")"+" \u2705");
    	newScore = Double.valueOf(tastiera.nextLine());
        this.nodeEqualScore =newScore;
        
        newScore= (double)0.0;
        System.out.println("\u2705 "+"Insert the Node_NOT_Equal score (default "+nodeNotEqualScore+")"+" \u2705");
    	newScore = Double.valueOf(tastiera.nextLine());
    	this.nodeNotEqualScore =newScore;
        
    	newScore= (double)0.0;
        System.out.println("\u2705 "+"Insert the Node_Semi_Equal score (default "+nodeSemiScore+")"+" \u2705");
    	newScore = Double.valueOf(tastiera.nextLine());
    	this.nodeSemiScore =newScore;
    	
    	newScore= (double)1.0;
        System.out.println("\u2705 "+"Insert the Edge_Equal score (default "+edgeEqualScore+")"+" \u2705");
    	newScore = Double.valueOf(tastiera.nextLine());
    	this.edgeEqualScore =newScore;
    	
    	newScore= (double)0.0;
        System.out.println("\u2705 "+"Insert the Edge_NOT_Equal score (default "+edgeNotEqualScore+")"+" \u2705");
    	newScore = Double.valueOf(tastiera.nextLine());
    	this.edgeNotEqualScore =newScore;
    	
    	newScore= (double)0.0;
        System.out.println("\u2705 "+"Insert the Edge_Semi_Equal score (default "+edgeSemiScore+")"+" \u2705");
    	newScore = Double.valueOf(tastiera.nextLine());
    	this.edgeSemiScore =newScore;
    }
    this.gamma=a;
	
}

/**
 * L'algoritmo esegue: Clicca per dettagli <br>
 * selectFolder() per prendere la cartella degli XES <br>
 * startMenu() per impostare i parametri <br>
 * analyzeTraces() per analizzare le tracce e salvare i rispettivi Grafi (ovvero Set di Nodi/Attività e Set di Archi/Transizioni) per ogni Log. <br>
 * generateDistanceMatrix() per generare una distanceMatrix con l'aiuto di GraphComparator <br>
 * convertToCSV() per l'output <br>
 * @author luigi.bucchicchioAtgmail.com
 * 
 * @param args
 */
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
		
		System.out.println("Use the file "+log.getOutputFileName()+" in output directory to make clusters\n");
//	
		
		log.generateNodeListReport("CUSTOM");

	}
	
	/**
	 * Metodo per impostare L'output della console su file .TXT
	 * @author luigi.bucchicchioAtgmail.com
	 * 
	 */
	public void consoleOutToFile() {
		PrintStream fileOut;
		try {
			fileOut = new PrintStream("./ConsoleOutput.txt");
			System.setOut(fileOut);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stringa del nome del file di output
	 * @return output file's name
	 */
	public String getOutputFileName() {
		return outputFileName;
	}

	/**
	 * Settaggio del nome del file di output
	 * @param outputFileName
	 */
	public void setOutputFileName(String outputFileName) {
		this.outputFileName = outputFileName;
	}

	/**
	 * array dei file XES
	 * @author luigi.bucchicchioAtgmail.com 
	 * @return the array of XES files
	 */
	public File[] getFileList() {
		return fileList;
	}

	/**
	 * settaggio array di file XES
	 * @author luigi.bucchicchioAtgmail.com 
	 * @param fileList the list of XES files, expecting a File[] 
	 */
	public void setFileList(File[] fileList) {
		this.fileList = fileList;
	}

	/**
	 * settaggio valore booleano per punteggio custom
	 * @author luigi.bucchicchioAtgmail.com 
	 * @return true is the score is custom, false to defaults
	 */
	public boolean isScoreChange() {
		return scoreChange;
	}

	/**
	 * valore booleano per punteggio custom
	 * @author luigi.bucchicchioAtgmail.com 
	 * @param scoreChange set True if the score is Custom, false to defaults
	 */
	public void setScoreChange(boolean scoreChange) {
		this.scoreChange = scoreChange;
	}

	/**
	 * punteggio equals tra archi
	 * @author luigi.bucchicchioAtgmail.com 
	 * @return the score used comparing two edges both repeating or both notRepeating (A_>B with A_>B) def. 1.0
	 */
	public double getEdgeEqualScore() {
		return edgeEqualScore;
	}

	/**
	 * settaggio punteggio equals tra archi
	 * @author luigi.bucchicchioAtgmail.com 
	 * @param edgeEqualScore the score used comparing two edges both repeating or both notRepeating (A_>B with A_>B) def. 1.0
	 */
	public void setEdgeEqualScore(double edgeEqualScore) {
		this.edgeEqualScore = edgeEqualScore;
	}

	/**
	 * punteggio semiEquals tra archi
	 * @author luigi.bucchicchioAtgmail.com 
	 * @return the score used comparing two edges, one repeating and one notRepeating (A_>B,R with A_>B) def. 0.5
	 */
	public double getEdgeSemiScore() {
		return edgeSemiScore;
	}

	/**
	 * settaggio punteggio semiEquals tra archi
	 * @author luigi.bucchicchioAtgmail.com 
	 * @param edgeSemiScore the score used comparing two edges, one repeating and one notRepeating (A_>B,R with A_>B) def. 0.5
	 */
	public void setEdgeSemiScore(double edgeSemiScore) {
		this.edgeSemiScore = edgeSemiScore;
	}

	/**
	 * punteggio disuguali tra archi
	 * @author luigi.bucchicchioAtgmail.com 
	 * @return the score used comparing two edges, one existing with one not existing (A_>B with null) def 0.0
	 */
	public double getEdgeNotEqualScore() {
		return edgeNotEqualScore;
	}

	/**
	 * settaggio punteggio disuguali tra archi
	 * @author luigi.bucchicchioAtgmail.com 
	 * @param edgeNotEqualScore the score used comparing two edges, one existing with one not existing (A_>B with null) def 0.0
	 */
	public void setEdgeNotEqualScore(double edgeNotEqualScore) {
		this.edgeNotEqualScore = edgeNotEqualScore;
	}

	/**
	 * punteggio equals tra nodi
	 * @author luigi.bucchicchioAtgmail.com 
	 * @return the nodeEqualScore() the score used comparing two nodes, both reapeating or both notRepeating (es. A with A) def. 1.0
	 */
	public double getNodeEqualScore() {
		return nodeEqualScore;
	}

	/**
	 * settaggio punteggio equals tra nodi
	 * @author luigi.bucchicchioAtgmail.com 
	 * @param nodeEqualScore the score used comparing two nodes, both reapeating or both notRepeating (es. A with A) def. 1.0
	 */
	public void setNodeEqualScore(double nodeEqualScore) {
		this.nodeEqualScore = nodeEqualScore;
	}

	/**
	 * punteggio semiEquals tra nodi
	 * @author luigi.bucchicchioAtgmail.com 
	 * @return the node SemiScore the score used comparing two nodes, one repeating and one notRepeating (es. A with A_R) def. 0.5
	 */
	public double getNodeSemiScore() {
		return nodeSemiScore;
	}
	
	/**
	 * settaggio punteggio semiEquals tra nodi
	 * @author luigi.bucchicchioAtgmail.com
	 * @param nodeSemiScore the score used comparing two nodes, one repeating and one notRepeating (es. A with A_R) def. 0.5
	 */
	public void setNodeSemiScore(double nodeSemiScore) {
		this.nodeSemiScore = nodeSemiScore;
	}

	/**
	 * punteggio disuguali tra nodi
	 * @author luigi.bucchicchioAtgmail.com 
	 * @return the node notEqualsScore the score used comparing two nodes, one existing and one not existing (es. A with null) def. 0.0
	 */
	public double getNodeNotEqualScore() {
		return nodeNotEqualScore;
	}

	/**
	 * settaggio punteggio diseuguali tra nodi
	 * @author luigi.bucchicchioAtgmail.com
	 * @param nodeNotEqualScore the score used comparing two nodes, one existing and one not existing (es. A with null) def. 0.0
	 */
	public void setNodeNotEqualScore(double nodeNotEqualScore) {
		this.nodeNotEqualScore = nodeNotEqualScore;
	}

	/**
	 * parametro gamma
	 * @author luigi.bucchicchioAtgmail.com
	 * @return the gamma value, should be between 0.0 and 1.0
	 */
	public double getGamma() {
		return gamma;
	}

	/**
	 * settaggio parametro gamma
	 * @author luigi.bucchicchioAtgmail.com
	 * @param gamma between 0.0 and 1.0, representing the weigth between the Edges/Transitions (0.0 edges only) and the Nodes/Activities (1.0 nodes only) 
	 */
	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	public double getStartingTime() {
		return startingTime;
	}

	public static void setStartingTime(double startingTime) {
		LogUtilsRepeatingGraph.startingTime = startingTime;
	}

	/**
	 * Lista dei grafi (rappresentano uno XES ognuno)
	 * @author luigi.bucchicchioAtgmail.com
	 * @return a List of Graphs, each generated from a XES file.
	 */
	public List<Graph> getGraphList() {
		return graphList;
	}
	
	/**
	 * Set della Lista dei grafi (se già computata)
	 * @author luigi.bucchicchioAtgmail.com
	 * @param graphList, List of type Graph
	 */
	public void setGraphList(List<Graph> graphList) {
		this.graphList = graphList;
	}

	/**
	 * DissimilarityMatrix (versione non formattata e non completa della distance matrix)
	 * @author luigi.bucchicchioAtgmail.com
	 * @return the Dissimilarity Matrix, in String[][] format.
	 */
	public String[][] getGraphDissimilarity() {
		return graphDissimilarity;
	}

	/**
	 * array del numero di tracce dei file XES
	 * @author luigi.bucchicchioAtgmail.com
	 * @return the integer array representing the number of traces for each xes file
	 */
	public int[] getTraceNum() {
		return traceNum;
	}

	/**
	 * array della dimensione media delle tracce dei file XES
	 * @author luigi.bucchicchioAtgmail.com
	 * @return the double array representing the average trace length for each xes file
	 */
	public double[] getAvgTraceLen() {
		return avgTraceLen;
	}

	/**
	 * Set Average Trace Length for each XES file.
	 * @author luigi.bucchicchioAtgmail.com
	 * 
	 * @param avgTraceLen in the setup, Expecting (new double[x]) where x is the number of XES files. Require an already defined double[x] otherwise
	 */
	public void setAvgTraceLen(double[] avgTraceLen) {
		this.avgTraceLen = avgTraceLen;
	}

	/**
	 * Set Number of Traces for each XES file.
	 * @author luigi.bucchicchioAtgmail.com
	 * 
	 * @param traceNum in the setup, Expecting (new int[x]) where x is the number of XES files. Require an already defined int[x] otherwise
	 */
	public void setTraceNum(int[] traceNum) {
		this.traceNum = traceNum;
	}
	
	public void generateNodeListReport(String etichettaDataset) {
		
		List<String[]> rows = new ArrayList<String[]>();
		
		List<Graph> graphsList = getGraphList();
		Iterator<Graph> graphIterator = graphsList.iterator();
		int index = 0;
		while(graphIterator.hasNext()) {
			Graph g = graphIterator.next();
			Iterator<Node> nodeList = g.nodes().iterator();
			while(nodeList.hasNext()) {
				Node n = nodeList.next();
				String id = n.getId();
				String repeatingString = (String) n.getAttribute("ui.label");
				boolean isRepeating = false;
				if(repeatingString.length()>1){
					if(repeatingString.charAt(1)=='_')
						isRepeating=true;
				}
				String logName = getFileList()[index].getName();
				String[] row = new String[6];
				row[0] = id;
				row[1] = logName;
				row[2] = etichettaDataset;
				row[3] = "null";
				if(isRepeating)
					row[4] = String.valueOf(1);
				else
					row[4] = String.valueOf(0);
				row[5] = "null";
				rows.add(row);
			}
			index++;
		}
		
		try {
			//cartellina
			File f = new File("output");
			f.mkdir();
			File csvFile = new File(f.getAbsolutePath()+"\\"+"select_all_from_attivita.csv");
			//scriba, pls
			CSVWriter writer = new CSVWriter(new FileWriter(csvFile));

			for (String[] array : rows) {
				writer.writeNext(array);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
	}
	
	
}