package de.intranda.goobi.plugins.statistics;
/**
 * This file is part of a plugin for the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - https://goobi.io - https://www.intranda.com - https://github.com/intranda/goobi
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
//import java.sql.Connection;

//import java.sql.SQLException;
//import java.util.Date;
//import java.util.List;
//
//import org.apache.commons.dbutils.QueryRunner;
//import org.apache.commons.dbutils.handlers.BeanListHandler;
//import org.goobi.production.enums.PluginType;
//import org.goobi.production.plugin.interfaces.IStatisticPlugin;
//
//import de.intranda.goobi.plugins.statistics.util.TestProjectElement;
//import de.sub.goobi.persistence.managers.MySQLHelper;
//import lombok.Data;
//import lombok.extern.log4j.Log4j;
//import net.xeoh.plugins.base.annotations.PluginImplementation;

//@Data
//@PluginImplementation
//@Log4j
//public class TestProjectPlugin implements IStatisticPlugin {
//
//	private PluginType type = PluginType.Statistics;
//    private static final String PLUGIN_TITLE = "test_projects";
//    private List<TestProjectElement> resultList = null;
//    private int projectId;
//    private String filter;
//    
//    @Override
//    public String getTitle() {
//        return PLUGIN_TITLE;
//    }
//
//    @Override
//    public String getData() {
//        return null;
//    }
//
//    @Override
//    public String getGui() {
//        return "/uii/statistics_testProjects.xhtml";
//    }
//
//    public void resetStatistics() {
//        resultList = null;
//    }
//
//    @Override
//    public boolean getPermissions() {
//        return true;
//    }
//
//    @Override
//    public void calculate() {
//    		calculateData();
//    }
//
//    /**
//     * calculate data from database
//     */
//    private void calculateData() {
//        StringBuilder sb = new StringBuilder();
//        sb.append("select projekteid as id, titel as title from projekte;");
//        
//        Connection connection = null;
//        try {
//            connection = MySQLHelper.getInstance().getConnection();
//            resultList = new QueryRunner().query(connection, sb.toString(), new BeanListHandler<TestProjectElement>(TestProjectElement.class));
//        } catch (SQLException e) {
//            log.error(e);
//        } finally {
//            if (connection != null) {
//                try {
//                    MySQLHelper.closeConnection(connection);
//                } catch (SQLException e) {
//                    log.error(e);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void setStartDate(Date date) {
//    }
//
//    @Override
//    public void setEndDate(Date date) {
//    }

//}
