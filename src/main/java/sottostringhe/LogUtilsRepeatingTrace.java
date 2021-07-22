package sottostringhe;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;

import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class LogUtilsRepeatingTrace {
	private Map<String,Set<ArrayList<String>>> modelsMap;
	private Map<String, ArrayList<String>> modelsActions;

	private List<Set<String>> listaMistica= new ArrayList<Set<String>>();

	static double startingTime;
	public static final String ANSI_GREEN = "\u001B[32m";

	public XLog parseXES(String filePath) throws Exception {
		XesXmlParser parser = new XesXmlParser();
		return parser.parse(new File(filePath)).get(0);	
	}

	public void getTraces(File[] listOfFiles) {
		startingTime=System.currentTimeMillis();
		modelsMap=new HashMap<String, Set<ArrayList<String>>>();
		modelsActions=new HashMap<String, ArrayList<String>>();


		int filePointer=0;
		for (int i = 0; i < listOfFiles.length; i++) {
			File file=listOfFiles[i];
			Set<ArrayList<String>> traceSet=new HashSet<ArrayList<String>>();
			try {
				XLog xlog=parseXES(file.getAbsolutePath());
				//XLog xlog=parseXES("/home/cippus/Downloads/log (5).xes");

				System.out.println("\n*******************************************"
						+ "************************************\n"
						+ "******** Analizzando il log: "
						+listOfFiles[filePointer].getName()+
						" ********\n*******************************************"
						+ "************************************\n");

				Set<String> logRepeatingSet = new TreeSet<String>();

				for (XTrace xTrace : xlog) {
					ArrayList<String> trace=new ArrayList<String>();

					TraceAnalyzer analyzer = new TraceAnalyzer();
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

					// stampa del file di log che sta per essere analizzato (delimitatore)
					//analyzer.setDelimiter('T');

					analyzer.setTrace(traceLine.toString());
					analyzer.KMPAnalyze();
					System.out.println(""+xTrace.getAttributes().get("concept:name".toString())+": "+traceLine.toString());
					Set<String> repeatingSet= analyzer.getRepeatingSet();
					//System.out.println("KMP time: "+analyzer.getTime());
					System.out.println("RepeatingSet: "+repeatingSet);

					Iterator<String> it= repeatingSet.iterator();
					while(it.hasNext()) {
						logRepeatingSet.add(it.next());
					}

					traceSet.add(trace);
					modelsMap.put(file.getName(), traceSet);	

				}
				filePointer++;
				listaMistica.add(logRepeatingSet);

				modelsActions.put(file.getName(), traceSet.stream().flatMap(x -> x.stream()).distinct().sorted().collect(Collectors.toCollection(ArrayList::new)));
				//System.out.println(modelsActions);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public File[] selectFolder() {
		JFileChooser chooser = new JFileChooser(".");
		System.out.println(ANSI_GREEN+"Please select the folder containing the XES Files"+ANSI_GREEN);

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
		
		// max string senza overlap
		// set di stringhe contenute nella massima stringa senza overlap da levare
		


		LogUtilsRepeatingTrace log=new LogUtilsRepeatingTrace();

		File[] files= log.selectFolder();


		//				PrintStream fileOut;
		//				try {
		//					fileOut = new PrintStream("./ConsoleOutput.txt");
		//					System.setOut(fileOut);
		//				} catch (FileNotFoundException e) {
		//					// TODO Auto-generated catch block
		//					e.printStackTrace();
		//				}

		log.getTraces(files);

		log.getRepeatingSet(files);

		//log.printLog();
		//log.adjacencyMatrix();
		//log.createDistanceMatrix();
		System.out.println("Execution Time:" + String.valueOf(System.currentTimeMillis()-startingTime));


	}

	private void getRepeatingSet(File[] files) {

		for(int i=0;i<files.length;i++) {
			System.out.println("\n--------------------------"
					+ " Repeating Set del file : "+files[i].getName()+"--------------------\n");
			System.out.print("[");
			Set<String> lista= listaMistica.get(i);
			Iterator<String> it=lista.iterator();
			while(it.hasNext()) {
				System.out.print(it.next()+", ");
			}
			System.out.println("]\n------------------------------------"
					+ "------------------------------\n");

			System.out.println("\n--------------------------"
					+ " Collapsed Set del file : "+files[i].getName()+"--------------------\n");
			System.out.print("[");

			Set<String> collapsed= collapseRepeatingSet(lista);
			it=collapsed.iterator();
			while(it.hasNext()) {
				System.out.print(it.next()+", ");
			}

			System.out.println("]\n------------------------------------"
					+ "------------------------------\n");
			
//			System.out.println("\n--------------------------"
//					+ " Max Repeat Set del file : "+files[i].getName()+"--------------------\n");
//			System.out.print("[");
//
//			Set<String> maxRepeat= generateMaxRepeat(collapsed);
//			it=maxRepeat.iterator();
//			while(it.hasNext()) {
//				System.out.print(it.next()+", ");
//			}
//
//			System.out.println("]\n------------------------------------"
//					+ "------------------------------\n");
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
		return maxRepeat;
	}

}
