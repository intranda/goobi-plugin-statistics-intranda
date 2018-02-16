package de.intranda.goobi.plugins.statistics.util;

public class StatisticsHelper {
	
	/**
	 * generate a human readable file size for bytes
	 * 
	 * @param bytes the input size to calculate
	 * @param basis define if the size shall be calculated with 1000 or 1024 bytes, true means 1000
	 * @return String with readable size
	 */
	public static String humanReadableByteCount(long bytes, boolean basis) {
	    int unit = basis ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (basis ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (basis ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
