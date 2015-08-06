package de.intranda.goobi.plugins.statistics;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.goobi.beans.Project;
import org.goobi.managedbeans.LoginBean;
import org.goobi.production.plugin.interfaces.AbstractStatisticsPlugin;
import org.goobi.production.plugin.interfaces.IStatisticPlugin;

import de.intranda.goobi.plugins.statistics.util.UserGroupProjectData;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProjectManager;

@PluginImplementation
public class UserGroupsPerProjectPlugin extends AbstractStatisticsPlugin implements IStatisticPlugin {

    private static final String PLUGIN_TITLE = "intranda_statistics_userGroupsPerProject";

    private static final Logger logger = Logger.getLogger(UserGroupsPerProjectPlugin.class);

    //    private List<PieType> list;
    //
    //    private String data;

    private List<UserGroupProjectData> projectDataList = new ArrayList<UserGroupProjectData>();

    public void initProjectData() {
        List<Project> projectList = ProjectManager.getAllProjects();

        for (Project project : projectList) {
            UserGroupProjectData pd = new UserGroupProjectData();
            pd.setTitle(PLUGIN_TITLE);
            pd.setProject(project);
            projectDataList.add(pd);
        }
    }

    @Override
    public void calculate() {

        for (UserGroupProjectData pd : projectDataList) {
            if (pd.isSelected()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Calculating data for project " + pd.getProject().getTitel());
                }
                pd.calculate();
            }
        }
    }

    @Override
    public String getTitle() {
        return PLUGIN_TITLE;
    }

    @Override
    public String getGui() {
        return "/uii/statistics_openStepsPerProject.xhtml";
    }

    @Override
    public void setStartDate(Date date) {

    }

    @Override
    public void setEndDate(Date date) {

    }

    public List<UserGroupProjectData> getProjectDataList() {
        if (projectDataList.isEmpty()) {
            initProjectData();
        }
        return projectDataList;
    }

    public void setProjectDataList(List<UserGroupProjectData> projectDataList) {
        this.projectDataList = projectDataList;
    }

    @Override
    public String getData() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getPermissions() {
        // nur mit admin Rechten 
        LoginBean bean = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
        return (bean.getMaximaleBerechtigung() == 1);
    }

}
