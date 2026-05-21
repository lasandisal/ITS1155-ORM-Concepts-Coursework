package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapySessionDTO;

import java.time.format.DateTimeFormatter;

public class TherapySessionDetailCardController {

    @FXML private Label lblId;
    @FXML private Label lblPatientName; // Retained FX ID name but handles multi-passenger output displays dynamically
    @FXML private Label lblTherapistName;
    @FXML private Label lblProgramName;
    @FXML private Label lblDateTime;
    @FXML private Label lblStatus;
    @FXML private Button btnClose;

    public void setSessionData(TherapySessionDTO session) {
        if (session != null) {
            lblId.setText(String.valueOf(session.getId()));

            // Refactored to harvest the complete comma-separated list of names out of the upgraded junction DTO
            lblPatientName.setText(session.getFormattedPatientNames());

            lblTherapistName.setText(session.getTherapistName() != null ? session.getTherapistName() : "ID: " + session.getTherapistId());
            lblProgramName.setText(session.getProgramName() != null ? session.getProgramName() : "Code: " + session.getProgramId());

            if (session.getSessionDateTime() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd | hh:mm a");
                lblDateTime.setText(session.getSessionDateTime().format(formatter));
            } else {
                lblDateTime.setText("-");
            }

            lblStatus.setText(session.getStatus() != null ? session.getStatus().name() : "PENDING");
        }
    }

    @FXML
    void btnCloseOnAction(ActionEvent event) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}