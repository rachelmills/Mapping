/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mapping;

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
}
