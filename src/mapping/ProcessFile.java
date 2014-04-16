/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapping;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author rachelmills
 */
public class ProcessFile {

    private final static Charset ENCODING = StandardCharsets.UTF_8;

    private FileOutputStream out;
    private BufferedWriter bufferedWriter;

    private final Path path;
    private Map<Integer, String> map;
    private Map<String, Integer> spamMap;
    private final Map<Integer, Frequency> frequencyMap;

    public ProcessFile(Path path, String outputPath) {
//        try {
//            out = new FileOutputStream(outputPath + "ClusterSpamPercentage.txt");
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(ProcessFile.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        try {
//            bufferedWriter = new BufferedWriter(new OutputStreamWriter(out, "UTF8"));
//        } catch (UnsupportedEncodingException ex) {
//            Logger.getLogger(ProcessFile.class.getName()).log(Level.SEVERE, null, ex);
//        }
        this.path = path;
        this.frequencyMap = new HashMap<>();
    }

    void processLineByLine(boolean spamMappingYesNo) {
        try (Scanner scanner = new Scanner(path, ENCODING.name())) {
            while (scanner.hasNextLine()) {
                if (spamMappingYesNo == false) {
                    processLine(scanner.nextLine());
                } else {
                    processLineForSpamEval(scanner.nextLine());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ProcessFile.class.getName()).log(Level.SEVERE, null, ex);
        }
//        try {
//            bufferedWriter.close();
//        } catch (IOException ex) {
//            Logger.getLogger(ProcessFile.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    private void processLine(String nextLine) {

        String title = null;
        //use a second Scanner to parse the content of each line 
        Scanner sc = new Scanner(nextLine);

        // extract and remove id from line
        int id = Integer.parseInt(sc.next().replaceAll("[^\\d]", ""));
//        int id = Integer.valueOf(sc.nextInt());
        int cluster = sc.nextInt();

        // find id in map
        title = getMap().get(id);

        try {
            bufferedWriter.write(id + ", " + title + ", " + cluster + "\n");
            bufferedWriter.flush();
        } catch (IOException ex) {
            Logger.getLogger(ProcessFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processLineForSpamEval(String nextLine) {
        int spamScore = 0;
        Scanner sc = new Scanner(nextLine);
        String cluewebId = sc.next();
        int cluster = sc.nextInt();

        if (spamMap.get(cluewebId) != null) {
            spamScore = spamMap.get(cluewebId);
            if (getFrequencyMap().containsKey(cluster)) {
                int total = getFrequencyMap().get(cluster).getTotal() + spamScore;
                int count = getFrequencyMap().get(cluster).getCount() + 1;
                getFrequencyMap().put(cluster, new Frequency(total, count));
            } else {
                getFrequencyMap().put(cluster, new Frequency(spamScore, 1));
            }
//            System.out.println("Frequency Map = " + cluster + "Count:  " + getFrequencyMap().get(cluster).getCount() + "Total:  " + getFrequencyMap().get(cluster).getTotal());
        }      
    }

    /**
     * @return the map
     */
    public Map<Integer, String> getMap() {
        return map;
    }

    /**
     * @param map the map to set
     */
    public void setMap(Map<Integer, String> map) {
        this.map = map;
    }

    /**
     * @return the spamMap
     */
    public Map<String, Integer> getSpamMap() {
        return spamMap;
    }

    /**
     * @param spamMap the spamMap to set
     */
    public void setSpamMap(Map<String, Integer> spamMap) {
        this.spamMap = spamMap;
    }

    /**
     * @return the frequencyMap
     */
    public Map<Integer, Frequency> getFrequencyMap() {
        return frequencyMap;
    }
}
