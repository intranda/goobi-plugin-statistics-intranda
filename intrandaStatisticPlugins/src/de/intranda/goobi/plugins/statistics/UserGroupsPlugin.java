package de.intranda.goobi.plugins.statistics;

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
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;

import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import com.lowagie.text.pdf.PdfWriter;

import de.intranda.goobi.plugins.statistics.util.PieType;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UsergroupManager;

@PluginImplementation
public class UserGroupsPlugin extends AbstractStatisticsPlugin implements IStatisticPlugin {

    private static final String PLUGIN_TITLE = "intranda_statistics_userGroups";

    private static final Logger logger = Logger.getLogger(UserGroupsPlugin.class);

    private static final String XLS_TEMPLATE_NAME = "/opt/digiverso/goobi/plugins/statistics/statistics_template.xls";

//    private static final String PDF_TEMPLATE_NAME = "/opt/digiverso/goobi/plugins/statistics/statistics_template.pdf";

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
        return "/uii/statistics_usergroups.xhtml";
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

            transformer.transformXLS(XLS_TEMPLATE_NAME, map, tempFile.getAbsolutePath());

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

                tempFile.delete();

            }

        } catch (ParsePropertyException | InvalidFormatException | IOException e) {
            logger.error(e);
        }

    }

    private PdfPTable createTable() throws DocumentException, IOException {
        PdfPTable table = new PdfPTable(2);

        table.getDefaultCell().setBorder(Rectangle.BOX);
        table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);

        PdfPCell cell1 = new PdfPCell(new Paragraph(Helper.getTranslation("benutzergruppe"), new Font(BaseFont.createFont(), 11)));
        cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell1.setMinimumHeight(25f);

        PdfPCell cell2 = new PdfPCell(new Paragraph(Helper.getTranslation("count"), new Font(BaseFont.createFont(), 11)));
        cell2.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell2.setMinimumHeight(25f);

        table.addCell(cell1);
        table.addCell(cell2);

        for (PieType pt : list) {
            PdfPCell tc = new PdfPCell(new Paragraph(pt.getLabel(), new Font(BaseFont.createFont(), 10)));
            tc.setHorizontalAlignment(Element.ALIGN_LEFT);
            tc.setVerticalAlignment(Element.ALIGN_TOP);
            tc.setMinimumHeight(18f);
            table.addCell(tc);

            PdfPCell tc2 = new PdfPCell(new Paragraph(pt.getData() + "", new Font(BaseFont.createFont(), 10)));
            tc2.setHorizontalAlignment(Element.ALIGN_LEFT);
            tc2.setVerticalAlignment(Element.ALIGN_TOP);
            tc2.setMinimumHeight(18f);
            table.addCell(tc2);
        }
        table.setHeaderRows(1);
        table.setTotalWidth(510);
        return table;
    }

    public void createPdfFile() {
        try {
            FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();

            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            OutputStream out = response.getOutputStream();
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment;filename=\"export.pdf\"");

            Document document = new Document();
            PdfWriter.getInstance(document, out);

            document.open();

            PdfPTable table = createTable();

            document.add(table);

            document.close();

            out.flush();
            facesContext.responseComplete();
        } catch (IOException | DocumentException e) {
            logger.error(e);
        }
    }

    //    public void createPdfFile() {
    //        try {
    //            FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
    //
    //            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
    //            OutputStream out = response.getOutputStream();
    //            response.setContentType("application/pdf");
    //            response.setHeader("Content-Disposition", "attachment;filename=\"export.pdf\"");
    //
    //            PdfPTable table = new PdfPTable(2);
    //
    //            table.getDefaultCell().setBorder(Rectangle.BOX);
    //            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
    //            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
    //            
    //            PdfPCell cell1 = new PdfPCell(new Paragraph(Helper.getTranslation("benutzergruppe"),new Font(BaseFont.createFont(), 11)));
    //            cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
    //            cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
    //            cell1.setMinimumHeight(25f);
    //            
    //            PdfPCell cell2 = new PdfPCell(new Paragraph(Helper.getTranslation("count"),new Font(BaseFont.createFont(), 11)));
    //            cell2.setHorizontalAlignment(Element.ALIGN_LEFT);
    //            cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
    //            cell2.setMinimumHeight(25f);
    //            
    //            table.addCell(cell1);
    //            table.addCell(cell2);
    //
    //            for (PieType pt : list) {
    //            	PdfPCell tc = new PdfPCell(new Paragraph(pt.getLabel(),new Font(BaseFont.createFont(), 10)));
    //               	tc.setHorizontalAlignment(Element.ALIGN_LEFT);
    //            	tc.setVerticalAlignment(Element.ALIGN_TOP);
    //            	tc.setMinimumHeight(18f);
    //              	table.addCell(tc);
    //              	
    //              	PdfPCell tc2 = new PdfPCell(new Paragraph(pt.getData() + "",new Font(BaseFont.createFont(), 10)));
    //            	tc2.setHorizontalAlignment(Element.ALIGN_LEFT);
    //               	tc2.setVerticalAlignment(Element.ALIGN_TOP);
    //            	tc2.setMinimumHeight(18f);
    //            	table.addCell(tc2);
    //            }
    //
    //            PdfReader pdfReader = new PdfReader(PDF_TEMPLATE_NAME);
    //            PdfStamper pdfStamper = new PdfStamper(pdfReader, out);
    //
    //            PdfImportedPage page = pdfStamper.getImportedPage(pdfReader, 1);
    //
    //            PdfContentByte content = pdfStamper.getOverContent(1);
    //            content.addTemplate(page, 0, 0);
    //
    //            table.setHeaderRows(1);
    //            table.setTotalWidth(510);
    //
    //            Paragraph p = new Paragraph(Helper.getTranslation(PLUGIN_TITLE), new Font(BaseFont.createFont(), 14));
    //            ColumnText.showTextAligned(content, Element.ALIGN_LEFT, p, 40, 750, 0);
    //
    //            table.writeSelectedRows(0, -1, 40, 730, content);
    //
    //            pdfStamper.close();
    //            pdfReader.close();
    //            out.flush();
    //            facesContext.responseComplete();
    //        } catch (IOException | DocumentException e) {
    //            logger.error(e);
    //        }
    //    }

    @Override
    public boolean getPermissions() {
        return true;
        // Nur bestimmte Nutzer
        //        User user = (User) Helper.getManagedBeanValue("#{LoginForm.myBenutzer}");
        //        return user.getLogin().equals("testadmin");
    }

}
