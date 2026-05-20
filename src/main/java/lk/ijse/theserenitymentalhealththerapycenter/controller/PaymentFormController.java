package lk.ijse.theserenitymentalhealththerapycenter.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class PaymentFormController {

    @FXML
    private Button btnDeselectAll;

    @FXML
    private Button btnRefresh;

    @FXML
    private Button btnSelectAll;

    @FXML
    private Button btnSendNotification;

    @FXML
    private ComboBox<?> cmbPaymentStatusFilter;

    @FXML
    private ComboBox<?> cmbProgramFilter;

    @FXML
    private TableColumn<?, ?> colAmount;

    @FXML
    private TableColumn<?, ?> colDate;

    @FXML
    private TableColumn<?, ?> colInvoiceNo;

    @FXML
    private TableColumn<?, ?> colPatientName;

    @FXML
    private TableColumn<?, ?> colProgramName;

    @FXML
    private TableColumn<?, ?> colSelect;

    @FXML
    private TableColumn<?, ?> colStatus;

    @FXML
    private TableColumn<?, ?> colTherapistName;

    @FXML
    private Label lblSelectedCount;

    @FXML
    private Label lblTotalRecords;

    @FXML
    private TableView<?> tblPayment;

    @FXML
    private TextField txtSearchPayments;

    @FXML
    void btnDeselectAllOnAction(ActionEvent event) {

    }

    @FXML
    void btnRefreshOnAction(ActionEvent event) {

    }

    @FXML
    void btnSelectAllOnAction(ActionEvent event) {

    }

    @FXML
    void btnSendNotificationOnAction(ActionEvent event) {

    }

    @FXML
    void tblPaymentOnMouseClicked(MouseEvent event) {

    }

    @FXML
    void txtSearchPaymentsOnKeyReleased(KeyEvent event) {

    }

}
