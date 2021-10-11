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
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IStatisticPlugin;

import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ControllingManager;
import lombok.Data;
import lombok.extern.log4j.Log4j;

@Data
//@PluginImplementation
@Log4j
public class ProductivityPlugin implements IStatisticPlugin {

    private PluginType type = PluginType.Statistics;
    private static final String PLUGIN_TITLE = "test_productivity";
    private int projectId;
    private String filter;

    private Date startDate;
    private Date endDate;

    private String startDateText;
    private String endDateText;

    private String timeRange = "%Y-%m";

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private List<Map<String, String>> resultList;

    @Override
    public String getTitle() {
        return PLUGIN_TITLE;
    }

    @Override
    public String getData() {
        return null;
    }

    @Override
    public String getGui() {
        return "/uii/statistics_test_productivity.xhtml";
    }

    public void resetStatistics() {
        resultList = null;
    }

    @Override
    public boolean getPermissions() {
        return true;
    }

    @Override
    public void calculate() {
        calculateData();
    }

    /**
     * calculate data from database
     */
    private void calculateData() {

        //How much data has been produced?  How many files and objects?

        //# To use different ranges, change the dates between,
        //# possible date formats are:
        //# daily: date_format(BearbeitungsEnde, '%Y-%m-%d')
        //# monthly: date_format(BearbeitungsEnde, '%Y-%m')
        //# quarterly: concat(YEAR(BearbeitungsEnde),'/',QUARTER(BearbeitungsEnde))
        //# yearly: date_format(BearbeitungsEnde, '%Y')
        //# this must be changed in the select area and  group by clause
        StringBuilder sb = new StringBuilder();
        // check for each process, if the last step was finished
        sb.append("select date_format(BearbeitungsEnde, ");
        sb.append(timeRange);
        sb.append(") as timerange, sum(p.sortHelperImages) as pages, count(p.sortHelperImages) as processes ");
        sb.append("from schritte s left join prozesse p on s.ProzesseID = p.ProzesseID ");
        sb.append("where s.SchritteID in (select max(SchritteId) as stepid from schritte s group by s.ProzesseID) ");
        sb.append("and BearbeitungsStatus = 3 ");

        // TODO switch back after date picker problems are solved
        //        String startDateText = getStartDateAsString();
        //        String endDateText = getEndDateAsString();

        if (StringUtils.isNotBlank(startDateText) && StringUtils.isNotBlank(endDateText)) {
            sb.append("and BearbeitungsEnde between '");
            sb.append(startDateText);
            sb.append("' and '");
            sb.append(endDateText);
            sb.append("' ");
        } else if (StringUtils.isNotBlank(startDateText)) {
            sb.append("and BearbeitungsEnde >= '");
            sb.append(startDateText);
            sb.append("' ");
        } else if (StringUtils.isNotBlank(endDateText)) {
            sb.append("and BearbeitungsEnde <= '");
            sb.append(endDateText);
            sb.append("' ");
        }

        // TODO add filter and p.ProzesseID in ....

        sb.append("group by date_format(BearbeitungsEnde, ");
        sb.append(timeRange);
        sb.append(") ");

        resultList = ControllingManager.getResultsAsMaps(sb.toString());

    }

    public void generateExcelDownload() {
        if (resultList.isEmpty()) {
            Helper.setMeldung("No results to export.");
            return;
        }
        Workbook wb = new XSSFWorkbook();

        Sheet sheet = wb.createSheet("productivity results");

        // create header
        Row headerRow = sheet.createRow(0);
        Set<String> columnHeader = resultList.get(0).keySet();
        int columnCounter = 0;
        for (String headerName : columnHeader) {
            headerRow.createCell(columnCounter).setCellValue(headerName);
            columnCounter = columnCounter + 1;
        }

        int rowCounter = 1;
        // add results
        for (Map<String, String> result : resultList) {
            Row resultRow = sheet.createRow(rowCounter);
            columnCounter = 0;
            for (String headerName : columnHeader) {
                resultRow.createCell(columnCounter).setCellValue(result.get(headerName));
                columnCounter = columnCounter + 1;
            }
            rowCounter = rowCounter + 1;
        }

        // write result into output stream
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();

        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        OutputStream out;
        try {
            out = response.getOutputStream();
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=\"report.xlsx\"");
            wb.write(out);
            out.flush();

            facesContext.responseComplete();
        } catch (IOException e) {
            log.error(e);
        }
        try {
            wb.close();
        } catch (IOException e) {
            log.error(e);
        }
    }

    public void setStartDateText(String value) {
        startDateText = value;
    }

    public void setEndDateText(String value) {
        endDateText = value;
    }

    public String getStartDateAsString() {
        if (startDate != null) {
            return dateFormat.format(startDate);
        }
        return null;
    }

    public String getEndDateAsString() {
        if (endDate != null) {
            return dateFormat.format(endDate);
        }
        return null;
    }
}
