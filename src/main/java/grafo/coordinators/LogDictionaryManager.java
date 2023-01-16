package grafo.coordinators;

import grafo.Trace;
import grafo.model.LogData;
import grafo.model.ProcessMiningRunProperties;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Questa classe è il nuovo LogUtils. La differenza principale sta nel fatto
 * che questa classe permette di confrontare i log con l'utilizzo dei dizionari
 */
public class LogDictionaryManager {

    private final String filesDirectory;
    private File[] listFiles = null;

    private final ProcessMiningRunProperties runProperties;

    private List<LogData> analyzedLogs;

    public static void main(String[] args) {
        LogDictionaryManager logCoordinator = new LogDictionaryManager("C:\\Users\\bogda\\Desktop\\Git\\LogMetrics\\input2", new ProcessMiningRunProperties());
        logCoordinator.analyzeTraces();
        logCoordinator.applyGramsToAnalyzedLogs();
        logCoordinator.generateDistanceMatrix();
    }

    /**
     * Concettualmente per lavorare con i file .xes e generare la distance matrix abbiamo bisogno
     * di avere in nostro possesso la directory dei file xes e le proprietà dell'esecuzione.
     *
     * @param filesDirectory - la directory di input per i files di log in formato .xes
     * @param runProperties  - le proprietà da applicare all'algoritmo della generazione delle distanze
     * @see ProcessMiningRunProperties
     */
    public LogDictionaryManager(String filesDirectory, ProcessMiningRunProperties runProperties) {
        this.filesDirectory = filesDirectory;
        this.runProperties = runProperties;
        analyzedLogs = new LinkedList<>();
        initConfig();

    }


    private void initConfig() {
        File directory = new File(filesDirectory);
        listFiles = directory.listFiles();
    }

    public void analyzeTraces() {
        for (File xesFile : listFiles) {
            try {
                XLog log = parseXES(xesFile.getAbsolutePath());
                analyzedLogs.add(convertToLogData(xesFile.getName(), extractTraces(log, xesFile.getName())));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    private List<Trace> extractTraces(XLog log, String fileName) {
        List<Trace> traces = new LinkedList<>();
        for (XTrace trace : log) {
            ArrayList<String> activitySequence = new ArrayList<>();
            StringBuffer traceLine = new StringBuffer();
            trace.stream().map(xEvent -> xEvent.getAttributes().get("concept:name").toString()).forEach(activity ->
                    addActivityToTraceLineAndSequence(activitySequence, traceLine, activity));
            Trace toAdd = new Trace();
            toAdd.setActivitySequence(activitySequence);
            toAdd.setTraceLine(traceLine.toString());
            toAdd.setLogId(fileName);
            toAdd.setTraceId(trace.getAttributes().get("concept:name").toString());
            traces.add(toAdd);
        }
        return traces;
    }

    private LogData convertToLogData(String logName, List<Trace> traces) {
        return new LogData(logName, traces);
    }

    private void addActivityToTraceLineAndSequence(ArrayList<String> activitySequence, StringBuffer traceLine, String activity) {
        traceLine.append(activity);
        activitySequence.add(activity);
    }

    private XLog parseXES(String filePath) throws Exception {
        XesXmlParser parser = new XesXmlParser();
        return parser.parse(new File(filePath)).get(0);
    }

    public void applyGramsToAnalyzedLogs() {
        analyzedLogs.forEach(LogData::generateDictionaryOfActivities);
        analyzedLogs.forEach(logData -> logData.generateDictionaryOfGramsByValue(runProperties.getGrams()));
        analyzedLogs.forEach(LogData::printMapOfActivities);
        analyzedLogs.forEach(LogData::printMapOfGrams);
    }

    public void generateDistanceMatrix() {

    }

}
