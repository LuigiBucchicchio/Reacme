package sottostringhe;

public class TraceTest {

	public static void main(String[] args) {


		TraceAnalyzer ta= new TraceAnalyzer("t11t9t11133t26t35t41t36t42t62t54t66t75t33t74t23t64t35t41t53"
				+ "t43t34t42t25t36t62t33t22t35t41t42t54t33t66t23t32t82t41t51t62t71t73"
				+ "t63t42t51t62t72t63t42t51t62t71t73t63t42t42t33t22t32t41t51t72t63t42t51t62t72t63"
				+ "t42t42t33t22t32t41t42t33t23t21t32t41t42t33t23t32t41t42t33t22t35t41t42t33t22t32t41"
				+ "t51t62t72t63t42t51t62t71t73t63t42t51t61t71t81t91");
		
		ta.KMPAnalyze();
		System.out.println("RepeatingSet: "+ta.getRepeatingSet());
		System.out.println("KMP time: "+ta.getTime());
		
	}

}
