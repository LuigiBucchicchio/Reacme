package sottostringhe;

import java.util.ArrayList;
import java.util.List;

public class LogStringOccurrenceTest { 

	public static void main(String[] args) {
		
		// non ancora finito lo sviluppo
		
		String trace1= "t11t11t11t11t11t11t11t11t11t11"; 
		String trace2= "t11t12t13t14t15t16t17t18t19t20"; 
		String trace3 = "t34t12t34t56t56t76t90t10t49t12t34t56";
		List<String> traceList = new ArrayList<String>();
		traceList.add(trace1);
		traceList.add(trace2);
		traceList.add(trace3);
		
		LogStringOccurrence log= new LogStringOccurrence();
		log.generateRepeatingSet(traceList);
		
		System.out.println(log.getRepeatingSet());
		

	}

}
