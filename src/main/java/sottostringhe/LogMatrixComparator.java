package sottostringhe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class LogMatrixComparator {
	
	private static char delimiter='t';
	private List<String> dominion1;
	private List<String> dominion2;
	private List<List<RelationType>> relations1;
	private List<List<RelationType>> relations2;
	
	public LogMatrixComparator(List<String> d1, List<String> d2, List<List<RelationType>> r1, List<List<RelationType>> r2) {
		this.dominion1=d1;
		this.dominion2=d2;
		this.relations1=r1;
		this.relations2=r2;
	}
	
	public double compare(){
		GradedCompare gCompare = new GradedCompare();
		
		Set<String> superSet = new TreeSet<String>();
		
		superSet.addAll(dominion1);
		superSet.addAll(dominion2);
		
		List<String> superSetAsList = new ArrayList<String>(superSet);
		
		for(int i=0;i<superSetAsList.size();i++) {
			String activity1=superSetAsList.get(i);
			for(int j=0;j<superSetAsList.size();j++) {
				String activity2= superSetAsList.get(j);

				
				int grade= Math.abs( (getGrade(activity1)-getGrade(activity2)) );
				
				int index1= dominion1.indexOf(activity1);
				int index2= dominion1.indexOf(activity2);
				
				boolean exist1=true;
				if(index1 <0 || index2 <0)
					exist1=false;
				
				RelationType r1 = null;
				if(index1> -1 && index2>-1)
				r1=relations1.get(index1).get(index2);
				
				index1= dominion2.indexOf(activity1);
				index2= dominion2.indexOf(activity2);
				boolean exist2=true;
				if(index1 <0 || index2 <0)
					exist2=false;
				
				RelationType r2= null;
				if(index1> -1 && index2 >-1)
					r2=relations2.get(index1).get(index2);
				
				if(exist1 && exist2) {
				
				if(r1==r2) {
					gCompare.addScore((double)1.0, grade);
				}else if(r1==null || r2==null) {
					gCompare.addScore((double)0.0, grade);
				}else if(r1==RelationType.precedeDirettamente && r2==RelationType.segueDirettamente) {
					gCompare.addScore((double)0.0, grade);
				}else {
						gCompare.addScore((double)0.5, grade);
					}
				}else {
					//skip
				}
		}
		}
		
		return gCompare.getFinalScore();
	}
	
	private int getGrade(String key) {
		int count=0;
		if(key.charAt(0)==delimiter) {
			count ++;
			for(int i=1; i<key.length();i++) {
				if(key.charAt(i)==delimiter)
					count ++;
			}
			return count;
		}else {
			return count;
		}
	}

}
