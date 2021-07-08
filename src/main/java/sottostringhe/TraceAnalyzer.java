package sottostringhe;
import java.util.Set;
import java.util.TreeSet;

public class TraceAnalyzer {

	private String trace;
	private Set<String> repeatingSet;
	private long startingTime;
	private long finishTime;
	private char delimiter='t';

	public TraceAnalyzer() {
		trace=null;
		repeatingSet=null;
	}

	public TraceAnalyzer(String trace) {
		this.trace=trace;
	}

	public Set<String> getRepeatingSet() {
		return this.repeatingSet;
	}

	public String getTrace() {
		return this.trace;
	}

	public void setTrace(String trace) {
		this.trace=trace;
	}

	public long getTime() {
		return this.finishTime;
	}

	public void KMPAnalyze() {
		this.startingTime=System.currentTimeMillis();
		StringBuffer check=new StringBuffer("");
		this.repeatingSet= new TreeSet<String>();
		//la prima volta non si scorda mai
		boolean setUpdated=true;
		//numero di caratteri letti di fila
		int grade=1;

		//per ogni Grado finché il grado precendente ha avuto update
		while(setUpdated) {
			setUpdated=false;

			int checkpoint=0;

			for(int i=0;i<trace.length();i++) {
				i=checkpoint;

				//leggi se non si sfora
				if((i+(grade*2)-1)<trace.length()) {

					//scorri e leggi tanti task quanti il grado

					//
					//t11t54t63t753t63t432
					int tcount=0;
					int j;
					for(j=i;j<trace.length();j++) {

						if(trace.charAt(j)==delimiter) { 
							if(tcount==1)
								checkpoint=j;
							tcount++;
							if(tcount>grade)
								break;
						}
						check.append(trace.charAt(j));
					}
					//to exit
					i=j-1;

					//solo se non è già una stringa ripetuta conosciuta
					if(check.length()==0||!this.repeatingSet.contains(check.toString())) {

						//step
						boolean update=KMPAlgorithm(check.toString());
						if(update)
							setUpdated=update;
					}

				}
				check.delete(0, check.length());
			}
			grade++;
		}
		this.finishTime=System.currentTimeMillis()-this.startingTime;
	}

	private boolean KMPAlgorithm(String pattern) {
		int times=0;
		int M = pattern.length();
		int N = trace.length();

		// array of Longest proper prefixes which are also proper suffixes

		// es. for pattern ABCAB
		// index [0.....length]
		// lps in index 3 is A.
		// lps in index 4 is AB. (B is not the longest)

		int lps[] = new int[M];
		int j = 0; // indice per il pattern

		// Preprocess the pattern (calculate lps[] array)

		computeLPSArray(pattern, M, lps);

		//computeAndPrintLPSArray(pattern, M, lps);

		int i = 0; // indice della traccia
		StringBuffer buffer=new StringBuffer("");
		while (i < N) {
			//System.out.println("checking ["+pattern.charAt(j)+"] == ["+trace.charAt(i)+"]");
			if (pattern.charAt(j) == trace.charAt(i)) {
				j++;
				i++;
			}
			if (j == M) {
				//System.out.println("Found pattern at index " + (i - j));
				times++;
				//se c'è un match ed è la seconda volta, vuol dire che c'è una ripetizione.
				if(times>1) {
					for(int k=0;k<pattern.length();k++) {
						buffer.append(trace.charAt((i-j)+k));
					}
					this.repeatingSet.add(buffer.toString());
					buffer.delete(0, buffer.length());
					return true;
				}
				j = lps[j - 1];
			}

			// mismatch after j matches
			else if (i < N && pattern.charAt(j) != trace.charAt(i)) {
				// torniamo indietro usando l'informazione dell' LPS

				//es. traccia= ABCDABCABCABF pattern= ABCABF
				// lps = 0,0,0,1,2,0

				//cercando sulla traccia all'indice 9 "C"=/="F" ci troviamo un mismatch dopo match ABCAB,
				//invece di cercare da capo, lps mi dice di arretrare di 2 e riprovare
				//allora cercherò "C"=="C" e possiamo proseguire con A poi B poi F e quindi match dell'interno pattern.

				if (j != 0)
					j = lps[j - 1];
				else
					i = i + 1;
			}
		}
		return false;
	}

	private void computeLPSArray(String pattern, int M, int lps[])
	{

		// length of the previous longest prefix suffix
		int len = 0;
		int i = 1;
		lps[0] = 0; // lps[0] is always 0

		// for i = 1 to M-1
		while (i < M) {
			if (pattern.charAt(i) == pattern.charAt(len)) {
				len++;
				lps[i] = len;
				i++;
			}
			else // (pattern[i] != pattern[len])
			{

				if (len != 0) {
					len = lps[len - 1];
					// not increment i
				}
				else // if (len == 0)
				{
					lps[i] = len;
					i++;
				}
			}
		}

	}

	public void computeAndPrintLPSArray(String pattern, int M, int lps[])
	{

		StringBuffer lpsTableForPrint = new StringBuffer("");

		// length of the previous longest prefix suffix
		int len = 0;
		int i = 1;
		lps[0] = 0; // lps[0] is always 0
		lpsTableForPrint.append("[0");
		// for i = 1 to M-1
		while (i < M) {
			if (pattern.charAt(i) == pattern.charAt(len)) {
				len++;
				lps[i] = len;
				lpsTableForPrint.append(len);
				i++;
			}
			else // (pattern[i] != pattern[len])
			{

				if (len != 0) {
					len = lps[len - 1];
					// not increment i
				}
				else // if (len == 0)
				{
					lps[i] = len;
					lpsTableForPrint.append(len);
					i++;
				}
			}
		}
		lpsTableForPrint.append("]");
		System.out.println("Longest Prefix Suffix Table Created: "+lpsTableForPrint.toString());
	}

	public char getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}

}