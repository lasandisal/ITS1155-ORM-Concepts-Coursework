package lk.ijse.theserenitymentalhealththerapycenter.util;

import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportPrintUtil {

    /**
     * Compiles data list parameters, asks user to export an external hard backup copy on disk,
     * and shows a pre-scaled preview window at a comfortable 50% scale.
     */
    public static void generateFinancialReport(List<PaymentDTO> paymentRecords, String totalRevenue, String sessionUser) {
        if (paymentRecords == null || paymentRecords.isEmpty()) return;

        try {
            System.out.println(">> Report Utility pipeline: Initializing transactional rows down to source stream...");

            // 1. Dynamic Classpath Asset Stream Loader
            InputStream reportStream = ReportPrintUtil.class.getResourceAsStream(
                    "/lk/ijse/theserenitymentalhealththerapycenter/reports/serenity_financial_report.jrxml"
            );

            if (reportStream == null) {
                throw new JRException("Source template file (serenity_financial_report.jrxml) not found in the resource path mapping.");
            }

            // 2. Bind Parameter Configuration Maps
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("TotalRevenue", totalRevenue);
            parameters.put("GeneratedUser", sessionUser);

            // 3. Wrap DTO Collection Data into a Jasper Engine source instance
            JRBeanCollectionDataSource beanCollectionDataSource = new JRBeanCollectionDataSource(paymentRecords);

            // 4. Compile and fill raw structural boundaries into a displayable Canvas layout
            JasperReport compiledReport = JasperCompileManager.compileReport(reportStream);
            JasperPrint jasperPrint = JasperFillManager.fillReport(compiledReport, parameters, beanCollectionDataSource);

            // =========================================================================
            // ✅ DYNAMIC USER PERSISTENCE INTERFACE PROMPT
            // =========================================================================
            boolean wantToSaveBackup = AlertUtil.showConfirmation(
                    "Export Action",
                    "Save Financial Report Backup?",
                    "Would you like to export and write a localized secure PDF archival backup of this report directly to your disk workspace folder?"
            );

            if (wantToSaveBackup) {
                String projectRoot = System.getProperty("user.dir");
                String outputDirectoryPath = projectRoot + File.separator + "financial_reports" + File.separator;

                // Build localized directory folder on disk workspace if missing
                File directory = new File(outputDirectoryPath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Append an immutable unique timestamp suffix to keep histories from overwriting each other
                String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String targetPdfFileName = "Financial_Audit_" + timeStamp + ".pdf";
                String fullSavePath = outputDirectoryPath + targetPdfFileName;

                // Silently write to hard storage
                JasperExportManager.exportReportToPdfFile(jasperPrint, fullSavePath);
                System.out.println(">> Audit System Archive: Hardcopy saved to local tracking stack: " + fullSavePath);

                AlertUtil.showInformation("Export Complete", "Document Persisted", "Financial statement spreadsheet written to folder destination cleanly:\n" + fullSavePath);
            }

            // 5. Initialize Window Container Preview Frame
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("The Serenity Center - Financial Audit Ledger Viewer Portal");

            // =========================================================================
            // ✅ REFLECTION ENGINE WORKAROUND: FORCE 50% VIEW ZOOM STATE
            // =========================================================================
            if (viewer.getContentPane() != null) {
                for (java.awt.Component comp : viewer.getContentPane().getComponents()) {
                    if (comp.getClass().getName().endsWith("JRViewer")) {
                        try {
                            Method setZoomMethod = comp.getClass().getMethod("setZoomRatio", float.class);
                            setZoomMethod.invoke(comp, 0.5f); // 0.5f scales cleanly to 50%
                            System.out.println(">> Zoom Engine: Safely forced 50% scaling using class reflection.");
                            break;
                        } catch (Exception e) {
                            System.out.println(">> Zoom Engine Warning: Unable to invoke runtime zoom: " + e.getMessage());
                        }
                    }
                }
            }
            // =========================================================================

            viewer.setVisible(true);

        } catch (JRException e) {
            AlertUtil.showError(
                    "Report Engine Error",
                    "Compilation Aborted",
                    "An unexpected exception halted your report generator pipeline processing layout cells: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}