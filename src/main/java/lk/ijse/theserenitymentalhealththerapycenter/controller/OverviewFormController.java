package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOFactory;
import lk.ijse.theserenitymentalhealththerapycenter.bo.BOType;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PatientBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.PaymentBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapistBO;
import lk.ijse.theserenitymentalhealththerapycenter.bo.custom.TherapySessionBO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;
import lk.ijse.theserenitymentalhealththerapycenter.util.AlertUtil;
import lk.ijse.theserenitymentalhealththerapycenter.util.ReportPrintUtil;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverviewFormController {

    @FXML private BarChart<String, Number> barChartSessionVolumes;
    @FXML private PieChart pieChartRevenueShare;
    @FXML private Button btnGenerateReport;
    @FXML private Button btnRefreshCharts;

    @FXML private Label lblMonthlyRevenue;
    @FXML private Label lblTodaySessions;
    @FXML private Label lblTotalPatients;
    @FXML private Label lblTotalTherapists;

    // Decoupled Business Object Dependency Layers
    private final PatientBO patientBO = (PatientBO) BOFactory.getInstance().getBO(BOType.PATIENT);
    private final TherapySessionBO sessionBO = (TherapySessionBO) BOFactory.getInstance().getBO(BOType.THERAPY_SESSION);
    private final PaymentBO paymentBO = (PaymentBO) BOFactory.getInstance().getBO(BOType.PAYMENT);
    private final TherapistBO therapistBO = (TherapistBO) BOFactory.getInstance().getBO(BOType.THERAPIST);

    @FXML
    public void initialize() {
        loadHeaderBannerStatMetrics();
        populateAnalyticsCharts();

        CategoryAxis xAxis = (CategoryAxis) barChartSessionVolumes.getXAxis();
        xAxis.setTickLabelRotation(-30);
        xAxis.setGapStartAndEnd(true);
        pieChartRevenueShare.setLegendSide(javafx.geometry.Side.BOTTOM);
        pieChartRevenueShare.setLabelsVisible(true);
        pieChartRevenueShare.setStartAngle(90);
    }

    private void loadHeaderBannerStatMetrics() {
        try {
            int patientCount = patientBO.getAllActivePatients().size();
            int therapistCount = therapistBO.getAllActiveTherapists().size();
            List<TherapySessionDTO> allSessions = sessionBO.getAllSessionsWithFullDetails();
            List<PaymentDTO> allPayments = paymentBO.getAllTransactionsLog();

            lblTotalPatients.setText(String.valueOf(patientCount));
            lblTotalTherapists.setText(String.valueOf(therapistCount));

            long todayCount = allSessions.stream()
                    .filter(s -> s.getSessionDateTime() != null && s.getSessionDateTime().toLocalDate().equals(LocalDate.now()))
                    .count();
            lblTodaySessions.setText(String.valueOf(todayCount));

            double grossRevenueSum = allPayments.stream()
                    .filter(p -> p.getStatus() != null && ("SUCCESS".equalsIgnoreCase(p.getStatus().name()) || "PAID".equalsIgnoreCase(p.getStatus().name())))
                    .mapToDouble(PaymentDTO::getAmount)
                    .sum();

            lblMonthlyRevenue.setText(String.format("LKR %,.2f", grossRevenueSum));

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Sync Failure", "Metrics Load Dropped", "Failed to compile background stat configurations: " + e.getMessage());
        }
    }

    private void populateAnalyticsCharts() {
        try {
            barChartSessionVolumes.getData().clear();
            pieChartRevenueShare.getData().clear();

            List<TherapySessionDTO> sessionRecords = sessionBO.getAllSessionsWithFullDetails();
            List<PaymentDTO> revenueRecords = paymentBO.getAllTransactionsLog();

            Map<String, Integer> programSessionMaps = new HashMap<>();
            for (TherapySessionDTO session : sessionRecords) {
                String programName = session.getProgramName() != null ? session.getProgramName() : "General Clinic";
                programSessionMaps.put(programName, programSessionMaps.getOrDefault(programName, 0) + 1);
            }

            XYChart.Series<String, Number> sessionSeries = new XYChart.Series<>();
            sessionSeries.setName("Booked Appointments");
            for (Map.Entry<String, Integer> entry : programSessionMaps.entrySet()) {
                sessionSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            barChartSessionVolumes.getData().add(sessionSeries);

            Map<String, Double> programRevenueMaps = new HashMap<>();
            double totalRevenueSum = 0;

            for (PaymentDTO payment : revenueRecords) {
                if (payment.getStatus() != null && ("SUCCESS".equalsIgnoreCase(payment.getStatus().name()) || "PAID".equalsIgnoreCase(payment.getStatus().name()))) {
                    String programName = payment.getProgramName() != null ? payment.getProgramName() : "Unassigned";
                    programRevenueMaps.put(programName, programRevenueMaps.getOrDefault(programName, 0.0) + payment.getAmount());
                    totalRevenueSum += payment.getAmount();
                }
            }

            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            for (Map.Entry<String, Double> entry : programRevenueMaps.entrySet()) {
                double slicePercentage = totalRevenueSum > 0 ? (entry.getValue() / totalRevenueSum) * 100 : 0;
                String informativeLabel = String.format("%s (%.1f%%)", entry.getKey(), slicePercentage);
                pieData.add(new PieChart.Data(informativeLabel, entry.getValue()));
            }
            pieChartRevenueShare.setData(pieData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void btnRefreshChartsOnAction(ActionEvent event) {
        loadHeaderBannerStatMetrics();
        populateAnalyticsCharts();
    }


    @FXML
    void btnGenerateReportOnAction(ActionEvent event) {
        try {
            List<PaymentDTO> activeRecordsLog = paymentBO.getAllTransactionsLog();

            if (activeRecordsLog == null || activeRecordsLog.isEmpty()) {
                AlertUtil.showWarning("Report Engine", "Export Aborted", "The active ledger sheets contain no transaction history records to build an export layout map.");
                return;
            }

            ReportPrintUtil.generateFinancialReport(
                    activeRecordsLog,
                    lblMonthlyRevenue.getText(),
                    "SYSTEM_ADMIN"
            );

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("System Exception", "Task Interrupted", "Unexpected system drop error inside UI frame logic: " + e.getMessage());
        }
    }
}