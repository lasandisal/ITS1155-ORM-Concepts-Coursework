package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import lk.ijse.theserenitymentalhealththerapycenter.dto.TherapyProgramDTO;

public class TherapyProgramDetailCardController {

    @FXML private Label lblId;
    @FXML private Label lblName;
    @FXML private Label lblDuration;
    @FXML private Label lblFee;
    @FXML private Label lblStatus;
    @FXML private Button btnClose;

    public void setProgramData(TherapyProgramDTO program) {
        if (program != null) {
            lblId.setText(program.getId());
            lblName.setText(program.getName());
            lblDuration.setText(program.getDuration());
            lblFee.setText(String.format("LKR %,.2f", program.getFee()));
            lblStatus.setText(program.getStatus() != null ? program.getStatus().name() : "UNKNOWN");
        }
    }

    @FXML
    void btnCloseOnAction(ActionEvent event) {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}