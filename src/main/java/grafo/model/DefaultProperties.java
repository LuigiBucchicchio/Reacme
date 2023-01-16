package grafo.model;

/**
 * Questa enumerazione rappresenta i parametri di default per
 * l'esecuzione del Process Mining
 */
public enum DefaultProperties {
    DEFAULT_EDGE_EQUAL_SCORE(1.0),
    DEFAULT_EDGE_SEMI_EQUAL_SCORE(0.0),
    DEFAULT_EDGE_NOT_EQUAL_SCORE(0.0),
    DEFAULT_NODE_EQUAL_SCORE(1.0),
    DEFAULT_NODE_SEMI_EQUAL_SCORE(0.0),
    DEFAULT_NODE_NOT_EQUAL_SCORE(0.0),
    DEFAULT_GAMMA(0.0),
    DEFAULT_GRAMS(3);

    private final Number value;

    DefaultProperties(Number value) {
        this.value = value;
    }

    public Double getValue() {
        return value.doubleValue();
    }

}
