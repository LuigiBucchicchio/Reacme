package grafo.model;

import grafo.Trace;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Contenitore per i dati analizzati di un file log. Usiamo questa classe per generare il dizionario
 * che è utile nella generazione dell distance matrix
 */
public class LogData {
    private String logName;
    private final List<Trace> traces;
    private final Map<List<String>, Integer> dictionaryOfGrams;

    private final Map<String, Integer> dictionaryOfActivities;

    public LogData(String logName, List<Trace> traces) {
        this.logName = logName;
        this.traces = traces;
        this.dictionaryOfGrams = new HashMap<>();
        this.dictionaryOfActivities = new HashMap<>();
    }

    public Map<List<String>, Integer> getDictionary() {
        return dictionaryOfGrams;
    }

    public void generateDictionaryOfGramsByValue(int grams) {
        List<List<String>> nGramsList = getGramsByValue(grams);
        nGramsList.forEach(this::addToDictionary);
    }

    public void generateDictionaryOfActivities() {
        for (Trace trace : traces)
            trace.getActivitySequence().forEach(this::addToActivityDictionary);

    }

    private void addToActivityDictionary(String activity) {
        if (dictionaryOfActivities.containsKey(activity)) {
            dictionaryOfActivities.put(activity, dictionaryOfActivities.get(activity) + 1);
        } else {
            dictionaryOfActivities.put(activity, 1);
        }
    }

    private List<List<String>> getGramsByValue(int gramsValue) {
        List<List<String>> toReturn = new LinkedList<>();
        for (Trace trace : traces) {
            List<String> activityList = trace.getActivitySequence();
            for (int i = 0; i < activityList.size() - gramsValue; i++) {
                toReturn.add(generateSubSequence(gramsValue, activityList, i));
            }
        }
        return toReturn;
    }

    private List<String> generateSubSequence(int grams, List<String> activityList, int i) {
        List<String> subsequence = new LinkedList<>();
        for (int subsequenceStartPosition = 0; subsequenceStartPosition < grams; subsequenceStartPosition++) {
            subsequence.add(activityList.get(subsequenceStartPosition + i));
        }
        return subsequence;
    }

    private void addToDictionary(List<String> subsequence) {
        if (dictionaryOfGrams.containsKey(subsequence)) {
            dictionaryOfGrams.put(subsequence, dictionaryOfGrams.get(subsequence) + 1);
        } else {
            dictionaryOfGrams.put(subsequence, 1);
        }
    }

    @Override
    public String toString() {
        StringBuilder toReturn = new StringBuilder();
        dictionaryOfGrams.forEach((key, value) -> toReturn.append(key).append(" : ").append(value).append("\n"));
        return toReturn.toString();
    }

    public void printMapOfGrams() {
        dictionaryOfGrams.forEach((key, value) -> System.out.println(key + " :" + value));
    }

    public void printMapOfActivities() {
        dictionaryOfActivities.forEach((key, value) -> System.out.println(key + " :" + value));
    }


    public String getName() {
        return this.logName;
    }
}
