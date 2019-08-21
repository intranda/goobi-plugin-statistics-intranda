package de.intranda.goobi.plugins.statistics;

/**
 * This file is part of a plugin for the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.goobi.production.flow.statistics.hibernate.Converter;
import org.goobi.production.flow.statistics.hibernate.StatisticsFactory;
import org.goobi.production.plugin.interfaces.AbstractStatisticsPlugin;
import org.goobi.production.plugin.interfaces.IStatisticPlugin;

import de.sub.goobi.persistence.managers.ProcessManager;
import net.xeoh.plugins.base.annotations.PluginImplementation;

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
        String natSQL = StatisticsFactory.getStorage(this.startDate, this.endDate, this.targetTimeUnit, IDlist).getSQL();

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

    @Override
    public String getGui() {
        return "/uii/statistics_storageIncrease.xhtml";
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public String getAxis() {
        return axis;
    }

    @Override
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

    @Override
    public boolean getPermissions() {
        return true;
        //    	// nur Nutzergruppe Projectmanager
        //        User user = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        //        for (Usergroup ug : user.getBenutzergruppen()) {
        //            if (ug.getTitel().equals("Projectmanager")) {
        //                return true;
        //            }
        //        }
        //        return false;
    }

}
