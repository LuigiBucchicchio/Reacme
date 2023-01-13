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
    private final Map<List<String>, Integer> dictionary;

    public LogData(String logName, List<Trace> traces) {
        this.logName = logName;
        this.traces = traces;
        dictionary = new HashMap<>();
    }

    public Map<List<String>, Integer> getDictionary() {
        return dictionary;
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
        for (int j = 0; j < grams; j++) {
            subsequence.add(activityList.get(j + i));
        }
        return subsequence;
    }

    public void generateDictionaryByValue(int grams) {
        List<List<String>> nGramsList = getGramsByValue(grams);
        nGramsList.forEach(this::addToDictionary);
    }

    private void addToDictionary(List<String> subsequence) {
        if (dictionary.containsKey(subsequence)) {
            dictionary.put(subsequence, dictionary.get(subsequence) + 1);
        } else {
            dictionary.put(subsequence, 1);
        }
    }


}
