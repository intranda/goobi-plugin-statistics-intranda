package de.intranda.goobi.plugins;

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

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import net.sf.jxls.exception.ParsePropertyException;
import net.sf.jxls.transformer.XLSTransformer;
import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.goobi.beans.Step;
import org.goobi.beans.Usergroup;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.goobi.production.plugin.interfaces.AbstractStatisticsPlugin;
import org.goobi.production.plugin.interfaces.IStatisticPlugin;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.intranda.goobi.plugins.util.PieType;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UsergroupManager;

@PluginImplementation
public class UserGroupPlugin extends AbstractStatisticsPlugin implements IStatisticPlugin {

    private static final String PLUGIN_TITLE = "UserGroupPlugin";

    private static final Logger logger = Logger.getLogger(UserGroupPlugin.class);

    private List<PieType> list;

    private String data;

    @Override
    public void calculate() {
        String filterString = FilterHelper.criteriaBuilder(filter, false, null, null, null, true, false);
        List<Step> stepList = null;
        if (filterString == null || filterString.length() == 0) {
            stepList = StepManager.getSteps(null, " (bearbeitungsstatus = 1 OR bearbeitungsstatus = 2)  ");
        } else {
            stepList =
                    StepManager.getSteps(null,
                            " (bearbeitungsstatus = 1 OR bearbeitungsstatus = 2) AND schritte.ProzesseID in (select ProzesseID from prozesse where "
                                    + filterString + ")");
        }

        Map<String, Integer> counter = new TreeMap<String, Integer>();

        for (Step step : stepList) {
            for (Usergroup group : UsergroupManager.getUserGroupsForStep(step.getId())) {

                if (counter.containsKey(group.getTitel())) {
                    counter.put(group.getTitel(), counter.get(group.getTitel()) + 1);
                } else {
                    counter.put(group.getTitel(), 1);
                }
            }
        }

        list = new ArrayList<PieType>();

        for (String groupName : counter.keySet()) {
            int value = counter.get(groupName);

            PieType type = new PieType();
            type.setLabel(groupName);
            type.setData(value);
            type.setColor(getRandomColor());
            list.add(type);

        }

        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writeValue(writer, list);
        } catch (IOException e) {
            logger.error(e);
        }

        data = writer.toString();

    }

    private static String getRandomColor() {
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String getGui() {
        return "/uii/usergroup_statistics.xhtml";
    }

    @Override
    public void setStartDate(Date date) {

    }

    @Override
    public void setEndDate(Date date) {

    }

    public void setDataList(List<PieType> list) {
        this.list = list;
    }

    public List<PieType> getDataList() {
        return list;
    }

    public void createExcelFile() {
        try {
            File tempFile = File.createTempFile("test", ".xls");

            Map<String, List<PieType>> map = new HashMap<>();
            map.put("groups", list);

            XLSTransformer transformer = new XLSTransformer();
            transformer.markAsFixedSizeCollection("groups");

            transformer.transformXLS("/home/robert/template.xls", map, tempFile.getAbsolutePath());

            if (tempFile.exists()) {
                FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();

                HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
                OutputStream out = response.getOutputStream();
                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Content-Disposition", "attachment;filename=\"export.xls\"");
                byte[] buf = new byte[8192];

                InputStream is = new FileInputStream(tempFile);

                int c = 0;

                while ((c = is.read(buf, 0, buf.length)) > 0) {
                    out.write(buf, 0, c);
                    out.flush();
                }

                out.flush();
                is.close();
                facesContext.responseComplete();
            }
        } catch (ParsePropertyException | InvalidFormatException | IOException e) {
            logger.error(e);
        }

    }
}
