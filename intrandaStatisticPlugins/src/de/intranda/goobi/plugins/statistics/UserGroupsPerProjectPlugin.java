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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.beans.Project;
import org.goobi.production.plugin.interfaces.AbstractStatisticsPlugin;
import org.goobi.production.plugin.interfaces.IStatisticPlugin;

import de.intranda.goobi.plugins.statistics.util.UserGroupProjectData;
import de.sub.goobi.persistence.managers.ProjectManager;
import net.xeoh.plugins.base.annotations.PluginImplementation;

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
        return "/uii/statistics_stepsPerProject.xhtml";
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
        //        LoginBean bean = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
        //        return (bean.getMaximaleBerechtigung() == 1);
        return true;
    }

}
