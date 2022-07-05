package grafo;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

import com.opencsv.CSVReader;

public class EnsembleRun {

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

		if(cores>1 && ((log.getFileList().length-2)>(cores*2))) {
			System.out.println("Clustering Algorithm start");

			ProcessBuilder[] builders = new ProcessBuilder[cores];
			Process[] processes = new Process[cores];

			int subpart = (int) Math.floor(log.getFileList().length/cores);

			int diff = log.getFileList().length - subpart*cores;

			int last = 0;

			//			if(log.getFileList().length<=(cores*4)) {

			for(int i=0; i<cores; i++) {
				ProcessBuilder pb;
				if(i==0) {
					pb = new ProcessBuilder("python",scriptPath,""+2,""+(last+subpart+diff)+"",""+currentPath+"\\output");
					last = last+subpart+diff;
				}else {
					pb = new ProcessBuilder("python",scriptPath,""+last,""+(last+subpart)+"",""+currentPath+"\\output");
					last = last+subpart;
				}
				builders[i] = pb;
			}
			//			}else {
			//				int maxScale = cores/2;
			//				int scale = maxScale;
			//				
			//				for(int i=0; i<cores; i++) {
			//					ProcessBuilder pb=null;
			//					if(i==0) {
			//					pb = new ProcessBuilder("python",scriptPath,""+2,""+(last+subpart+diff+((int)Math.floor(subpart*scale)/(scale+1))+1)+"",""+currentPath+"\\output");
			//					last = (last+subpart+diff+((int)Math.floor(subpart*scale)/(scale+1))+1);
			//					}else if(scale>0){
			//						pb = new ProcessBuilder("python",scriptPath,""+last,""+(last+subpart+((int)Math.floor(subpart*scale)/(scale+1))+1)+"",""+currentPath+"\\output");
			//						last = (last+subpart+((int)Math.floor(subpart*scale)/(scale+1))+1);
			//					}else if(scale==0){
			//						if(cores%2==1) {
			//							pb = new ProcessBuilder("python",scriptPath,""+last,""+(last+subpart)+"",""+currentPath+"\\output");
			//							last = last+subpart;
			//						}else {
			//							scale--;
			//							pb = new ProcessBuilder("python",scriptPath,""+last,""+(last+subpart-((int)Math.floor(subpart*scale)/(scale-1)))+"",""+currentPath+"\\output");
			//							last = (last+subpart-((int)Math.floor(subpart*scale)/(scale-1)));
			//						}
			//					}else if(scale<0){
			//						pb = new ProcessBuilder("python",scriptPath,""+last,""+(last+subpart-((int)Math.floor(subpart*scale)/(scale-1)))+"",""+currentPath+"\\output");
			//						last = (last+subpart-((int)Math.floor(subpart*scale)/(scale-1)));
			//					}
			//				builders[i] = pb;
			//				scale--;
			//				}
			//			}

			for(int i=0; i<cores; i++) {
				processes[i] = builders[i].start();
			}
			System.out.print("waiting for "+processes.length+" processes to end");
			for(int i=0; i<cores; i++) {
				processes[i].waitFor();
				System.out.print(".");
			}
			System.out.println();
			System.out.println("Clustering Algorithm terminated - total execution time: "+String.valueOf(System.currentTimeMillis()-startingTime));
			System.out.println("Incoming Results on output directory...");
			Thread.sleep(1000);

			File dir = new File("");
			String dirPath = dir.getAbsolutePath();
			dir = new File(dirPath);

			List<File> fileList = new ArrayList<File>();
			Collections.addAll(fileList, dir.listFiles());

			List<File> outputList = new ArrayList<File>();
			Iterator<File> fileIterator = fileList.iterator();
			while(fileIterator.hasNext()){
				File nextFile = fileIterator.next();
				if(nextFile.getName().contains("clustering") || nextFile.getName().contains("smallOut"))
					outputList.add(nextFile);
			}

			Iterator<File> outputFileIterator = outputList.iterator();
			double max = (double) 0.0;
			File winner = null;
			CSVReader reader;
			while(outputFileIterator.hasNext()) {
				File nextOutputFile = outputFileIterator.next();
				if(nextOutputFile.getName().contains("smallOut")) {
					reader = new CSVReader(new FileReader(nextOutputFile));
					String[] row = reader.readNext();
					double score = Double.valueOf(row[1]);
					if(score>max) {
						max = score;
						winner = nextOutputFile;
					}
					reader.close();
				}
			}

			File[] winners = new File[2];
			int winnersIndex = 0;
			String winnerName = winner.getName();
			for(int i=0; i<outputList.size();i++) {
				int winnerNameIndex = winnerName.indexOf("smallOut");
				String winnerNameNumber = winnerName.substring(0, winnerNameIndex);
				if(!outputList.get(i).getName().contains(winnerNameNumber)) {
					outputList.get(i).deleteOnExit();
				}else {
					winners[winnersIndex] = outputList.get(i);
					winnersIndex++;
				}
			}

			String parentDir0 = winners[0].getParent();
			parentDir0 = parentDir0+"\\output";
			String winner0name = winners[0].getName();
			winners[0].renameTo(new File(parentDir0+"\\"+winner0name));

			String parentDir1 = winners[1].getParent();
			parentDir1 = parentDir1+"\\output";
			String winner1name = winners[1].getName();
			winners[1].renameTo(new File(parentDir1+"\\"+winner1name));

			System.out.println("Done");

		}else {
			System.out.println("Clustering Algorithm start");
			ProcessBuilder pb = new ProcessBuilder("python",scriptPath,""+2,""+log.getFileList().length+"",""+currentPath+"\\output");
			Process p = pb.start();
			p.waitFor();
			BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			while ((line = bfr.readLine()) != null) {
				System.out.println(line);
			}
			bfr.close();
			System.out.println("Clustering Algorithm terminated - total execution time: "+String.valueOf(System.currentTimeMillis()-startingTime));
			System.out.println("Incoming Results on output directory...");
			Thread.sleep(1000);


			File dir = new File("");
			String dirPath = dir.getAbsolutePath();
			dir = new File(dirPath);

			List<File> fileList = new ArrayList<File>();
			Collections.addAll(fileList, dir.listFiles());

			List<File> outputList = new ArrayList<File>();
			Iterator<File> fileIterator = fileList.iterator();
			while(fileIterator.hasNext()){
				File nextFile = fileIterator.next();
				if(nextFile.getName().contains("clustering") || nextFile.getName().contains("smallOut"))
					outputList.add(nextFile);
			}

			if(outputList.size()==2) {
				String parentDir0 = outputList.get(0).getParent();
				parentDir0 = parentDir0+"\\output";
				String winner0name = outputList.get(0).getName();
				outputList.get(0).renameTo(new File(parentDir0+"\\"+winner0name));

				String parentDir1 = outputList.get(1).getParent();
				parentDir1 = parentDir1+"\\output";
				String winner1name = outputList.get(1).getName();
				outputList.get(1).renameTo(new File(parentDir1+"\\"+winner1name));
			}
			System.out.println("Done");
		}
			log.generateNodeListReport("CUSTOM");
			prepareForHeatMap();
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
