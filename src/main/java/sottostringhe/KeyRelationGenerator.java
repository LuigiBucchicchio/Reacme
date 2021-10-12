package sottostringhe;

import java.util.ArrayList;
import java.util.List;

public class KeyRelationGenerator {
	
	private String key;
	private String traceLine;
	private List<RelationType> relations;
	private List<String> dominion;
	private char delimiter='t';
	
	public KeyRelationGenerator(String key, List<String> dominion) {
		relations = new ArrayList<RelationType>();
	    for(int i=0;i<dominion.size();i++) {
	    	relations.add(null);
	    }
		this.dominion=dominion;
		this.key=key;
	}
	
	public List<RelationType> getRelations(){
		return this.relations;
	}
	
	public List<String> getDominion(){
		return this.dominion;
	}
	
	public void iteration(String traceLine) {
		this.traceLine=traceLine;
		this.relations=generateRelation();
	}
	
	public List<RelationType> generateRelation(){
		StringBuffer s= new StringBuffer("");
		
		//ricerca della chiave nella traccia
		for(int i=0; i<traceLine.length();i++) {
			
			s = new StringBuffer("");
			int keyPosition=i;
			int tcount=0;
			
			//solo se con delimitatore
			if(traceLine.charAt(i)==delimiter) {
			
			//legge tanti delimitatori quanti il grado della chiave
			int grade = getGrade(key);
			
			for(int j=i;j<traceLine.length();j++) {
				
				if(traceLine.charAt(j)==delimiter) {
					tcount++;
					if(tcount<=grade)
					s.append(traceLine.charAt(j));
					else break;
				}else {
					s.append(traceLine.charAt(j));
				}
			}
			}else {
				//skip 
				
			}
			//solo se è un'attività valida e coincide con la chiave
			if(s.toString().length()>1&&s.toString().equals(key)) {
				
				//per ogni attività, da questa posizione Precede/Segue/Precede&Segue direttamente. Skip se già precede&segue
				for(int k=0;k<dominion.size();k++) {
					if(relations.get(k)!=RelationType.precedeSegueDirettamente) {
						boolean precede=false;
						boolean segue=false;
						String activity = dominion.get(k);
						int activityLength= activity.length();
						if(keyPosition-activityLength >= 0) {
							
							String previous = traceLine.substring(keyPosition-activityLength, keyPosition);
							if(previous.equals(activity))
								segue=true; // se un'attività mi precede, io le succedo
						}
						
						if(keyPosition+key.length()-1+activityLength < traceLine.length()){
							
							String following = traceLine.substring(keyPosition+key.length(),keyPosition+key.length()+activityLength);
							if(following.equals(activity))
								precede=true; // se un'attività mi segue, io le precedo
							
						}
						
						if(segue&&precede)
							relations.set(k, RelationType.precedeSegueDirettamente);
						else if(segue && relations.get(k)==RelationType.precedeDirettamente) {
							relations.set(k, RelationType.precedeSegueDirettamente);
						}else if(precede && relations.get(k)== RelationType.segueDirettamente) {
							relations.set(k,RelationType.precedeSegueDirettamente);
						}else if(relations.get(k)==null) {
							if(segue)
								relations.set(k, RelationType.segueDirettamente);
							else if(precede) {
								relations.set(k, RelationType.precedeDirettamente);
							}
						}
					}
				}
			}
			
		}
		return this.relations;
	}
	
	public int getGrade(String key) {
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
