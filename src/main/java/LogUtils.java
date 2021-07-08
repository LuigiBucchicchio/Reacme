import java.awt.Event;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.CollectionCertStoreParameters;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;

import org.apache.commons.io.IOUtils;
import org.checkerframework.common.reflection.qual.NewInstance;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeContainerImpl;

import com.opencsv.CSVWriter;




public class LogUtils {
	private Map<String,Set<ArrayList<String>>> modelsMap;
	private Map<String, ArrayList<String>> modelsActions;
	private Map<String, ArrayList<String[][]>> modelsAdjacencyMatrix=new HashMap<String, ArrayList<String[][]>>();
	private Map<String, String[][]> modelsVarianceMatrix=new HashMap<String, String[][]>();
	static double startingTime;
	public static final String ANSI_GREEN = "\u001B[32m";

	private  String a="0.5";
	private  String b="0.5";
	private  String c="0.2";
	//private final String d="0";
	//private final String e="0";
	//private final String f="0";
	//private final String g="0";
	private final String h="0";

	public XLog parseXES(String filePath) throws Exception {
		XesXmlParser parser = new XesXmlParser();
		return parser.parse(new File(filePath)).get(0);	

	}

	public void getTraces(File[] listOfFiles) {
		startingTime=System.currentTimeMillis();
		modelsMap=new HashMap<String, Set<ArrayList<String>>>();
		modelsActions=new HashMap<String, ArrayList<String>>();
		for (int i = 0; i < listOfFiles.length; i++) {
			File file=listOfFiles[i];
			Set<ArrayList<String>> traceSet=new HashSet();
			try {
				XLog xlog=parseXES(file.getAbsolutePath());
				//XLog xlog=parseXES("/home/cippus/Downloads/log (5).xes");

				for (XTrace xTrace : xlog) {
					ArrayList<String> trace=new ArrayList();
					for (XEvent xevent : xTrace) {
						String activity=xevent.getAttributes().get("concept:name").toString();
						trace.add(activity);
					}
					traceSet.add(trace);
					modelsMap.put(file.getName(), traceSet);		
				}
				
				modelsActions.put(file.getName(), traceSet.stream().flatMap(x -> x.stream()).distinct().sorted().collect(Collectors.toCollection(ArrayList::new)));
				System.out.println(modelsActions);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private String[][] createMatrix(ArrayList<String> actions, String diagonal, String background){
		int matrixSize=actions.size()+1;
		String[][] matrix=new String[matrixSize][matrixSize];
		for (int r = 0; r < matrixSize; r++) {
			for (int c = 0; c < matrixSize; c++) {
				matrix[r][c] = background;
				if (r == c ) {
					matrix[r][c] = diagonal;
				} else if (r == 0) {
					matrix[r][c] = actions.get(c - 1);
				} else if (c == 0) {
					matrix[r][c] = actions.get(r - 1);
				}
			}
		}
		return matrix;
	}

	private void adjacencyMatrix() {

		for (String modelName : modelsMap.keySet()) {
			ArrayList<String[][]> adjacencymatrix=new ArrayList<String[][]>();
			System.out.println("___________"+modelName+"___________\n");
			Set<ArrayList<String>> traces=modelsMap.get(modelName);
			for (ArrayList<String> singleTrace : traces) {
				System.out.println(singleTrace);
				ArrayList<String> tableActions=modelsActions.get(modelName);

				String[][] matrix=createMatrix(tableActions, " ", "4");


				for (int i = 0; i < singleTrace.size(); i++) {
					for (int t = i + 1; t < singleTrace.size(); t++) {
						if (Math.abs(i-t)==1) {
							//PRECEDE DIRETTAMENTE
							matrix[tableActions.indexOf(singleTrace.get(i))+1][tableActions.indexOf(singleTrace.get(t))+1] = "2";
							//SUCCEDE DIRETTAMENTE
							matrix[tableActions.indexOf(singleTrace.get(t))+ 1][tableActions.indexOf(singleTrace.get(i))+1] = "3";
						}else{

							//PRECEDE
							matrix[tableActions.indexOf(singleTrace.get(i))+1][tableActions.indexOf(singleTrace.get(t))+1] = "1";
							//SUCCEDE
							matrix[tableActions.indexOf(singleTrace.get(t))+ 1][tableActions.indexOf(singleTrace.get(i))+1] = "0";
						}
					}
				}

				adjacencymatrix.add(matrix);
				print2D(matrix);

			}
			System.out.println("LOG MATRIX");
			String[][] varianceMatrix=createVariantMatrixNew(adjacencymatrix, modelName);
			//String[][] varianceMatrix=createVariantMatrix(adjacencymatrix);
			print2D(varianceMatrix);
			modelsAdjacencyMatrix.put(modelName, adjacencymatrix);
			modelsVarianceMatrix.put(modelName, varianceMatrix);
		}

	}

	public void print2D(String mat[][]) {
		// Loop through all rows
		for (int i = 0; i < mat.length; i++) {
			System.out.println(" ");
			// Loop through all elements of current row
			for (int j = 0; j < mat[i].length; j++)
				System.out.print(mat[i][j] + "| ");
		}
		System.out.println(" \n");
	}

	public void printLog() {

		for (String model : modelsMap.keySet()) {
			System.out.println(model +" --> "+ modelsMap.get(model));
		}
	}

	public  String[][] createVariantMatrix(ArrayList<String[][]> mat) {
		int i;
		int j;
		String[][] variantMatrix = new String[mat.get(0).length][mat.get(0).length];

		for (i = 0; i < mat.get(0).length; i++) {
			for (j = 0; j < mat.get(0).length; j++) {
				String simbol = "S";				
				for (String[][] matrix : mat) {

					if (matrix[i][j].equals("4"))
						variantMatrix[i][j] = "-";
					else {						
						if (simbol.equals("S"))							
							simbol = matrix[i][j];
						if (simbol.equals("1") && matrix[i][j].equals("0"))							
							simbol = "+";
						if (simbol.equals("0") && matrix[i][j].equals("1"))							
							simbol = "+";
					}
				}

				if (simbol.equals("S")) {					
					variantMatrix[i][j] = "-";

				} else {					
					variantMatrix[i][j] = simbol;
				}
			}
		}

		//SetVariantMatrix.add(variantMatrix);
		return variantMatrix;

	}

	public  String[][] createVariantMatrixNew(ArrayList<String[][]> mat, String modelName) {
		String[][] variantMatrix = createMatrix(modelsActions.get(modelName), " ", "");
		int matrixDimension=mat.get(0).length;

		for (int i = 1; i < matrixDimension; i++) {
			for (int j = 1; j < matrixDimension; j++) {
				int[] occurrences = {0, 0, 0, 0, 0};
				for (String[][] matrix : mat) {
					if (matrix[i][j]!=" ") {
						occurrences[Integer.valueOf(matrix[i][j])]++;
					}

				}
				//				System.out.println("i="+i+" j="+j + "  0="+ occurrences[0]
				//						+ "  1="+ occurrences[1]
				//								+ "  2="+ occurrences[2]
				//										+ "  3="+ occurrences[3]
				//												+ "  4="+ occurrences[4]);
				if (occurrences[4]==mat.size()) {
					variantMatrix[i][j]="-";
				}
				//				else if(occurrences[0]==mat.size()) {
				//					variantMatrix[i][j]="0";
				//				}else if(occurrences[1]==mat.size()) {
				//					variantMatrix[i][j]="1";
				//				}else if(occurrences[2]==mat.size()) {
				//					variantMatrix[i][j]="2";
				//				}else if(occurrences[3]==mat.size()) {
				//					variantMatrix[i][j]="3";
				//				}
				else if(occurrences[2]!=0 && occurrences[1]!=0 && occurrences[0]==0 && occurrences[3]==0){
					variantMatrix[i][j]="1";
				}else if(occurrences[0]!=0 && occurrences[3]!=0 && occurrences[2]==0 && occurrences[1]==0){
					variantMatrix[i][j]="0";

				}else if(occurrences[0]!=0 && occurrences[1]==0&&occurrences[2]==0&&occurrences[3]==0) {
					variantMatrix[i][j]="0";
				}else if(occurrences[1]!=0 && occurrences[0]==0&&occurrences[2]==0&&occurrences[3]==0) {
					variantMatrix[i][j]="1";
				}else if(occurrences[2]!=0 && occurrences[1]==0&&occurrences[0]==0&&occurrences[3]==0) {
					variantMatrix[i][j]="2";
				}
				else if(occurrences[3]!=0 && occurrences[1]==0&&occurrences[2]==0&&occurrences[0]==0) {
					variantMatrix[i][j]="3";
				}

				else {
					if(i!=j) {
						variantMatrix[i][j]="+";
					}

				}

			}
		}

		//SetVariantMatrix.add(variantMatrix);
		return variantMatrix;

	}

	public void createDistanceMatrix() {
		ArrayList<String> modelNames = new ArrayList<String>(modelsVarianceMatrix.keySet());
		String[][] resultDistanceMatrix=createMatrix(modelNames, "0", "0");
		int cardinality=modelNames.size();
		for (int i = 0; i < cardinality; i++) {
			for (int j = i+1; j < cardinality; j++) {
				String[][] matrix1=modelsVarianceMatrix.get(modelNames.get(i));
				ArrayList<String> activitiesMatrix1 = modelsActions.get(modelNames.get(i));
				ArrayList<String> activitiesMatrix2 = modelsActions.get(modelNames.get(j));
				String[][] matrix2=modelsVarianceMatrix.get(modelNames.get(j));

				//System.out.println("--------------DISTANCE MATRIX--------------");
				//System.out.println(modelNames.get(i));
				//print2D(matrix1);
				//System.out.println(modelNames.get(j));
				//print2D(matrix2);

				ArrayList<String> activitiesSuperset=(ArrayList<String>) Stream.of(activitiesMatrix1,activitiesMatrix2)
						.flatMap(x -> x.stream()).distinct()
						.collect(Collectors.toList());
				//				System.out.println(activitiesSuperset);

				String[][] distanceMatrix=createMatrix(activitiesSuperset, " ", " ");
				for (int r = 0; r < activitiesSuperset.size(); r++) {
					for (int c = 0; c < activitiesSuperset.size(); c++) {
						if (activitiesMatrix1.contains(activitiesSuperset.get(r))&&
								activitiesMatrix1.contains(activitiesSuperset.get(c))&&
								activitiesMatrix2.contains(activitiesSuperset.get(r))&&
								activitiesMatrix2.contains(activitiesSuperset.get(c))) {
							String element1=matrix1[activitiesMatrix1.indexOf(activitiesSuperset.get(r))+1][activitiesMatrix1.indexOf(activitiesSuperset.get(c))+1];
							String element2=matrix2[activitiesMatrix2.indexOf(activitiesSuperset.get(r))+1][activitiesMatrix2.indexOf(activitiesSuperset.get(c))+1];	
							if(element1.equals(element2)) {
								if (r==c) {
									//se hanno le stesse lettere e ci troviamo nella diagonale
									distanceMatrix[r+1][c+1]=a;
								}else {
									//se hanno la stessa lettera
									distanceMatrix[r+1][c+1]=b;
								}

							}else if(element1=="2"&& element2=="1"||
									element1=="1"&&element2=="2"||
									element1=="0"&& element2=="3"||
									element1=="3"&&element2=="0") {
								//se mantengono lo stesso ordine di precedenza
								distanceMatrix[r+1][c+1]=this.c;
							}
							/*else if(element1=="2"&& element2=="0"||
									element1=="0"&&element2=="2"||
									element1=="3"&&element2=="1"||
									element1=="1"&& element2=="3" ){
								//Ordine inverso con uno strettamente e uno no
								distanceMatrix[r+1][c+1]=d;
							}else if(element1=="2"&& element2=="3"||
									element1=="3"&& element2=="2" ){
								//Ordine inverso con entrambi inversi strettamente
								distanceMatrix[r+1][c+1]=e;
							}else if(element1=="1"&& element2=="0"||
									element1=="0"&& element2=="1" ){
								//Ordine inverso con entrambi inversi non strettamente
								distanceMatrix[r+1][c+1]=f;
							}else {
								distanceMatrix[r+1][c+1]=g;
							}
							 */
						}

						else {
							//se un modello non contiene un'attività
							distanceMatrix[r+1][c+1]=h;
						}


					}
				}
				Double similarity=calculateDistance(distanceMatrix);
				System.out.println("VARIANCE MATRIX " + modelNames.get(i)+" --> "+modelNames.get(j)+ " DISSIMILARITY="+similarity);
				print2D(distanceMatrix);
				resultDistanceMatrix[i+1][j+1]=String.valueOf(similarity);



			}
		}
		System.out.println("DISTANCE MATRIX");
		print2D(resultDistanceMatrix);
		analizeResults(resultDistanceMatrix);
		//convertToCSV(resultDistanceMatrix);
	}

	public double calculateDistance(String[][] matrix) {
		double sum=0;
		for (int i = 1; i < matrix.length; i++) {
			for (int j = 1; j < matrix.length; j++) {
				if(matrix[i][j]!=" ") {
					sum+=Double.valueOf(matrix[i][j]);
				}

			}
		}
		//QUI!!!!!!!!!!!!!
		Double result=0.0;
		if(a.equals("1")) {
			result=100-sum/(matrix.length-1)*100;
		}else if(a.equals("0")){
			result=100-sum/((matrix.length-1)*(matrix.length-2))*100;
		}else {
			result=100-sum/(Math.pow(matrix.length-1, 2))*100;
		}
		//Double result=100-sum/(Math.pow(matrix.length-1, 2)/2)*100;
		//Double result=100-sum/((matrix.length-1)*(matrix.length-2))*100;
		DecimalFormat df2= new DecimalFormat("#.##");
		//return Double.valueOf(df2.format(result));
		return result; 
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
		System.out.println(ANSI_GREEN+"Please insert the value of Alpha in a range between 0 and 1"+ANSI_GREEN);
		Scanner tastiera = new Scanner(System.in);
		a = tastiera.nextLine();
		b=String.valueOf(1-Double.valueOf(a));
		if (a.equals("1")) {
			c=String.valueOf(0);
		}else {
			System.out.println(ANSI_GREEN+"Please insert the value of Beta in a range between 0 and (1-alpha)"+ANSI_GREEN);
			c=String.valueOf(Double.valueOf(tastiera.nextLine())*Double.valueOf(b));
		}

		return folder.listFiles();
	}

	public void analizeResults(String[][] distanceMatrix) {
		TreeMap<String, ArrayList<String>> similarityPlot=new TreeMap<String, ArrayList<String>>();
		for (int i = 1; i < distanceMatrix.length; i++) {
			for (int j = i+1; j < distanceMatrix.length; j++) {
				if (similarityPlot.containsKey(distanceMatrix[i][j])) {
					similarityPlot.get(distanceMatrix[i][j]).add(distanceMatrix[i][0]+"-->"+distanceMatrix[0][j]);
				}else {
					ArrayList<String> list=new ArrayList<String>();
					list.add(distanceMatrix[i][0]+"-->"+distanceMatrix[0][j]);
					similarityPlot.put(distanceMatrix[i][j], list);
				}
			}

		}

		//System.out.println(similarityPlot);
	}

	public String convertToCSV(String[][] data) {
		Path path;
		try {
			path = Paths.get(LogUtils.class.getResource("resources/DistanceMatrix.csv").toURI());
			CSVWriter writer = new CSVWriter(new FileWriter(path.toString()));
			for (String[] array : data) {
				writer.writeNext(array);
			}
			writer.close();

			System.out.println(IOUtils.toString(this.getClass().getResourceAsStream("DistanceMatrix.csv")));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		return this.getClass().getResourceAsStream("DistanceMatrix.csv").toString();

	}



	public static void main(String[] args) {
		LogUtils log=new LogUtils();
		log.getTraces(log.selectFolder());

		log.printLog();
		log.adjacencyMatrix();
		log.createDistanceMatrix();
		System.out.println("Execution Time:" + String.valueOf(System.currentTimeMillis()-startingTime));


	}
}
