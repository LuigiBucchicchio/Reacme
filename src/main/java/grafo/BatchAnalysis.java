package grafo;

import java.io.File;
import java.util.Locale;

import javax.swing.JFileChooser;

public class BatchAnalysis {
	public static File[] fileList;

	public static void main(String[] args) {

		startup();

		for(double l=(double)0.0;l<=(double)1.0;l=l+(double)0.5) {
		
		for(double i= (double)0.0; i<=(double)1.0;i=i+(double)0.25) {

			for(double j=(double)0.0;j<=(double)1.0;j=j+(double)0.1) {
				iteration(i,j,l);
			}

		}
		}
		System.out.println("END");
		
	}

	private static void iteration(double a, double b, double c) {
		LogUtilsRepeatingGraph log=new LogUtilsRepeatingGraph();
		log.setFileList(fileList);
		log.setScoreChange(true);
		log.setGamma(a);
		log.setNodeEqualScore((double)1.0);
		log.setNodeNotEqualScore((double)0.0);
		log.setNodeSemiScore(c);
		log.setEdgeEqualScore((double)1.0);
		log.setEdgeNotEqualScore((double)0.0);
		log.setEdgeSemiScore(b);
		log.analyzeTraces();
		String[][] distanceMatrix = log.generateDistanceMatrix();
		log.convertToCSV(distanceMatrix);
		System.out.println("a Distance Matrix is created");
		log=null;
	}

	private static void startup() {
		Locale.setDefault(Locale.US);
		System.out.println("Log evaluation - ");
		JFileChooser chooser = new JFileChooser(".");
		System.out.println("\u2705 " +"Please select the folder containing the XES Files" +" \u2705");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to open this folder: " + chooser.getSelectedFile().getName());
		}
		File folder= new File(chooser.getSelectedFile().getAbsolutePath());
		fileList= folder.listFiles();
	}

}
