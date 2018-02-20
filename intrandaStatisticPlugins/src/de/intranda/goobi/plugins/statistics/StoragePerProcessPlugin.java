package de.intranda.goobi.plugins.statistics;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang.StringUtils;
import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.goobi.production.plugin.interfaces.IStatisticPlugin;

import de.intranda.goobi.plugins.statistics.util.StatisticsHelper;
import de.intranda.goobi.plugins.statistics.util.StoragePerProjectType;
import de.sub.goobi.persistence.managers.MySQLHelper;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@Data
@PluginImplementation
@Log4j
public class StoragePerProcessPlugin implements IStatisticPlugin {

    private PluginType type = PluginType.Statistics;
    private String title = "plugin_intranda_statistics_storagePerProcess";
    private String gui = "/uii/statistics_storagePerProcess.xhtml";
    private String filter;

    private Date startDate;
    private Date endDate;

    private List<StoragePerProjectType> resultList = new ArrayList<>();

    private long totalSizeMaster;
    private long totalSizeMedia;
    private long totalSizeAll;

    @Override
    public String getData() {
        return null;
    }

    @Override
    public void calculate() {

    	    StringBuilder processFilterQuery = new StringBuilder();
//        processList.append("SELECT ");
//        processList.append(
//                " prozesse.ProzesseID as processid, prozesse.Titel as title, h1.numericvalue as totalSize, h2.numericvalue as mediaSize, h3.numericvalue as masterSize");
        processFilterQuery.append("FROM prozesse ");
        processFilterQuery.append("left join batches on prozesse.batchID = batches.id ");
        processFilterQuery.append("LEFT JOIN history h1 on h1.processId = prozesse.ProzesseID and (h1.type = 1) ");
        processFilterQuery.append("LEFT JOIN history h2 on h2.processId = prozesse.ProzesseID and (h2.type = 14) ");
        processFilterQuery.append("LEFT JOIN history h3 on h3.processId = prozesse.ProzesseID and (h3.type = 15) ");
        processFilterQuery.append(",projekte ");
        processFilterQuery.append("WHERE prozesse.ProjekteID = projekte.ProjekteID ");
        String subquery = FilterHelper.criteriaBuilder(filter, false, null, null, null, true, false);
        if (StringUtils.isNotBlank(subquery)) {
            processFilterQuery.append(" AND ");
            processFilterQuery.append(subquery);

        }
        processFilterQuery.append(" AND ");
        processFilterQuery.append(" prozesse.istTemplate = false group by prozesse.ProzesseID");

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            resultList = run.query(connection, "SELECT max(prozesse.ProzesseID) as processid, max(prozesse.Titel) as title, sum(h1.numericvalue) as totalSize, sum(h2.numericvalue) as mediaSize, sum(h3.numericvalue) as masterSize " +  processFilterQuery.toString(), new BeanListHandler<StoragePerProjectType>(StoragePerProjectType.class));
            totalSizeAll = run.query(connection, "SELECT sum(tbl.total) from (SELECT sum(h1.numericvalue) as total " + processFilterQuery.toString() + ") tbl", MySQLHelper.resultSetToLongHandler);
            totalSizeMedia = run.query(connection, "SELECT sum(tbl.mediaSize) from (SELECT sum(h2.numericvalue) as mediaSize " + processFilterQuery.toString() + ") tbl", MySQLHelper.resultSetToLongHandler);
            totalSizeMaster = run.query(connection, "SELECT sum(tbl.masterSize) from (SELECT sum(h3.numericvalue) as masterSize " + processFilterQuery.toString() + ") tbl", MySQLHelper.resultSetToLongHandler);

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
    public boolean getPermissions() {
        return true;
    }
    
    public String getTotalSizeMasterFormatted() {
		return StatisticsHelper.humanReadableByteCount(totalSizeMaster, true);
    }
    
    public String getTotalSizeMediaFormatted() {
		return StatisticsHelper.humanReadableByteCount(totalSizeMedia, true);
    }
    
    public String getTotalSizeAllFormatted() {
		return StatisticsHelper.humanReadableByteCount(totalSizeAll, true);
    }
    
    public List<StoragePerProjectType> resultListShort(int inMax){
    		if (inMax > resultList.size()) {
    			return resultList;
    		} else {
    			return resultList.subList(0, inMax);
    		}
    }

}
