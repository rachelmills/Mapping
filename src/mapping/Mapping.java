/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapping;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import cluewebhelper.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author rachelmills
 */
public class Mapping {

    final static boolean SPAM_MAPPING_TRUE = true;
    final static boolean SPAM_MAPPING_FALSE = false;
    private final Map<Integer, Frequency> frequencyMap = new HashMap<>();
    private Properties prop;
    private InputStream input;
    private BufferedReader br;
    private BufferedWriter bw;
    private FileOutputStream out;
    GZIPInputStream gzipstream;
    Map<String, Integer> cluewebMap = new HashMap<>();

    public Mapping() {
        prop = new Properties();
        input = null;
        //   bw = null;
        out = null;
        try {
//          load properties file containing all file paths
            input = new FileInputStream("/Users/rachelmills/Desktop/ClueWeb/CluewebHelper/config.properties");
            prop.load(input);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public Map<String, Integer> createCluewebMap(Path cluewebPath) {
        MappingFile clueweb = new MappingFile(cluewebPath, "SPAM_MAPPING");
        cluewebMap = clueweb.getSpamMapping();
        System.out.println("Map created " + cluewebMap.size());
        return cluewebMap;
    }

    public File[] getZippedSpamFiles(String folderName) {
        File[] listOfFiles = ReadFile.getAllFilesInFolder(folderName);
        return listOfFiles;
    }

    public void processZippedSpamFiles(File[] listOfFiles) {
        int count = 1;
        for (File f : listOfFiles) {
            if (!f.isHidden()) {
                try {
                    FileInputStream stream = new FileInputStream(f);
                    try {
                        gzipstream = new GZIPInputStream(stream);

                    } catch (IOException ex) {
                        Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
                }
                createFrequencyMapFromZipStream(gzipstream);
            }
            System.out.println("Zip file processed:  " + count);
            count++;
        }
    }

    public void createFrequencyMapFromZipStream(GZIPInputStream gzipstream) {
        br = new BufferedReader(new InputStreamReader(gzipstream));
        Scanner sc = new Scanner(br);
        while (sc.hasNext()) {
            int score = sc.nextInt();
            String line = sc.next();
            if (cluewebMap.containsKey(line)) {
                int cluster = cluewebMap.remove(line);
                if (frequencyMap.containsKey(cluster)) {
                    int total = frequencyMap.get(cluster).getTotal() + score;
                    int count = frequencyMap.get(cluster).getCount() + 1;
                    frequencyMap.put(cluster, new Frequency(total, count));
                } else {
                    frequencyMap.put(cluster, new Frequency(score, 1));
                }
            }
        }
        try {
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeFrequencyMapToFile() {
        try {
            out = new FileOutputStream(getProp().getProperty("OUTPUT_FILE_PATH") + "ClusterSpamPercentageTest.txt");
            try {
                bw = new BufferedWriter(new OutputStreamWriter(out, "UTF8"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (Map.Entry<Integer, Frequency> ma : frequencyMap.entrySet()) {
            try {
                bw.write(ma.getKey() + "," + ma.getValue().getCount() + "," + ma.getValue().getTotal() + "," + (ma.getValue().getTotal() / ma.getValue().getCount()) + "\n");
//                System.out.println(m.getKey() + "," + m.getValue().getCount() + "," + m.getValue().getTotal() + "," + (m.getValue().getTotal() / m.getValue().getCount()));
            } catch (IOException ex) {
                Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            bw.flush();
        } catch (IOException ex) {
            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        Properties prop = new Properties();
//        InputStream input = null;
//        BufferedReader br;
//        BufferedWriter bw = null;
//        FileOutputStream out = null;
//
//        try {
////          load properties file containing all file paths
//            input = new FileInputStream(prop.getProperty("CONFIG_FILE_LOCATION"));
//            prop.load(input);
//
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            if (input != null) {
//                try {
//                    input.close();
//                } catch (IOException ex) {
//                    Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        }

        Mapping mapping = new Mapping();
        Path cluewebPath = ReadFile.getFilePath(mapping.getProp().getProperty("CLUEWEB_CLUSTERS"));
        mapping.createCluewebMap(cluewebPath);
        File[] listOfFiles = mapping.getZippedSpamFiles(mapping.getProp().getProperty("FOLDER_CONTAINING_GZ_FILES"));
        mapping.processZippedSpamFiles(listOfFiles);
        mapping.writeFrequencyMapToFile();
        //      Get file and path of input file
        //        Path filePath = ReadFile.getFilePath(prop.getProperty("INPUT_FILE"));
        // new
        //            MappingFile clueweb = new MappingFile(cluewebPath, "SPAM_MAPPING");
        //            Map<String, Integer> cluewebMap = clueweb.getSpamMapping();
        //            System.out.println("Map created " + cluewebMap.size());
        //            GZIPInputStream gzipstream = null;

//            for (File f : listOfFiles) {
//                if (!f.isHidden()) {
//                    try {
//                        FileInputStream stream = new FileInputStream(f);
//                        try {
//                            gzipstream = new GZIPInputStream(stream);
//
//                        } catch (IOException ex) {
//                            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    } catch (FileNotFoundException ex) {
//                        Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    br = new BufferedReader(new InputStreamReader(gzipstream));
//
//                    Scanner sc = new Scanner(br);
//                    while (sc.hasNext()) {
//                        int score = sc.nextInt();
//                        String line = sc.next();
//                        if (cluewebMap.containsKey(line)) {
//                            int cluster = cluewebMap.remove(line);
//
//                            if (m.getFrequencyMap().containsKey(cluster)) {
//                                int total = m.getFrequencyMap().get(cluster).getTotal() + score;
//                                int count = m.getFrequencyMap().get(cluster).getCount() + 1;
//                                m.getFrequencyMap().put(cluster, new Frequency(total, count));
//                            } else {
//                                m.getFrequencyMap().put(cluster, new Frequency(score, 1));
//                            }
//                        }
//
//                    }
//                }
    }
//            ProcessFile process = new ProcessFile(cluewebPath, prop.getProperty("OUTPUT_FILE_PATH"));
//            GZIPInputStream gzipstream = null;
//            File[] listOfFiles = ReadFile.getAllFilesInFolder(prop.getProperty("FOLDER_CONTAINING_GZ_FILES"));
//            for (File f : listOfFiles) {
//                if (!f.isHidden()) {
//                    try {
//                        FileInputStream stream = new FileInputStream(f);
//                        try {
//                            gzipstream = new GZIPInputStream(stream);
//
//                        } catch (IOException ex) {
//                            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
//                        }
//                    } catch (FileNotFoundException ex) {
//                        Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                    br = new BufferedReader(new InputStreamReader(gzipstream));
//                    MappingFile mappingForSpam = new MappingFile(br);
//                    spamMap = mappingForSpam.getSpamMapping();
//                    process.setSpamMap(spamMap);
//                    process.processLineByLine(SPAM_MAPPING_TRUE);
//                    mappingForSpam.clearMap();
//                }
//            }
//            Map<Integer, Frequency> mapToSave = process.getFrequencyMap();
//    if (bw
//
//    
//        != null) {
//                try {
//            out = new FileOutputStream(getProp().getProperty("OUTPUT_FILE_PATH") + "ClusterSpamPercentageTest.txt");
//            try {
//                bw = new BufferedWriter(new OutputStreamWriter(out, "UTF8"));
//            } catch (UnsupportedEncodingException ex) {
//                Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        for (Map.Entry<Integer, Frequency> ma : m.getFrequencyMap().entrySet()) {
//            try {
//                bw.write(ma.getKey() + "," + ma.getValue().getCount() + "," + ma.getValue().getTotal() + "," + (ma.getValue().getTotal() / ma.getValue().getCount()) + "\n");
////                System.out.println(m.getKey() + "," + m.getValue().getCount() + "," + m.getValue().getTotal() + "," + (m.getValue().getTotal() / m.getValue().getCount()));
//            } catch (IOException ex) {
//                Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        try {
//            bw.flush();
//        } catch (IOException ex) {
//            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        try {
//            bw.close();
//        } catch (IOException ex) {
//            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
//        }
////            }
//
//    }

    /**
     * @return the frequencyMap
     */
    public Map<Integer, Frequency> getFrequencyMap() {
        return frequencyMap;
    }

    /**
     * @return the prop
     */
    public Properties getProp() {
        return prop;
    }

}
