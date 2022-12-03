package grafo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

import com.opencsv.CSVReader;

public class ForcedRun {

	public static void main(String[] args) throws IOException, InterruptedException {

		long startingTime = System.currentTimeMillis();
		Locale.setDefault(Locale.US);
		System.out.println("Log evaluation start");
		LogUtilsRepeatingGraph log=new LogUtilsRepeatingGraph();
		File f = new File("input");
		f.mkdir();
		log.setFileList(f.listFiles());
		int x = log.getFileList().length;
		if(x<=2) {
			System.out.println("Not enough Input XES Files found");
			System.exit(99);
		}
		log.setTraceNum(new int[x]);
		log.setAvgTraceLen(new double[x]);
		
		if(args.length==0) {
		Scanner tastiera = new Scanner(System.in);
		log.startMenu(tastiera);
		}else if(args.length==4){
			double gamma = Double.valueOf(args[0]);
			double nodiRepeating = Double.valueOf(args[1]);
			double archiRepeating = Double.valueOf(args[2]);
			int primiTreSimboli = Integer.valueOf(args[3]);
			boolean treSimboli;
			if(primiTreSimboli==0)
				treSimboli=false;
			else
				treSimboli=true;
			
			log.setScoreChange(true);
			log.setGamma(gamma);
			log.setNodeSemiScore(nodiRepeating);
			log.setEdgeSemiScore(archiRepeating);
			if(treSimboli)
			log.setTreCifre(true);
			else
				log.setTreCifre(false);
			
		}else {
			double gamma = Double.valueOf(args[0]);
			double nodiEqualScore = Double.valueOf(args[1]);
			double nodiNotEqualScore = Double.valueOf(args[2]);
			double nodiSemiEqualScore = Double.valueOf(args[3]);
			double archiEqualScore = Double.valueOf(args[4]);
			double archiNotEqualScore = Double.valueOf(args[5]);
			double archiSemiScore = Double.valueOf(args[6]);
			int primiTreSimboli = Integer.valueOf(args[7]);
			boolean treSimboli;
			if(primiTreSimboli==0)
				treSimboli=false;
			else
				treSimboli=true;
			
			log.setScoreChange(true);
			log.setGamma(gamma);
			log.setNodeEqualScore(nodiEqualScore);
			log.setNodeNotEqualScore(nodiNotEqualScore);
			log.setNodeSemiScore(nodiSemiEqualScore);
			log.setEdgeEqualScore(archiEqualScore);
			log.setEdgeNotEqualScore(archiNotEqualScore);
			log.setEdgeSemiScore(archiSemiScore);
			
			if(treSimboli)
			log.setTreCifre(true);
			else
				log.setTreCifre(false);
		}
		log.analyzeTraces();
		String[][] distanceMatrix = log.generateDistanceMatrix();
		log.convertToCSV(distanceMatrix);
		System.out.println("Evaluation Terminated - Execution Time:" + String.valueOf(System.currentTimeMillis()-startingTime));

		int cores = Runtime.getRuntime().availableProcessors();

		System.out.println("System cores: "+cores);
		File script = new File(
				Optional
				.ofNullable(System.getenv("CLUSTERING_SCRIPT_PATH"))
				.orElse("main.py")
				);
		String scriptPath = script.getAbsolutePath();
		scriptPath = scriptPath.replace('\\','/');
		File currentDirectory = new File("");
		String currentPath = currentDirectory.getAbsolutePath();
		currentPath = currentPath.replace('\\','/');

			System.out.println("Clustering Algorithm start");

			//			if(log.getFileList().length<=(cores*4)) {

			
				ProcessBuilder pb;
					pb = new ProcessBuilder("python",scriptPath,""+6,""+7+"",""+currentPath+"\\output");
				

			Process proc = pb.start();
			proc.waitFor();
			System.out.println();
			System.out.println("Clustering Algorithm terminated - total execution time: "+String.valueOf(System.currentTimeMillis()-startingTime));
			System.out.println("Incoming Results on output directory...");
			Thread.sleep(100);
	}
	
	
	public static void prepareForHeatMap() throws IOException {
		File dir = new File("");
		String dirPath = dir.getAbsolutePath();
		dir = new File(dirPath);
		File outputDirectory = new File(dir+"\\output");
		if(outputDirectory.isDirectory()) {
			File[] fileList = outputDirectory.listFiles();
			for(int i=0; i<fileList.length;i++) {
				File one = fileList[i];
				if(one.getName().contains("clustering")) {
					File newClusteringFile = new File(outputDirectory+"\\preparedLabelsForHeatmap.csv");
					FileWriter fw = new FileWriter(newClusteringFile);
					BufferedWriter bw = new BufferedWriter(fw);
					Scanner s = new Scanner(one);
					String line = null;
					while(s.hasNextLine()){
						line = s.nextLine();
						if(!line.contains(".")) {
							bw.newLine();
							line = line.replace("['", "");
							line = line.replace("]", "");
							line = line.replace("[", "");
							line = line.replace("' ", ",");
							bw.write(line);
						}else if(line.contains("DistanceGraph")) {
							bw.write("NomeLog,ClusterId");
						}else {
							//skip
						}
						}
					s.close();
					bw.close();
				}
			}
		}
	}

}