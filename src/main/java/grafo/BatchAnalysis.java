package grafo;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class BatchAnalysis {
	public static boolean nested=false;
	public static File[] folderList;
	public static List<File[]> listOfFileList;
	
	public static File[] fileList;

	
	public static void main(String[] args) throws IOException,InterruptedException {
		System.console();
		
		int n = JOptionPane.showConfirmDialog(null, "Nested Folder?","Folder Option",JOptionPane.YES_NO_OPTION);
		if(n==JOptionPane.YES_OPTION)
			nested=true;
		else nested =false;

		if(!nested)
		startup();
		else {
			nestedFolderStartup();
		}

		double repeatingIncr=(double) 0.5;
		double repeatingEdge=0.0;
		double repeatingNode=0.0;

		//solo nodi
		double gamma=1.0;
		
		// scenari E-commerce, neutro, Maggioli
		for(repeatingNode= (double)0.0;repeatingNode<=(double) 1.0;repeatingNode=repeatingNode+repeatingIncr) {
			if(!nested)
			iteration(gamma,repeatingEdge,repeatingNode);
			else {
				for(int i=0; i<listOfFileList.size();i++) {
					fileList = listOfFileList.get(i);
					iteration(gamma,repeatingEdge,repeatingNode);
				}
			}
		}

		//reset
		repeatingNode=(double) 0.0;

		//solo archi
		gamma=0.0;

		// scenari E-commerce, neutro, Maggioli
		for(repeatingEdge=(double) 0.0;repeatingEdge<=(double) 1.0;repeatingEdge=repeatingEdge+repeatingIncr) {
			if(!nested)
			iteration(gamma, repeatingEdge, repeatingNode);
			else {
			    for(int i=0;i<listOfFileList.size();i++) {
			    	fileList = listOfFileList.get(i);
			    	iteration(gamma, repeatingEdge, repeatingNode);
			    }
			}
		}

		//reset
		repeatingEdge=(double) 0.0;
		
		//50-50
		gamma=0.5;

		// scenari (E-commerce, neutro, Maggioli) Archi <-X-> (E-commerce, neutro, Maggioli) Nodi
		for(repeatingNode = (double)0.0;repeatingNode<=(double)1.0;repeatingNode=repeatingNode+repeatingIncr) {
			for(repeatingEdge=(double)0.0;repeatingEdge<=(double)1.0; repeatingEdge=repeatingEdge+repeatingIncr) {
				if(!nested)
				iteration(gamma, repeatingEdge, repeatingNode);
				else {
					for(int i=0;i<listOfFileList.size();i++) {
						fileList = listOfFileList.get(i);
						iteration(gamma, repeatingEdge, repeatingNode);
					}
				}
			}
		}
		JOptionPane.showMessageDialog(null,"END");

	}

	private static void iteration(double a, double b, double c) {
		LogUtilsRepeatingGraph log=new LogUtilsRepeatingGraph();
		log.setFileList(fileList);
		int x = fileList.length;
		log.setTraceNum(new int[x]);
		log.setAvgTraceLen(new double[x]);
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
	
	private static void nestedFolderStartup() {
		Locale.setDefault(Locale.US);
		System.out.println("Log evaluation - ");
		JFileChooser chooser = new JFileChooser(".");
		System.out.println("\u2705 " +"Please select the main folder containing folder of XES files" +" \u2705");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			System.out.println("You chose to open this folder: " + chooser.getSelectedFile().getName());
		}
		File mainFolder= new File(chooser.getSelectedFile().getAbsolutePath());
		folderList = mainFolder.listFiles();
		listOfFileList = new ArrayList<File[]>(folderList.length);
		for(int i=0;i<folderList.length;i++) {
			File folder = folderList[i];
			if(folder.isDirectory())
			listOfFileList.add(folder.listFiles());
		}
		
	}

}
