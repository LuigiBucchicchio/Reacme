package grafo;


public class GraphTraceAnalyzerTest {

	public static void main(String[] args) {
		
		// Join tra più tracce per rappresentare i log.
		
		// t11 -- 1 --> t12   grafo 1 (traccia 1)
		// t11 -- 0 --> t12   grafo 2 (traccia 2)
		// t11 -- 3 --> t13   grafo 1 (traccia 1)
		// t11 -- 0 --> t13   grafo 2 (traccia 2)
		// t11 -- 1 --> t14   grafo 3 (traccia 3)
		
		// confronto tra grafi/tracce. 
		// Map<Traccia,Edge,Integer> mappa ripetizioni (oppure sul grafo tenere conto di una Lista di Interi invece di un intero
		
		// lista. traccia 1 t11-->t12 3 volte
	    // lista traccia 2 t11-->t12 4 volte
		
		
		// determinazione di tutti i cicli
		
		// punto di accumulazione nel grafo. Nodi caldi?
		
		
		//superset nodi. punteggio Similarità NODI punteggio diviso N nodi
		
		
		GraphTraceAnalyzer analyzer= new GraphTraceAnalyzer();
		analyzer.setTrace("t11t11t12t13t14t15t12t13t14t15t11t12t13t14");
		analyzer.GraphAnalyze(); 
		analyzer.GraphImage("traceTestGraph");
		
		GraphTraceAnalyzer analyzer2= new GraphTraceAnalyzer();
		analyzer2.setTrace("t11t21t32t26t35t62t41t43t51t62t72t34t63t25t42t35t62t54t51t65t75t62t82t72t63t42t51t62t72t63t42t51t62t71t73t63t42t51t62t72t63t42t51t62t72t63t42t51t62t72t63t42t51t62t72t63t42t51t62t71t73t63t42t51t62t72t63t42t51t62t71t73t63t42t51t62t72t63t42t51t62t72t63t42t51t62t72t63t42t51t62t72t63t42t51t62t72t63t42t51t62t71t73t63t42t51t62t72t63t42t51t62t72t63t42t51t62t72t63t42t51t62t71t73t63t42t51t62t72t63t42t51t62t72t63t42t51t62t71t81t91");
		analyzer2.GraphAnalyze(); 
		analyzer2.GraphImage("traceTestGraph");
		//analyzer2.Tarjan();
	}

}
