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
import java.io.Writer;
import java.util.HashMap;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author rachelmills
 */
public class Mapping {

    final static boolean SPAM_MAPPING_TRUE = true;
    final static boolean SPAM_MAPPING_FALSE = false;
    private final Map<Integer, Frequency> frequencyMap = new HashMap<>();
    private final Properties prop;
    private InputStream input;
    private BufferedReader br;
    private BufferedWriter bw;
    private FileOutputStream out;
    GZIPInputStream gzipstream;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Mapping mapping = new Mapping();
        Path cluewebPath = ReadFile.getFilePath(mapping.getProp().getProperty("CLUEWEB_CLUSTERS"));
        File[] listOfFiles = mapping.getZippedSpamFiles(mapping.getProp().getProperty("FOLDER_CONTAINING_GZ_FILES"));
        mapping.splitCluewebClustersIntoSpamFileSections(cluewebPath, listOfFiles);
        mapping.processZippedSpamFiles(listOfFiles);
        mapping.writeFrequencyMapToFile();
    }

    public Mapping() {
        prop = new Properties();
        input = null;
        //   bw = null;
        out = null;
        try {
//          load properties file containing all file paths
            input = new FileInputStream("/Users/rachelmills/Desktop/ClueWeb/CluewebHelper/config.properties");
//            input = new FileInputStream("/home/wikiprep/config.properties");
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

    public void splitCluewebClustersIntoSpamFileSections(Path cluewebPath, File[] listOfFiles) {

        File parentDirectory = new File(prop.getProperty("CLUEWEB_SPLIT_PATH"));
        if (parentDirectory.exists()) {
            for (File f : parentDirectory.listFiles()) {
                f.delete();
            }
        }
        parentDirectory.mkdir();

        //For each file in spam directory, create a matching file in clueweb directory
        for (File f : listOfFiles) {
            if (!f.isHidden()) {
                String fileName = f.getName();
                try {
                    File file = new File(parentDirectory + "/" + (fileName.substring(0, fileName.length() - 3)));
                    FileOutputStream fos = new FileOutputStream(file);
                    Writer write = new OutputStreamWriter(fos, "UTF8");
                    write.flush();
                } catch (IOException ex) {
                    Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        //For each line in clueweb cluster file, get the id and write it to the matching filename in the clueweb directory
        try {
            Scanner sc = new Scanner(cluewebPath);
            if (parentDirectory.exists()) {
                File[] files = parentDirectory.listFiles();
                while (sc.hasNext()) {
                    String id = sc.next();
                    int cluster = sc.nextInt();
                    for (File file : files) {
                        if (!file.isHidden()) {
                            if (id.substring(7, 16).equals(file.getName().substring(2, 11))) {
                                FileOutputStream fos = new FileOutputStream(file, true);
                                Writer write = new OutputStreamWriter(fos, "UTF8");
                                try (BufferedWriter fbw = new BufferedWriter(write)) {
                                    fbw.write(id + " " + cluster + "\n");
                                    fbw.flush();
                                }
                                break;
                            }
                        }
                    }
                }
            } else {
            }

        } catch (IOException ex) {
            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Compress the files in the clueweb directory and delete the originals
        if (parentDirectory.exists()) {
            File[] files = parentDirectory.listFiles();
            for (File file : files) {
                if (!file.isHidden()) {
                    String from = prop.getProperty("CLUEWEB_SPLIT_PATH") + file.getName();
                    String to = prop.getProperty("CLUEWEB_SPLIT_PATH") + file.getName() + ".gz";
                    compress(from, to);
                    deleteDecompressedFiles(new File(from));
                }
            }
        }
    }

    public void compress(String from, String to) {
        FileInputStream in;
        try {
            in = new FileInputStream(from);
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(new FileOutputStream(to))) {
                byte[] buffer = new byte[4096];
                int bytes_read;
                while ((bytes_read = in.read(buffer)) != -1) {
                    gzipOut.write(buffer, 0, bytes_read);
                }
                in.close();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void deleteDecompressedFiles(File from) {
        from.delete();
    }

    public File[] getZippedSpamFiles(String folderName) {
        File[] listOfFiles = ReadFile.getAllFilesInFolder(folderName);
        return listOfFiles;
    }
    
    public void processZippedSpamFiles(File[] listOfFiles) {
        Map<String, Integer> cluewebMap = new HashMap<>();
        int count = 1;
        for (File f : listOfFiles) {
            if (!f.isHidden()) {
                try {
                    FileInputStream stream = new FileInputStream(f);
                    cluewebMap = createMatchingCluewebMap(f);
                    try {
                        gzipstream = new GZIPInputStream(stream);
                    } catch (IOException ex) {
                        Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
                }
                createFrequencyMapFromZipStream(gzipstream, cluewebMap);
            }
            System.out.println("Zip file processed:  " + count);
            count++;
        }
    }

    public Map<String, Integer> createMatchingCluewebMap(File f) {
        Map<String, Integer> cluewebMap = new HashMap<>();

        File file = new File(prop.getProperty("CLUEWEB_SPLIT_PATH") + f.getName());
        try {
            FileInputStream stream = new FileInputStream(file);
            GZIPInputStream gzipStream = new GZIPInputStream(stream);
            BufferedReader reader = new BufferedReader(new InputStreamReader(gzipStream));
            Scanner sc = new Scanner(reader);
            while (sc.hasNext()) {
                String id = sc.next();
                int cluster = sc.nextInt(); 
                cluewebMap.put(id, cluster);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Mapping.class.getName()).log(Level.SEVERE, null, ex);
          }
        
        return cluewebMap;
    }

    public void createFrequencyMapFromZipStream(GZIPInputStream gzipstream, Map<String, Integer> cluewebMap) {
        br = new BufferedReader(new InputStreamReader(gzipstream));
        Scanner sc = new Scanner(br);
        while (sc.hasNext()) {
            int score = sc.nextInt();
            String line = sc.next();
            if (cluewebMap.containsKey(line)) {
                int cluster = cluewebMap.remove(line);
                if (frequencyMap.containsKey(cluster)) {
                    long total = frequencyMap.get(cluster).getTotal() + score;
                    int count = frequencyMap.get(cluster).getCount() + 1;
                    frequencyMap.put(cluster, new Frequency(total, count));
                } else {
//                    frequencyMap.put(cluster, new Frequency(214748364799L, 1));
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
            out = new FileOutputStream(getProp().getProperty("OUTPUT_FILE_PATH") + "ClusterSpamPercentage_10000.txt");
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

    public Map<Integer, Frequency> getFrequencyMap() {
        return frequencyMap;
    }

    public Properties getProp() {
        return prop;
    }
}
