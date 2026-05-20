package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lk.ijse.theserenitymentalhealththerapycenter.dto.tm.PaymentDisplayTM;

public class PaymentDetailCardController {

    @FXML private Label lblInvoiceNo;
    @FXML private Label lblPatientName;
    @FXML private Label lblProgramName;
    @FXML private Label lblTherapistName;
    @FXML private Label lblUser;
    @FXML private Label lblDate;
    @FXML private Label lblAmount;
    @FXML private Label lblStatus;
    @FXML private Button btnClose;

    /**
     * Binds selection properties out of the UI presentation wrapper model
     * to render clear summary strings inside the labels.
     */
    public void setPaymentDetails(PaymentDisplayTM selectedPayment) {
        if (selectedPayment != null) {
            lblInvoiceNo.setText(selectedPayment.getInvoiceNumber());
            lblPatientName.setText(selectedPayment.getPatientName());
            lblProgramName.setText(selectedPayment.getProgramName());
            lblTherapistName.setText(selectedPayment.getTherapistName());
            lblUser.setText(selectedPayment.getUsername() != null ? selectedPayment.getUsername().toUpperCase() : "SYSTEM");
            lblDate.setText(selectedPayment.getPaymentDate());
            lblAmount.setText(String.format("LKR %,.2f", selectedPayment.getAmount()));
            lblStatus.setText(selectedPayment.getStatus());

            // =========================================================================
            // ✅ CLEAN STYLE CLASS INJECTION
            // =========================================================================
            // Flush any existing style classes from the node to prevent style bleeding overrides
            lblStatus.getStyleClass().removeAll("status-badge-success", "status-badge-danger", "status-badge-warning");

            String statusStr = selectedPayment.getStatus();
            if ("SUCCESS".equalsIgnoreCase(statusStr) || "PAID".equalsIgnoreCase(statusStr)) {
                lblStatus.getStyleClass().add("status-badge-success");
            } else if ("FAILED".equalsIgnoreCase(statusStr) || "OVERDUE".equalsIgnoreCase(statusStr)) {
                lblStatus.getStyleClass().add("status-badge-danger");
            } else {
                lblStatus.getStyleClass().add("status-badge-warning");
            }
            // =========================================================================
        }
    }

    @FXML
    void btnCloseOnAction(ActionEvent event) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}