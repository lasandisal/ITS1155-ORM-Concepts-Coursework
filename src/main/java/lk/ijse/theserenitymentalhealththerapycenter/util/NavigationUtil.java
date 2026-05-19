package lk.ijse.theserenitymentalhealththerapycenter.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;

public class NavigationUtil {

    /**
     * Swaps the view inside an active AnchorPane container.
     */
    public static void navigateTo(AnchorPane contextPane, String fxmlPath) throws IOException {
        URL resource = NavigationUtil.class.getResource("/lk/ijse/theserenitymentalhealththerapycenter/view/" + fxmlPath);
        if (resource == null) {
            throw new IOException("FXML file not found: /view/" + fxmlPath);
        }
        Parent root = FXMLLoader.load(resource);
        contextPane.getChildren().clear();
        contextPane.getChildren().add(root);

        // Ensure the loaded view stretches completely across the parent container anchor anchors
        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setBottomAnchor(root, 0.0);
        AnchorPane.setLeftAnchor(root, 0.0);
        AnchorPane.setRightAnchor(root, 0.0);
    }
}
