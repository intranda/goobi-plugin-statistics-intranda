package de.intranda.goobi.plugins.statistics.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Project;
import org.goobi.beans.Step;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;

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

import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.StepManager;
import io.goobi.workflow.xslt.JxlsOutputStream;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ProjectData {
    private boolean selected = false;
    private Project project;
    private List<PieType> list;
    private String data;
    private String title;

    private static final String XLS_TEMPLATE_NAME = "/opt/digiverso/goobi/plugins/statistics/statistics_template.xlsx";

    //    private static final String PDF_TEMPLATE_NAME = "/opt/digiverso/goobi/plugins/statistics/statistics_template.pdf";

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void calculateSteps(int status) {

        String filterString = FilterHelper.criteriaBuilder("\"project:" + project.getTitel() + "\"", false, null, null, null, true, false);
        List<Step> stepList = null;

        stepList = StepManager.getSteps("Reihenfolge",
                " (bearbeitungsstatus = " + status + ") AND schritte.ProzesseID in (select ProzesseID from prozesse where " + filterString + ")",
                null);

        //        stepList =
        //                StepManager.getSteps(null, " (bearbeitungsstatus != 3) AND schritte.ProzesseID in (select ProzesseID from prozesse where "
        //                        + filterString + ")");

        Map<String, Integer> counter = new LinkedHashMap<>();

        for (Step step : stepList) {
            //System.out.println(step.getTitel());
            if (counter.containsKey(step.getTitel())) {
                counter.put(step.getTitel(), counter.get(step.getTitel()) + 1);
            } else {
                counter.put(step.getTitel(), 1);
            }

        }

        list = new ArrayList<>();

        for (String stepName : counter.keySet()) {
            int value = counter.get(stepName);
            PieType type = new PieType();
            type.setLabel(stepName);
            type.setData(value);
            type.setColor(getRandomColor());
            list.add(type);
        }

        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.writeValue(writer, list);
        } catch (IOException e) {
            log.error(e);
        }

        data = writer.toString();

    }

    private static String getRandomColor() {
        String possibleValues = "0123456789ABCDEF";
        StringBuilder hexCode = new StringBuilder("#");
        for (int i = 0; i <= 5; i++) {
            int index = (int) (Math.random() * 15);
            hexCode.append(possibleValues.charAt(index));
        }
        return hexCode.toString();
    }
    //    public void calculateUserGroupAssignment() {
    //
    //        String filterString = FilterHelper.criteriaBuilder("project:" + project.getTitel(), false, null, null, null, true, false);
    //        List<Step> stepList = null;
    //        if (filterString == null || filterString.length() == 0) {
    //            stepList = StepManager.getSteps(null, " (bearbeitungsstatus = 1 OR bearbeitungsstatus = 2)  ");
    //        } else {
    //            stepList =
    //                    StepManager.getSteps(null,
    //                            " (bearbeitungsstatus = 1 OR bearbeitungsstatus = 2) AND schritte.ProzesseID in (select ProzesseID from prozesse where "
    //                                    + filterString + ")");
    //        }
    //
    //        Map<String, Integer> counter = new TreeMap<String, Integer>();
    //
    //        for (Step step : stepList) {
    //            for (Usergroup group : UsergroupManager.getUserGroupsForStep(step.getId())) {
    //
    //                if (counter.containsKey(group.getTitel())) {
    //                    counter.put(group.getTitel(), counter.get(group.getTitel()) + 1);
    //                } else {
    //                    counter.put(group.getTitel(), 1);
    //                }
    //            }
    //        }
    //
    //        list = new ArrayList<PieType>();
    //
    //        for (String groupName : counter.keySet()) {
    //            int value = counter.get(groupName);
    //
    //            PieType type = new PieType();
    //            type.setLabel(groupName);
    //            type.setData(value);
    //            type.setColor(PluginInfo.getRandomColor());
    //            list.add(type);
    //
    //        }
    //
    //        StringWriter writer = new StringWriter();
    //        ObjectMapper mapper = new ObjectMapper();
    //
    //        try {
    //            mapper.writeValue(writer, list);
    //        } catch (IOException e) {
    //            logger.error(e);
    //        }
    //
    //        data = writer.toString();
    //
    //    }

    public List<PieType> getList() {
        return list;
    }

    public void setList(List<PieType> list) {
        this.list = list;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void createExcelFile() {
        try {

            Map<String, Object> model = new HashMap<>();
            model.put("groups", list);
            try (InputStream is = new FileInputStream(XLS_TEMPLATE_NAME)) {

                FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
                HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
                OutputStream out = response.getOutputStream();
                response.setContentType("application/vnd.ms-excel");
                response.setHeader("Content-Disposition", "attachment;filename=\"export.xlsx\"");

                JxlsPoiTemplateFillerBuilder.newInstance()
                        .withTemplate(is)
                        .build()
                        .fill(model, new JxlsOutputStream(out));

                out.flush();
                facesContext.responseComplete();
            }
        } catch (IOException e) {
            log.error(e);
        }

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
            log.error(e);
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
    //            	tc.setHorizontalAlignment(Element.ALIGN_LEFT);
    //            	tc.setVerticalAlignment(Element.ALIGN_TOP);
    //            	tc.setMinimumHeight(18f);
    //            	table.addCell(tc);
    //
    //            	PdfPCell tc2 = new PdfPCell(new Paragraph(pt.getData() + "",new Font(BaseFont.createFont(), 10)));
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
    ////            page.setFontAndSize(BaseFont.createFont(), 9);
    //
    //            PdfContentByte content = pdfStamper.getOverContent(1);
    //            content.addTemplate(page, 0, 0);
    //            content.setFontAndSize(BaseFont.createFont(), 9);
    //
    //            table.setHeaderRows(1);
    //            table.setTotalWidth(510);
    //
    //            Paragraph p = new Paragraph(Helper.getTranslation(title), new Font(BaseFont.createFont(), 14));
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

    public String getEndDate() {
        if (project.getEndDate() != null) {
            return DateFormat.getDateInstance().format(project.getEndDate());
        } else {
            return "";
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
