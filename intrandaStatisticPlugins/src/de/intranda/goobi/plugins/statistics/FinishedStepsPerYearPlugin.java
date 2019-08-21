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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.faces.model.SelectItem;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IStatisticPlugin;

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
    private List<String> myStepnames = new ArrayList<>();
    private Future<List<String>> futureStepnames;

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
        Callable<List<String>> callable = () -> {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                return run.query(connection, "SELECT distinct titel FROM schritte ORDER BY titel;", new ColumnListHandler<String>(1));
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
            return new ArrayList<>();
        };
        ExecutorService service = Executors.newSingleThreadExecutor();
        futureStepnames = service.submit(callable);
        service.shutdown();
    }

    public List<String> getStepnames() {
        if (myStepnames.isEmpty()) {
            try {
                myStepnames = futureStepnames.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error(e);
            }
        }
        return myStepnames;
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
            resultList = run.query(connection, processFilterQuery.toString(),
                    new BeanListHandler<FinishedStepsPerYearType>(FinishedStepsPerYearType.class));
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
     * 
     * @return List of selectable step names
     */
    public List<SelectItem> getSelectableSteps() {
        List<SelectItem> list = new ArrayList<SelectItem>();
        for (String s : getStepnames()) {
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
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }

    public String getChartValuesImages() {
        String result = "";
        for (FinishedStepsPerYearType t : resultList) {
            result += t.getImages() + ", ";
        }
        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 2);
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
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }

}
