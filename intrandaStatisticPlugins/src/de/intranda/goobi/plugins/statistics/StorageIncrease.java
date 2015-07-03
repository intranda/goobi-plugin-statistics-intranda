package de.intranda.goobi.plugins.statistics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.goobi.production.flow.statistics.hibernate.Converter;
import org.goobi.production.flow.statistics.hibernate.SQLStorage;
import org.goobi.production.plugin.interfaces.AbstractStatisticsPlugin;
import org.goobi.production.plugin.interfaces.IStatisticPlugin;

import de.sub.goobi.persistence.managers.ProcessManager;

@PluginImplementation
public class StorageIncrease extends AbstractStatisticsPlugin implements IStatisticPlugin {

	private static final String PLUGIN_TITLE = "intranda_statistics_storageIncrease";
	
    @Override
    public String getTitle() {
        return PLUGIN_TITLE;
    }
    
    private Date startDate;

    private Date endDate;

    private TimeUnit targetTimeUnit;

    private CalculationUnit targetCalculationUnit;

    private String data;

    private String axis;

    private int max = 0;

    @Override
    public void calculate() {

        List<Integer> IDlist = ProcessManager.getIDList(filter);
        String natSQL = new SQLStorage(this.startDate, this.endDate, this.targetTimeUnit, IDlist).getSQL();

        @SuppressWarnings("rawtypes")
        List list = ProcessManager.runSQL(natSQL);

        //        List<Map<Integer, Double>> value = new ArrayList<Map<Integer, Double>>();

        //        min: 0, ticks: [[0, ""], [1, "hello"], [2, "hi"], [3,"helloagain"]], max: 3 
        StringBuilder rawData = new StringBuilder();
        StringBuilder rawAxis = new StringBuilder();
        rawAxis.append("[");
        //        rawAxis.append("[0, \"\"],");
        //        "min: 0, max: " + list.size() + ", ticks: 
        rawData.append("[");
        //        rawData.append("[0, \"\"],");
        max = list.size();
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            //        for (Object obj : list) {
            //
            //            Map<Integer, Double> row = new HashMap<Integer, Double>();
            //
            //            value.add(row);
            try {
                Object[] objArr = (Object[]) obj;
                String key = new Converter(objArr[1]).getString() + "";
                Double d = round(new Converter(objArr[0]).getGB(), 2);
                rawData.append("[" + (i) + ", " + d + "]");
                rawAxis.append("[" + (i) + ", \"" + key + "\"]");
                if (i < list.size() - 1) {
                    rawData.append(", ");
                    rawAxis.append(", ");
                }
                System.out.println(key + ": " + d);

                //                row.put(i++, new Converter(objArr[0]).getGB());

            } catch (Exception e) {
                //                row.put(e.getMessage(), new Double(0));
            }
        }

        rawData.append("]");
        rawAxis.append("]");
        data = rawData.toString();
        axis = rawAxis.toString();
    }

    public String getGui() {
        return "/uii/statistics_storageIncrease.xhtml";
    }

    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public String getAxis() {
        return axis;
    }

    public String getData() {
        return data;
    }

    public int getMax() {
        return max;
    }

    public TimeUnit getTargetTimeUnit() {
        return targetTimeUnit;
    }

    public CalculationUnit getTargetCalculationUnit() {
        return targetCalculationUnit;
    }

    public void setTargetTimeUnit(TimeUnit targetTimeUnit) {
        this.targetTimeUnit = targetTimeUnit;
    }

    public void setTargetCalculationUnit(CalculationUnit targetCalculationUnit) {
        this.targetCalculationUnit = targetCalculationUnit;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setAxis(String axis) {
        this.axis = axis;
    }

    public void setMax(int max) {
        this.max = max;
    }

    @Override
    public void setStartDate(Date date) {
        startDate = date;

    }

    @Override
    public void setEndDate(Date date) {
        endDate = date;
    }

}
