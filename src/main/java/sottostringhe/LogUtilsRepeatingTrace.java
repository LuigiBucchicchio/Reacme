package sottostringhe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JFileChooser;

import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import com.opencsv.CSVWriter;

public class LogUtilsRepeatingTrace {

	private List<Set<String>> listaMistica= new ArrayList<Set<String>>();
	private List<Set<String>> logsActivityList = new ArrayList<Set<String>>();
	private List<ArrayList<String>> traceLineSuperSet = new ArrayList<ArrayList<String>>();
	private List<List<String>> dominionLogList = new ArrayList<List<String>>();
	private List<List<List<RelationType>>> relationsLogList = new ArrayList<List<List<RelationType>>>();
	static int timesIndex=-1;
	static long startingTime;

	public XLog parseXES(String filePath) throws Exception {
		XesXmlParser parser = new XesXmlParser();
		return parser.parse(new File(filePath)).get(0);	
	}

	public void logMatrixConvertToCSV(String[][] data,File[] listOfFiles) {
		try {
			timesIndex++;
			File csvFile = new File("LogMatrix_"+listOfFiles[timesIndex].getName()+".csv");
			CSVWriter writer = new CSVWriter(new FileWriter(csvFile));
			for (String[] array : data) {
				writer.writeNext(array);
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}

	public void convertToCSV(String[][] data) {
		try {
			File csvFile = new File("DistanceMatrix.csv");
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

	public void getTraces(File[] listOfFiles) {
		startingTime=System.currentTimeMillis();


		int filePointer=0;
		for (int i = 0; i < listOfFiles.length; i++) {
			File file=listOfFiles[i];
			Set<ArrayList<String>> traceSet=new HashSet<ArrayList<String>>();
			ArrayList<String> traceLineSet=new ArrayList<String>();
			try {
				XLog xlog=parseXES(file.getAbsolutePath());
				//XLog xlog=parseXES("/home/cippus/Downloads/log (5).xes");

				System.out.println("\n*******************************************"
						+ "************************************\n"
						+ "******** Analizzando il log: "
						+listOfFiles[filePointer].getName()+
						" ********\n*******************************************"
						+ "************************************");

				Set<String> logRepeatingSet = new TreeSet<String>();
				Set<String> logActivitySet = new TreeSet<String>();

				for (XTrace xTrace : xlog) {
					ArrayList<String> trace=new ArrayList<String>();

					TraceAnalyzer analyzer = new TraceAnalyzer();
					StringBuffer traceLine=new StringBuffer("");
					// Trace = [t11, t45, t63, t12, t113, t9]
					// traceLine = t11t45t63t12t113t9

					for (XEvent xevent : xTrace) {
						String activity=xevent.getAttributes().get("concept:name").toString();
						
						// 'A' 'B' 'C' activities
//						StringBuffer activityT= new StringBuffer("");
//						activityT.append("t");
//						activityT.append(activity);
//						activity=activityT.toString();
						
						logActivitySet.add(activity);
						traceLine.append(activity);

						trace.add(activity);
					}


					//analyzer.setDelimiter('T');

					traceLineSet.add(traceLine.toString());

					analyzer.setTrace(traceLine.toString());
					analyzer.KMPAnalyze();
					Set<String> repeatingSet= analyzer.getRepeatingSet();

					Iterator<String> it= repeatingSet.iterator();
					while(it.hasNext()) {
						logRepeatingSet.add(it.next());
					}

					traceSet.add(trace);

				}
				filePointer++;
				listaMistica.add(logRepeatingSet);
				traceLineSuperSet.add(traceLineSet);
				logsActivityList.add(logActivitySet);

			} catch (Exception e) {
				e.printStackTrace();
			}

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

	public static void main(String[] args) {

		Thread.currentThread().setPriority(6);

		Locale.setDefault(Locale.US);
		System.out.println("Log evaluation - ");
		LogUtilsRepeatingTrace log=new LogUtilsRepeatingTrace();

		File[] files= log.selectFolder();

//		PrintStream fileOut;
//		try {
//			fileOut = new PrintStream("./ConsoleOutput.txt");
//			System.setOut(fileOut);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}

		System.out.println("Repeating set generation...");
		log.getTraces(files);

		log.printRepeatingSet(files);

		long repTime = System.currentTimeMillis()-startingTime;
		long minutes = (repTime / 1000) / 60;
		long seconds = (repTime / 1000) % 60;
		System.out.println("Repeating Set Generation Execution Time: " + minutes+" minutes and "+seconds+" seconds");
		System.out.println("Relations...");

		// (files, COLLAPSED, MAXREPEAT);
		log.generateRelations(files,true,true);

		long relTime = (System.currentTimeMillis()-startingTime)-repTime;
		minutes = (relTime / 1000) / 60;
		seconds = (relTime / 1000) % 60;

		System.out.println("Relations Generation Execution Time: " + minutes+" minutes and "+seconds+" seconds");
		System.out.println("Comparing...");
		String[][] distanceMatrix = log.generateBlankFileMatrix(files);

		for(int i=0;i< files.length;i++) {
			for(int j=i+1;j<files.length;j++) {
				LogMatrixComparator com = null;
				com = new LogMatrixComparator(log.dominionLogList.get(i),log.dominionLogList.get(j),
						log.relationsLogList.get(i),log.relationsLogList.get(j));

				double similarity = com.compare();
				//cambio da similarita' a distanza
				double distance = ((double)1.0) - similarity;
				//per cento
				distance=distance*100;
				System.out.println(files[i].getName()+" -> "+files[j].getName()+" score: "+String.valueOf(distance));

				distanceMatrix[i+1][j+1] = String.valueOf(distance);
			}
		}
		log.convertToCSV(distanceMatrix);

		long distanceGeneration = (System.currentTimeMillis()-startingTime)-relTime;
		minutes = (distanceGeneration / 1000) / 60;
		seconds = (distanceGeneration / 1000) % 60;
		System.out.println("Distance Matrix Generation Time:" + minutes+" minutes and "+seconds+" seconds");

		long finalTime = System.currentTimeMillis()-startingTime;
		minutes = (finalTime / 1000) / 60;
		seconds = (finalTime / 1000) % 60;
		System.out.println("Final Execution Time:" + minutes+" minutes and "+seconds+" seconds");

	}

	public String[][] generateBlankFileMatrix(File[] files) {

		String[][] blankFileMatrix = new String[files.length+1][files.length+1];
		blankFileMatrix[0][0]=" ";
		for(int i=1;i<blankFileMatrix.length;i++) {
			blankFileMatrix[i][0] = files[i-1].getName().replace(".", "");
			blankFileMatrix[0][i] = files[i-1].getName().replace(".", "");
		}

		return blankFileMatrix;
	}


	private void generateRelations(File[] files, boolean collapse, boolean maxRepeat) {

		for(int i=0;i<files.length;i++) {

			System.out.println("\n-----"
					+ " Relazioni nelle tracce del file : "+files[i].getName()+"-----");

			Set<String> repeatingSet1= listaMistica.get(i);
			Set<String> collapsed1 = null;
			Set<String> maxRepeat1 = null;

			if(collapse)
				collapsed1= collapseRepeatingSet(repeatingSet1);
			if(maxRepeat) {
				if(collapse)
					maxRepeat1 = generateMaxRepeat(collapsed1);
				else maxRepeat1= generateMaxRepeat(repeatingSet1);
			}

			Set<String> activitySet1 = logsActivityList.get(i);

			Set<String> dominion= new TreeSet<String>();

			Iterator<String> itR1 = repeatingSet1.iterator();
			if(collapse)
				itR1 = collapsed1.iterator();
			if(maxRepeat)
				itR1 = maxRepeat1.iterator();

			while(itR1.hasNext()) {
				dominion.add(itR1.next());
			}

			Iterator<String> itA1= activitySet1.iterator();
			while(itA1.hasNext()) {
				dominion.add(itA1.next());
			}

			List<String> dominionAsList = new ArrayList<String>(dominion);
			List<List<RelationType>> relationsList = new ArrayList<List<RelationType>>();

			String[][] LogMatrix = new String[dominion.size()+1][dominion.size()+1];
			LogMatrix[0][0]=" ";
			for(int p=0; p<dominionAsList.size(); p++) {
				LogMatrix[0][p+1] = dominionAsList.get(p);
				LogMatrix[p+1][0] = dominionAsList.get(p);
			}

			int counter =1;
			Iterator<String> dominionIt = dominion.iterator();
			while(dominionIt.hasNext()) {
				String activity = dominionIt.next();
				KeyRelationGenerator k = new KeyRelationGenerator(activity,dominionAsList);
				List<String> traces = traceLineSuperSet.get(i);
				Iterator<String> traceIterator = traces.iterator();
				while(traceIterator.hasNext()) {
					String line= traceIterator.next();
					k.iteration(line);
				}
				List<RelationType> rel = k.getRelations();
				relationsList.add(rel);

				//LogMatrix in csv

				for(int j=0; j<dominionAsList.size();j++) {

					if(rel.get(j)==null) {
						LogMatrix[counter][j+1] = "null";
					}else if(rel.get(j)==RelationType.precedeDirettamente) {
						LogMatrix[counter][j+1] = "pr";
					}else if(rel.get(j)==RelationType.segueDirettamente) {
						LogMatrix[counter][j+1] = "se";
					}else if(rel.get(j)==RelationType.precedeSegueDirettamente) {
						LogMatrix[counter][j+1] = "pr&se";
					}		
				}
				counter++;
			}

			logMatrixConvertToCSV(LogMatrix,files);

			dominionLogList.add(dominionAsList);
			relationsLogList.add(relationsList);

		}

	}

	public void printRepeatingSet(File[] files) {

		for(int i=0;i<files.length;i++) {
			System.out.println("\n--------------------------"
					+ " Repeating Set del file : "+files[i].getName()+"--------------------\n");
			System.out.print("[");
			Set<String> lista= listaMistica.get(i);
			Iterator<String> it=lista.iterator();
			while(it.hasNext()) {
				System.out.print(it.next());
				if(it.hasNext())
					System.out.print(", ");
			}
			System.out.println("]\n------------------------------------"
					+ "------------------------------\n");

			System.out.println("\n--------------------------"
					+ " Collapsed Set del file : "+files[i].getName()+"--------------------\n");
			System.out.print("[");

			Set<String> collapsed= collapseRepeatingSet(lista);
			it=collapsed.iterator();
			while(it.hasNext()) {
				System.out.print(it.next());
				if(it.hasNext())
					System.out.print(", ");
			}

			System.out.println("]\n------------------------------------"
					+ "------------------------------\n");

			System.out.println("\n--------------------------"
					+ " Max Repeat Set del file : "+files[i].getName()+"--------------------\n");
			System.out.print("[");

			Set<String> maxRepeat= generateMaxRepeat(collapsed);
			it=maxRepeat.iterator();
			while(it.hasNext()) {
				System.out.print(it.next());
				if(it.hasNext())
					System.out.print(", ");
			}

			System.out.println("]\n------------------------------------"
					+ "------------------------------\n");
		}

	}

	private Set<String> collapseRepeatingSet(Set<String> repeatingSet) {
		Set<String> collapsedRepeatingSet = new TreeSet<String>();
		String[] repeatingSetArray = repeatingSet.toArray( new String[repeatingSet.size()]);
		StringBuffer buffer= new StringBuffer();
		for(int i=0; i<repeatingSetArray.length; i++) {

			if(buffer.isEmpty())
				buffer.append(repeatingSetArray[i]);
			else {
				while(repeatingSetArray[i].startsWith(buffer.toString())) {
					buffer = new StringBuffer(repeatingSetArray[i]);
					i++;
					if(i>=repeatingSetArray.length)
						break;
				}
				collapsedRepeatingSet = decomposeBuffer(buffer.toString(),collapsedRepeatingSet);
				if(i>=repeatingSetArray.length)
					break;
				else
					buffer = new StringBuffer(repeatingSetArray[i]);
			}

		}
		if(!buffer.isEmpty())
			collapsedRepeatingSet = decomposeBuffer(buffer.toString(),collapsedRepeatingSet);

		return collapsedRepeatingSet;

	}

	private Set<String> decomposeBuffer(String maxRepeatingTrace, Set<String> collapsedRepeatingSet) {
		List<String> result = new ArrayList<String>();

		StringBuffer check=new StringBuffer("");
		int checkpoint=0;
		for(int i=0;i<maxRepeatingTrace.length();i++) {
			i=checkpoint;
			if((i+1)<maxRepeatingTrace.length()) {
				int tcount=0;
				int j;
				for(j=i;j<maxRepeatingTrace.length();j++) {
					if(maxRepeatingTrace.charAt(j)=='t') { 
						if(tcount==1)
							checkpoint=j;
						tcount++;
						if(tcount>1)
							break;
					}
					check.append(maxRepeatingTrace.charAt(j));
				}
				//to exit
				i=j-1;
				if(check.length()==0||!result.contains(check.toString())) {
					if(check.length()!=0)
						result.add(check.toString());
				}else break;
			}
			check.delete(0, check.length());
		}

		StringBuffer s= new StringBuffer();
		Iterator<String> it = result.iterator();
		while(it.hasNext()) {
			s.append(it.next());
			if(!collapsedRepeatingSet.contains(s.toString()))
				collapsedRepeatingSet.add(s.toString());
		}
		return collapsedRepeatingSet;
	}

	private Set<String> generateMaxRepeat(Set<String> repeatingSet){
		Set<String> maxRepeat = new TreeSet<String>();
		Iterator<String> it = repeatingSet.iterator();
		StringBuffer s= new StringBuffer();
		while(it.hasNext()) {
			if(s.isEmpty())
				s.append(it.next());
			else {
				String stringa = it.next();
				if(stringa.startsWith(s.toString()))
					s= new StringBuffer(stringa);
				else {
					maxRepeat.add(s.toString());
					s= new StringBuffer(stringa);
				}
			}
		}
		if(!s.isEmpty())
			maxRepeat.add(s.toString());

		return maxRepeat;
	}

}
