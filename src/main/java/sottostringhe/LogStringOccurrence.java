package sottostringhe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class LogStringOccurrence {

	private Set<String> repeatingSet = new HashSet<String>();

		private List<List<Integer>> occurrencesList = new ArrayList<List<Integer>>();
		private List<String> keyList = new ArrayList<String>();
		
		public int getNonOverlapOccurrencesNumber(String key) {
			int times=1;
			int index = keyList.indexOf(key);
			List<Integer> occList = occurrencesList.get(index);
			for(int i=0; i<(occList.size()-1);i++) {
				for(int j=i+1; j<(occList.size()-1);j++) {
					if(Math.abs(occList.get(i)-occList.get(j)) >= key.length())
						times++;
				}
			}
			return times;
		}
		
		public Set<String> getRepeatingSet(){
			return this.repeatingSet;
		}
		
		public List<Integer> getOccurrences(String key) {
			int index = keyList.indexOf(key);
			return occurrencesList.get(index);
		}
		
		public void addOccurrence(String key, int occ) {
			int index= keyList.indexOf(key);
			occurrencesList.get(index).add(occ);
		}
		
		public void add(String key) {
			keyList.add(key);
			List<Integer> occurrences = new ArrayList<Integer>();
			occurrencesList.add(occurrences);
			
		}
		
		public void setOccurrences(String key, int occurrenceIndex) {
			getOccurrences(key).add(occurrenceIndex);
		}
	
	public void generateRepeatingSet(List<String> traces) {
		Iterator<String> traceIt = traces.iterator();
		boolean setUpdate = false;
		
			while(traceIt.hasNext()) {
				occurrencesList = new ArrayList<List<Integer>>();
				keyList = new ArrayList<String>();
				String trace= traceIt.next();
				int grade=1;
				do {
				setUpdate = false;
				
				
				if(grade<=1) {
			    StringBuffer ref = new StringBuffer();
			    StringBuffer letta = new StringBuffer();
			    
				for(int i=0; i<trace.length();i++) {
					if(!(trace.charAt(i)=='t'))
						throw new IllegalArgumentException();
					
					ref.append(trace.charAt(i)); //t
					int initialIndex = i;
					while(true) {
						i++;
						if(i>=trace.length() || trace.charAt(i)=='t') {
							i--;
							break;
						}else {
							ref.append(trace.charAt(i)); //t11
						}
					}
					
					if(!this.keyList.contains(ref.toString())) {
						add(ref.toString());
						addOccurrence(ref.toString(), initialIndex);
					
					for(int j=i+1; j<trace.length();j++) {
						
						if(!(trace.charAt(j)=='t'))
							throw new IllegalArgumentException();
						
						letta.append(trace.charAt(j)); //t
						int occurrenceIndex= j; 
						
						while(true) {
							j++;
							if(j>=trace.length() || trace.charAt(j)=='t') {
								j--;
								break;
							}else {
								letta.append(trace.charAt(j));
							}
						}
						
						if(ref.toString().equals(letta.toString())) {
							addOccurrence(letta.toString(), occurrenceIndex);
							if((getOccurrences(letta.toString()).size()>1) && !repeatingSet.contains(letta.toString())) {
								repeatingSet.add(letta.toString());
								setUpdate=true;
							}
						}
					letta.delete(0, letta.length());	
					}
					
					
				}else {
					// key already done
				}
				ref.delete(0, ref.length());
					
				}
				
				}// fare questo solo per il grado 1
				else {
					
					//grado maggiore di 1 
					int firstStringEnd=0;
					for(int i=0;i<trace.length();i++) {
						
					StringBuffer ref = new StringBuffer();	
						int tcount=0;
						int conta=0;
						
						while(tcount<=grade && i<trace.length()) {
							if(trace.charAt(i+conta)=='t') {
								tcount++;
								if(tcount==2)
									firstStringEnd=firstStringEnd+conta;
							}
							if(tcount<=grade)
							ref.append(trace.charAt(i+conta));
							conta++;
							if((i+conta)>=trace.length()) {
								i=trace.length();
								break;
							}
						}
					    
						
						if(!this.keyList.contains(ref.toString())) {
							
								add(ref.toString());
						        
							
							String firstString = ref.toString().substring(i,firstStringEnd);
						String endString = ref.toString().substring(firstStringEnd, ref.length());
						
						List<Integer> occ1 = getOccurrences(firstString);
						List<Integer> occ2 = getOccurrences(endString);
						
						Iterator<Integer> occIterator =occ1.iterator();
						while(occIterator.hasNext()) {
							int occ = occIterator.next();
							for(int k=0; k<(occ2.size()-1);k++) {
								
								if((occ2.get(k) == occ+firstString.length())) {
									addOccurrence(ref.toString(), occ);
							        if((getNonOverlapOccurrencesNumber(ref.toString())>1) && (!repeatingSet.contains(ref.toString()))) {
							        	repeatingSet.add(ref.toString());
							        	setUpdate=true;
							        }
								}
								
							}// per tutte le seconde occorrenze
						}//per tutte le prime occorrenze
						}else {
							//string already done
						}
						i=firstStringEnd-1;
					}
					
				}
			grade++;
			}while(setUpdate);
				
		
			
		} // next trace
	}
	
}
