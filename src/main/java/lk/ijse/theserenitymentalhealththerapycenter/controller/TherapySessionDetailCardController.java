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
    @FXML private Label lblPatientName;
    @FXML private Label lblTherapistName;
    @FXML private Label lblProgramName;
    @FXML private Label lblDateTime;
    @FXML private Label lblStatus;
    @FXML private Button btnClose;

    /**
     * Binds incoming TherapySession DTO properties into summary card items.
     */
    public void setSessionData(TherapySessionDTO session) {
        if (session != null) {
            lblId.setText(String.valueOf(session.getId()));

            // Build descriptions with fallback options if relational models are detached
            lblPatientName.setText(session.getPatientName() != null ? session.getPatientName() : "ID: " + session.getPatientId());
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