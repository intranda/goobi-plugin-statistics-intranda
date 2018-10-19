package de.intranda.goobi.plugins.statistics;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IStatisticPlugin;

import de.sub.goobi.persistence.managers.ControllingManager;
import lombok.Data;

@Data
//@PluginImplementation
public class ProductivityPlugin implements IStatisticPlugin {

    private PluginType type = PluginType.Statistics;
    private static final String PLUGIN_TITLE = "test_productivity";
    private int projectId;
    private String filter;

    private List<Map<String, String>> resultList;

    @Override
    public String getTitle() {
        return PLUGIN_TITLE;
    }

    @Override
    public String getData() {
        return null;
    }

    @Override
    public String getGui() {
        return "/uii/statistics_test_productivity.xhtml";
    }

    public void resetStatistics() {
        resultList = null;
    }

    @Override
    public boolean getPermissions() {
        return true;
    }

    @Override
    public void calculate() {
        calculateData();
    }

    /**
     * calculate data from database
     */
    private void calculateData() {

        //How much data has been produced?  How many files and objects?

        //# To use different ranges, change the dates between,
        //# possible date formats are:
        //# daily: date_format(BearbeitungsEnde, '%Y-%m-%d')
        //# monthly: date_format(BearbeitungsEnde, '%Y-%m')
        //# quarterly: concat(YEAR(BearbeitungsEnde),'/',QUARTER(BearbeitungsEnde))
        //# yearly: date_format(BearbeitungsEnde, '%Y')
        //# this must be changed in the select area and  group by clause
        StringBuilder sb = new StringBuilder();
        // check for each process, if the last step was finished
        sb.append("select sum(p.sortHelperImages) as pages, count(p.sortHelperImages) as processes, date_format(BearbeitungsEnde, '%Y-%m') as timerange ");
        sb.append("from schritte s left join prozesse p on s.ProzesseID = p.ProzesseID ");
        sb.append("where s.SchritteID in (select max(SchritteId) as stepid from schritte s group by s.ProzesseID) ");
        sb.append("and BearbeitungsStatus = 3 ");
        sb.append("and BearbeitungsEnde between '2010-01-01' and '2018-12-31' ");
        sb.append("group by date_format(BearbeitungsEnde, '%Y-%m') ");

        resultList = ControllingManager.getResultsAsMaps(sb.toString());

    }

    @Override
    public void setStartDate(Date date) {
    }

    @Override
    public void setEndDate(Date date) {
    }
}
