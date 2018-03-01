package de.intranda.goobi.plugins.statistics.util;

import lombok.Data;

@Data
public class FinishedStepsPerYearType {
	private String processes;
	private String images;
	private String year;
}