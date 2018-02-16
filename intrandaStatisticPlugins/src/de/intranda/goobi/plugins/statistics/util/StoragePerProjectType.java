package de.intranda.goobi.plugins.statistics.util;

import lombok.Data;

@Data
public class StoragePerProjectType {
	
    private Integer processid;
    private String title;
    private long totalSize;
    private long mediaSize; 
    private long masterSize;
    
    public String getTotalSizeFormatted() {
	    return StatisticsHelper.humanReadableByteCount(totalSize, true);
	}
    
    public String getMediaSizeFormatted() {
	    return StatisticsHelper.humanReadableByteCount(mediaSize, true);
	}
    
    public String getMasterSizeFormatted() {
	    return StatisticsHelper.humanReadableByteCount(masterSize, true);
	}
	
	
    
}
