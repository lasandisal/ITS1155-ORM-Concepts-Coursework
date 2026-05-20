package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CustomMessageModalController {

    @FXML private Button btnCancel;
    @FXML private Button btnProceed;
    @FXML private TextArea txtMessageBody;
    @FXML private TextField txtSubject;

    private boolean submitConfirmed = false;
    private String subjectText = "";
    private String bodyText = "";

    @FXML
    void btnCancelOnAction(ActionEvent event) {
        closeStageContext();
    }

    @FXML
    void btnProceedOnAction(ActionEvent event) {
        if (txtSubject.getText().trim().isEmpty() || txtMessageBody.getText().trim().isEmpty()) {
            txtSubject.setStyle("-fx-border-color: #EF4444;");
            return;
        }

        this.submitConfirmed = true;
        this.subjectText = txtSubject.getText().trim();
        this.bodyText = txtMessageBody.getText().trim();
        closeStageContext();
    }

    private void closeStageContext() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    public boolean isSubmitConfirmed() { return submitConfirmed; }
    public String getSubjectText() { return subjectText; }
    public String getBodyText() { return bodyText; }
}