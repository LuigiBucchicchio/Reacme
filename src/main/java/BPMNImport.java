import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.impl.instance.ParallelGatewayImpl;
import org.camunda.bpm.model.bpmn.instance.*;

import javax.management.PersistentMBean;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BPMNImport {
	static Set<String> SuperSet=new HashSet<String>();
	static ArrayList<String> strings = new ArrayList<String>();
	static ArrayList<String> elements = new ArrayList<String>();
	static ArrayList<String[][]> flowMatrix = new ArrayList<String[][]>();
	static ArrayList<String[][]> SetVariantMatrix = new ArrayList<String[][]>();
	static ArrayList<String> variantMatrixName=new ArrayList<String>();

	public static void main(String[] args) {
		JFileChooser chooser = new JFileChooser(".");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//FileNameExtensionFilter filter = new FileNameExtensionFilter("BPMN", "bpmn");
		//chooser.setFileFilter(filter);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
		}
		File file = new File(chooser.getSelectedFile().getAbsolutePath()); // ciaone/ciao/cartella
		// File file = new File("");
		File[] listOfFiles = file.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			System.out.println("You chose to open this file: " + listOfFiles[i].getName());
			strings.clear();
			elements.clear();
			BpmnModelInstance modelInstance = Bpmn.readModelFromFile(listOfFiles[i]);
			for (StartEvent start : modelInstance.getModelElementsByType(StartEvent.class)) {
				recursiveExploration(start, "");
			}

			modelInstance.getModelElementsByType(Task.class).stream().map(o -> o.getName()).distinct()
			.forEach(a -> elements.add(a));
			;

			SuperSet.addAll(elements);

			for (String path : strings) {
				System.out.print(path);
				createMatrix(path);
			}

			variantMatrixName.add(listOfFiles[i].getName());
			System.out.println("->[Variant Matrix of " + variantMatrixName.get(variantMatrixName.size()-1)+"]<-");
			print2D(createVariantMatrix(flowMatrix));
			flowMatrix.clear();



		}

		System.out.println(SuperSet.toString());
		calculateDistanceMatrix();
	}



	private static void createMatrix(String path) {
		String[] s = path.split("->");
		int rows = elements.size() + 1;
		int columns = elements.size() + 1;
		String[][] sum = new String[rows][columns];
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				sum[r][c] = "4";
				if (r == c ) {
					sum[r][c] = "5";
				} else if (r == 0) {
					sum[r][c] = elements.get(c - 1);
				} else if (c == 0) {
					sum[r][c] = elements.get(r - 1);
				}
			}
		}

		for (int i = 0; i < s.length; i++) {
			for (int t = i + 1; t < s.length; t++) {
				sum[elements.indexOf(s[i]) + 1][elements.indexOf(s[t]) + 1] = "1";
				sum[elements.indexOf(s[t]) + 1][elements.indexOf(s[i]) + 1] = "0";
			}
		}
		sum[0][0]=" ";
		flowMatrix.add(sum);
		print2D(sum);

	}



	public static String[][] createVariantMatrix(ArrayList<String[][]> mat) {
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

		SetVariantMatrix.add(variantMatrix);
		return variantMatrix;

	}

	public static void calculateDistanceMatrix() {
		String[][] distanceMatrix=new String[variantMatrixName.size()+1][variantMatrixName.size()+1];
		System.out.println("DISTANCE MATRIX");
		int cardinality=SetVariantMatrix.size();
		for (String[][] matrix1 : SetVariantMatrix) {
			for (int i = SetVariantMatrix.indexOf(matrix1)+1; i < cardinality; i++) {
				print2D(matrix1);
				String[][] matrix2=SetVariantMatrix.get(i);

				ArrayList<String> elementMatrix1=new ArrayList<String>(Arrays.asList(matrix1[0]));
				ArrayList<String> elementMatrix2=new ArrayList<String>(Arrays.asList(matrix2[0]));
				ArrayList<String> elementSuperset=new ArrayList<String>(SuperSet);
				elementSuperset.add(0, " ");
				print2D(matrix2);

				int rows=SuperSet.size()+1; 
				int columns=rows;
				String[][] matrix = new String[rows][columns];
				matrix[0][0]=" ";
				for (int r = 0; r < rows; r++) {
					for (int c = 0; c < columns; c++) {
						String elementRow=elementSuperset.get(r);
						String elementColumn=elementSuperset.get(c);

						if (r == 0) {
							matrix[r][c] = elementSuperset.get(c);
						} else if (c == 0) {
							matrix[r][c] = elementSuperset.get(r);
						} else if(elementMatrix1.contains(elementRow)&& elementMatrix1.contains(elementColumn)&&elementMatrix2.contains(elementRow)&& elementMatrix2.contains(elementColumn)) {
							if(matrix1[elementMatrix1.indexOf(elementRow)][elementMatrix1.indexOf(elementColumn)].equals(matrix2[elementMatrix2.indexOf(elementRow)][elementMatrix2.indexOf(elementColumn)])){
								matrix[r][c] ="0";
							}else {
								matrix[r][c] ="1";
							}
						}else if(elementMatrix1.contains(elementRow)&& elementMatrix1.contains(elementColumn)|| elementMatrix2.contains(elementRow)&& elementMatrix2.contains(elementColumn)) {
							matrix[r][c] ="2";
						}

						else {
							matrix[r][c] ="0";
						}
					}
				}
				System.out.println("Distance Calculated between "+ variantMatrixName.get(SetVariantMatrix.indexOf(matrix1))+" <--> "+variantMatrixName.get(SetVariantMatrix.indexOf(matrix2)));
				distanceMatrix[SetVariantMatrix.indexOf(matrix1)+1][SetVariantMatrix.indexOf(matrix2)+1]=String.valueOf(DistanceSum(matrix));
				System.out.println("DISTANCE = " + DistanceSum(matrix));
				print2D(matrix);


				System.out.println("____________________");
			}

		}
		System.out.println("FINAL DISTANCE MATRIX");
		print2D(generateMatrix(distanceMatrix, variantMatrixName));
	}	


	private static String[][] generateMatrix(String[][] distanceMatrix, ArrayList<String> variantMatrixName2) {
		for (int i = 0; i < variantMatrixName2.size(); i++) {
			distanceMatrix[0][i+1]=variantMatrixName2.get(i);
			distanceMatrix[i+1][0]=variantMatrixName2.get(i);
			distanceMatrix[i+1][i+1]="0";
		}
		return distanceMatrix;
	}



	public static double DistanceSum(String[][] matrix) {
		double sum =0;
		for (int i = 1; i < matrix.length; i++) {
			for (int j = 1; j < matrix.length; j++) {
				sum+=Double.valueOf(matrix[i][j]);
			}
		}
		return sum;
	}

	public static void recursiveExploration(FlowNode node, String s) {
		if (node.getName() != null && !(node instanceof StartEvent)) {
			s += node.getName() + "->";
		}
		for (FlowNode n : node.getSucceedingNodes().list()) {

			if (n instanceof EndEvent) {

				s = s.substring(0, s.length() - 2);
				strings.add(s);
				System.out.println(s + " \n");
				recursiveExploration(n, s);

			}

			else {
				recursiveExploration(n, s);
			}
		}
	}



	public static void print2D(String mat[][]) {
		// Loop through all rows
		for (int i = 0; i < mat.length; i++) {
			System.out.println(" ");
			// Loop through all elements of current row
			for (int j = 0; j < mat[i].length; j++)
				System.out.print(mat[i][j] + "| ");
		}
		System.out.println(" \n");
	}


}