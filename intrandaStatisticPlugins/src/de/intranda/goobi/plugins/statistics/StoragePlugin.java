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

import de.intranda.goobi.plugins.statistics.util.StorageType;
import de.sub.goobi.persistence.managers.MySQLHelper;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@Data
@PluginImplementation
@Log4j
public class StoragePlugin implements IStatisticPlugin {

    private PluginType type = PluginType.Statistics;
    private String title = "StoragePerProcesses";
    private String gui = "/uii/statistics_storage.xhtml";
    private String filter;

    private Date startDate;
    private Date endDate;

    private List<StorageType> dataList = new ArrayList<>();

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
        processFilterQuery.append("FROM  ");
        processFilterQuery.append("(select processid, sum(numericvalue) as total from history where type = 1 group by processid) as h1, ");
        processFilterQuery.append("(select processid, sum(numericvalue) as media from history where type = 14 group by processid) as h2, ");
        processFilterQuery.append("(select processid, sum(numericvalue) as master from history where type = 15 group by processid) as h3 ");
        processFilterQuery.append(", prozesse left join batches on prozesse.batchID = batches.id, projekte ");
//        processFilterQuery.append(" FROM prozesse left join batches on prozesse.batchID = batches.id ");
//        processFilterQuery.append("LEFT JOIN history h1 on h1.processId = prozesse.ProzesseID and (h1.type = 1) ");
//        processFilterQuery.append("LEFT JOIN history h2 on h2.processId = prozesse.ProzesseID and (h2.type = 14) ");
//        processFilterQuery.append("LEFT JOIN history h3 on h3.processId = prozesse.ProzesseID and (h3.type = 15) ");
        processFilterQuery.append("WHERE prozesse.ProjekteID = projekte.ProjekteID ");
        String subquery = FilterHelper.criteriaBuilder(filter, false, null, null, null, true, false);
        if (StringUtils.isNotBlank(subquery)) {
            processFilterQuery.append(" AND ");
            processFilterQuery.append(subquery);

        }
        processFilterQuery.append(" AND ");
        processFilterQuery.append(" prozesse.istTemplate = false and h1.processid = prozesse.ProzesseID and h2.processid = prozesse.ProzesseID and h3.processid = prozesse.ProzesseID");

        

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            dataList = run.query(connection, "SELECT prozesse.ProzesseID as processid, prozesse.Titel as title, h1.total as totalSize, h2.media as mediaSize, h3.master as masterSize " +  processFilterQuery.toString(), new BeanListHandler<StorageType>(StorageType.class));

            totalSizeAll = run.query(connection, "SELECT sum(h1.total) " + processFilterQuery.toString(), MySQLHelper.resultSetToLongHandler);
            totalSizeMedia = run.query(connection, "SELECT sum(h2.media) " + processFilterQuery.toString(), MySQLHelper.resultSetToLongHandler);
            totalSizeMaster = run.query(connection, "SELECT sum(h3.master) " + processFilterQuery.toString(), MySQLHelper.resultSetToLongHandler);

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

}
