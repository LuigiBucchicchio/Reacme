package grafo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

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
	private Map<String,Set<ArrayList<String>>> modelsMap;
	private Map<String, ArrayList<String>> modelsActions;

	static double startingTime;

	public XLog parseXES(String filePath) throws Exception {
		XesXmlParser parser = new XesXmlParser();
		return parser.parse(new File(filePath)).get(0);	
	}

	public void getTraces(File[] listOfFiles) {
		startingTime=System.currentTimeMillis();
		modelsMap=new HashMap<String, Set<ArrayList<String>>>();
		modelsActions=new HashMap<String, ArrayList<String>>();


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
					modelsMap.put(file.getName(), traceSet);	

				}
				GraphLogAnalyzer analyzer = new GraphLogAnalyzer();
				analyzer.setTraceSet(traceList);
				analyzer.LogAnalyze();
				graphList.add(analyzer.getGraph());
				//analyzer.GraphImage("Log "+listOfFiles[i].getName()+" graph");
				
				//filePointer++;

				modelsActions.put(file.getName(), traceSet.stream().flatMap(x -> x.stream()).distinct().sorted().collect(Collectors.toCollection(ArrayList::new)));
				//System.out.println(modelsActions);
			} catch (Exception e) {
				e.printStackTrace();
			}

		} 
	}
	
	public void convertToCSV(String[][] data) {
		try {
			File csvFile = new File("DistanceGraph.csv");
			CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
//			 CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ';',
//                     CSVWriter.NO_QUOTE_CHARACTER,
//                     CSVWriter.DEFAULT_ESCAPE_CHARACTER,
//                     CSVWriter.DEFAULT_LINE_END);
			 
			for (String[] array : data) {
				writer.writeNext(array);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}
	
	public void print2D(String mat[][]) {
		// Loop through all rows
		System.out.println(" ");
		System.out.print("   ");
		for(int header =0; header<mat.length; header++) {
			System.out.print("  G"+(header+1)+"");
			for(int spacing=3; spacing>= String.valueOf(header+1).length(); spacing--) {
				System.out.print(" ");
			}
		}
		
		for (int i = 0; i < mat.length; i++) {
			System.out.print("\n");
			System.out.print("G"+(i+1)+"|");
			// Loop through all elements of current row
			for (int j = 0; j < mat[i].length; j++) {
				System.out.print(mat[i][j] + "| ");
			}
		}
		System.out.println(" \n");
	}

	
	public File[] selectFolder() {
		JFileChooser chooser = new JFileChooser(".");
		
		System.out.println("\u2705 " +"Please select the folder containing the XES Files" +" \u2705");

		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//		FileNameExtensionFilter filter = new FileNameExtensionFilter("XES", "xes");
		//		chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to open this folder: " + chooser.getSelectedFile().getName());
		}
		File folder= new File(chooser.getSelectedFile().getAbsolutePath());
		return folder.listFiles();

	}

	public static void main(String[] args) {
		
		Locale.setDefault(Locale.US);
		
		LogUtilsRepeatingGraph log=new LogUtilsRepeatingGraph();

		File[] files= log.selectFolder();
		log.fileList= files;
		System.out.println("\u2705 "+"Please insert the value of Gamma in a range between 0.0 and 1.0"+" \u2705");
		Scanner tastiera = new Scanner(System.in);
		double  a = Double.valueOf(tastiera.nextLine());
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
					comp.setGraph1(graph1);
					comp.setGraph2(graph2);
					Double metrics = (comp.getMetrics(a))*100;
					DecimalFormat df = new DecimalFormat("#.00");
					log.graphSimilarity[i][j] = String.valueOf(df.format(metrics));
					//log.graphSimilarity[j][i] = String.valueOf(df.format(metrics));
					}
				}
			}
		}

		//log.print2D(log.graphSimilarity);
		
		// Adding the Header with LOG files names
		
		String [][] similarity = new String [log.graphSimilarity.length+1][log.graphSimilarity.length+1];
		
		for(int i=0;i<similarity.length;i++) {
			for(int j=0; j<similarity.length;j++) {
				if(j==i) {
					similarity[i][j] = " ";
				}else {
					if(similarity[i][j]==null) {
						if(i==0) {
							File f= log.fileList[j-1];
							String filename = f.getName();
							int extension = filename.lastIndexOf('.');
							String nameOnly = filename.substring(0, extension);
							similarity[i][j] = nameOnly;
							similarity[j][i] = nameOnly;
						}else {
							similarity[i][j] = log.graphSimilarity[i-1][j-1];
							similarity[j][i] = log.graphSimilarity[j-1][i-1];
						}
					}
				}
			}
		}
		
		log.convertToCSV(similarity);

		System.out.println("Execution Time:" + String.valueOf(System.currentTimeMillis()-startingTime));
		tastiera.close();

	}
}