module lk.ijse.theserenitymentalhealththerapycenter {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires static lombok;
    requires lk.ijse.theserenitymentalhealththerapycenter;
    requires jbcrypt;

    opens lk.ijse.theserenitymentalhealththerapycenter to javafx.fxml;
    exports lk.ijse.theserenitymentalhealththerapycenter;
}