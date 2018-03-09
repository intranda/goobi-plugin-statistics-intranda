package de.intranda.goobi.plugins.statistics;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.poi.xdgf.usermodel.section.GenericSection;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IStatisticPlugin;

import com.google.gson.Gson;

import de.intranda.goobi.plugins.statistics.util.FinishedStepsPerYearType;
import de.sub.goobi.persistence.managers.MySQLHelper;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@Data
@PluginImplementation
@Log4j
public class FinishedStepsPerYearPlugin implements IStatisticPlugin {

    private PluginType type = PluginType.Statistics;
    private String title = "plugin_intranda_statistics_finishedStepsPerYear";
    private String gui = "/uii/statistics_finishedStepsPerYear.xhtml";
    private List<FinishedStepsPerYearType> resultList = new ArrayList<>();
    private List<String> stepnames = new ArrayList<>();
    
    private String filter;
    private Date startDate;
    private Date endDate;
    
    @Override
    public String getData() {
    	return null;
    }

    @Override
    public boolean getPermissions() {
        return true;
    }
    
    /**
     * generate a list of all distinct step names 
     */
    public FinishedStepsPerYearPlugin() {
    		Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            stepnames = run.query(connection, "SELECT distinct titel FROM schritte ORDER BY titel;", new ColumnListHandler<String>(1));
        } catch (SQLException e) {
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
    }

    @Override
    public void calculate() {
    		// get all finished steps per year
    	
		//	select sum(sortHelperImages) as images, YEAR(s.BearbeitungsBeginn) as year
		//	from schritte s left join prozesse p on p.ProzesseID = s.ProzesseID
		//	where s.titel ='Export to viewer' and Bearbeitungsstatus = 3
		//	group by YEAR(s.BearbeitungsBeginn);
    	
    	    StringBuilder processFilterQuery = new StringBuilder();
    	    processFilterQuery.append("select count(sortHelperImages) as processes, sum(sortHelperImages) as images, YEAR(s.BearbeitungsEnde) as year ");
        processFilterQuery.append("from schritte s left join prozesse p on p.ProzesseID = s.ProzesseID ");
        processFilterQuery.append("where s.titel ='" + filter + "' and Bearbeitungsstatus = 3 ");
        processFilterQuery.append("group by YEAR(s.BearbeitungsEnde);");

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            resultList = run.query(connection, processFilterQuery.toString(), new BeanListHandler<FinishedStepsPerYearType>(FinishedStepsPerYearType.class));
        } catch (SQLException e) {
            log.error(e);
        } finally {
            if (connection != null) {
                try {
                    MySQLHelper.closeConnection(connection);
                } catch (SQLException e) {
                }
            }
        }
    }

    /**
	 * create a selectable list of the unique step names
	 * @return List of selectable step names
	 */
	public List<SelectItem> getSelectableSteps() {
        List<SelectItem> list = new ArrayList<SelectItem>();
        for (String s : stepnames) {
            list.add(new SelectItem(s, s, null));
        }
        return list;
    }
	
	public String getChartLabels() {
		String result = "";
		for (FinishedStepsPerYearType t : resultList) {
			result += "\"" + t.getYear() + "\", ";
		}
		if (result.endsWith(", ")) {
			result = result.substring(0,result.length()-2);
		}
		return result;
	}
	
	public String getChartValuesImages() {
		String result = "";
		for (FinishedStepsPerYearType t : resultList) {
			result += t.getImages() + ", ";
		}
		if (result.endsWith(",")) {
			result = result.substring(0,result.length()-2);
		}
		return result;

		
//		String processes = "";
//		String images = "";
//		for (FinishedStepsPerYearType t : resultList) {
//			processes += t.getProcesses() + ", ";
//			images += t.getImages() + ", ";
//		}
//		if (processes.endsWith(", ")) {
//			processes = processes.substring(0,processes.length()-2);
//		}
//		if (images.endsWith(", ")) {
//			images = images.substring(0,images.length()-2);
//		}
//		
//		String result = "[{ label:\"Processes\",data:[" + processes + "]},{label:\"Images\",data:[" + images +"]}]";
//		return result;
	}
	
	public String getChartValuesProcesses() {
		String result = "";
		for (FinishedStepsPerYearType t : resultList) {
			result += t.getProcesses() + ", ";
		}
		if (result.endsWith(",")) {
			result = result.substring(0,result.length()-2);
		}
		return result;
	}
       
}
