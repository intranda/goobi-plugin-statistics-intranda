package de.intranda.goobi.plugins;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.goobi.beans.Project;
import org.goobi.production.plugin.interfaces.AbstractStatisticsPlugin;
import org.goobi.production.plugin.interfaces.IStatisticPlugin;

import de.intranda.goobi.plugins.util.UserGroupProjectData;
import de.sub.goobi.persistence.managers.ProjectManager;

@PluginImplementation
public class UserGroupProjectPlugin extends AbstractStatisticsPlugin implements IStatisticPlugin {

    private static final String PLUGIN_TITLE = "UserGroupProjectPlugin";

    private static final Logger logger = Logger.getLogger(UserGroupProjectPlugin.class);

    //    private List<PieType> list;
    //
    //    private String data;

    private List<UserGroupProjectData> projectDataList = new ArrayList<UserGroupProjectData>();

    public void initProjectData() {
        List<Project> projectList = ProjectManager.getAllProjects();

        for (Project project : projectList) {
            UserGroupProjectData pd = new UserGroupProjectData();
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
        return "/uii/opensteps_statistics.xhtml";
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

}
