package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PatientDTO;

public class PatientDetailCardController {

    @FXML private Label lblId;
    @FXML private Label lblName;
    @FXML private Label lblEmail;
    @FXML private Label lblPhone;
    @FXML private Label lblRegDate;
    @FXML private Label lblMedicalHistory;
    @FXML private Button btnClose;

    public void setPatientData(PatientDTO patient) {
        if (patient != null) {
            lblId.setText(String.valueOf(patient.getId()));
            lblName.setText(patient.getName());
            lblEmail.setText(patient.getEmail());
            lblPhone.setText(patient.getPhone());
            lblRegDate.setText(patient.getRegistrationDate().toString());
            lblMedicalHistory.setText(patient.getMedicalHistory() == null || patient.getMedicalHistory().isEmpty()
                    ? "No specific historical logs documented." : patient.getMedicalHistory());
        }
    }

    @FXML
    void btnCloseOnAction(ActionEvent event) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}