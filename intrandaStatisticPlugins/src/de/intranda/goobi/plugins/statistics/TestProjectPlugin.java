package de.intranda.goobi.plugins.statistics;

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
