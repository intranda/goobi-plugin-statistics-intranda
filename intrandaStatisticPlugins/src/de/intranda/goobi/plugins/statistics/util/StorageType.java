package de.intranda.goobi.plugins.statistics.util;

import lombok.Data;

@Data
public class StorageType {
    private Integer processid;
    
    private String title;

    private long totalSize;
    
    private long mediaSize; 
    
    private long masterSize;
    
    
}
