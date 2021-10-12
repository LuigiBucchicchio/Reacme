package sottostringhe;

import java.util.ArrayList;
import java.util.List;

public class GradedCompare {

	private int maxGrade=-1;
	private List<List<Double>> scores = new ArrayList<List<Double>>();
	
	public void addScore(Double score, int grade) {
		
		if(grade>maxGrade) {
			int diff = grade-maxGrade;
			for(int i=0;i<diff;i++)
			scores.add(new ArrayList<Double>());
			
			maxGrade=grade;
		}
		scores.get(grade).add(score);
	}
	
	//calcola la similarita'
	public double getFinalScore() {
		double finalSum= (double)0.0;
		for(int i=0; i<scores.size(); i++) { //per tutti i gradi
			double sum = (double)0.0;
			for(int j=0; j<scores.get(i).size();j++) { //per ogni confronto fatto (e relativo punteggio)
				sum=sum+scores.get(i).get(j);
			}
			double x=(double)0.0;
			if(scores.get(i).size()!=0)
			x=sum/scores.get(i).size(); // diviso numero di confronti
			double alpha = 1/(Math.pow(i+1, 2)); // termine alpha0 poi alpha1
			double alphaX = x*alpha; //applicazione del peso
			finalSum=finalSum+alphaX; //somma pesata
		}
		return finalSum/((Math.pow(Math.PI,2))/6); //convergenza della somma
	}
	
}
