package de.intranda.goobi.plugins;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import net.sf.jxls.exception.ParsePropertyException;
import net.sf.jxls.transformer.XLSTransformer;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.goobi.beans.Project;
import org.goobi.beans.Step;
import org.goobi.beans.Usergroup;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.goobi.production.plugin.interfaces.AbstractStatisticsPlugin;
import org.goobi.production.plugin.interfaces.IStatisticPlugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import de.intranda.goobi.plugins.util.PieType;
import de.intranda.goobi.plugins.util.ProjectData;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UsergroupManager;

@PluginImplementation
public class OpenStepProjectPlugin extends AbstractStatisticsPlugin implements IStatisticPlugin {

    private static final String PLUGIN_TITLE = "OpenStepProjectPlugin";

    private static final Logger logger = Logger.getLogger(OpenStepProjectPlugin.class);

    

    //    private List<PieType> list;
    //
    //    private String data;

    private List<ProjectData> projectDataList = new ArrayList<ProjectData>();

    public void initProjectData() {
        List<Project> projectList = ProjectManager.getAllProjects();

        for (Project project : projectList) {
            ProjectData pd = new ProjectData();
            pd.setProject(project);
            projectDataList.add(pd);
        }
    }

    @Override
    public void calculate() {

        for (ProjectData pd : projectDataList) {
            if (pd.isSelected()) {
                pd.calculate();
            }
        }

    }

    public static String getRandomColor() {
        String possibleValues = "0123456789ABCDEF";
        String hexCode = "#";
        for (int i = 0; i <= 5; i++) {
            int index = (int) (Math.random() * 15);
            hexCode += possibleValues.charAt(index);
        }
        return hexCode;
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

    public List<ProjectData> getProjectDataList() {
        if (projectDataList.isEmpty()) {
            initProjectData();
        }
        return projectDataList;
    }

    public void setProjectDataList(List<ProjectData> projectDataList) {
        this.projectDataList = projectDataList;
    }

    @Override
    public String getData() {
        // TODO Auto-generated method stub
        return null;
    }

}
