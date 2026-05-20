package lk.ijse.theserenitymentalhealththerapycenter.dto.tm;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lk.ijse.theserenitymentalhealththerapycenter.dto.PaymentDTO;


public class PaymentDisplayTM {
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final PaymentDTO paymentData;

    public PaymentDisplayTM(PaymentDTO paymentData) {
        this.paymentData = paymentData;
    }

    public BooleanProperty selectedProperty() { return this.selected; }
    public boolean isSelected() { return this.selected.get(); }
    public void setSelected(boolean value) { this.selected.set(value); }

    public String getInvoiceNumber() { return paymentData.getInvoiceNumber(); }
    public String getPatientName() { return paymentData.getPatientName(); }
    public String getProgramName() { return paymentData.getProgramName(); }
    public String getTherapistName() { return paymentData.getProgramName();  }
    public String getUsername() { return paymentData.getUsername(); }
    public String getPaymentDate() { return paymentData.getPaymentDate() != null ? paymentData.getPaymentDate().toString() : "-"; }
    public double getAmount() { return paymentData.getAmount(); }
    public String getStatus() { return paymentData.getStatus() != null ? paymentData.getStatus().name() : "PENDING"; }
}