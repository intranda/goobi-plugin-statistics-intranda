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
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@Data
@PluginImplementation
@Log4j2
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
        processFilterQuery.append(
                "select prozesse.ProzesseID as processid, prozesse.Titel as title, tbl1.total as totalSize, tbl2.mediaSize as mediaSize, tbl3.masterSize as masterSize ");
        //        processList.append(
        //                " prozesse.ProzesseID as processid, prozesse.Titel as title, h1.numericvalue as totalSize, h2.numericvalue as mediaSize, h3.numericvalue as masterSize");
        processFilterQuery.append("FROM prozesse ");
        processFilterQuery.append("left join batches on prozesse.batchID = batches.id ");
        processFilterQuery.append(
                "LEFT JOIN (select processid as processid, sum(numericvalue) as total from history where type = 1 group by processid) tbl1 ON prozesse.ProzesseID = tbl1.processid ");
        processFilterQuery.append(
                "LEFT JOIN (select processid as processid, sum(numericvalue) as mediaSize from history where type = 14 group by processid) tbl2 ON prozesse.ProzesseID = tbl2.processid ");
        processFilterQuery.append(
                "LEFT JOIN (select processid as processid, sum(numericvalue) as masterSize from history where type = 15 group by processid) tbl3 ON prozesse.ProzesseID = tbl3.processid ");
        processFilterQuery.append(",projekte ");
        processFilterQuery.append("WHERE prozesse.ProjekteID = projekte.ProjekteID ");
        String subquery = FilterHelper.criteriaBuilder(filter, false, null, null, null, true, false);
        if (StringUtils.isNotBlank(subquery)) {
            processFilterQuery.append(" AND ");
            processFilterQuery.append(subquery);

        }
        processFilterQuery.append(" AND ");
        processFilterQuery.append(" prozesse.istTemplate = false ");

        Connection connection = null;
        try {
            // TODO change queries to sum per type/process
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            resultList =
                    run.query(connection, processFilterQuery.toString(), new BeanListHandler<StoragePerProjectType>(StoragePerProjectType.class));
            totalSizeAll = run.query(connection, "select sum(totalSize) from (" + processFilterQuery.toString() + " ) t",
                    MySQLHelper.resultSetToLongHandler);
            totalSizeMedia = run.query(connection, "SELECT sum(mediaSize) from (" + processFilterQuery.toString() + " ) t",
                    MySQLHelper.resultSetToLongHandler);
            totalSizeMaster = run.query(connection, "SELECT sum(masterSize) from (" + processFilterQuery.toString() + " ) t",
                    MySQLHelper.resultSetToLongHandler);

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

    public List<StoragePerProjectType> resultListShort(int inMax) {
        if (inMax > resultList.size()) {
            return resultList;
        } else {
            return resultList.subList(0, inMax);
        }
    }

}
