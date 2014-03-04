/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package mapping;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author rachelmills
 */
public class ReadFile {
    
    private Path filePath;
    
    public ReadFile(String filename) {
        filePath = Paths.get(filename);
    }
    
    /**
     * @return the filePath
     */
    public Path getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }
}
