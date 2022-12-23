package grafo.controller;

import grafo.Trace;
import org.graphstream.graph.Node;

import java.util.*;
import java.util.stream.Stream;

public class TraceController {

    public static Map<List<String>, Integer> dictionary = new HashMap<>();

    public static List<List<String>> generateGrams(int n, List<String> activitySequence) {
        List<List<String>> toReturn = new LinkedList<>();
        for (int i = 0; i < activitySequence.size() - n; i++) {
            List<String> x = new LinkedList<>();
            for (int j = 0; j < n; j++) {
                x.add(activitySequence.get(j + i));
            }
            checkAndAddToDictionary(x);
            toReturn.add(x);
        }

//        System.out.println("Grams for this trace: ");
//        Stream.of(toReturn).forEach(System.out::println);
        return toReturn;
    }

    private static void checkAndAddToDictionary(List<String> x) {
        if (dictionary.containsKey(x)) {
            dictionary.put(x, dictionary.get(x) + 1);
        } else {
            dictionary.put(x, 1);
        }
    }
}
