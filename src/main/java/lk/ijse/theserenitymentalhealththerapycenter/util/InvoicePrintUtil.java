package lk.ijse.theserenitymentalhealththerapycenter.util;

import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class InvoicePrintUtil {

    public static void printInvoice(PaymentDTO invoice) {
        if (invoice == null) return;

        try {
            InputStream reportStream = InvoicePrintUtil.class.getResourceAsStream(
                    "/lk/ijse/theserenitymentalhealththerapycenter/reports/InvoiceSerenityCenter.jasper"
            );

            if (reportStream == null) {
                throw new JRException("Compiled report file (InvoiceSerenityCenter.jasper) not found in the resources folder path.");
            }

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("pInvoiceNo", invoice.getInvoiceNumber());
            parameters.put("pPatientName", invoice.getPatientName());
            parameters.put("pProgramName", invoice.getProgramName());
            parameters.put("pAmount", invoice.getAmount());

            if (invoice.getPaymentDate() != null) {
                parameters.put("pPaymentDate", invoice.getPaymentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else {
                parameters.put("pPaymentDate", "-");
            }

            JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, parameters, new JREmptyDataSource());

            String projectRoot = System.getProperty("user.dir");
            String outputDirectoryPath = projectRoot + File.separator + "invoices" + File.separator;

            File directory = new File(outputDirectoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String targetPdfFileName = invoice.getInvoiceNumber().replace("/", "-") + ".pdf";
            String fullSavePath = outputDirectoryPath + targetPdfFileName;

            JasperExportManager.exportReportToPdfFile(jasperPrint, fullSavePath);
            System.out.println(">> Production System Archive: Copy saved dynamically to: " + fullSavePath);

            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("Serenity Payment Invoice - " + invoice.getInvoiceNumber());


            if (viewer.getContentPane() != null) {
                for (java.awt.Component comp : viewer.getContentPane().getComponents()) {
                    if (comp.getClass().getName().endsWith("JRViewer")) {
                        try {
                            Method setZoomMethod = comp.getClass().getMethod("setZoomRatio", float.class);
                            setZoomMethod.invoke(comp, 0.5f); // 0.5f maps mathematically to a clean 50% scale
                            System.out.println(">> Zoom Engine: Safely forced 50% scaling using class reflection.");
                            break;
                        } catch (Exception e) {
                            System.out.println(">> Zoom Engine Warning: Unable to invoke zoom method: " + e.getMessage());
                        }
                    }
                }
            }
            viewer.setVisible(true);

        } catch (JRException e) {
            AlertUtil.showError(
                    "Print & Save Failure",
                    "Jasper Engine Error",
                    "Unable to render or save the visual receipt document copy: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}