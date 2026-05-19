package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapistDTO;

public class TherapistDetailCardController {

    @FXML private Label lblId;
    @FXML private Label lblName;
    @FXML private Label lblSpecialization;
    @FXML private Label lblEmail;
    @FXML private Label lblPhone;
    @FXML private Button btnClose;

    public void setTherapistData(TherapistDTO therapist) {
        if (therapist != null) {
            lblId.setText(String.valueOf(therapist.getId()));
            lblName.setText(therapist.getName());
            lblSpecialization.setText(therapist.getSpecialization());
            lblEmail.setText(therapist.getEmail());
            lblPhone.setText(therapist.getPhone());
        }
    }

    @FXML
    void btnCloseOnAction(ActionEvent event) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}