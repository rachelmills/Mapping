/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapping;

import java.nio.file.Path;
import java.util.Map;

/**
 *
 * @author rachelmills
 */
public class Mapping {

    final static String INPUT_FILE = "/Users/rachelmills/Desktop/ClueWeb/subset_clusters.txt";
//    final static String INPUT_FILE = "/home/wikiprep/LMW-tree/subset_clusters.txt";
//    final static String MAPPING_FILE = "/home/wikiprep/wikiprep/work/ID_Title.txt";
//    final static String MAPPING_FILE = "/Volumes/Untitled/wikiprep/WikiOutput/ID_Title.txt";
    final static String MAPPING_FILE = "/Users/rachelmills/Desktop/ClueWeb/Wikiparser/ID_Title.txt";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ReadFile rf = new ReadFile(INPUT_FILE);
        Path filePath = rf.getFilePath();

        ReadFile mapping = new ReadFile(MAPPING_FILE);
        Path mappingFilePath = mapping.getFilePath();

        MappingFile mappingFile = new MappingFile(mappingFilePath);
        Map<Integer, String> map = mappingFile.getMapping();
        
        System.out.println("Files ready for processing.");

        ProcessFile pf = new ProcessFile(filePath, map);
        
        System.out.println("Set up finished");
        pf.processLineByLine();
    }

}
