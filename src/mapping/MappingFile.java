/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mapping;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author rachelmills
 */
public class MappingFile {

    Path path;
    private Map<Integer, String> mapping;
    private Map<String, Integer> spamMapping;

    private final static Charset ENCODING = StandardCharsets.UTF_8;

    public MappingFile(Path path) {
        mapping = new HashMap<>();

        this.path = path;
        try {
            Scanner s = new Scanner(path, ENCODING.name());
            while (s.hasNextLine()) {
                Integer i = 0;
                String str = s.nextLine();
                Pattern p = Pattern.compile("\\d+");
                Matcher m = p.matcher(str);

                if (m.find()) {
                    i = Integer.parseInt(m.group());
                    str = str.replace(m.group() + ",", "");
                }

                mapping.put(i, str);
            }
        } catch (IOException ex) {
            Logger.getLogger(ProcessFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public MappingFile(Path path, String spamFile) {
        this.path = path;
        spamMapping = new HashMap<>();

        try {
            Scanner sc = new Scanner(path, ENCODING.name());
            while (sc.hasNext()) {
                String s = sc.next();
                int i = sc.nextInt();
                spamMapping.put(s, i);
            }

        } catch (IOException ex) {
            Logger.getLogger(ProcessFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public MappingFile(BufferedReader br) {
        spamMapping = new HashMap<>();
        Scanner sc = new Scanner(br);
        while (sc.hasNext()) {
            int score = sc.nextInt();
            String line = sc.next();
            spamMapping.put(line, score);
        }
    }

    /**
     * @return the mapping
     */
    public Map<Integer, String> getMapping() {
        return mapping;
    }

    /**
     * @param mapping the mapping to set
     */
    public void setMapping(Map<Integer, String> mapping) {
        this.mapping = mapping;
    }

    /**
     * @return the spamMapping
     */
    public Map<String, Integer> getSpamMapping() {
        return spamMapping;
    }

    /**
     * @param spamMapping the spamMapping to set
     */
    public void setSpamMapping(Map<String, Integer> spamMapping) {
        this.spamMapping = spamMapping;
    }

    public void clearMap() {
        spamMapping.clear();
    }
}
