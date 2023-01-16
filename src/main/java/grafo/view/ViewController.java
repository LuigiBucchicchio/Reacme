package grafo.view;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import grafo.LogUtilsRepeatingGraph;
import grafo.model.ProcessMiningRunProperties;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static grafo.EnsembleRun.prepareForHeatMap;

public class ViewController implements Initializable {


    @FXML
    public TextArea _consoleOutput;
    @FXML
    private Label _xesFiles;
    @FXML
    private TextField _gammaID;
    @FXML
    private ChoiceBox<String> _changeScoreID = new ChoiceBox<>();

    @FXML
    private TextField _nodeEqualScoreID;
    @FXML
    private TextField _nodeNotEqualScoreID;
    @FXML
    private TextField _nodeSemiEqualScoreID;
    @FXML
    private TextField _edgeEqualScoreID;
    @FXML
    private TextField _edgeNotEqualScoreID;
    @FXML
    private TextField _edgeSemiEqualScoreID;
    @FXML
    private TextField _nGramID;


    private final LogUtilsRepeatingGraph logUtils = new LogUtilsRepeatingGraph();

    private File _xesDirectory = new File("");
    private final File outputDir = new File("./output/");


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        _changeScoreID.getItems().addAll("No", "Yes");
        _changeScoreID.setValue("No");
        _consoleOutput.setEditable(false);
        PrintStream out = new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                _consoleOutput.appendText(String.valueOf((char) b));
            }
        });
        System.setOut(out);
    }

    public static List<Path> checkExtension(Path path, String xesExtension) {
        List<Path> result = null;
        try (Stream<Path> files = Files.walk(path)) {
            result = files
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(xesExtension))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /* REMINDER --> QUESTA ANNOTAZIONE, PERMETTE DI IGNORARE GLI WARNINGS. IN QUESTO CASO ESSI SONO RELATIVI AL
     *          Dereference of '_xesDirectory.listFiles()' may produce 'NullPointerException'
     */
    @SuppressWarnings("ConstantConditions")
    public void loadDirectory() throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select XES Files Directory");
        directoryChooser.setInitialDirectory(new java.io.File("."));

        _xesDirectory = directoryChooser.showDialog(null);


        List<Path> allXesFiles = checkExtension(Paths.get(_xesDirectory.getAbsolutePath()), ".xes");
        if (allXesFiles.size() <= 2) {
            System.exit(99);
        } else {
            _xesFiles.setText(_xesDirectory.getAbsolutePath());
        }

    }

    public void changeScore() {
        if (_changeScoreID.getValue().equals("Yes")) {
            _nodeEqualScoreID.setDisable(false);
            _nodeNotEqualScoreID.setDisable(false);
            _nodeSemiEqualScoreID.setDisable(false);
            _edgeEqualScoreID.setDisable(false);
            _edgeNotEqualScoreID.setDisable(false);
            _edgeSemiEqualScoreID.setDisable(false);
            _nGramID.setDisable(false);
        } else {
            _nodeEqualScoreID.setDisable(true);
            _nodeNotEqualScoreID.setDisable(true);
            _nodeSemiEqualScoreID.setDisable(true);
            _edgeEqualScoreID.setDisable(true);
            _edgeNotEqualScoreID.setDisable(true);
            _edgeSemiEqualScoreID.setDisable(true);
            _nGramID.setDisable(true);
        }
    }

    private void resetOutputDir() {
        try {
            if (!isOutputDirEmpty()) {
                deleteFiles();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isOutputDirEmpty() throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(outputDir.getPath()))) {
            return !dirStream.iterator().hasNext();
        }
    }

    private void deleteFiles() throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(outputDir.getPath()))) {
            dirStream.forEach(p -> p.toFile().delete());
        }
        isOutputDirEmpty();
    }

    public void runMining() throws Exception {
        if (outputDir.exists()) {
            resetOutputDir();
            if (!isOutputDirEmpty()) {
                deleteFiles();
            }
        }
        if (checkInputValues()) {
            startMining();
        } else {
            System.out.println("Invalid inputs");
        }
    }


    /**
     * Questo metodo permette di controllare la validità di tutti i valori inseriti: gamma, score, ngram.
     *
     * @return true se tutti i valori in input sono validi.
     */
    private boolean checkInputValues() {
        if (_changeScoreID.getValue().equals("Yes")) {
            return visualValidate(_gammaID) & visualValidate(_nodeEqualScoreID) & visualValidate(_nodeNotEqualScoreID) & visualValidate(_nodeSemiEqualScoreID) & visualValidate(_edgeEqualScoreID) & visualValidate(_edgeNotEqualScoreID) & visualValidate(_edgeSemiEqualScoreID) & validateVisualNGram(_nGramID);
        } else {
            return visualValidate(_gammaID);
        }
    }


    /**
     * Questo metodo permette di validare l'input di gamma ed eventualmente degli score.
     * Un campo è valido se è un numero reale compreso tra 0 e 1.
     *
     * @param textField il campo da validare
     */
    private boolean visualValidate(TextField textField) {
        if (isValidNumber(textField) && !isNumberOutOfRange(textField)) {
            setValidStyle(textField);
            return true;
        } else {
            setInvalidStyle(textField);
            return false;
        }
    }

    /**
     * Questo metodo serve per togliere le propriet&agrave; applicate con il metodo <code>setInvalidStyle</code>
     *
     * @param textField field al quale applicare lo stile
     */
    private void setValidStyle(TextField textField) {
        textField.setStyle("-fx-border-color: transparent ; -fx-border-width: 0px ;");
    }

    /**
     * Questo metodo serve per applicare uno stile di errore in modo da aiutare l'utente a capire qual'&egrave; il
     * field che &egrave; risultato invalido
     *
     * @param textField field al quale applicare lo stile
     */
    private void setInvalidStyle(TextField textField) {
        textField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
    }


    /**
     * Questo metodo controlla se il contenuto del Field rispetta il formato numerico
     *
     * @param textField il field da controllare
     * @return <code>true</code> se valido, <code>false</code> altrimenti
     */
    private static boolean isValidNumber(TextField textField) {
        return textField.getText().matches("^[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?$");
    }

    /**
     * Questo metodo controlla che il numero rispetti il range.
     * Il range &egrave; definito da [0.0 - 1.0]
     *
     * @param textField field al quale controllare il valore
     * @return <code>true</code> se valido, <code>false</code> altrimenti
     */
    private static boolean isNumberOutOfRange(TextField textField) {
        return Double.parseDouble(textField.getText()) < 0 || Double.parseDouble(textField.getText()) > 1;
    }

    private boolean validateVisualNGram(TextField textField) {
        if (_nGramID.getText().matches("[0-9]*") && !_nGramID.getText().equals("")) {
            setValidStyle(textField);
            return true;
        } else {
            setInvalidStyle(textField);
            return false;
        }
    }

    private void startMining() throws Exception {
        long startingTime = System.currentTimeMillis();
        Locale.setDefault(Locale.US);
        System.out.println("Log evaluation start");

        logUtils.setFileList(_xesDirectory.listFiles());
        int size = _xesDirectory.listFiles().length;

        logUtils.setTraceNum(new int[size]);
        logUtils.setAvgTraceLen(new double[size]);
        logUtils.setScoreChange(true);
        logUtils.setTreCifre(false);

        setProcessMiningRunProperties();

        logUtils.analyzeTraces();

        logUtils.applyGramsToLogs(logUtils.getProcessMiningRunProperties().getGrams());

        String[][] distanceMatrix = logUtils.generateDistanceMatrix();

        logUtils.convertToCSV(distanceMatrix);


        System.out.println("Evaluation Terminated - Execution Time:" + (System.currentTimeMillis() - startingTime));
        // Multi-threading
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("System cores: " + cores);
        File script = new File(Optional.ofNullable(System.getenv("CLUSTERING_SCRIPT_PATH")).orElse("main.py"));
        String scriptPath = script.getAbsolutePath();
        scriptPath = scriptPath.replace('\\', '/');
        File currentDirectory = new File("");
        String currentPath = currentDirectory.getAbsolutePath();
        currentPath = currentPath.replace('\\', '/');
        if (cores > 1 && ((logUtils.getFileList().length - 2) > (cores * 2))) {
            System.out.println("Clustering Algorithm start");
            runProcessMultiCores(logUtils, cores, scriptPath, currentPath);
            System.out.println("\nClustering Algorithm terminated - total execution time: " + (System.currentTimeMillis() - startingTime));
            System.out.println("Incoming Results on output directory...");
            List<File> outputList = getFilesFromProcess();
            File winner = selectWinnerFile(outputList);
            File[] winners = deleteLosersFiles(outputList, winner);
            moveFilesToOutputDirectory(winners[0], winners[1]);
            System.out.println("Done");
        } else {
            System.out.println("Clustering Algorithm start");
            runProcessSingleCore(logUtils, scriptPath, currentPath);
            System.out.println("Clustering Algorithm terminated - total execution time: " + (System.currentTimeMillis() - startingTime));
            System.out.println("Incoming Results on output directory...");
            Thread.sleep(1000);
            List<File> outputList = getFilesFromProcess();
            if (outputList.size() == 2) {
                moveFilesToOutputDirectory(outputList.get(0), outputList.get(1));
            }
            System.out.println("Done");
        }
        logUtils.generateNodeListReport("CUSTOM");
        prepareForHeatMap();
        // closeApplication();


    }

    private void setProcessMiningRunProperties() {
        // Coi valori di default
        ProcessMiningRunProperties processMiningRunProperties = new ProcessMiningRunProperties();
        var gamma = Double.parseDouble(_gammaID.getText());

        if (_changeScoreID.getValue().equals("Yes")) {
            var nodeEqualScoreID = Double.parseDouble(_nodeEqualScoreID.getText());
            var nodeNotEqualScoreID = Double.parseDouble(_nodeNotEqualScoreID.getText());
            var nodeSemiEqualScoreID = Double.parseDouble(_nodeSemiEqualScoreID.getText());
            var edgeEqualScoreID = Double.parseDouble(_edgeEqualScoreID.getText());
            var edgeNotEqualScoreID = Double.parseDouble(_edgeNotEqualScoreID.getText());
            var edgeSemiEqualScoreID = Double.parseDouble(_edgeSemiEqualScoreID.getText());
            var nGramID = Integer.parseInt(_nGramID.getText());
            // Coi valori dell'utente
            processMiningRunProperties = new ProcessMiningRunProperties(edgeEqualScoreID, edgeSemiEqualScoreID, edgeNotEqualScoreID, nodeEqualScoreID, nodeSemiEqualScoreID, nodeNotEqualScoreID, gamma, nGramID);
        } else {
            // Il gamma e gli n gram a prescindere sono dati in input
            processMiningRunProperties.setGamma(gamma);
        }
        logUtils.setProcessMiningRunProperties(processMiningRunProperties);
    }

    private static void checkNumerOfFilesAvailable(int size) {
        if (size <= 2) {
            System.out.println("Not enough Input XES Files found");
            System.exit(99);
        }
    }

    /**
     * sposta i file vincitori nella cartella di output
     *
     * @param winners
     * @param winners1
     */
    private void moveFilesToOutputDirectory(File winners, File winners1) {
        String parentDir0 = winners.getParent();
        parentDir0 = parentDir0 + "\\output";
        String winner0name = winners.getName();
        winners.renameTo(new File(parentDir0 + "\\" + winner0name));

        String parentDir1 = winners1.getParent();
        parentDir1 = parentDir1 + "\\output";
        String winner1name = winners1.getName();
        winners1.renameTo(new File(parentDir1 + "\\" + winner1name));
    }

    /**
     * cancella i file smallOut non vincitori (max più basso)
     *
     * @param outputList
     * @param winner
     * @return
     */
    private File[] deleteLosersFiles(List<File> outputList, File winner) {
        File[] winners = new File[2];
        int winnersIndex = 0;
        String winnerName = winner.getName();
        for (File file : outputList) {
            int winnerNameIndex = winnerName.indexOf("smallOut");
            String winnerNameNumber = winnerName.substring(0, winnerNameIndex);
            if (!file.getName().contains(winnerNameNumber)) {
                file.deleteOnExit();
            } else {
                winners[winnersIndex] = file;
                winnersIndex++;
            }
        }
        return winners;
    }

    /**
     * seleziona il file smallOut con lo score più alto
     *
     * @param outputList
     * @return
     * @throws IOException
     * @throws CsvValidationException
     */
    private File selectWinnerFile(List<File> outputList) throws IOException, CsvValidationException {
        Iterator<File> outputFileIterator = outputList.iterator();
        double max = 0.0;
        File winner = null;
        CSVReader reader;
        while (outputFileIterator.hasNext()) {
            File nextOutputFile = outputFileIterator.next();
            if (nextOutputFile.getName().contains("smallOut")) {
                reader = new CSVReader(new FileReader(nextOutputFile));
                String[] row = reader.readNext();

                double score = Double.parseDouble(row[1]);
                if (score > max) {
                    max = score;
                    winner = nextOutputFile;
                }
                reader.close();
            }
        }
        return winner;
    }

    /**
     * @param logUtils
     * @param scriptPath
     * @param currentPath
     * @throws IOException
     * @throws InterruptedException
     */
    private void runProcessSingleCore(LogUtilsRepeatingGraph logUtils, String scriptPath, String currentPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("python", scriptPath, "" + 2, "" + logUtils.getFileList().length + "", "" + currentPath + "\\output");
        Process p = pb.start();
        p.waitFor();
        BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = bfr.readLine()) != null) {
            System.out.println(line);
        }
        bfr.close();
    }

    /**
     * @return
     */
    private List<File> getFilesFromProcess() {
        File dir = new File("");
        String dirPath = dir.getAbsolutePath();
        dir = new File(dirPath);

        List<File> fileList = new ArrayList<>();
        Collections.addAll(fileList, dir.listFiles());

        List<File> outputList = new ArrayList<>();
        for (File nextFile : fileList) {
            if (nextFile.getName().contains("clustering") || nextFile.getName().contains("smallOut"))
                outputList.add(nextFile);
        }
        return outputList;
    }

    /**
     * @param logUtils
     * @param cores
     * @param scriptPath
     * @param currentPath
     * @throws IOException
     * @throws InterruptedException
     */
    private void runProcessMultiCores(LogUtilsRepeatingGraph logUtils, int cores, String scriptPath, String currentPath) throws IOException, InterruptedException {
        ProcessBuilder[] builders = new ProcessBuilder[cores];
        Process[] processes = new Process[cores];

        int subpart = (int) Math.floor((double) logUtils.getFileList().length / cores);

        int diff = logUtils.getFileList().length - subpart * cores;

        int last = 0;


        for (int i = 0; i < cores; i++) {
            ProcessBuilder pb;
            if (i == 0) {
                pb = new ProcessBuilder("python", scriptPath, "" + 2, "" + (last + subpart + diff) + "", "" + currentPath + "\\output");
                last = last + subpart + diff;
            } else {
                pb = new ProcessBuilder("python", scriptPath, "" + last, "" + (last + subpart) + "", "" + currentPath + "\\output");
                last = last + subpart;
            }
            pb.redirectErrorStream(true);
            builders[i] = pb;
        }

        for (int i = 0; i < cores; i++) {
            processes[i] = builders[i].start();
        }

        System.out.print("waiting for " + processes.length + " processes to end");
        for (int i = 0; i < cores; i++) {
            processes[i].waitFor();
            System.out.print(".");
        }
    }

    public void closeApplication() throws IOException {
        if (isOutputDirEmpty()) {
            Platform.exit();
        } else {
            try {
                Desktop.getDesktop().open(outputDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Platform.exit();
        }
    }


}

