package sottostringhe;

public class TraceTest {

	public static void main(String[] args) {


//		TraceAnalyzer ta= new TraceAnalyzer("t11t9t11133t26t35t41t36t42t62t54t66t75t33t74t23t64t35t41t53"
//				+ "t43t34t42t25t36t62t33t22t35t41t42t54t33t66t23t32t82t41t51t62t71t73"
//				+ "t63t42t51t62t72t63t42t51t62t71t73t63t42t42t33t22t32t41t51t72t63t42t51t62t72t63"
//				+ "t42t42t33t22t32t41t42t33t23t21t32t41t42t33t23t32t41t42t33t22t35t41t42t33t22t32t41"
//				+ "t51t62t72t63t42t51t62t71t73t63t42t51t61t71t81t91");
//		
//		ta.KMPAnalyze();
//		System.out.println("RepeatingSet: "+ta.getRepeatingSet());
//		System.out.println("KMP time: "+ta.getTime());
		
		String s="t21t21";
		String s2="AA";
		String s3="miciomicio";
		
		String prima = s.substring(0, s.length()/2);
		String seconda = s.substring(s.length()/2, s.length());
		boolean same = prima.equals(seconda);
		System.out.println(prima+" "+seconda+", "+same);
		prima = s2.substring(0, s2.length()/2);
		seconda = s2.substring(s2.length()/2, s2.length());
		same = prima.equals(seconda);
		System.out.println(prima+" "+seconda+", "+same);
		prima = s3.substring(0, s3.length()/2);
		seconda = s3.substring(s3.length()/2, s3.length());
		same = prima.equals(seconda);
		System.out.println(prima+" "+seconda+", "+same);
		
	}

}
