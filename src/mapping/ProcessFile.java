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

    private Path path;
    private Map<Integer, String> map;

//    private final static String OUTPUT_FILE_PATH = "/home/wikiprep/LMW-tree/";
    private final static String OUTPUT_FILE_PATH = "/Users/rachelmills/Desktop/Clueweb/";

    public ProcessFile(Path path, Map<Integer, String> map) {
        try {
            out = new FileOutputStream(OUTPUT_FILE_PATH + "mapped_clusters.txt");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ProcessFile.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(out, "UTF8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ProcessFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.path = path;
        this.map = map;
    }

    void processLineByLine() {
        int count = 0;
        try (Scanner scanner = new Scanner(path, ENCODING.name())) {
            while (scanner.hasNextLine()) {
                processLine(scanner.nextLine());
                System.out.println("count = " + count);
                count++;
            }
        } catch (IOException ex) {
            Logger.getLogger(ProcessFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            bufferedWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(ProcessFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processLine(String nextLine) {

        String title = null;
        //use a second Scanner to parse the content of each line 
        Scanner sc = new Scanner(nextLine);

        // extract and remove id from line
        int id = Integer.parseInt(sc.next().replaceAll("[^\\d]", ""));

        int cluster = sc.nextInt();

        // find id in map
        title = map.get(id);

        try {
            bufferedWriter.write(id + ", " + title + ", " + cluster + "\n");
            bufferedWriter.flush();
        } catch (IOException ex) {
            Logger.getLogger(ProcessFile.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
