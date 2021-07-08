package grafo;

import java.util.List;

public class Trace implements Comparable<Trace>{
	
	private List<String> activitySet;
	private String traceLine;
	private String traceId;
	private String logId;
	
	public List<String> getActivitySet() {
		return activitySet;
	}
	public void setActivitySet(List<String> activitySet) {
		this.activitySet = activitySet;
	}
	public String getTraceLine() {
		return traceLine;
	}
	public void setTraceLine(String traceLine) {
		this.traceLine = traceLine;
	}
	public String getTraceId() {
		return traceId;
	}
	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}
	public String getLogId() {
		return logId;
	}
	public void setLogId(String logId) {
		this.logId = logId;
	}
	@Override
	public int compareTo(Trace o) {
		if(this.traceLine.equals(o.traceLine))
			return 0;
		else return this.traceId.compareTo(o.getTraceId());
	}
	
	

}
