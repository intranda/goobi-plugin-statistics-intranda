package de.intranda.goobi.plugins;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        Map<String, Integer> counter = new HashMap<String, Integer>();

        for (Step step : stepList) {
            for (Usergroup group : UsergroupManager.getUserGroupsForStep(step.getId())) {

                if (counter.containsKey(group.getTitel())) {
                    counter.put(group.getTitel(), counter.get(group.getTitel()) + 1);
                } else {
                    counter.put(group.getTitel(), 1);
                }
            }
        }

        //        {{label: "Asia", data: 4119630000, color: "#005CDE" },
        //        { label: "Latin America", data: 590950000, color: "#00A36A" },
        //        { label: "Africa", data: 1012960000, color: "#7D0096" },
        //        { label: "Oceania", data: 35100000, color: "#992B00" },
        //        { label: "Europe", data: 727080000, color: "#DE000F" },
        //        { label: "North America", data: 344120000, color: "#ED7B00" }}    
        /*
                {{label: "Quantitative QA Officer", data: 3, color: "#93D3C0"},{label: "ImagingOfficer", data: 1477, color: "#878C3C"},{label: "Qualitative QA Officer", data: 11, color: "#B0EB23"}, {label: "Projectmanager", data: 71, color: "#04600E"}, {label: "Administration", data: 339, color: "#624EB6"}, {label: "MetadataOfficer", data: 567, color: "#CDAD87"}, {label: "AutomaticTasks", data: 154, color: "#A5B2C2"}, {label: "PreparationOfficer", data: 782, color: "#767D92"}{label: "Archivist", data: 1, color: "#210050"}}
               
                
        */

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

        Map<String, List<PieType>> map = new HashMap<>();
        map.put("groups", list);

        XLSTransformer transformer = new XLSTransformer();
        transformer.markAsFixedSizeCollection("groups");
        try {
            transformer.transformXLS("/home/robert/template.xls", map, "/home/robert/test.xls");
        } catch (ParsePropertyException | InvalidFormatException | IOException e) {
            logger.error(e);
        }

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
}
