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

    /**
     * Dynamically loads the invoice template, exports a portable PDF archive copy to the root 'invoices' folder,
     * and displays an on-screen preview window using reflection to force a 50% zoom scale seamlessly.
     */
    public static void printInvoice(PaymentDTO invoice) {
        if (invoice == null) return;

        try {
            // 1. Dynamic Classpath Asset Stream Loader
            InputStream reportStream = InvoicePrintUtil.class.getResourceAsStream(
                    "/lk/ijse/theserenitymentalhealththerapycenter/reports/InvoiceSerenityCenter.jasper"
            );

            if (reportStream == null) {
                throw new JRException("Compiled report file (InvoiceSerenityCenter.jasper) not found in the resources folder path.");
            }

            // 2. Bind Invoice Data Parameters
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

            // 3. Populate and fill the report layout
            JasperPrint jasperPrint = JasperFillManager.fillReport(reportStream, parameters, new JREmptyDataSource());

            // 4. Dynamic Path Saving Logic (Best Practice)
            String projectRoot = System.getProperty("user.dir");
            String outputDirectoryPath = projectRoot + File.separator + "invoices" + File.separator;

            // Automatically construct directory layout if missing on disk
            File directory = new File(outputDirectoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Sanitize filename syntax from forward slashes to hyphens
            String targetPdfFileName = invoice.getInvoiceNumber().replace("/", "-") + ".pdf";
            String fullSavePath = outputDirectoryPath + targetPdfFileName;

            // Silently write and export the PDF backup file
            JasperExportManager.exportReportToPdfFile(jasperPrint, fullSavePath);
            System.out.println(">> Production System Archive: Copy saved dynamically to: " + fullSavePath);

            // 5. Initialize Window Preview Container Frame
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("Serenity Payment Invoice - " + invoice.getInvoiceNumber());

            // =========================================================================
            // ✅ REFLECTION WORKAROUND: FORCE 50% ZOOM STATE (No Imports Needed)
            // =========================================================================
            if (viewer.getContentPane() != null) {
                for (java.awt.Component comp : viewer.getContentPane().getComponents()) {
                    // This checks if the component is the text rendering view window component dynamically
                    if (comp.getClass().getName().endsWith("JRViewer")) {
                        try {
                            // Find the public setZoomRatio(float) method inside Jasper's component pool
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
            // =========================================================================

            // Reveal the pre-scaled frame to your user interface space
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